package com.yoe.client.netty;

import com.alibaba.fastjson.JSONObject;
import com.yoe.client.constants.Constants;
import com.yoe.client.netty.future.ChannelFutureManager;
import com.yoe.client.netty.future.DefaultFuture;
import com.yoe.client.netty.handler.NettyClientHandler;

import com.yoe.client.request.RpcRequest;
import com.yoe.client.response.RpcResponse;
import com.yoe.client.zookeeper.ServerWatcher;
import com.yoe.client.zookeeper.ZookeeperFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;

import java.util.List;

public class TcpClient {
    public static final Bootstrap bootstrap = new Bootstrap();

    public static ChannelFuture future = null;

    static{
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            pipeline.addLast(new DelimiterBasedFrameDecoder(65535, Delimiters.lineDelimiter()[0]));//分割帧
                            pipeline.addLast(new StringDecoder());


                            pipeline.addLast(new NettyClientHandler());

                            pipeline.addLast(new StringEncoder());
                        }
                    });

            /**
             * 通过zk发现服务列表
             */

            String host;
            int port;

            int weight;

            CuratorFramework client = ZookeeperFactory.create();


            /**
             * 加上zk监听服务器变化，解决临时节点没有立刻被zk删除的问题
             */
            CuratorWatcher watcher = new ServerWatcher();
            client.getChildren().usingWatcher(watcher).forPath(Constants.SERVER_PATH);


            //通过zk获取到 服务 路径
            List<String> serverPaths = client.getChildren().forPath(Constants.SERVER_PATH);//192.168.10.1#9091

            for (String serverPath : serverPaths) {
                String[] str = serverPath.split("#");
                host = str[0];
                port = Integer.valueOf(str[1]);

                weight = Integer.valueOf(str[2]);

                if(weight > 0){//有权重，对应的服务器的channelFuture会更多
                    for(int i = 0;i<weight;i++){
                        ChannelFutureManager.realServerPaths.add(host+ "#" + port);
                        ChannelFuture channelFuture = bootstrap.connect(host, port);
                        ChannelFutureManager.add(channelFuture);
                    }
                }else{

                    ChannelFutureManager.realServerPaths.add(host+ "#" + port);

                    ChannelFuture channelFuture = bootstrap.connect(host, port);

                    ChannelFutureManager.add(channelFuture);
                }

            }


//            if(realServerPaths.size() > 0){
//                String[] hostAndPort = realServerPaths.toArray()[0].toString().split("#");
//
//                host = hostAndPort[0];
//                port = Integer.valueOf(hostAndPort[1]);
//            }

            //future = bootstrap.connect(host, port).sync();

        }catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }


    /**
     * 每一个请求都是同一个连接，一个客户端对应一个连接，但是在这条连接上有多个请求，那么在众多响应以及众多请求当中应该作出区分
     *  解决方案：在请求中加入唯一id
     * @param request
     * @return
     */
    public static RpcResponse send(RpcRequest request) throws InterruptedException {
        future = ChannelFutureManager.get(ChannelFutureManager.position);
        future.channel().writeAndFlush(JSONObject.toJSONString(request));//序列化并回写
        future.channel().writeAndFlush("\r\n");
        DefaultFuture df = new DefaultFuture(request);//获取响应结果

        return df.get();
    }

}

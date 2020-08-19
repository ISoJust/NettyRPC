package com.yoe.server.init;
import com.yoe.server.constants.Constants;
import com.yoe.server.netty.NettyServerHandler;
import com.yoe.server.zookeeper.ZookeeperFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * 这个通过spring来自动运行
 */
@Component
public class NettyServerInit implements ApplicationListener<ContextRefreshedEvent> {

    /**
     *
     * @param hostname ：绑定主机ip
     * @param port ：绑定端口
     */
    public static void startServer(String hostname,int port){
        startServer0(hostname,port);
    }


    private static void startServer0(String hostname,int port){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);//主线程组，负责监听连接事件
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();//从线程组，负责监听读写事件

        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup,workerGroup)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,false)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new DelimiterBasedFrameDecoder(65535, Delimiters.lineDelimiter()[0]));//分割帧
                            pipeline.addLast(new StringDecoder());

                            pipeline.addLast(new IdleStateHandler(60,40,20, TimeUnit.SECONDS));
                            pipeline.addLast(new NettyServerHandler());

                            pipeline.addLast(new StringEncoder());
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(hostname, port).sync();

            //注册NettyServer到zk中

            int weight = 2;//添加权重
            CuratorFramework client = ZookeeperFactory.create();
            InetAddress inetAddress = InetAddress.getLocalHost();
            client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(Constants.SERVER_PATH+ "/" + inetAddress.getHostAddress()+"#"+port+"#"+weight+"#");//创建临时节点


            System.out.println("服务提供方开始提供服务");

            future.channel().closeFuture().sync();//服务器不提供服务后，关闭通道

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        startServer("127.0.0.1",9091);
    }
}

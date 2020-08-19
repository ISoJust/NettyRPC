package com.yoe.client.zookeeper;

import com.yoe.client.netty.TcpClient;
import com.yoe.client.netty.future.ChannelFutureManager;
import io.netty.channel.ChannelFuture;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import java.util.List;

public class ServerWatcher implements CuratorWatcher {
    @Override
    public void process(WatchedEvent watchedEvent) throws Exception {

        CuratorFramework client = ZookeeperFactory.create();

        String path = watchedEvent.getPath();//获取父类路径

        client.getChildren().usingWatcher(this).forPath(path);


        //获取子路径
        List<String> serverPaths = client.getChildren().forPath(path);

        ChannelFutureManager.realServerPaths.clear();

        //子路径加入Client,不在该循环进行连接是因为还没有去重
        for (String serverPath : serverPaths) {//192.168.10.1#9092

            String[] str = serverPath.split("#");

            String host = str[0];
            int port = Integer.valueOf(str[1]);
            int weight = Integer.valueOf(str[2]);
            if(weight > 0){
                for (int i = 0; i < weight; i++) {
                    ChannelFutureManager.realServerPaths.add(host+ "#" + port);
                }
            }else{
                ChannelFutureManager.realServerPaths.add(host+ "#" + port);
            }
        }



        ChannelFutureManager.clear();
        for (String realServerPath : ChannelFutureManager.realServerPaths) {
            String[] str = realServerPath.split("#");

            String host = str[0];
            int port = Integer.valueOf(str[1]);
            int weight = Integer.valueOf(str[2]);

            if(weight > 0){//有权重，权重越高的服务器对应的channelFuture会更多
                for(int i = 0;i<weight;i++){
                    ChannelFuture channelFuture = TcpClient.bootstrap.connect(host, port);
                    ChannelFutureManager.add(channelFuture);
                }
            }else{

                ChannelFuture channelFuture = TcpClient.bootstrap.connect(host, port);

                ChannelFutureManager.add(channelFuture);
            }
        }




    }
}

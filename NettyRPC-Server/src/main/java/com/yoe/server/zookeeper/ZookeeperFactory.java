package com.yoe.server.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;


public class ZookeeperFactory {

    private static final String CONNECT = "127.0.0.1:2181";

    public static CuratorFramework client;

    /**
     * 单例模式,饿汉
     * @return
     */
    public static CuratorFramework create(){
        if(client == null){
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);//重试机制
            client = CuratorFrameworkFactory.newClient(CONNECT, retryPolicy);
            client.start();
        }

        return client;
    }

    public static void main(String[] args) throws Exception {
        CuratorFramework client = create();
        client.create().forPath("/netty");//创建一个节点
        System.out.println("创建节点成功");
    }
}

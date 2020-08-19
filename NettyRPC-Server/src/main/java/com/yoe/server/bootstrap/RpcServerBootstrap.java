package com.yoe.server.bootstrap;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * RPC框架服务启动类
 */

@Configuration
@ComponentScan("com.yoe.server")//扫描所有的注解
public class RpcServerBootstrap {

    public static void main(String[] args) {
       new AnnotationConfigApplicationContext(RpcServerBootstrap.class);
    }
}

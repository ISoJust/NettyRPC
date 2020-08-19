package com.yoe.client.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.yoe.client.netty.future.DefaultFuture;
import com.yoe.client.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if("ping".equals(msg.toString())){//恢复心跳
            ctx.channel().writeAndFlush("pong\r\n");
            return;
        }

        RpcResponse response = JSONObject.parseObject(msg.toString(),RpcResponse.class);
        DefaultFuture.receive(response);
        System.out.println("收到服务端发送的消息：" + msg.toString());

    }

}


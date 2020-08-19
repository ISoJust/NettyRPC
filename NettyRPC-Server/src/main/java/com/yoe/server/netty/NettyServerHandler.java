package com.yoe.server.netty;

import com.alibaba.fastjson.JSONObject;
import com.yoe.server.mediator.Media;
import com.yoe.server.request.ServerRequest;
import com.yoe.server.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //这里为什么用到ServerRequest而不是RpcRequest，因为RpcRequest是客户端的，这里是server，需要做分开
        //ServerRequest 的组成和RpcRequest是一样的
        ServerRequest request = JSONObject.parseObject(msg.toString(), ServerRequest.class);//将接收到的数据封装成ServerRequest

        Media media = Media.getInstance();

        Object result = media.process(request);


        RpcResponse resp = new RpcResponse();

        resp.setRequestId(request.getId());


        resp.setResult(result);

        ctx.channel().writeAndFlush(JSONObject.toJSONString(resp));

        ctx.channel().writeAndFlush("\r\n");


    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){

            IdleStateEvent event = (IdleStateEvent) evt;//获取事件

            IdleState state = event.state();//得到事件状态

            if(state.equals(IdleState.READER_IDLE)){
                System.out.println("读空闲===");//客户端没有读取服务端数据
                ctx.channel().close();
            }else if(state.equals(IdleState.WRITER_IDLE)){
                System.out.println("写空闲===");

            }else if(state.equals(IdleState.ALL_IDLE)){
                ctx.channel().writeAndFlush("ping\r\n");//给客户端发送ping指令
            }
        }
    }
}

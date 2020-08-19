package com.yoe.client.request;

import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC请求
 */
public class RpcRequest {

    private final long requestId;//请求id

    private Object content;//参数内容

    private String command;//请求的类名+方法名

    private final AtomicLong aid = new AtomicLong(1);

    public RpcRequest() {
        this.requestId = aid.incrementAndGet();
    }

    public long getId() {
        return requestId;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}

package com.yoe.server.request;

import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC请求
 */
public class RpcRequest {

    private final long requestId;//请求id

    private Object content;

    private String command;

    private final AtomicLong aid = new AtomicLong(1);

    public RpcRequest(long id) {
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

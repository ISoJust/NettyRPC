package com.yoe.server.response;

/**
 * 响应结果
 */
public class RpcResponse {
    private Long requestId;//请求id

    private Object result;//调用结果

    private String status;//状态：00000表示成功，其他表示失败

    private String msg;//失败的原因

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

package com.yoe.client.netty.future;

import com.yoe.client.request.RpcRequest;
import com.yoe.client.response.RpcResponse;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultFuture {

    //结果集：key为requestId，value为DefaultFuture
    public static ConcurrentHashMap<Long,DefaultFuture> allDefaultFuture = new ConcurrentHashMap<>();


    private final Lock lock = new ReentrantLock();

    public Condition condition = lock.newCondition();

    public RpcResponse response;


    private long timeOut = 2*60*1000L;

    private long startTime = System.currentTimeMillis();

    public DefaultFuture(RpcRequest request) {
        allDefaultFuture.put(request.getId(),this);
    }


    public DefaultFuture() {
    }



    //主线程获取调用结果，首先需要等待结果返回，因为netty是异步的
    public RpcResponse get(){
        lock.lock();

        try{
            while(!done()){
                condition.await();//这里应该计时等待
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }

        return this.response;
    }


    //主线程获取调用结果，首先需要等待结果返回，因为netty是异步的,超时等待
    public RpcResponse get(long timeOut){
        lock.lock();

        try{
            while(!done()){
                condition.await(timeOut, TimeUnit.SECONDS);//这里应该计时等待
                if((System.currentTimeMillis()-startTime) > timeOut){
                    System.out.println("请求超时");
                    break;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }

        return this.response;
    }




    /**
     * 接收Netty-client的返回结果
     * @param response
     */
    public static void receive(RpcResponse response){
        DefaultFuture df = allDefaultFuture.get(response.getRequestId());

        if(df != null){
            Lock lock = df.lock;
            lock.lock();
            try {
                df.setResponse(response);
                df.condition.signal();

                allDefaultFuture.remove(response.getRequestId());

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }

        }
    }

    /**
     * 是否已经收到调用结果
     * @return
     */
    private boolean done(){
        return response != null;
    }

    public RpcResponse getResponse() {
        return response;
    }

    public void setResponse(RpcResponse response) {
        this.response = response;
    }


    static class FutureThread extends Thread{
        @Override
        public void run() {
             Set<Long> ids = allDefaultFuture.keySet();
            for (Long id : ids) {
                DefaultFuture future = allDefaultFuture.get(id);
                if(future == null){
                    allDefaultFuture.remove(id);
                }else{
                    if(future.getTimeOut() < (System.currentTimeMillis() - future.getStartTime())){//链路发生超时
                        RpcResponse response = new RpcResponse();

                        response.setRequestId(id);

                        response.setMsg("链路请求超时");

                        receive(response);
                    }
                }
            }
        }
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * 开一个线程不断扫描超时defaultFuture
     */
    static{
        FutureThread futureThread = new FutureThread();

        futureThread.setDaemon(true);

        futureThread.start();

    }
}

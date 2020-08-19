package com.yoe.client.netty.future;

import io.netty.channel.ChannelFuture;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelFutureManager {

    public static CopyOnWriteArrayList<String> realServerPaths = new CopyOnWriteArrayList<>();


    public static AtomicInteger position = new AtomicInteger(0);

    public static CopyOnWriteArrayList<ChannelFuture> channelFuturesList = new CopyOnWriteArrayList<>();

    public static void remove(ChannelFuture channelFuture){
        channelFuturesList.remove(channelFuture);
    }


    public static void add(ChannelFuture channelFuture){
        channelFuturesList.add(channelFuture);
    }


    public static void clear(){
        channelFuturesList.clear();
    }


    public static ChannelFuture get(AtomicInteger i){
        int size = channelFuturesList.size();
        ChannelFuture future = null;
        if(i.get() > size){
            future = channelFuturesList.get(0);
            position = new AtomicInteger(1);
        }else{
            future = channelFuturesList.get(i.getAndIncrement());
        }

        if(!future.channel().isActive()){
            remove(future);
            return get(position);
        }

        return future;
    }

}

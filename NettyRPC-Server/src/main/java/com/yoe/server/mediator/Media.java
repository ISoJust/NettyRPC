package com.yoe.server.mediator;

import com.alibaba.fastjson.JSONObject;
import com.yoe.server.request.ServerRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Media {
    public static Map<String,BeanMethod> beanMethodMap;

    private volatile static Media uniqueInstance;

    static {
        beanMethodMap = new HashMap<>();
    }

    private Media(){

    }

    public static Media getInstance(){
        if(uniqueInstance == null){
            synchronized (Media.class){
                if(uniqueInstance == null){
                    return new Media();
                }
            }
        }
        return uniqueInstance;
    }

    /**
     * 真正的业务处理：RPC-Server 收到 RPC-client的请求后
     * @param request
     * @return
     */
    public Object process(ServerRequest request){
        Object result = null;
        try{
            String command = request.getCommand();//获取类

            BeanMethod beanMethod = beanMethodMap.get(command);//获取目标类所有方法

            if(beanMethod == null){
                return null;
            }

            Object bean = beanMethod.getBean();//获取到controller


            Method method = beanMethod.getMethod();

            Class<?> parameterType = method.getParameterTypes()[0];

            Object content = request.getContent();//获取参数

            Object args = JSONObject.parseObject(JSONObject.toJSONString(content),parameterType);

            result = method.invoke(bean, args);

        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

}

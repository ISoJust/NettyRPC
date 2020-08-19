package com.yoe.client.proxy;

import com.yoe.client.annotation.RemoteInvoke;
import com.yoe.client.netty.TcpClient;
import com.yoe.client.request.RpcRequest;
import com.yoe.client.response.RpcResponse;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class InvokeProxy implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        /**
         * 动态代理
         */
        Field[] fields = bean.getClass().getDeclaredFields();

        for (Field field : fields) {
            if(field.isAnnotationPresent(RemoteInvoke.class)){//判断bean的一个字段是否被RemoteInvoke标注，如果被RemoteInvoke标注，则证明该bean需要动态代理
                field.setAccessible(true);

                final Map<Method,Class> methodClassMap = new HashMap<>();

                putMethodClass(methodClassMap,field);

                Enhancer enhancer = new Enhancer();
                enhancer.setInterfaces(new Class[]{field.getType()});//设置需要代理的对象

                enhancer.setCallback(new MethodInterceptor() {
                    @Override
                    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                        //需要通过netty-client调用server

                        RpcRequest rpcRequest = new RpcRequest();

                        rpcRequest.setCommand(methodClassMap.get(method) + "." + method.getName());

                        rpcRequest.setContent(args[0]);

                        RpcResponse response = TcpClient.send(rpcRequest);//通过netty发送远程调用

                        return response;
                    }
                });

                try {
                    field.set(bean,enhancer.create());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }




        return bean;
    }

    /**
     * 对注入的对象的所有方法和属性接口类型放入到一个map当中
     * @param methodClassMap
     * @param field
     */

    private void putMethodClass(Map<Method, Class> methodClassMap, Field field) {

        Method[] methods = field.getType().getDeclaredMethods();

        for (Method method : methods) {
            methodClassMap.put(method,field.getType());
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}

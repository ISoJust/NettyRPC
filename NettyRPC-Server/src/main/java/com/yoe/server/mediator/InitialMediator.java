package com.yoe.server.mediator;


import com.yoe.server.annotation.RemoteService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component
public class InitialMediator implements BeanPostProcessor {
    //初始化Mediator时，应该把所有的Controller以及Controller下的所有的方法都获取到


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean.getClass().isAnnotationPresent(RemoteService.class)){//判断当前bean是否有@Remote注解

            Method[] methods = bean.getClass().getDeclaredMethods();//获取到这个@Remote上的所有方法

            for(Method m : methods){

                //bean.getClass().getInterfaces()[0].getName()得到实现类的第一个接口类名
                String key = bean.getClass().getInterfaces()[0].getName() + "." + m.getName();
                Map<String, BeanMethod> beanMethodMap = Media.beanMethodMap;

                beanMethodMap.put(key,new BeanMethod(key,m));
            }
        }

        return bean;
    }
}

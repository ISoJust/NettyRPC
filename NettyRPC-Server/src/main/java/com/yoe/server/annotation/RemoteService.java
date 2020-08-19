package com.yoe.server.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 被该注解标注的都是远程服务接口
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RemoteService {

    String value() default "";
}

package com.yoe.client.annotation;



import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 被该接口标注的对象会被动态代理
 */


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RemoteInvoke {
}

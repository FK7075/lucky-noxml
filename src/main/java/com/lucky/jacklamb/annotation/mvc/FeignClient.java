package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.*;

/**
 * 定义一个访问注册中心资源的Api
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignClient {

    String value();

    String id() default "";

    String agreement() default "HTTP";

}

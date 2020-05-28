package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.*;

/**
 * @author fk7075
 * @time 2020-5-28
 * 声明该方法在服务器启动时将会被执行
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InitRun {

    /**
     * 若方法有返回值，则将返回值存入IOC容器，使用该id作为ID
     * @return
     */
    String id() default "";

    /**
     * 提供方法运行时所需要的参数
     * @return
     */
    String[] runParam() default{};
}

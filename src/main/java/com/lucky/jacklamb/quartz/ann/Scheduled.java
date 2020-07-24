package com.lucky.jacklamb.quartz.ann;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scheduled {

    String id() default "";

    String cron() default "";

    /** 指定间隔时间 */
    long fixedDelay() default -1L;

    boolean runNow() default false;
}
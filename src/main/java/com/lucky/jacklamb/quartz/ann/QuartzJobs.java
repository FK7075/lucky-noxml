package com.lucky.jacklamb.quartz.ann;

import java.lang.annotation.*;

/**
 * 定义一个定时任务组件
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QuartzJobs {

    /** 组件的唯一ID */
    String value() default "";
}

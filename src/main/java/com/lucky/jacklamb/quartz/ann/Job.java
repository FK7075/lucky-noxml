package com.lucky.jacklamb.quartz.ann;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Job {
    String value() default "";
}

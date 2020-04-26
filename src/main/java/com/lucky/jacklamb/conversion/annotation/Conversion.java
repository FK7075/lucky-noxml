package com.lucky.jacklamb.conversion.annotation;

import com.lucky.jacklamb.conversion.LuckyConversion;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conversion {

    String id() default "";

    Class<? extends LuckyConversion>[] value() default LuckyConversion.class;
}

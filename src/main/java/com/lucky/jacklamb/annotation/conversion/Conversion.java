package com.lucky.jacklamb.annotation.conversion;

import com.lucky.jacklamb.tcconversion.todto.LuckyConversion;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conversion {

    String id() default "";

    Class<? extends LuckyConversion>[] value() default LuckyConversion.class;
}

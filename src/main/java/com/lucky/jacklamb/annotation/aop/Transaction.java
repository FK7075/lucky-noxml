package com.lucky.jacklamb.annotation.aop;

import java.lang.annotation.*;
import java.sql.Connection;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transaction {

    int isolationLevel() default -1;

}

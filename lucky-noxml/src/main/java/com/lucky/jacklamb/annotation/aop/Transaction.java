package com.lucky.jacklamb.annotation.aop;

import java.lang.annotation.*;
import java.sql.Connection;

/**
 * 数据库事务
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transaction {

    /**
     * 设置事务的隔离级别
     * @return isolationLevel
     */
    int isolationLevel() default -1;

}

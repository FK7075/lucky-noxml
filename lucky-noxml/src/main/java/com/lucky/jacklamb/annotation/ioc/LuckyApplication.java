package com.lucky.jacklamb.annotation.ioc;

import com.lucky.jacklamb.ioc.auto.ann.AopImport;
import com.lucky.jacklamb.aop.expandpoint.CacheExpandPoint;
import com.lucky.jacklamb.aop.expandpoint.TransactionPoint;

import java.lang.annotation.*;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/14 5:24 上午
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AopImport(points = {TransactionPoint.class, CacheExpandPoint.class})
public @interface LuckyApplication {
}

package com.lucky.jacklamb.ioc.auto.ann;

import com.lucky.jacklamb.aop.core.InjectionAopPoint;
import com.lucky.jacklamb.ioc.auto.factory.InjectionAopPointFactory;

import java.lang.annotation.*;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/14 2:28 上午
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AopImport {

    Class<? extends InjectionAopPoint>[] points() default{};

    String[] classpath() default {};

    Class<? extends InjectionAopPointFactory>[] factory() default {};
}

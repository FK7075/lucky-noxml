package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.*;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/7/19 11:33 下午
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionHandler {

     Class<? extends Throwable>[] value();
}

package com.lucky.jacklamb.annotation.mvc;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/7/19 11:33 下午
 */
public @interface ExceptionHandler {
     Class<? extends Throwable>[] value() default {};
}

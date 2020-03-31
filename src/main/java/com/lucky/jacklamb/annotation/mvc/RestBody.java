package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lucky.jacklamb.enums.Rest;

/**
 * 
 * @author fk-7075
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestBody {
	Rest value() default Rest.JSON;
}

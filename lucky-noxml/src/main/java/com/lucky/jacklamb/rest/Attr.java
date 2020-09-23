package com.lucky.jacklamb.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于配置Json和Xml映射名
 * @author fk-7075
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Attr {
	
	/**
	 * 配置Json或Xml映射名
	 * @return
	 */
	String value() default "";
	
}

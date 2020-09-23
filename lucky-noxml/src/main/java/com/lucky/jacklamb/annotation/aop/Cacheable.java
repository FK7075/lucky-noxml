package com.lucky.jacklamb.annotation.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AOP缓存，用于方法上，将方法的返回值以指定的key加入缓存
 * @author fk-7075
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {
	
	/**
	 * 缓存容器的名字
	 * @return
	 */
	String value();
	
	/**
	 * 缓存对象在缓存容器中的key
	 * @return
	 */
	String key();
	
	/**
	 * 条件，满足此条件则执行缓存
	 * @return
	 */
	String condition() default "";
}

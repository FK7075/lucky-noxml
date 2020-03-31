package com.lucky.jacklamb.annotation.orm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 外键标识
 * @author fk-7075
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Key {
	
	/**
	 * 外键字段名
	 * @return
	 */
	String value() default "";
	
	/**
	 * 设置建表时的字段长度,默认35
	 * @return
	 */
	int length() default 35;
	
	/**
	 * 建表时是否允许该字段为NULL，默认true
	 * @return
	 */
	boolean allownull() default true;
	
	/**
	 * 外键所指向主表对应的实体类Class
	 * @return
	 */
	Class<?> pojo();
}

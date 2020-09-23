package com.lucky.jacklamb.annotation.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lucky.jacklamb.enums.scope;

/**
 * 定义IOC组件的作用域
 * @author fk-7075
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {
	
	/**
	 * 作用域
	 * @return
	 */
	scope value() default scope.SINGLETON;
}

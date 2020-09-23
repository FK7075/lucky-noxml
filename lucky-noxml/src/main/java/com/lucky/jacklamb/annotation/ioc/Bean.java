package com.lucky.jacklamb.annotation.ioc;

import com.lucky.jacklamb.ioc.enums.IocCode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注册一个Component组件，需要配合@BeanFactory注解使用
 * @author fk-7075
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {
	
	/**
	 * 为该Component组件指定一个唯一ID，默认会使用[类名.方法名]作为组件的唯一ID
	 * @return
	 */
	String value() default "";

	/**
	 * 指定组件的类型，默认为Component组件
	 * @return
	 */
	IocCode iocCode() default IocCode.COMPONENT;
}

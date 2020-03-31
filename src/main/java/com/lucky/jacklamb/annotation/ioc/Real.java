package com.lucky.jacklamb.annotation.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lucky.jacklamb.enums.Location;

/**
 * 与Aop相关，定义一个被增强的类和方法
 * 	value：标注在类上时使用，设置该被增强类所实现的接口的全路径
 * 	classname：所使用的的扩展类的ID
 * 	method：所使用的扩展方法的ID
 * 	parameter：扩展方法中的参数列表所对应的值，如果要使用被增强方法中的参数，则使用"#需要共享的参数名"
 * 	location：增强方式(前置，后置)
 * @author fk-7075
 *
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Real {
	String value() default "";
	String classname() default "";
	String method() default "";
	String[] parameter() default {};
	Location location() default Location.BEFORE;
	
}

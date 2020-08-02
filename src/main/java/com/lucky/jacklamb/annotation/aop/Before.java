package com.lucky.jacklamb.annotation.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明一个前置增强
 * @author fk-7075
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Before {
	
	/**
	 * 设置增强方法的唯一标记(默认值：方法名)
	 * @return
	 */
	String value() default "";
	
	/**
	 * 配置切面(Class)，增强方法执行的范围，用来定位需要代理的真实类<br>
	 * pointCutClass的值必须以下列前缀开始,多个值使用","分隔:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * ioc:表示增强一种或多种类型的所有组件,可选值有:[controller,service,repository,component] eg:mateClass="ioc:component,service"<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * id:表示增强一个或多个指定ID的IOC组件,eg:mateClass="id:beanId1,beanId2"<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * path:表示增强某个路径下的所有IOC组件，eg:mateClass="path:com.lucky.*" OR mateClass="path:com.lucky.User"<br>
	 * @return
	 */
	String pointCutClass() default "ioc:service";
	
	/**
	 * 配置切点(Method)， 增强方法执行的范围，用来定位需要代理的真实类的一些具体方法<br>
	 * 多个值使用","分隔,支持"*"、"!"<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * 方法名定位：需要增强的方法名，eg:mateMethod="method1,method2"<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * 方法名+参数类型定位：需要增强的方法名+参数列表，eg:mateMethod="method1(String,int),method2(User,Double)"<br>
	 * @return
	 */
	String pointCutMethod() default "public,*";

	/**
	 * 优先级，优先级高的增强将会被优先执行
	 * @return
	 */
	double priority() default 5;
}

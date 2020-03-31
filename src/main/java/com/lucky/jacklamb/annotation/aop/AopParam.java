package com.lucky.jacklamb.annotation.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 设置增强参数
 * @author fk-7075
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AopParam {
	
	/**
	 * 设置增强方法的参数<br>
	 * value有6种指定的写法,不同的前缀代表不同的含义：<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * ref:表示将IOC容器中ID为id的组件设置对应位置的参数，eg:params={"ref:beanId"}<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * ind:表示将真实方法作为增强方法的参数，eg:params={"ind:index"},表示将真实方法参数列表中的第index个设置为增强方法的参数<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * name:表示将真实方法作为增强方法的参数，eg:params={"name:lucky"},表示将真实方法参数列表中的lucky参数设置为增强方法的参数<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * 无前缀:设置Java基本类型的值<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * [method]:表示将真实方法对应的Method对象设置为增强方法的参数，此位置的参数类型必须为Method<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * [params]:表示将真实方法的参数列表作为增强对象的参数，此位置的参数类型必须为Object[]<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;
	 * [target]:表示将真实类的Class作为参数，此位置的参数类型必须为Class<br>
	 * @return
	 */
	String value();

}

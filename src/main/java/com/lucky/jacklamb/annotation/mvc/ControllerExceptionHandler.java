package com.lucky.jacklamb.annotation.mvc;

import com.lucky.jacklamb.enums.Rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ControllerExceptionHandler {
	
	String id() default "";
	
	String[] value() default {};

	/**
	 * 指定对Controller中所有方法的返回值处理策略<br>
	 * 1.Rest.NO(默认选项)：转发与重定向处理,只对返回值类型为String的结果进行处理<br>
	 *  &nbsp;&nbsp;&nbsp;
	 * a.转发到页面：无前缀 return page<br>
	 * 	&nbsp;&nbsp;&nbsp;
	 * b.转发到Controller方法:return forward:method<br>
	 *	&nbsp;&nbsp;&nbsp;
	 * c.重定向到页面：return page:pageing<br>
	 *	&nbsp;&nbsp;&nbsp;
	 * d.重定向到Controller方法：return redirect:method<br>
	 * 2.Rest.TXT：将返回值封装为txt格式，并返回给客户端<br>
	 * 3.Rest.JSON：将返回值封装为json格式，并返回给客户端<br>
	 * 4.Rest.XML：将返回值封装为xml格式，并返回给客户端
	 * @return
	 */
	Rest rest() default Rest.NO;
	
	boolean global() default true;
	
}

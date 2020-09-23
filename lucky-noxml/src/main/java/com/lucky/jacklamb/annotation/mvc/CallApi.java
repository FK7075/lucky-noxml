package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.*;

/**
 * 定义一个远程资源访问
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CallApi {
	
	/**
	 * 定义一个远程url请求映射
	 * @return
	 */
	String value() default "";

	
}

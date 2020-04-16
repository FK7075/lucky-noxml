package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.*;

/**
 * 使用MD5加密
 * @author fk-7075
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MD5 {

	String value() default "";

	/**
	 * 盐
	 * @return
	 */
	String salt() default "";

	/**
	 * 循环加密次数
	 * @return
	 */
	int cycle() default 1;

	/**
	 * 是否将英文变为大写
	 * @return
	 */
	boolean capital() default false;

}

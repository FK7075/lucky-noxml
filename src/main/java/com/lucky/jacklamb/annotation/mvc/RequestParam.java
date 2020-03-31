package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数注入
 * @author fk-7075
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
	
	/**
	 * 设置一个默认值，支持基本类型和Ioc容器值
	 * @return
	 */
	String def() default "LCL*#*$FK%_58314@XFL_*#*LCL";
	
	/**
	 * 请求中的参数名
	 * @return
	 */
	String value() default "";
}

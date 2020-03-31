package com.lucky.jacklamb.annotation.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DI相关的注解
 * @author fk-7075
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
	
	/**
	 * 指定要注入对象的ID，不指定则会启动类型扫描机制进行自动注入
	 * @return
	 */
	String value() default "";
}

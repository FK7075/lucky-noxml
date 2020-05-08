package com.lucky.jacklamb.annotation.orm;

import java.lang.annotation.*;

/**
 * 标注属性不参与SQL的包装
 * @author fk-7075
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoPackage {
	
	String value() default "";

}
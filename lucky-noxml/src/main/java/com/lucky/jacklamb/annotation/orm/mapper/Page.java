package com.lucky.jacklamb.annotation.orm.mapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义一个分页参数，使用@Page标注的页码参数会自动转化为开始位置参数[注意：两个分页参数必须写在参数列表的最后]
 * @author fk-7075
 *
 */
@Target({ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Page {
	String value() default "";
}

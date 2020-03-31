package com.lucky.jacklamb.annotation.orm.mapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ORM的Mapper接口中使用，定义一个增加的数据库操作
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Insert {
	
	/**
	 * 设置预编译的SQL （eg: INSERT INTO book(bname,price,author) VALUES(?,?,?)）
	 * @return
	 */
	String value() default "";
	
	/**
	 * 开启对象模式的批量操作(方法的入参必须为List<T>)
	 * @return
	 */
	boolean batch() default false;
	
	/**
	 * 自增主键的自动赋值,默认为false
	 * @return
	 */
	boolean setautoId() default false;
}

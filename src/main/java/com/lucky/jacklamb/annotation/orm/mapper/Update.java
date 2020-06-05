package com.lucky.jacklamb.annotation.orm.mapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ORM的Mapper接口中使用，定义一个更新的数据库操作
 * 	value：设置预编译的SQL (eg: UPDATE SET price=?,bname=? WHERE bid=?)
 *  指定更新操作的条件：更新操作的条件参数需要手动传入，参数类型限定为String和List[Strirng],并且使用时需要@X的标记
 * @author fk-7075
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Update {

	String value() default "";

	boolean batch() default false;

}

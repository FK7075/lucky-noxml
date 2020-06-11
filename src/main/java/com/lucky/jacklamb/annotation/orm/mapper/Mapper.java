package com.lucky.jacklamb.annotation.orm.mapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在Mapper接口上，注入一个java类型的mapper配置文件 java配置文件的SQl配置规则：
 * 每个没有使用注解的mapper接口方法都可以在配置类中绑定一组执行SQL，形式为一个String类型变量名+"特定SQL"
 * eg：
 * 普通模式：正常SQL->[SELECT * FROM book WHERE bid=?]
 * 开启基于非空检查的动态sql模式：C:SQL->[C:SELECT * FROM book WHERE bid=? AND bprice=?]
 * 
 * value() SQl配置类的Class
 * properties() SQl配置文件的classpth( eg : com/lucky/mapper/user.properties)
 * codedformat() properties配置文件的编码格式(默认值为gbk)
 * @author fk-7075
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapper {
	
	/**
	 * 为该Mapper组件指定一个唯一ID，默认会使用[首字母小写类名]作为组件的唯一ID
	 * @return
	 */
	String id() default "";
	
	/**
	 * Mapper接口所使用的数据源，默认defaultDB
	 * @return
	 */
	String dbname() default "defaultDB";
	
	/**
	 * 外部SQl配置类的Class
	 * @return
	 */
	Class<?> value() default Void.class;
	
	/**
	 * 外部SQl配置文件的classpth( eg : com/lucky/mapper/user.properties)
	 * @return
	 */
	String[] properties() default {};
	
	/**
	 * properties配置文件的编码格式(默认值为UTF-8)
	 * @return
	 */
	String  codedformat() default "UTF-8";
}

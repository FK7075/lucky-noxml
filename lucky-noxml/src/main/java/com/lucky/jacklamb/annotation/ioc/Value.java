package com.lucky.jacklamb.annotation.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DI相关的注解
 * @author fk-7075
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
	
	/**
	 * 指定要注入对象的信息<br>
	 * 1.当属性为Java基本类型时->[@Value("value")]<br>
	 * 2.当属性为自定义的引用类型时->[@Value("iocID")]
	 * 3.当属性类型为Java基本类型的数组时->[@Value({"value1",……,"value10"})]<br>
	 * 4.当属性类型为Java的Collection类型,泛型类型为基本类型的包装类型时->[@Value({"value1",……,"value10"})]<br>
	 * 5.当属性类型为Java的Collection类型,泛型类型为自定义的引用类型时->[@Value({"iocID1",……,"iocID10"})]<br>
	 * 6.当属性类型为Java的Map类型,泛型类型为基本类型的包装类型时->[@Value({"key1:value1",……,"key10:value10"})]<br>
	 * 7.当属性类型为Java的Map类型,泛型类型为自定义的引用类型时->[@Value({"iocID1:iocIDa",……,"iocID10:iocIDj"})]<br>
	 * 8.Map属性的其他情况请结合6,7<br>
	 * 9.默认情况时,启动类型扫描机制进行自动注入
	 * @return
	 */
	String[] value() default {};
	
}

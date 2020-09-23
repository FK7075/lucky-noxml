package com.lucky.jacklamb.annotation.orm;

import com.lucky.jacklamb.enums.PrimaryType;

import java.lang.annotation.*;

/**
 * 主键映射
 * 
 * @author fk-7075
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ids {
	Id[] value();
}

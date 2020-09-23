package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.*;

/**
 * 调用远程接口，并将返回的json结果封装到对象中
 * @author fk-7075
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CallBody {

	String value() default "";

}

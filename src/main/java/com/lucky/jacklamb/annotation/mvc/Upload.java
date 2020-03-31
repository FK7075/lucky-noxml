package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义一个文件上传操作，执行完上传操作后会返回所有文件的文件名如果需要接收，可以在方法的参数列表中使用与names对应的同名的String类型参数接收
 * 
 * @author fk-7075
 *
 */
@Target({ElementType.METHOD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Upload {
	
	/**
	 * 表单中<form enctype="multipart/form-data">---<input type='file'>的name属性值或者Ajax请求中{formData.append("name",$('#crowd_file')[0].files[0]);data:formData]name属性所组成的数组
	 * @return
	 */
	String[] names();
	
	/**
	 * 上传到项目文件夹的位置(对应names)
	 * @return
	 */
	String[] filePath();
	
	/**
	 * 允许上传的文件类型(eg:  .jpg,.jpeg,.png),默认不做限制
	 * @return
	 */
	String type() default "";
	
	/**
	 * 允许上传的最大文件大小(单位:kb)，默认不做限制
	 * @return
	 */
	int maxSize() default 0;
}

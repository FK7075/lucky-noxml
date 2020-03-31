  package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MVC中定义一个文件下载的操作，只能使用在Controller的方法映射方法上
 * @author fk-7075
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Download {
	
	/**
	 * 接受URL请求中包含的文件相对docBase文件夹的相对路径的参数值<br>
	 * eg:http://localhost:8080/download?file="image/1.jpg"<br>
	 * -@Download(name="file")
	 * @return
	 */
	String name() default "";
	
	/**
	 * 要下载文件的绝对路径
	 * @return
	 */
	String path() default "";
	
	/**
	 * 要下载文件相对docBase文件夹的相对路径 
	 * @return
	 */
	String docPath() default "";
	
	/**
	 * 文件所在文件夹相对docdocBase的位置
	 * @return
	 */
	String folder() default "";
	
	/**
	 * 下载网络上的资源（eg:https://github.com/FK7075/lucky-ex/blob/noxml/image/images.png）
	 * @return
	 */
	String url() default "";
}

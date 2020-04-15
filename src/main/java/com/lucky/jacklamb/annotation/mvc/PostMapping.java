package com.lucky.jacklamb.annotation.mvc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义一个只响应POST类型请求的URL映射(支持Rest风格的URL)<br>
 * 1.普通类型的URL映射<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;
 * -@PostMapping("/server/query")<br>
 * 2.Rest风格的URL映射<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;
 * -@PostMapping("/server/query/name/#{name}/sex/#{sex}")<br>
 * 3.带有特殊符号的URL映射<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;
 * a."?":匹配一个任意的字符串<br>&nbsp;&nbsp;&nbsp;&nbsp;
 * -@PostMapping("/server/?/query")<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;
 * b.xxx*:匹配一个以xxx开头的字符串<br>&nbsp;&nbsp;&nbsp;&nbsp;
 * -@PostMapping("/server/admin_* /query")<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;
 * c.*xxx：匹配一个以xxx结尾的字符串<br>&nbsp;&nbsp;&nbsp;&nbsp;
 * -@PostMapping("/server/*_admin/query")<br>
 * @author fk-7075
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostMapping {
	
	/**
	 * 定义一个url请求映射
	 * @return
	 */
	String value() default "";
	
	/**
	 * 指定一些合法访问的ip地址，来自其他ip地址的请求将会被拒绝
	 * @return
	 */
	String[] ip() default {};

	/**
	 * 调用远程接口
	 * @return
	 */
	String callapi() default "";

	/**
	 * 指定一些合法访问的ip段，来自其他ip地址的请求将会被拒绝
	 * @return
	 */
	String[] ipSection() default {};
	
}

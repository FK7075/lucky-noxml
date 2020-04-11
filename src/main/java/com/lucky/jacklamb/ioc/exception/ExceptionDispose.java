package com.lucky.jacklamb.ioc.exception;

import java.lang.reflect.Method;

import com.lucky.jacklamb.servlet.Model;

public abstract class ExceptionDispose {
	
	/**
	 * Model对象
	 */
	protected Model model;
	
	/**
	 * 当前请求响应的Controller对象
	 */
	protected Object controllerObj;

	/**
	 * 当前请求响应的Controller对象的Class对象
	 */
	protected Class<?> currClass;

	/**
	 * 当前请求响应的Controller方法
	 */
	protected Method currMethod;
	
	/**
	 * 当前请求响应的Controller方法参数
	 */
	protected Object[] params;
	
	
	
	public void init(Model model, Object controllerObj, Class<?> currClass,
			Method currMethod, Object[] params) {
		this.model = model;
		this.controllerObj = controllerObj;
		this.currClass = currClass;
		this.currMethod = currMethod;
		this.params = params;
	}



	/**
	 * 异常处理
	 */
	public abstract void dispose(Throwable e);
	

}

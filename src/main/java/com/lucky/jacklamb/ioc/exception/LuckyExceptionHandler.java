package com.lucky.jacklamb.ioc.exception;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import com.lucky.jacklamb.annotation.mvc.ControllerExceptionHandler;
import com.lucky.jacklamb.annotation.mvc.ExceptionHandler;
import com.lucky.jacklamb.annotation.mvc.RestBody;
import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.servlet.core.LuckyDispatcherServlet;
import com.lucky.jacklamb.servlet.core.Model;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

public abstract class LuckyExceptionHandler {

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
	public void dispose(Throwable e){
		Method method= getExceptionMethod(e);
		if(method==null){
			model.error(e, Code.ERROR);
			return;
		}
		Rest rest=this.getClass().getAnnotation(ControllerExceptionHandler.class).rest();
		Rest methodRest=null;
		if(method.isAnnotationPresent(RestBody.class)){
			methodRest=method.getAnnotation(RestBody.class).value();
		}
		rest=methodRest==null?rest:methodRest;
		Parameter[] parameters = method.getParameters();
		Object[] params=new Object[parameters.length];
		int i=0;
		for (Parameter parameter : parameters) {
			Class<?> type = parameter.getType();
			if(Throwable.class.isAssignableFrom(type)){
				params[i]=e;
			}else if(Model.class.isAssignableFrom(type)){
				params[i]=model;
			}else if(Method.class.isAssignableFrom(type)){
				params[i]=currMethod;
			}else if(Class.class.isAssignableFrom(type)){
				params[i]=currClass;
			}else if(HttpRequest.class.isAssignableFrom(type)){
				params[i]=model.getRequest();
			}else if(HttpResponse.class.isAssignableFrom(type)){
				params[i]=model.getResponse();
			}else if(HttpSession.class.isAssignableFrom(type)){
				params[i]=model.getSession();
			}else if(ServletContext.class.isAssignableFrom(type)){
				params[i]=model.getServletContext();
			}else if(Object[].class.isAssignableFrom(type)){
				params[i]=this.params;
			}else if(Object.class.isAssignableFrom(type)){
				params[i]=controllerObj;
			}else{

			}
			i++;
			method.setAccessible(true);
			final Object result;
			try {
				result = method.invoke(this, params);
			} catch (IllegalAccessException ef) {
				model.error(ef,Code.ERROR);
				return;
			} catch (InvocationTargetException ef) {
				model.error(ef,Code.ERROR);
				return;
			}
			if (result != null) {
				if (rest == Rest.JSON) {
					model.writerJson(result);
					return;
				}
				if (rest == Rest.XML) {
					model.witerXml(result);
					return;
				}
				if (rest == Rest.TXT) {
					model.writer(result.toString());
					return;
				}
				if (rest == Rest.NO) {
					if (String.class.isAssignableFrom(result.getClass())) {
						model.forward(rest.toString());
						return;
					} else {
						throw new RuntimeException("返回值类型错误，无法完成转发操作!合法的返回值类型为String，错误位置：" + method);
					}
				}
			}
		}

	}

	private Method getExceptionMethod(Throwable e){
		Method runMethod=null;
		Method[] allMethod = ClassUtils.getAllMethod(this.getClass());
		for (Method method : allMethod) {
			if(method.isAnnotationPresent(ExceptionHandler.class)){
				Class<? extends Throwable>[] es = method.getAnnotation(ExceptionHandler.class).value();
//				if(contains(es,e.getClass())){
//
//				}

			}

		}

		return runMethod;
	}

	private boolean contains(Class<?>[] classes,Class<?> clzz){
		if(!Throwable.class.isAssignableFrom(clzz))
			return false;
		for (Class<?> aClass : classes) {
			if(aClass.isAssignableFrom(clzz))
				return true;
		}
		return false;
	}
	

}

package com.lucky.jacklamb.ioc.exception;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.lucky.jacklamb.annotation.ioc.Controller;
import com.lucky.jacklamb.servlet.Model;
import com.lucky.jacklamb.utils.LuckyUtils;

/**
 * 全局异常处理基类
 * 
 * @author fk-7075
 *
 */
public abstract class LuckyExceptionHand {

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

	/**
	 * Model对象
	 */
	protected Model model;

	/**
	 * ExceptionDisposeHand的注册中心
	 */
	private List<ExceptionDisposeHand> registry;

	public void initialize(Model model, Object controllerObj, Method currMethod, Object[] params) {
		this.controllerObj = controllerObj;
		this.currClass = controllerObj.getClass();
		this.currMethod = currMethod;
		this.params = params;
		this.model = model;
		registry = new ArrayList<>();
	}

	/**
	 * 提供给用户重写，Controller全局异常处理
	 * @param e
	 */
	protected void globalExceptionHand(Throwable e) {
		model.writer(e.getMessage());
		e.printStackTrace();
	}
	
	/**
	 * 提供给用户重写，指定异常处理[指定类，指定方法]
	 */
	public void exceptionHand() {
		
	}
	
	/**
	 * 注册ExceptionDisposeHand
	 * @param exceptionDisposeHands
	 */
	public void registered(ExceptionDisposeHand...exceptionDisposeHands) {
		Stream.of(exceptionDisposeHands).forEach(registry::add);
	}
	

	/**
	 * 回调方法，被Lucky调用
	 * @param e
	 */
	public void exceptionRole(Throwable e) {
		if (registry.isEmpty()) {
			globalExceptionHand(e);
			return;
		}
		String ctrlName = getControllerID();
		String cmethodName = currMethod.getName();
		for(ExceptionDisposeHand methodED:registry) {
			//方法优先
			if(methodED.root(ctrlName,cmethodName)) {
				methodED.getDispose().dispose(e);
				return;
			}
			//类其次
			if(methodED.root(ctrlName)) {
				methodED.getDispose().dispose(e);
				return;
			}

		}
		globalExceptionHand(e);
	}
	
	private String getControllerID() {
		if(currClass.isAnnotationPresent(Controller.class)) {
			Controller annotation = currClass.getAnnotation(Controller.class);
			if(!"".equals(annotation.value()))
				return currClass.getAnnotation(Controller.class).value();
			return LuckyUtils.TableToClass1(currClass.getSimpleName());
		}else {
			return LuckyUtils.TableToClass1(currClass.getSimpleName());
		}
	}
}


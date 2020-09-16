package com.lucky.jacklamb.servlet.exceptionhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.lucky.jacklamb.ioc.ApplicationBeans;

/**
 * 异常处理映射器
 * 为某个LuckyExceptionHandler绑定一个执行映射，只有对应的Controller或者
 * Controller方法出现异常时才会执行这个LuckyExceptionHandler，否则执行默认的异常处理！
 * @author fk-7075
 *
 */
public class ExceptionMapping {
	
	/**
	 * 指定的对象/方法
	 */
	private List<String> classOrMethodNames;

	/**
	 * 对应的异常处理
	 */
	private LuckyExceptionHandler dispose;
	
	public ExceptionMapping(String[] classOrMethodNames, LuckyExceptionHandler dispose) {
		this.classOrMethodNames =new ArrayList<>();
		Stream.of(classOrMethodNames).forEach(this.classOrMethodNames::add);
		this.dispose = dispose;
	}

	public List<String> getClassOrMethodNames() {
		return classOrMethodNames;
	}

	public void setClassOrMethodNames(List<String> classOrMethodNames) {
		this.classOrMethodNames = classOrMethodNames;
	}

	public LuckyExceptionHandler getDispose() {
		return dispose;
	}

	public void setDispose(LuckyExceptionHandler dispose) {
		this.dispose = dispose;
	}
	
	public boolean root(String controllerID) {
		return classOrMethodNames.contains(controllerID)&&ApplicationBeans.createApplicationBeans().contains(controllerID);
	}
	
	public boolean root(String controllerID,String methodName) {
		return classOrMethodNames.contains(controllerID+"."+methodName)&&ApplicationBeans.createApplicationBeans().contains(controllerID);
	}


}

package com.lucky.jacklamb.ioc.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.lucky.jacklamb.ioc.ApplicationBeans;

/**
 * 特定对象/方法的异常处理器
 * @author fk-7075
 *
 */
public class ExceptionDisposeHand {
	
	/**
	 * 指定的对象/方法
	 */
	private List<String> hander;

	/**
	 * 对应的异常处理
	 */
	private LuckyExceptionHandler dispose;
	
	public ExceptionDisposeHand(String[] hander, LuckyExceptionHandler dispose) {
		this.hander=new ArrayList<>();
		Stream.of(hander).forEach(this.hander::add);
		this.dispose = dispose;
	}

	public List<String> getHander() {
		return hander;
	}

	public void setHander(List<String> hander) {
		this.hander = hander;
	}

	public LuckyExceptionHandler getDispose() {
		return dispose;
	}

	public void setDispose(LuckyExceptionHandler dispose) {
		this.dispose = dispose;
	}
	
	public boolean root(String controllerID) {
		return hander.contains(controllerID)&&ApplicationBeans.createApplicationBeans().contains(controllerID);
	}
	
	public boolean root(String controllerID,String methodName) {
		return hander.contains(controllerID+"."+methodName)&&ApplicationBeans.createApplicationBeans().contains(controllerID);
	}


}

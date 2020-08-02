package com.lucky.jacklamb.aop.core;

import java.util.List;

import net.sf.cglib.proxy.MethodProxy;

public class Chain {
	
	private List<Point> points;
	
	private int index=-1;
	
	private Object target;
	
	private Object[] params;
	
	private MethodProxy methodProxy;
	
	public Chain(List<Point> points, Object target, Object[] params, MethodProxy methodProxy) {
		this.points = points;
		this.target = target;
		this.params = params;
		this.methodProxy = methodProxy;
	}

	int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}



	public Object[] getParams() {
		return params;
	}



	public void setParams(Object[] params) {
		this.params = params;
	}



	public MethodProxy getMethodProxy() {
		return methodProxy;
	}



	public void setMethodProxy(MethodProxy methodProxy) {
		this.methodProxy = methodProxy;
	}


	public Object proceed() throws Throwable {
		Object result;
		if(++index==points.size()) {
			result=methodProxy.invokeSuper(target, params);
		}else {
			Point point=points.get(index);
			result=point.proceed(this);
		}
		return result;
	}
	
	

}

package com.lucky.jacklamb.aop.proxy;

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

	void setIndex(int index) {
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


	public Object proceed() {
		Object result;
		if(++index==points.size()) {
			try {
				result=methodProxy.invokeSuper(target, params);
			} catch (Throwable e) {
				throw new RuntimeException("Throwable", e);
			}
		}else {
			Point point=points.get(index);
			result=point.proceed(this);
		}
		return result;
	}
	
	

}

package com.lucky.jacklamb.aop.core;

import com.lucky.jacklamb.aop.proxy.LuckyAopMethodInterceptor;
import com.lucky.jacklamb.cglib.CglibProxy;
import com.lucky.jacklamb.utils.websocket.BaseWebSocket;

import java.util.List;

public class PointRunFactory {
	
	private static PointRunFactory proxyFactory;
	
	private PointRunFactory() {}
	
	public static PointRunFactory createProxyFactory() {
		if(proxyFactory==null)
			proxyFactory=new PointRunFactory();
		return proxyFactory;
	}
	
	/**
	 * 得到一个代理对象
	 * @param targetClass 真实类的Class
	 * @param pointRuns 增强Points(可变参形式)
	 * @return
	 */
	public Object getProxy(Class<?> targetClass, PointRun...pointRuns) {
		return CglibProxy.getCglibProxyObject(targetClass,new LuckyAopMethodInterceptor(pointRuns));
	}
	
	/**
	 * 得到一个代理对象
	 * @param targetClass 真实类的Class
	 * @param pointRuns 增强Points(集合参形式)
	 * @return
	 */
	public Object getProxy(Class<?> targetClass,List<PointRun> pointRuns) {
		return CglibProxy.getCglibProxyObject(targetClass,new LuckyAopMethodInterceptor(pointRuns));
	}

}

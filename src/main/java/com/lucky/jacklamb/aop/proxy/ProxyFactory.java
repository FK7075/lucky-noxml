package com.lucky.jacklamb.aop.proxy;

import com.lucky.jacklamb.cglib.CglibProxy;

import java.util.List;

public class ProxyFactory {
	
	private static ProxyFactory proxyFactory;
	
	private ProxyFactory() {}
	
	public static ProxyFactory createProxyFactory() {
		if(proxyFactory==null)
			proxyFactory=new ProxyFactory();
		return proxyFactory;
	}
	
	/**
	 * 得到一个代理对象
	 * @param targetClass 真实类的Class
	 * @param pointRuns 增强Points(可变参形式)
	 * @return
	 */
	public Object getProxy(Class<?> targetClass,PointRun...pointRuns) {
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

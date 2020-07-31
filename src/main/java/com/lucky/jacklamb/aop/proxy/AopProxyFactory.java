package com.lucky.jacklamb.aop.proxy;

import com.lucky.jacklamb.cglib.CglibProxy;

import java.util.List;

public class AopProxyFactory {
	
	private static AopProxyFactory proxyFactory;
	
	private AopProxyFactory() {}
	
	public static AopProxyFactory createProxyFactory() {
		if(proxyFactory==null)
			proxyFactory=new AopProxyFactory();
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

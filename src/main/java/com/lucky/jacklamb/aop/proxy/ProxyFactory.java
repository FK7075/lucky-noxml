package com.lucky.jacklamb.aop.proxy;

import java.util.List;

import net.sf.cglib.proxy.Enhancer;

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
	 * @param restPoints 增强Points(可变参形式)
	 * @return
	 */
	public Object getProxy(Class<?> targetClass,PointRun...pointRuns) {
		final Enhancer en=new Enhancer();
		en.setSuperclass(targetClass);
		en.setCallback(new LuckyMethodInterceptor(pointRuns));
		return en.create();
	}
	
	/**
	 * 得到一个代理对象
	 * @param targetClass 真实类的Class
	 * @param restPoints 增强Points(集合参形式)
	 * @return
	 */
	public Object getProxy(Class<?> targetClass,List<PointRun> pointRuns) {
		final Enhancer en=new Enhancer();
		en.setSuperclass(targetClass);
		en.setCallback(new LuckyMethodInterceptor(pointRuns));
		return en.create();
	}

}

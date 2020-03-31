package com.lucky.jacklamb.aop.proxy;

import java.lang.reflect.Method;

/**
 * 环绕增强的执行节点,该抽象类的子类对象将会是一个环形增强的切面
 * @author fk-7075
 *
 */
public abstract class Point {
	
	/**
	 * 当前执行的目标方法的代理实例
	 */
	protected Object aspectObject;
	
	/**
	 * 目标对象的Class
	 */
	protected Class<?> targetClass;
	
	/**
	 * 当前执行的目标方法的Method对象
	 */
	protected Method method;
	
	/**
	 * 当前执行的目标方执行时的参数列表
	 */
	protected Object[] params;
	
	/**
	 * 当前方法的签名信息
	 */
	protected TargetMethodSignature targetMethodSignature;
	
	protected void init(TargetMethodSignature targetMethodSignature) {
		this.targetMethodSignature=targetMethodSignature;
		aspectObject=targetMethodSignature.getAspectObject();
		method=targetMethodSignature.getCurrMethod();
		params=targetMethodSignature.getParams();
		targetClass=targetMethodSignature.getTargetClass();
	}
	
	
	/**
	 * 抽象方法，用于产生一个环绕增强方法，该方法必须使用@Around注解标注，并且必须配置aspect(切面信息)和pointcut(切入点信息)<br>
	 * @param chain
	 * @return
	 */
	public abstract Object proceed(Chain chain);

}

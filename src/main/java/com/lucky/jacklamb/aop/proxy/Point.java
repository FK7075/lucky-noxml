package com.lucky.jacklamb.aop.proxy;

/**
 * 环绕增强的执行节点,该抽象类的子类对象将会是一个环形增强的切面
 * @author fk-7075
 *
 */
public abstract class Point {

	/**
	 * 当前方法的签名信息
	 */
	protected ThreadLocal<TargetMethodSignature> tlTargetMethodSignature=new ThreadLocal();
	
	protected void init(TargetMethodSignature targetMethodSignature) {
		tlTargetMethodSignature.set(targetMethodSignature);
	}
	
	
	/**
	 * 抽象方法，用于产生一个环绕增强方法，该方法必须使用@Around注解标注，并且必须配置aspect(切面信息)和pointcut(切入点信息)<br>
	 * @param chain
	 * @return
	 */
	public abstract Object proceed(Chain chain) throws Throwable;

}

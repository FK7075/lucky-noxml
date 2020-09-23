package com.lucky.jacklamb.aop.proxy;

import com.lucky.jacklamb.annotation.aop.Cacheable;
import com.lucky.jacklamb.annotation.aop.Transaction;
import com.lucky.jacklamb.aop.expandpoint.CacheExpandPoint;
import com.lucky.jacklamb.aop.expandpoint.TransactionPoint;
import com.lucky.jacklamb.aop.core.AopChain;
import com.lucky.jacklamb.aop.core.AopPoint;
import com.lucky.jacklamb.aop.core.PointRun;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LuckyAopMethodInterceptor implements MethodInterceptor {
	
	
	private List<PointRun> pointRuns;//关于某一个类的所有增强的执行节点
	private TargetMethodSignature targetMethodSignature;
	
	/**
	 * 回调函数构造器，得到一个真实对象的的所有执行方法(MethodRun)和环绕执行节点集合(PointRun)，
	 * 根据实际情况为真实对象的每一个需要被增强的方法产生一个特定的回调策略
	 * @param pointRuns 环绕执行节点集合(可变参形式传入)
	 */
	public LuckyAopMethodInterceptor(PointRun...pointRuns) {
		this.pointRuns=new ArrayList<>();
		Stream.of(pointRuns).forEach(this.pointRuns::add);
	}
	
	/**
	 * 回调函数构造器，得到一个真实对象的的所有执行方法(MethodRun)和环绕执行链(PointRun)，
	 * 根据实际情况为真实对象的每一个需要被增强的方法产生一个特定的回调策略
	 * @param pointRuns 环绕执行节点集合(集合参形式传入)
	 */
	public LuckyAopMethodInterceptor(List<PointRun> pointRuns) {
		this.pointRuns=new ArrayList<>();
		this.pointRuns.addAll(pointRuns);
	}

	@Override
	public Object intercept(Object target, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
//		如果是final方法则执行原方法
//		if(Modifier.isFinal(method.getModifiers())){
//			return methodProxy.invokeSuper(target,params);
//		}
		final List<AopPoint> points=new ArrayList<>();
		targetMethodSignature=new TargetMethodSignature(target,method,params);

		//被@Cacheable注解标注的方法优先执行缓存代理
		if(method.isAnnotationPresent(Cacheable.class)) {
			AopPoint cacheExpandPoint = new CacheExpandPoint();
			cacheExpandPoint.init(targetMethodSignature);
			cacheExpandPoint.setPriority(0);
			points.add(cacheExpandPoint);
		}


		//被@Transaction注解标注的方法执行事务代理
		if(target.getClass().getSuperclass().isAnnotationPresent(Transaction.class)||
				method.isAnnotationPresent(Transaction.class)){
			AopPoint transactionPoint = new TransactionPoint();
			transactionPoint.init(targetMethodSignature);
			transactionPoint.setPriority(1);
			points.add(transactionPoint);
		}
		//得到所有自定义的的环绕增强节点
		pointRuns.stream().filter(a->a.standard(method)).forEach((a)->{
			AopPoint p=a.getPoint();p.init(targetMethodSignature);points.add(p);});

		//将所的环绕增强节点根据优先级排序后组成一个执行链
		AopChain chain=new AopChain(points.stream()
				.sorted(Comparator.comparing(AopPoint::getPriority))
				.collect(Collectors.toList()),target,params,methodProxy);
		Object resule;
		
		//执行增强策略
		resule= chain.proceed();
		chain.setIndex(-1);
		return resule;
	}


}

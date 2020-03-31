package com.lucky.jacklamb.aop.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.lucky.jacklamb.annotation.aop.Cacheable;
import com.lucky.jacklamb.aop.expandpoint.CacheExpandPoint;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class LuckyMethodInterceptor implements MethodInterceptor {
	
	
	private List<PointRun> pointRuns;//关于某一个类的所有增强的执行节点
	private TargetMethodSignature targetMethodSignature;
	
	/**
	 * 回调函数构造器，得到一个真实对象的的所有执行方法(MethodRun)和环绕执行节点集合(PointRun)，
	 * 根据实际情况为真实对象的每一个需要被增强的方法产生一个特定的回调策略
	 * @param pointRuns 环绕执行节点集合(可变参形式传入)
	 */
	public LuckyMethodInterceptor(PointRun...pointRuns) {
		this.pointRuns=new ArrayList<>();
		Stream.of(pointRuns).forEach(this.pointRuns::add);
	}
	
	/**
	 * 回调函数构造器，得到一个真实对象的的所有执行方法(MethodRun)和环绕执行链(PointRun)，
	 * 根据实际情况为真实对象的每一个需要被增强的方法产生一个特定的回调策略
	 * @param pointRuns 环绕执行节点集合(集合参形式传入)
	 */
	public LuckyMethodInterceptor(List<PointRun> pointRuns) {
		this.pointRuns=new ArrayList<>();
		this.pointRuns.addAll(pointRuns);
	}

	@Override
	public Object intercept(Object target, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
		List<Point> points=new ArrayList<>();
		targetMethodSignature=new TargetMethodSignature(target,method,params);
		//被@Cacheable注解标注的方法优先执行缓存代理
		if(method.isAnnotationPresent(Cacheable.class)) {
			Point cacheExpandPoint = new CacheExpandPoint();
			cacheExpandPoint.init(targetMethodSignature);
			points.add(cacheExpandPoint);
		}
		//得到所有自定义的的环绕增强节点
		pointRuns.stream().filter(a->a.standard(method)).forEach((a)->{Point p=a.getPoint();p.init(targetMethodSignature);points.add(p);});
		
		//将所的环绕增强节点组成一个执行链
		Chain chain=new Chain(points,target,params,methodProxy);
		Object resule;
		
		//执行增强策略
		resule= chain.proceed();
		chain.setIndex(-1);
		return resule;
	}


}

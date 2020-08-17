package com.lucky.jacklamb.aop.expandpoint;

import java.lang.reflect.Method;

import com.lucky.jacklamb.annotation.aop.Cacheable;
import com.lucky.jacklamb.aop.core.AopChain;
import com.lucky.jacklamb.aop.core.AopPoint;
import com.lucky.jacklamb.aop.proxy.TargetMethodSignature;
import com.lucky.jacklamb.expression.ExpressionEngine;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.sqlcore.abstractionlayer.cache.LRUCache;

public class CacheExpandPoint extends AopPoint {
	
	private ApplicationBeans beans;
	
	@SuppressWarnings("unchecked")
	public Object cacheResult(AopChain chain) throws Throwable {
//		String condition = cachAnn.condition();//表达式，满足就走缓存,依赖表达式解析引擎，后期完善
		TargetMethodSignature targetMethodSignature = tlTargetMethodSignature.get();
		Method method=targetMethodSignature.getCurrMethod();
		Object[] params=targetMethodSignature.getParams();
		beans=ApplicationBeans.createApplicationBeans();
		Object result = null;
		Cacheable cachAnn=method.getAnnotation(Cacheable.class);
		String mapid = cachAnn.value();//容器中的缓存的ids
		LRUCache<String,Object> cacheMap = null;
		String key = cachAnn.key();//结果在缓存中的key
		key=ExpressionEngine.removeSymbol(key, params, "#[", "]");
		if(beans.containsComponent(mapid)) {
			cacheMap=(LRUCache<String, Object>) beans.getComponentBean(mapid);
		}
		if(cacheMap==null) {//容器中还不存在该缓存容器
			result=chain.proceed();
			cacheMap=new LRUCache<>(100);
			cacheMap.put(key,result);
			beans.addComponentBean(mapid, cacheMap);
			return result;
			
		}else if(cacheMap!=null&&!cacheMap.containsKey(key)) {//容器中存在该缓存容器,但是缓存容器中不存在key
			result=chain.proceed();
			cacheMap.put(key, result);
			return result;
		}else {//容器中存在该缓存容器,但是缓存容器中存在key
			return cacheMap.get(key);
		}
		
	}
	
	@Override
	public Object proceed(AopChain chain) throws Throwable {
		// 返回缓存中的值
		return cacheResult(chain);
	}
	

}

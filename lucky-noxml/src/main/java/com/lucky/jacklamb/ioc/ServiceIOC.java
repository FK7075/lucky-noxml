package com.lucky.jacklamb.ioc;

import com.lucky.jacklamb.annotation.ioc.Service;
import com.lucky.jacklamb.aop.core.AopProxyFactory;
import com.lucky.jacklamb.exception.NotAddIOCComponent;
import com.lucky.jacklamb.exception.NotFindBeanException;
import com.lucky.jacklamb.utils.base.LuckyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * 服务组件
 */
public class ServiceIOC implements IOC{

	private static final Logger log= LogManager.getLogger(ServiceIOC.class);
	private Map<String, Object> serviceMap;
	private String IOC_CODE="service";
	private List<String> serviceIDS;
	
	public ServiceIOC() {
		serviceMap=new HashMap<>(16);
		serviceIDS=new ArrayList<>(16);
	}

	@Override
	public boolean contain(String id) {
		return serviceIDS.contains(id);
	}

	@Override
	public Object getBean(String id) {
		if (!contain(id))
			throw new NotFindBeanException("在Service(ioc)容器中找不到ID为--" + id + "--的Bean...");
		return serviceMap.get(id);
	}

	@Override
	public Map<String, Object> getBeanMap() {
		return serviceMap;
	}

	@Override
	public void addBean(String id, Object object) {
		if(contain(id))
			throw new NotAddIOCComponent("Service(ioc)容器中已存在ID为--"+id+"--的组件，无法重复添加（您可能配置了同名的@Service组件，这将会导致异常的发生！）......");
		serviceMap.put(id, object);
		addServiceIDS(id);
	}

	public void addServiceIDS(String id) {
		serviceIDS.add(id);
	}

	@Override
	public void registered(Set<Class<?>> serviceClass){
		String beanID;
		for (Class<?> service : serviceClass) {
			if (service.isAnnotationPresent(Service.class)) {
				Service ser = service.getAnnotation(Service.class);
				if (!"".equals(ser.value()))
					beanID=ser.value();
				else
					beanID=LuckyUtils.TableToClass1(service.getSimpleName());
				Object aspect = AopProxyFactory.Aspect(AspectAOP.getAspectIOC().getAspectMap(), IOC_CODE, beanID, service);
				addBean(beanID, aspect);
				log.info("@Service \"[id="+beanID+" class="+aspect+"]\"");
			}
		}
	}
}

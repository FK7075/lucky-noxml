package com.lucky.jacklamb.ioc;

import com.lucky.jacklamb.annotation.ioc.Service;
import com.lucky.jacklamb.aop.core.AopProxyFactory;
import com.lucky.jacklamb.exception.NotAddIOCComponent;
import com.lucky.jacklamb.exception.NotFindBeanException;
import com.lucky.jacklamb.utils.base.LuckyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceIOC extends ComponentFactory{

	private static final Logger log= LogManager.getLogger(ServiceIOC.class);

	private Map<String, Object> serviceMap;

	private String IOC_CODE="service";

	private List<String> serviceIDS;
	
	public ServiceIOC() {
		serviceMap=new HashMap<>();
		serviceIDS=new ArrayList<>();
	}

	public boolean containId(String id) {
		return serviceIDS.contains(id);
	}

	public Object getServiceBean(String id) {
		if (!containId(id))
			throw new NotFindBeanException("在Service(ioc)容器中找不到ID为--" + id + "--的Bean...");
		return serviceMap.get(id);
	}

	public Map<String, Object> getServiceMap() {
		return serviceMap;
	}

	public void setServiceMap(Map<String, Object> serviceMap) {
		this.serviceMap = serviceMap;
	}

	public void addServiceMap(String id, Object object) {
		if(containId(id))
			throw new NotAddIOCComponent("Service(ioc)容器中已存在ID为--"+id+"--的组件，无法重复添加（您可能配置了同名的@Service组件，这将会导致异常的发生！）......");
		serviceMap.put(id, object);
		addServiceIDS(id);
	}

	public List<String> getServiceIDS() {
		return serviceIDS;
	}

	public void setServiceIDS(List<String> serviceIDS) {
		this.serviceIDS = serviceIDS;
	}

	public void addServiceIDS(String id) {
		serviceIDS.add(id);
	}

	/**
	 * 加载Service组件到ServiceIOC容器
	 * @param serviceClass
	 */
	public void initServiceIOC(List<Class<?>> serviceClass){
		String beanID;
		for (Class<?> service : serviceClass) {
			if (service.isAnnotationPresent(Service.class)) {
				Service ser = service.getAnnotation(Service.class);
				if (!"".equals(ser.value()))
					beanID=ser.value();
				else
					beanID=LuckyUtils.TableToClass1(service.getSimpleName());
				Object aspect = AopProxyFactory.Aspect(AspectAOP.getAspectIOC().getAspectMap(), IOC_CODE, beanID, service);
				addServiceMap(beanID, aspect);
				log.info("@Service \"[id="+beanID+" class="+aspect+"]\"");
			}
		}
	}
}

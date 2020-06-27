package com.lucky.jacklamb.ioc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lucky.jacklamb.conversion.annotation.Conversion;
import com.lucky.jacklamb.conversion.proxy.ConversionProxy;
import com.lucky.jacklamb.conversion.LuckyConversion;

import com.lucky.jacklamb.annotation.ioc.Bean;
import com.lucky.jacklamb.annotation.ioc.Component;
import com.lucky.jacklamb.annotation.ioc.Configuration;
import com.lucky.jacklamb.annotation.mvc.ExceptionHander;
import com.lucky.jacklamb.annotation.mvc.LuckyFilter;
import com.lucky.jacklamb.annotation.mvc.LuckyListener;
import com.lucky.jacklamb.annotation.mvc.LuckyServlet;
import com.lucky.jacklamb.aop.util.PointRunFactory;
import com.lucky.jacklamb.exception.NotAddIOCComponent;
import com.lucky.jacklamb.exception.NotFindBeanException;
import com.lucky.jacklamb.file.ini.INIConfig;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.LuckyConfig;
import com.lucky.jacklamb.ioc.config.ScanConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;
import com.lucky.jacklamb.utils.base.LuckyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 普通IOC组件集合
 * 
 * @author DELL
 *
 */
public class ComponentIOC extends ComponentFactory {

	private static final Logger log= LogManager.getLogger(ComponentIOC.class);

	private Map<String, Object> appMap;

	private List<String> appIDS;
	
	public ComponentIOC() {
		appMap=new HashMap<>();
		appIDS=new ArrayList<>();
	}

	public boolean containId(String id) {
		return appIDS.contains(id);
	}

	public Object getComponentBean(String id) {
		if (!containId(id))
			throw new NotFindBeanException("在Component(ioc)容器中找不到ID为--" + id + "--的Bean...");
		return appMap.get(id);

	}

	public Map<String, Object> getAppMap() {
		return appMap;
	}

	public void setAppMap(Map<String, Object> appMap) {
		this.appMap = appMap;
	}

	public void addAppMap(String id, Object object) {
		if(containId(id))
			throw new NotAddIOCComponent("Component(ioc)容器中已存在ID为--"+id+"--的组件，无法重复添加（您可能配置了同名的@Component组件，这将会导致异常的发生！）......");
		appMap.put(id, object);
		addAppIDS(id);
	}

	public List<String> getAppIDS() {
		return appIDS;
	}

	public void setAppIDS(List<String> appIDS) {
		this.appIDS = appIDS;
	}

	public void addAppIDS(String id) {
		appIDS.add(id);
	}

	/**
	 * 加载Component组件
	 * 
	 * @param componentClass
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public void initComponentIOC(List<Class<?>> componentClass)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
		String beanID;
		for (Class<?> component : componentClass) {
			if (component.isAnnotationPresent(Component.class)) {
				Component com = component.getAnnotation(Component.class);
				if (!"".equals(com.value())) {
					beanID=com.value();
				} else {
					beanID=LuckyUtils.TableToClass1(component.getSimpleName());
				}
				Object aspect = PointRunFactory.Aspect(AspectAOP.getAspectIOC().getAspectMap(), "component", beanID, component);
				addAppMap(beanID, aspect);
				log.info("@Component \"[id="+beanID+" class="+aspect+"]\"");
				continue;
			} else if(component.isAnnotationPresent(Conversion.class)){
				Conversion com = component.getAnnotation(Conversion.class);
				if (!"".equals(com.id())) {
					beanID=com.id();
				} else {
					beanID=LuckyUtils.TableToClass1(component.getSimpleName());
				}
				Object aspect=ConversionProxy.getLuckyConversion((Class<? extends LuckyConversion>) component);
				addAppMap(beanID, aspect);
				log.info("@Conversion \"[id="+beanID+" class="+aspect+"]\"");
				continue;
			}else if (component.isAnnotationPresent(Configuration.class)) {
				Configuration cfg=component.getAnnotation(Configuration.class);
				Object obj = component.newInstance();
				if(!"".equals(cfg.section())){
					obj=new INIConfig(cfg.ini()).getObject(component,cfg.section());
					beanID="".equals(cfg.value())?LuckyUtils.TableToClass1(component.getSimpleName()):cfg.value();
					addAppMap(beanID,obj);
					log.info("@Configuration \"[id="+beanID+" class="+obj+"]\"");
				}
				Method[] methods=component.getDeclaredMethods();
				for(Method met:methods) {
					if(met.isAnnotationPresent(Bean.class)) {
						Object invoke = met.invoke(obj);
						Bean bean=met.getAnnotation(Bean.class);
						if("".equals(bean.value())) {
							beanID=component.getSimpleName()+"."+met.getName();
						}else {
							beanID=bean.value();
						}
						if(!LuckyConfig.class.isAssignableFrom(met.getReturnType())) {
							addAppMap(beanID, invoke);
						}else if(ScanConfig.class.isAssignableFrom(met.getReturnType())) {
							addAppMap(beanID, AppConfig.getAppConfig().getScanConfig());
						}else if(WebConfig.class.isAssignableFrom(met.getReturnType())) {
							addAppMap(beanID, AppConfig.getAppConfig().getWebConfig());
						}else if(ServerConfig.class.isAssignableFrom(met.getReturnType())) {
							addAppMap(beanID, AppConfig.getAppConfig().getServerConfig());
						}
						log.info("@Bean \"[id="+beanID+" class="+invoke+"]\"");
					}
				}
				continue;
			}else if(component.isAnnotationPresent(ExceptionHander.class)) {
				ExceptionHander annotation = component.getAnnotation(ExceptionHander.class);
				if (!"".equals(annotation.id())) {
					beanID=annotation.id();
				} else {
					beanID=LuckyUtils.TableToClass1(component.getSimpleName());
				}
				Object aspect = PointRunFactory.Aspect(AspectAOP.getAspectIOC().getAspectMap(), "component", beanID, component);
				addAppMap(beanID,aspect);
				log.info("@ExceptionHander \"[id="+beanID+" ,class="+aspect+"]\"");
				continue;
			}else if(component.isAnnotationPresent(LuckyServlet.class)
					||component.isAnnotationPresent(LuckyFilter.class)
					||component.isAnnotationPresent(LuckyListener.class)) {
				beanID=LuckyUtils.TableToClass1(component.getSimpleName());
				Object aspect = PointRunFactory.Aspect(AspectAOP.getAspectIOC().getAspectMap(), "component", beanID, component);
				addAppMap(beanID,aspect);
				log.info("@Web \"[id="+beanID+" ,class="+aspect+"]\"");
				continue;
			}else {
				continue;
			}
		}
	}

}

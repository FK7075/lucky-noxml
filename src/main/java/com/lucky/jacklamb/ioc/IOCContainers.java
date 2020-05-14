package com.lucky.jacklamb.ioc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import com.lucky.jacklamb.annotation.ioc.Autowired;
import com.lucky.jacklamb.annotation.ioc.Value;
import com.lucky.jacklamb.conversion.util.FieldUtils;
import com.lucky.jacklamb.exception.InjectionPropertiesException;
import com.lucky.jacklamb.file.ini.INIConfig;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.scan.ScanFactory;
import com.lucky.jacklamb.servlet.Model;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.ArrayCast;

/**
 * 扫描所有配置包，将所有的IOC组件都加载到相应的IOC容器中
 * @author DELL
 *
 */
public final class IOCContainers {
	
	private AspectAOP AspectIOC;
	
	private RepositoryIOC repositoryIOC;
	
	private ServiceIOC serviceIOC;
	
	private ComponentIOC appIOC;
	
	private ControllerIOC controllerIOC;
	
	public AspectAOP getAspectIOC() {
		return AspectIOC;
	}

	public void setAspectIOC(AspectAOP AspectIOC) {
		this.AspectIOC = AspectIOC;
	}

	public RepositoryIOC getRepositoryIOC() {
		return repositoryIOC;
	}

	public void setRepositoryIOC(RepositoryIOC repositoryIOC) {
		this.repositoryIOC = repositoryIOC;
	}

	public ServiceIOC getServiceIOC() {
		return serviceIOC;
	}

	public void setServiceIOC(ServiceIOC serviceIOC) {
		this.serviceIOC = serviceIOC;
	}

	public ComponentIOC getAppIOC() {
		return appIOC;
	}

	public void setAppIOC(ComponentIOC appIOC) {
		this.appIOC = appIOC;
	}
	
	public void addComponent(String key,Object value) {
		this.appIOC.addAppMap(key, value);
	}

	public ControllerIOC getControllerIOC() {
		return controllerIOC;
	}

	public void setControllerIOC(ControllerIOC controllerIOC) {
		this.controllerIOC = controllerIOC;
	}
	
	
	/**
	 *  1.加载配置->2.得到所有增强->3.IOC+AOP->4.DI
	 */
	public void init() {
		
		//得到配置信息
		scanConfigToComponentIOC();
		
		//得到所有的增强
		AspectIOC=AspectAOP.getAspectIOC();
		
		//控制反转+动态代理(IOC+AOP)
		inversionOfControlAndAop();
		
		//依赖注入(DI)
		dependencyInjection();
		
	}
	
	/**
	 * 控制反转
	 */
	public void inversionOfControlAndAop() {
		try {
			initComponentIOC();
			initControllerIOC();
			initServiceIOC();
			initRepositoryIOC();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 依赖注入,执行代理
	 */
	public void dependencyInjection() {
		try {
			injection(appIOC.getAppMap());
			injection(repositoryIOC.getRepositoryMap());
			injection(serviceIOC.getServiceMap());
			injection(controllerIOC.getControllerMap());
		} catch (IllegalArgumentException e) {
			throw new InjectionPropertiesException("属性注入异常，注入的属性与原属性类型不匹配....");
		} catch (IllegalAccessException e) {
			throw new InjectionPropertiesException("属性注入异常，没有权限访问该属性....");
		}
		
	}

	/**
	 * 得到有关包扫描的配置信息
	 */
	public void scanConfigToComponentIOC() {
		AppConfig.getAppConfig().getScanConfig();
	}
	
	/**
	 * 初始化Component组件
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void initComponentIOC() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		appIOC=new ComponentIOC();
		appIOC.initComponentIOC(ScanFactory.createScan().getComponentClass("component"));
	}
	
	/**
	 * 初始化Controller组件
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void initControllerIOC() throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		controllerIOC=new ControllerIOC();
		controllerIOC.initControllerIOC(ScanFactory.createScan().getComponentClass("controller"));
		controllerIOC.methodHanderSetting();
		
	}
	
	/**
	 * 初始化Service组件
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void initServiceIOC() throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		serviceIOC=new ServiceIOC();
		serviceIOC.initServiceIOC(ScanFactory.createScan().getComponentClass("service"));
	}
	
	/**
	 * 初始化Repository组件
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void initRepositoryIOC() throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		repositoryIOC=new RepositoryIOC();
		repositoryIOC.initRepositoryIOC(ScanFactory.createScan().getComponentClass("repository"));
	}
	
	/**
	 * 每次处理请求时为Controller注入Model、Request、Response和Session对象属性
	 * @param object
	 * @param model
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void autowReqAdnResp(Object object,Model model) throws IllegalArgumentException, IllegalAccessException {
		Class<?> controllerClass=object.getClass();
		Field[] fields=FieldUtils.getAllFields(controllerClass);
		for(Field field:fields) {
			field.setAccessible(true);
			if(Model.class.isAssignableFrom(field.getType())) {
				field.set(object, model);
			}else if(HttpSession.class.isAssignableFrom(field.getType())) {
				field.set(object, model.getSession());
			}else if(ServletRequest.class.isAssignableFrom(field.getType())) {
				field.set(object, model.getRequest());
			}else if(ServletResponse.class.isAssignableFrom(field.getType())) {
				field.set(object, model.getResponse());
			}else if(ServletContext.class.isAssignableFrom(field.getType())) {
				field.set(object, model.getServletContext());
			}else {
				continue;
			}
		}
	}
	
	/**
	 * 组件的属性注入(@Value和@Autowired)
	 * @param componentMap
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void injection(Map<String,Object> componentMap) throws IllegalArgumentException, IllegalAccessException {
		ApplicationBeans beans=ApplicationBeans.createApplicationBeans();
		Autowired auto;
		Value value;
		for(Entry<String,Object> entry:componentMap.entrySet()) {
			Object component=entry.getValue();
			Class<?> componentClass=component.getClass();
			Field[] fields= FieldUtils.getAllFields(componentClass);
			for(Field field:fields) {
				field.setAccessible(true);
				Class<?> fieldClass=field.getType();
				if(field.isAnnotationPresent(Autowired.class)) {
					auto=field.getAnnotation(Autowired.class);
					String auval = auto.value();
					if("".equals(auval)) {
						field.set(component, beans.getBean(fieldClass));//类型扫描
					}else if(auval.startsWith("${")&&auval.endsWith("}")){
						String key=auval.substring(2,auval.length()-1);
						if(key.startsWith("S:")){
							field.set(component,new INIConfig().getObject(field.getType(),key.substring(2)));
						}else if(key.startsWith("[")){
							String[] _arr=key.split(":");
							field.set(component, new INIConfig().getValue(_arr[0].substring(1,_arr[0].length()-1),_arr[1],field.getType()));
						}else{
							field.set(component, new INIConfig().getAppParam(auval.substring(2,auval.length()-1),field.getType()));
						}
					}else{
						field.set(component, beans.getBean(auto.value()));//id注入
					}
				}else if(field.isAnnotationPresent(Value.class)) {
					value=field.getAnnotation(Value.class);
					String[] val = value.value();
					if(val.length==0) {//类型扫描
						field.set(component, beans.getBean(fieldClass));
					}else {
						if(fieldClass.isArray()) {//基本类型的数组类型
							field.set(component,ArrayCast.strArrayChange(val, fieldClass));
						}else if(List.class.isAssignableFrom(fieldClass)) {//List类型
							List<Object> list=new ArrayList<>();
							String fx=ArrayCast.getFieldGenericType(field)[0];
							if(fx.endsWith("$ref")) {
								for(String z:val) {
									list.add(beans.getBean(z));
								}
							}else {
								for(String z:val) {
									list.add(JavaConversion.strToBasic(z, fx));
								}
							}
							field.set(component, list);
						}else if(Set.class.isAssignableFrom(fieldClass)) {//Set类型
							Set<Object> set=new HashSet<>();
							String fx=ArrayCast.getFieldGenericType(field)[0];
							if(fx.endsWith("$ref")) {
								for(String z:val) {
									set.add(beans.getBean(z));
								}
							}else {
								for(String z:val) {
									set.add(JavaConversion.strToBasic(z, fx));
								}
							}
							field.set(component, set);
						}else if(Map.class.isAssignableFrom(fieldClass)) {//Map类型
							Map<Object,Object> map=new HashMap<>();
							String[] fx=ArrayCast.getFieldGenericType(field);
							boolean one=fx[0].endsWith("$ref");
							boolean two=fx[1].endsWith("$ref");
							if(one&&two) {//K-V都不是基本类型
								for(String z:val) {
									String[] kv=z.split(":");
									map.put(beans.getBean(kv[0]), beans.getBean(kv[1]));
								}
							}else if(one&&!two) {//V是基本类型
								for(String z:val) {
									String[] kv=z.split(":");
									map.put(beans.getBean(kv[0]), JavaConversion.strToBasic(kv[1], fx[1]));
								}
							}else if(!one&&two) {//K是基本类型
								for(String z:val) {
									String[] kv=z.split(":");
									map.put(JavaConversion.strToBasic(kv[0], fx[0]),beans.getBean(kv[1]));
								}
							}else {//K-V都是基本类型
								for(String z:val) {
									String[] kv=z.split(":");
									map.put(JavaConversion.strToBasic(kv[0], fx[0]), JavaConversion.strToBasic(kv[1], fx[1]));
								}
							}
							field.set(component, map);
						}else {//自定义的基本类型
							field.set(component, JavaConversion.strToBasic(val[0], fieldClass.getSimpleName()));
						}
					}
				}
			}
		}
	}

	public Set<Class<?>> getWebSocketSet() {
		return new HashSet<Class<?>>(ScanFactory.createScan().getComponentClass("websocket"));
	}
}



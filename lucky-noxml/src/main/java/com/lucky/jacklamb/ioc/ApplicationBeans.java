package com.lucky.jacklamb.ioc;

import com.lucky.jacklamb.aop.core.PointRun;
import com.lucky.jacklamb.exception.NotFindBeanException;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
import com.lucky.jacklamb.start.LuckyServerApplicationConfig;
import com.lucky.jacklamb.utils.base.Assert;
import com.lucky.jacklamb.utils.base.JackLamb;
import com.lucky.jacklamb.utils.file.Resources;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.server.ServerApplicationConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

public class ApplicationBeans {
	
	public static IOCContainers iocContainers;
	
	public static Logger log;
	
	private static ApplicationBeans applicationBean;
	
	private static Set<Class<?>> webSocketSet;
	
	static {
		JackLamb.welcome();
		try {
			Properties p=new Properties();
			p.load(new BufferedReader(new InputStreamReader(Resources.getInputStream("/log4j2.xml"),"UTF-8")));
			PropertyConfigurator.configure(p);
		} catch (Exception e) {
			e.printStackTrace();
			BasicConfigurator.configure();
		}
		log= LogManager.getLogger(ApplicationBeans.class);
		iocContainers=new IOCContainers();
		iocContainers.init();


	}
	
	public static ApplicationBeans createApplicationBeans() {
		if(applicationBean==null) {
			applicationBean=new ApplicationBeans();
		}
		return applicationBean;
	}
	
	/**
	 * 得到所有简化的方法映射关系
	 * @return
	 */
	public Set<String> allMapping(){
		return iocContainers.getControllerIOC().getMappingSet();
	}
	
	/**
	 * 根据ID得到一个具体的方法映射
	 * @param uRLAndRequestMethod
	 * @return
	 */
	public ControllerAndMethod getHanderMethod(URLAndRequestMethod uRLAndRequestMethod) {
		return iocContainers.getControllerIOC().getControllerAndMethod(uRLAndRequestMethod);
	}
	
	/**
	 * 得到所有的方法映射
	 * @return
	 */
	public ControllerAndMethodMap getHanderMethods() {
		return iocContainers.getControllerIOC().getHanderMap();
	}
	
	/**
	 * 根据ID得到Aspect组件
	 * @param id
	 * @return
	 */
	public Object getAspectBean(String id) {
		return iocContainers.getAspectIOC().getAspectBean(id);
	}

	/**
	 * 得到所有Aspect组件
	 * @return
	 */
	public Map<String,PointRun> getAspectBeans(){
		return iocContainers.getAspectIOC().getAspectMap();
	}

	/**
	 * 根据ID得到Bean组件
	 * @param id
	 * @return
	 */
	public Object getBeansBean(String id){return iocContainers.getBeansIOC().getBean(id);}

	/**
	 * 得到所有Aspect组件
	 * @return
	 */
	public Map<String,Object> getBeansBeans(){
		return iocContainers.getBeansIOC().getBeanMap();
	}
	
	/**
	 * 根据ID得到Service容器中的bean
	 * @param beanId
	 * @return
	 */
	public Object getServiceBean(String beanId) {
		return iocContainers.getServiceIOC().getBean(beanId);
	}
	
	/**
	 * 得到所有Service组件
	 * @return
	 */
	public Map<String,Object> getServiceBeans(){
		return iocContainers.getServiceIOC().getBeanMap();
	}
	
	/**
	 * 根据ID得到一个Controller组件
	 * @param beanId
	 * @return
	 */
	public Object getControllerBean(String beanId) {
		return iocContainers.getControllerIOC().getControllerBean(beanId);
	}
	
	/**
	 * 得到所有的Controller组件
	 * @return
	 */
	public Map<String,Object> getControllerBeans(){
		return iocContainers.getControllerIOC().getControllerMap();
	}
	
	/**
	 * 得到所有的Repository组件
	 * @return
	 */
	public Map<String,Object> getRepositoryBeans(){
		return iocContainers.getRepositoryIOC().getRepositoryMap();
	}
	
	/**
	 * 根据ID得到一个Mapper或Repositroy组件
	 * @param mapperId 组件ID
	 * @return
	 */
	public Object getMapperBean(String mapperId) {
		return iocContainers.getRepositoryIOC().getMaRepBean(mapperId);
	}
	
	/**
	 * 得到所有的Mapper组件
	 * @return
	 */
	public Map<String,Object> getMapperBeans(){
		return iocContainers.getRepositoryIOC().getMapperMap();
	}
	
	/**
	 * 根据ID得到一个Component组件
	 * @param componentId 组件ID
	 * @return
	 */
	public Object getComponentBean(String componentId) {
		return iocContainers.getAppIOC().getBean(componentId);
	}
	
	/**
	 * 得到所有Component组件
	 * @return
	 */
	public Map<String,Object> getComponentBeans() {
		return iocContainers.getAppIOC().getBeanMap();
	}

	/**
	 * 判断IOC容器中是否含有该类型的bean
	 * @param clzz 目标类型
	 * @return
	 */
	public boolean isIocBean(Class<?> clzz){
		return !getBeans(clzz).isEmpty();
	}

	public boolean isIocBean(String iocId){
		try{
			getBean(iocId);
			return true;
		}catch (NotFindBeanException e){
			return false;
		}
	}

	public Object getBean(Class<?> type){
		return getBean(type,null);
	}
	
	/**
	 * 根据类型得到一个IOC组件
	 * @param clzz
	 * @return
	 */
	public Object getBean(Class<?> clzz,Class<?>locationClass ) {
		List<Object> beans = getBeans(clzz);
		if(beans.isEmpty()) {
			throw new NotFindBeanException("在IOC容器中找不到类型为--"+clzz+"--的Bean...");
		}
		if(beans.size()==1) {
			return beans.get(0);
		}
		if(!Assert.isNull(locationClass)&&
				!Assert.isNull(ClassUtils.getGenericType(locationClass.getGenericSuperclass()))){
			Class<?>[] genericTypes = ClassUtils.getGenericType(locationClass.getGenericSuperclass());
			List<Object> filterBeans=new ArrayList<>();
			for (Class<?> genericType : genericTypes) {
				for (Object bean : beans) {
					if(genericType.isAssignableFrom(bean.getClass())){
						filterBeans.add(bean);
					}
				}
			}
			if(filterBeans.size()==1){
				return filterBeans.get(0);
			}
		}
		throw new NotFindBeanException("在IOC容器中类型为-"+clzz+"-的bean不是唯一的!  请使用@Value或者@Autowired的value属性指定bean的ID！");
	}
	
	/**
	 * 根据类型得到一组IOC组件
	 * @param clzz 组件类型
	 * @return
	 */
	public List<Object> getBeans(Class<?> clzz) {
		List<Object> controllerObj=getBeanByClass(iocContainers.getControllerIOC().getControllerMap(),clzz);
		List<Object> serviceObj=getBeanByClass(iocContainers.getServiceIOC().getBeanMap(),clzz);
		List<Object> repositoryObj=getBeanByClass(iocContainers.getRepositoryIOC().getRepositoryMap(),clzz);
		List<Object> mapperObj=getBeanByClass(iocContainers.getRepositoryIOC().getMapperMap(),clzz);
		List<Object> componentObj=getBeanByClass(iocContainers.getAppIOC().getBeanMap(),clzz);
		List<Object> beansObj=getBeanByClass(iocContainers.getBeansIOC().getBeanMap(),clzz);
		if(!controllerObj.isEmpty()) {
			return controllerObj;
		} else if(!serviceObj.isEmpty()) {
			return serviceObj;
		} else if(!repositoryObj.isEmpty()) {
			return repositoryObj;
		} else if(!mapperObj.isEmpty()) {
			return mapperObj;
		} else if(!componentObj.isEmpty()) {
			return componentObj;
		}  else if(!beansObj.isEmpty()) {
			return beansObj;
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 *
	 * @param map
	 * @param clzz
	 * @return
	 */
	private List<Object> getBeanByClass(Map<String,Object> map,Class<?> clzz) {
		List<Object> sameClassObjects=new ArrayList<>();
		for(Entry<String,Object> entry:map.entrySet()) {
			Object obj=entry.getValue();
			Class<?> mapClass=obj.getClass();
			if(clzz.isAssignableFrom(mapClass)) {
				sameClassObjects.add(obj);
			}
		}
		return sameClassObjects;
	}
	
	/**
	 * 得到容器中所有的DataSource对象
	 * @return
	 */
	public List<LuckyDataSource> getDataSources() {
		List<LuckyDataSource> list=new ArrayList<>();
		for(Entry<String,Object> entry:getComponentBeans().entrySet()) {
			Object obj=entry.getValue();
			Class<?> mapClass=obj.getClass();
			if(LuckyDataSource.class.isAssignableFrom(mapClass)) {
				list.add((LuckyDataSource)entry.getValue());
			}
		}
		return list;
	}
	
	/**
	 * 判断IOC容器中是否含有该ID对应的组件
	 * @param beanId
	 * @return
	 */
	public boolean contains(String beanId) {
		return iocContainers.getControllerIOC().containId(beanId)||iocContainers.getServiceIOC().contain(beanId)
				||iocContainers.getRepositoryIOC().containId(beanId)||iocContainers.getAppIOC().contain(beanId)
				||iocContainers.getAspectIOC().containId(beanId)||iocContainers.getBeansIOC().contain(beanId);
	}
	
	/**
	 * 向Component容器中加入一个组件
	 * @param Id
	 * @param component
	 */
	public void  addComponentBean(String Id,Object component) {
		iocContainers.addComponent(Id, component);
	}

	/**
	 * 移除一个Component组件
	 * @param Id 组件ID
	 */
	public void removeComponentBean(String Id){
		iocContainers.removeComponent(Id);
	}
	
	/**
	 * 判断Component容器中是否存在该Id的组件
	 * @param componentId
	 * @return
	 */
	public boolean containsComponent(String componentId) {
		return iocContainers.getAppIOC().contain(componentId);
	}

	/**
	 * 根据ID得到一个IOC组件
	 * @param beanId
	 * @return
	 */
	public Object getBean(String beanId) {
		if(iocContainers.getControllerIOC().containId(beanId)) {
			return iocContainers.getControllerIOC().getControllerBean(beanId);
		} else if(iocContainers.getServiceIOC().contain(beanId)) {
			return iocContainers.getServiceIOC().getBean(beanId);
		} else if(iocContainers.getRepositoryIOC().containId(beanId)) {
			return iocContainers.getRepositoryIOC().getMaRepBean(beanId);
		} else if(iocContainers.getAppIOC().contain(beanId)) {
			return iocContainers.getAppIOC().getBean(beanId);
		} else if(iocContainers.getAspectIOC().containId(beanId)) {
			return iocContainers.getAspectIOC().getAspectBean(beanId);
		}else if(iocContainers.getBeansIOC().contain(beanId)) {
			return iocContainers.getBeansIOC().getBean(beanId);
		} else {
			throw new NotFindBeanException("在IOC容器中找不到ID为--"+beanId+"--的Bean...");
		}
	}

	/**
	 * 得到所有WebSocket组件的Class
	 * @return
	 */
	public Set<Class<?>> getWebSocketSet() {
		if(webSocketSet==null) {
			webSocketSet = iocContainers.getWebSocketSet();
			if(!webSocketSet.isEmpty()) {
				for(Class<?> clzz:webSocketSet) {
					if(ServerApplicationConfig.class.isAssignableFrom(clzz)) {
						return webSocketSet;
					}
				}
				webSocketSet.add(LuckyServerApplicationConfig.class);
			}
		}
		return webSocketSet;
	}
}

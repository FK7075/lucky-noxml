package com.lucky.jacklamb.ioc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lucky.jacklamb.annotation.ioc.CallController;
import com.lucky.jacklamb.annotation.mvc.*;
import com.lucky.jacklamb.httpclient.HttpClientControllerProxy;
import com.lucky.jacklamb.httpclient.service.FeignClientControllerProxy;
import com.lucky.jacklamb.mapping.Mapping;
import com.lucky.jacklamb.mapping.MappingDetails;

import com.lucky.jacklamb.annotation.ioc.Controller;
import com.lucky.jacklamb.aop.util.PointRunFactory;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.exception.NotAddIOCComponent;
import com.lucky.jacklamb.exception.NotFindBeanException;
import com.lucky.jacklamb.servlet.ServerStartRun;
import com.lucky.jacklamb.utils.LuckyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControllerIOC extends ComponentFactory{

	private static final Logger log= LogManager.getLogger(ControllerIOC.class);

	private Map<String, Object> controllerMap;

	private List<String> controllerIDS;
	
	private ControllerAndMethodMap handerMap;
	
	private Set<String> mappingSet;

	private List<ServerStartRun> serverStartRuns;

	private List<ServerStartRun> serverCloseRuns;

	public List<ServerStartRun> getServerCloseRuns() {
		return serverCloseRuns;
	}

	public List<ServerStartRun> getServerStartRuns() {
		return serverStartRuns;
	}

	public Set<String> getMappingSet() {
		return mappingSet;
	}

	public void setMappingSet(Set<String> mappingSet) {
		this.mappingSet = mappingSet;
	}

	public ControllerAndMethodMap getHanderMap() {
		return handerMap;
	}

	public void setHanderMap(ControllerAndMethodMap handerMap) {
		this.handerMap = handerMap;
	}
	public void addHanderMap(URLAndRequestMethod uRLAndRequestMethod, ControllerAndMethod controllerAndMethod) {
		if(this.handerMap.containsKey(uRLAndRequestMethod))
			throw new NotAddIOCComponent("URL-ControllerMethod(url映射)容器中已存在一个作用域相同的URLAndRequestMethod！同一个URL只能被不同类型的请求代理一次！请求类型" + uRLAndRequestMethod.getMethods() + "中的某一种已经将此URL代理了一次！URL:"+uRLAndRequestMethod.getUrl());
		this.handerMap.put(uRLAndRequestMethod, controllerAndMethod);
	}

	public ControllerIOC() {
		controllerMap = new HashMap<>();
		controllerIDS = new ArrayList<>();
		handerMap = new ControllerAndMethodMap();
		mappingSet = new HashSet<>();
		serverStartRuns=new ArrayList<>();
		serverCloseRuns=new ArrayList<>();
	}
	
	public boolean containHander(URLAndRequestMethod uRLAndRequestMethod) {
		return handerMap.containsKey(uRLAndRequestMethod);
	}
	
	public ControllerAndMethod getControllerAndMethod(URLAndRequestMethod uRLAndRequestMethod) {
		if(!containHander(uRLAndRequestMethod))
			throw new NotFindBeanException("在ControllerAndMethod(ioc)容器中找不到URL为:" + uRLAndRequestMethod.getUrl() + ",且请求类型代理为"+uRLAndRequestMethod.getMethods()+"的映射！");
		return handerMap.get(uRLAndRequestMethod);
	}

	public boolean containId(String id) {
		return controllerIDS.contains(id);
	}

	public Object getControllerBean(String id) {
		if (!containId(id))
			throw new NotFindBeanException("在Controller(ioc)容器中找不到ID为--" + id + "--的Bean...");
		return controllerMap.get(id);
	}

	public Map<String, Object> getControllerMap() {
		return controllerMap;
	}

	public void setControllerMap(Map<String, Object> controllerMap) {
		this.controllerMap = controllerMap;
	}

	public void addControllerMap(String id, Object object) {
		if (containId(id))
			throw new NotAddIOCComponent("Controller(ioc)容器中已存在ID为--" + id + "--的组件，无法重复添加（您可能配置了同名的@Controller组件，这将会导致异常的发生！）......");
		controllerMap.put(id, object);
		addControllerIDS(id);
	}

	public List<String> getControllerIDS() {
		return controllerIDS;
	}

	public void setControllerIDS(List<String> controllerIDS) {
		this.controllerIDS = controllerIDS;
	}

	public void addControllerIDS(String id) {
		controllerIDS.add(id);
	}

	/**
	 * 加载Controller组件
	 * 
	 * @param controllerClass
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public void initControllerIOC(List<Class<?>> controllerClass) 
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		String beanID;
		for (Class<?> controller : controllerClass) {
			if (controller.isAnnotationPresent(Controller.class)) {
				Controller cont = controller.getAnnotation(Controller.class);
				if (!"".equals(cont.value())) {
					beanID=cont.value();
				}
				else {
					beanID=LuckyUtils.TableToClass1(controller.getSimpleName());
				}
				addControllerMap(beanID, PointRunFactory.Aspect(AspectAOP.getAspectIOC().getAspectMap(), "controller", beanID, controller));
			}else if(controller.isAnnotationPresent(CallController.class)){
				CallController cont = controller.getAnnotation(CallController.class);
				if (!"".equals(cont.id())) {
					beanID=cont.id();
				}else{
					beanID=LuckyUtils.TableToClass1(controller.getSimpleName());
				}
				log.info("@CallController :\"{id="+beanID+" ,class="+controller+"}\"");
				addControllerMap(beanID, HttpClientControllerProxy.getCallControllerProxyObject(controller));
			} else if(controller.isAnnotationPresent(FeignClient.class)){
				FeignClient cont = controller.getAnnotation(FeignClient.class);
				if (!"".equals(cont.id())) {
					beanID=cont.id();
				}else{
					beanID=LuckyUtils.TableToClass1(controller.getSimpleName());
				}
				log.info("@FeignClient :\"{id="+beanID+" ,class="+controller+"}\"");
				addControllerMap(beanID, FeignClientControllerProxy.getFeignClientControllerProxyObject(controller));
			}

		}
	}

	/**
	 * URL-ControllerMethod的映射解析
	 * @return
	 */
	public void methodHanderSetting() {
		for (Map.Entry<String, Object> entry : controllerMap.entrySet()) {
			Object instance = entry.getValue();
			Class<?> clzz = instance.getClass();
			if(clzz.getSimpleName().contains("$$EnhancerByCGLIB$$"))
				clzz=clzz.getSuperclass();
			String url_c;
			if (clzz.isAnnotationPresent(RequestMapping.class)) {
				RequestMapping crm = clzz.getAnnotation(RequestMapping.class);
				url_c = crm.value();
				if (!"/".equals(url_c)) {
					if (!url_c.startsWith("/"))
						url_c = "/" + url_c;
					if (!url_c.endsWith("/"))
						url_c += "/";
				}
			} else {
				url_c = "/";
			}
			Method[] publicMethods = clzz.getDeclaredMethods();
			String ip,ips,rest;
			MappingDetails md;
			for (Method method : publicMethods) {
				if (Mapping.isMappingMethod(method)) {
					method.setAccessible(true);
					md=Mapping.getMappingDetails(method);
					ControllerAndMethod come = new ControllerAndMethod();
					String[] controllerIps=clzz.getAnnotation(Controller.class).ip();
					String[] mappingIps=md.ip;
					come.addIds(controllerIps);
					come.addIds(mappingIps);
					if(clzz.getAnnotation(Controller.class).rest()!=Rest.NO)
						come.setRest(clzz.getAnnotation(Controller.class).rest());
					if(method.isAnnotationPresent(RestBody.class))
						come.setRest(method.getAnnotation(RestBody.class).value());
					come.setIpSection(clzz.getAnnotation(Controller.class).ipSection());
					come.setIpSection(md.ipSection);
					come.setController(entry.getValue());
					String url_m=md.value;
					if(url_m.startsWith("/"))
						url_m=url_m.substring(1);
					come.setMethod(method);
					RequestMethod[] mappingRequestMethod = md.method;
					come.setRequestMethods(mappingRequestMethod);
					URLAndRequestMethod uRLAndRequestMethod=new URLAndRequestMethod();
					uRLAndRequestMethod.setUrl(url_c + url_m);
					uRLAndRequestMethod.addMethods(mappingRequestMethod);
					addHanderMap(uRLAndRequestMethod, come);
					ip=come.getIps().isEmpty()?"":" , IP: "+come.getIps().toString();
					ips=come.getIpSection().length==0?"":" , IP段: "+Arrays.toString(come.getIpSection());
					rest=" , Rest: "+come.getRest().toString();
					log.info("@Mapping \"{"+"URL: ["+ url_c +url_m+"] , RequestMethod: "+uRLAndRequestMethod.getMethods() +ip+ips+rest+" , Method: "+method+"}\"");
				} else if(method.isAnnotationPresent(InitRun.class)){
					InitRun initRun=method.getAnnotation(InitRun.class);
					String id="".equals(initRun.id())?LuckyUtils.TableToClass1(clzz.getSimpleName())+"."+method.getName():initRun.id();
					serverStartRuns.add(new ServerStartRun(initRun.priority(),id,instance,method,initRun.runParam()));
				} else if(method.isAnnotationPresent(CloseRun.class)){
					CloseRun closeRun=method.getAnnotation(CloseRun.class);
					serverCloseRuns.add(new ServerStartRun(closeRun.priority(),"CLOSE",instance,method,closeRun.runParam()));
				}else  {
					continue;
				}
			}
		}
	}
}

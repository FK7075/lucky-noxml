 package com.lucky.jacklamb.ioc.scan;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import com.lucky.jacklamb.annotation.aop.Aspect;
import com.lucky.jacklamb.annotation.ioc.*;
import com.lucky.jacklamb.annotation.mvc.*;
import com.lucky.jacklamb.annotation.orm.Table;
import com.lucky.jacklamb.annotation.orm.mapper.Mapper;
import com.lucky.jacklamb.aop.core.Point;
import com.lucky.jacklamb.conversion.annotation.Conversion;
import com.lucky.jacklamb.httpclient.registry.RegistrationController;
import com.lucky.jacklamb.httpclient.service.LuckyClientController;
import com.lucky.jacklamb.ioc.config.ApplicationConfig;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.LuckyConfig;
import com.lucky.jacklamb.ioc.config.ScanConfig;
import com.lucky.jacklamb.ioc.enums.IocCode;
import com.lucky.jacklamb.quartz.ann.QuartzJobs;
import com.lucky.jacklamb.rest.LSON;
import com.lucky.jacklamb.rest.XStreamAllowType;
import com.lucky.jacklamb.sqlcore.mapper.xml.MapperXMLParsing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpoint;

 /**
 * 包扫描的基类
 * @author fk-7075
 *
 */
public abstract class Scan {

	 private static final Logger log= LogManager.getLogger(Scan.class);

	 protected LSON lson;
	
	/**
	 * Map<iocCode,iocType>
	 */
	protected Map<String, List<Class<?>>> componentClassMap;
	
	/**
	 * Controller组件
	 */
	protected List<Class<?>> controllerClass;
	
	/**
	 * Service组件
	 */
	protected List<Class<?>> serviceClass;
	
	/**
	 * Repository组件
	 */
	protected List<Class<?>> repositoryClass;
	
	/**
	 * Component组件
	 */
	protected List<Class<?>> componentClass;
	
	/**
	 * Aspect组件
	 */
	protected List<Class<?>> aspectClass;
	
	/**
	 * WebSocket组件
	 */
	protected List<Class<?>> webSocketClass;
	
	/**
	 * 配置类修改器
	 */
	protected ApplicationConfig appConfig;
	
	/**
	 * 全局配置类
	 */
	private AppConfig configuration;

	protected List<Class<?>> pojoClass;

	protected List<Class<?>> deserializationXStream;
	
	protected boolean isFirst=true;
	
	public Scan() {
		lson=new LSON();
		componentClassMap=new HashMap<>();
		controllerClass=new ArrayList<>();
		serviceClass=new ArrayList<>();
		repositoryClass=new ArrayList<>();
		componentClass=new ArrayList<>();
		aspectClass=new ArrayList<>();
		webSocketClass=new ArrayList<>();
		pojoClass=new ArrayList<>();
		deserializationXStream=new ArrayList<>();
	}

	public List<Class<?>> getPojoClass() {
		return pojoClass;
	}

	public void init() {
		configuration=AppConfig.getAppConfig();
		if(configuration.getServiceConfig().isRegistrycenter()){
			controllerClass.add(RegistrationController.class);
		}else if(configuration.getServiceConfig().getServiceName()!=null&&!configuration.getServiceConfig().isRegistrycenter()){
			controllerClass.add(LuckyClientController.class);
		}
		if(configuration.getScanConfig().getScanMode()==com.lucky.jacklamb.enums.Scan.AUTO_SCAN) {
			log.info("LUCKY-SCAN-MODE        => AUTO_SCAN");
			autoScan();
		}else {
			suffixScan();
		}
		componentClassMap.put("controller", controllerClass);
		componentClassMap.put("service", serviceClass);
		componentClassMap.put("repository", repositoryClass);
		componentClassMap.put("component", componentClass);
		componentClassMap.put("aspect", aspectClass);
		componentClassMap.put("websocket", webSocketClass);
		componentClassMap.put("xStream",deserializationXStream);
	}
	
	public List<Class<?>> getComponentClass(String iocCode){
		return componentClassMap.get(iocCode);
	}
	
	/**
	 * 后缀扫描
	 */
	public void  suffixScan() {
		ScanConfig scanConfig = configuration.getScanConfig();
		log.info("LUCKY-SCAN-MODE =>   SUFFIX_SCAN");
		log.info("CONTROLLER-PACK-SUFFIX : "+scanConfig.getControllerPackSuffix());
		log.info("SERVICE-PACK-SUFFIX    : "+scanConfig.getServicePackSuffix());
		log.info("REPOSITORY-PACK-SUFFIX : "+scanConfig.getRepositoryPackSuffix());
		log.info("COMPONENT-PACK-SUFFIX  : "+scanConfig.getComponentPackSuffix());
		log.info("ASPECT-PACK-SUFFIX     : "+scanConfig.getAspectPackSuffix());
		log.info("WEBSOCKET-PACK-SUFFIX  : "+scanConfig.getWebSocketPackSuffix());
		log.info("POJO-PACK-SUFFIX       : "+scanConfig.getPojoPackSuffix());
		controllerClass=loadComponent(scanConfig.getControllerPackSuffix());
		serviceClass=loadComponent(scanConfig.getServicePackSuffix());
		repositoryClass=loadComponent(scanConfig.getRepositoryPackSuffix());
		componentClass=loadComponent(scanConfig.getComponentPackSuffix());
		aspectClass=loadComponent(scanConfig.getAspectPackSuffix());
		webSocketClass=loadComponent(scanConfig.getWebSocketPackSuffix());
		pojoClass=loadComponent(scanConfig.getPojoPackSuffix());
	}
	
	public ApplicationConfig getApplicationConfig() {
		if(isFirst) {
			findAppConfig();
			isFirst=false;
		}
		return appConfig;
	}
	
	/**
	 * 找到ApplicationConfig配置类
	 */
	public abstract void findAppConfig();

	 /**
	  * 解析分类Class文件
	  * @param fileClass
	  */
	public void load(Class<?> fileClass){
		if(fileClass.isAnnotationPresent(XStreamAllowType.class)){
			deserializationXStream.add(fileClass);
		}
		if(fileClass.isAnnotationPresent(Controller.class)||fileClass.isAnnotationPresent(CallController.class)||fileClass.isAnnotationPresent(LuckyClient.class)) {
			controllerClass.add(fileClass);
		} else if(fileClass.isAnnotationPresent(Service.class)) {
			serviceClass.add(fileClass);
		} else if(fileClass.isAnnotationPresent(Repository.class)||fileClass.isAnnotationPresent(Mapper.class)) {
			repositoryClass.add(fileClass);
		} else if(fileClass.isAnnotationPresent(Component.class)
				||fileClass.isAnnotationPresent(Configuration.class)
				||fileClass.isAnnotationPresent(ControllerExceptionHandler.class)
				||fileClass.isAnnotationPresent(LuckyServlet.class)
				||fileClass.isAnnotationPresent(LuckyFilter.class)
				||fileClass.isAnnotationPresent(LuckyListener.class)
				|| fileClass.isAnnotationPresent(Conversion.class)
				|| fileClass.isAnnotationPresent(QuartzJobs.class)) {
			componentClass.add(fileClass);
		} else if(fileClass.isAnnotationPresent(Table.class)) {
			pojoClass.add(fileClass);
		} else if(fileClass.isAnnotationPresent(Aspect.class)|| Point.class.isAssignableFrom(fileClass)) {
			aspectClass.add(fileClass);
		}else {
			try {
				if(fileClass.isAnnotationPresent(ServerEndpoint.class)|| ServerApplicationConfig.class.isAssignableFrom(fileClass)|| Endpoint.class.isAssignableFrom(fileClass)) {
					webSocketClass.add(fileClass);
				}
			}catch(Exception e) {

			}
		}
	}

	 /**
	  * 将被@Configuration标注的@Bean方法中的组件注册到待加载的准容器中
	  * @param componentClass
	  * @throws IllegalAccessException
	  * @throws InstantiationException
	  * @throws InvocationTargetException
	  */
	public void registered(Class<?> componentClass) throws IllegalAccessException, InstantiationException, InvocationTargetException {
		if(componentClass.isAnnotationPresent(Configuration.class)) {
			Method[] beanMethods = componentClass.getDeclaredMethods();
			Object cfgObj = componentClass.newInstance();
			Class<?> returnType;
			Object config;
			StringBuilder info;
			for (Method method : beanMethods) {
				returnType = method.getReturnType();
				if (method.isAnnotationPresent(Bean.class)) {
					IocCode iocCode = method.getAnnotation(Bean.class).iocCode();
					method.setAccessible(true);
					if (LuckyConfig.class.isAssignableFrom(returnType)) {
						config = method.invoke(cfgObj);
						info = new StringBuilder();
						info.append("@").append(config.getClass().getSimpleName()).append(" \"").append(lson.toJson1(config)).append("\"");
						log.info(info.toString());
					} else if (iocCode == IocCode.CONTROLLER) {
						config = method.invoke(cfgObj);
						if (returnType == Class.class) {
							Class ctrlClass = (Class) config;
							controllerClass.add(ctrlClass);
						} else if (returnType == Class[].class) {
							Class[] ctrlClasses = (Class[]) config;
							Stream.of(ctrlClasses).forEach(controllerClass::add);
						} else {
							throw new RuntimeException("尝试创建一个@Controller组件失败！不合法的返回值类型" + returnType + ",合法的返回值类型为Class和Class[]，错误位置：" + method);
						}
					} else if (iocCode == IocCode.SERVICE) {
						config = method.invoke(cfgObj);
						if (returnType == Class.class) {
							Class ctrlClass = (Class) config;
							serviceClass.add(ctrlClass);
						} else if (returnType == Class[].class) {
							Class[] ctrlClasses = (Class[]) config;
							Stream.of(ctrlClasses).forEach(serviceClass::add);
						} else {
							throw new RuntimeException("尝试创建一个@Service组件失败！不合法的返回值类型" + returnType + ",合法的返回值类型为Class和Class[]，错误位置：" + method);
						}
					} else if (iocCode == IocCode.REPOSITORY) {
						config = method.invoke(cfgObj);
						if (returnType == Class.class) {
							Class ctrlClass = (Class) config;
							repositoryClass.add(ctrlClass);
						} else if (returnType == Class[].class) {
							Class[] ctrlClasses = (Class[]) config;
							Stream.of(ctrlClasses).forEach(repositoryClass::add);
						} else {
							throw new RuntimeException("尝试创建一个@Repository组件失败！不合法的返回值类型" + returnType + ",合法的返回值类型为Class和Class[]，错误位置：" + method);
						}
					} else if (iocCode == IocCode.JOB) {
						config = method.invoke(cfgObj);
						if (returnType == Class.class) {
							Class ctrlClass = (Class) config;
							this.componentClass.add(ctrlClass);
						} else if (returnType == Class[].class) {
							Class[] ctrlClasses = (Class[]) config;
							Stream.of(ctrlClasses).forEach(this.componentClass::add);
						} else {
							throw new RuntimeException("尝试创建一个@Job组件失败！不合法的返回值类型" + returnType + ",合法的返回值类型为Class和Class[]，错误位置：" + method);
						}
					} else if (iocCode == IocCode.TABLE) {
						config = method.invoke(cfgObj);
						if (returnType == Class.class) {
							Class ctrlClass = (Class) config;
							pojoClass.add(ctrlClass);
						} else if (returnType == Class[].class) {
							Class[] ctrlClasses = (Class[]) config;
							Stream.of(ctrlClasses).forEach(pojoClass::add);
						} else {
							throw new RuntimeException("尝试创建一个@Table组件失败！不合法的返回值类型" + returnType + ",合法的返回值类型为Class和Class[]，错误位置：" + method);
						}
					} else if (iocCode == IocCode.ASPECT) {
						config = method.invoke(cfgObj);
						if (returnType == Class.class) {
							Class ctrlClass = (Class) config;
							aspectClass.add(ctrlClass);
						} else if (returnType == Class[].class) {
							Class[] ctrlClasses = (Class[]) config;
							Stream.of(ctrlClasses).forEach(aspectClass::add);
						} else {
							throw new RuntimeException("尝试创建一个@Aspect组件失败！不合法的返回值类型" + returnType + ",合法的返回值类型为Class和Class[]，错误位置：" + method);
						}
					}else if(iocCode == IocCode.X_STREAM){
						config = method.invoke(cfgObj);
						if (returnType == Class.class) {
							Class ctrlClass = (Class) config;
							deserializationXStream.add(ctrlClass);
						} else if (returnType == Class[].class) {
							Class[] ctrlClasses = (Class[]) config;
							Stream.of(ctrlClasses).forEach(deserializationXStream::add);
						} else {
							throw new RuntimeException("尝试创建一个@XStreamAllowType组件失败！不合法的返回值类型" + returnType + ",合法的返回值类型为Class和Class[]，错误位置：" + method);
						}
					} else if (iocCode == IocCode.WEBSOCKET) {
						config = method.invoke(cfgObj);
						if (returnType == Class.class) {
							Class ctrlClass = (Class) config;
							webSocketClass.add(ctrlClass);
						} else if (returnType == Class[].class) {
							Class[] ctrlClasses = (Class[]) config;
							Stream.of(ctrlClasses).forEach(webSocketClass::add);
						} else {
							throw new RuntimeException("尝试创建一个@ServerEndpoint组件失败！不合法的返回值类型" + returnType + ",合法的返回值类型为Class和Class[]，错误位置：" + method);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 后缀扫描
	 * @param suffixs
	 * @return
	 */
	public abstract List<Class<?>> loadComponent(List<String> suffixs);
	
	/**
	 * 自动扫描
	 */
	public abstract void autoScan();

	public abstract Set<MapperXMLParsing> getAllMapperXml(String path);

	public Map<String,Map<String,String>> getAllMapperSql(String path){
		Set<MapperXMLParsing> mapperXmlSet=getAllMapperXml(path);
		Map<String,Map<String,String>> mapperSqls=new HashMap<>();
		for (MapperXMLParsing mapXmlp : mapperXmlSet) {
			Map<String, Map<String, String>> sqlMap = mapXmlp.getXmlMap();
			for(Map.Entry<String,Map<String,String>> en:sqlMap.entrySet()){
				String key = en.getKey();
				Map<String, String> map = en.getValue();
				if(mapperSqls.containsKey(key)){
					Map<String, String> contextMap = mapperSqls.get(key);
					for(Map.Entry<String,String> e:map.entrySet()){
						if(contextMap.containsKey(e.getKey())){
							throw new RuntimeException("同一个Mapper接口方法的SQL配置出现在了两个xml配置文件中！ "+key+"."+e.getKey()+"(XXX)");
						}else{
							contextMap.put(e.getKey(),e.getValue());
						}
					}
				}else{
					mapperSqls.put(key,map);
				}
			}
		}
		return mapperSqls;
	}

}

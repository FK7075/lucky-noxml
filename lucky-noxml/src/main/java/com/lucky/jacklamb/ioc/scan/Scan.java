 package com.lucky.jacklamb.ioc.scan;

 import com.lucky.jacklamb.annotation.aop.Aspect;
 import com.lucky.jacklamb.annotation.ioc.*;
 import com.lucky.jacklamb.annotation.mvc.*;
 import com.lucky.jacklamb.annotation.orm.Table;
 import com.lucky.jacklamb.annotation.orm.mapper.Mapper;
 import com.lucky.jacklamb.aop.core.AopPoint;
 import com.lucky.jacklamb.aop.core.InjectionAopPoint;
 import com.lucky.jacklamb.conversion.annotation.Conversion;
 import com.lucky.jacklamb.httpclient.registry.RegistrationController;
 import com.lucky.jacklamb.httpclient.service.LuckyClientController;
 import com.lucky.jacklamb.ioc.AspectAOP;
 import com.lucky.jacklamb.ioc.ComponentFilter;
 import com.lucky.jacklamb.ioc.config.AppConfig;
 import com.lucky.jacklamb.ioc.config.ApplicationConfig;
 import com.lucky.jacklamb.ioc.config.LuckyConfig;
 import com.lucky.jacklamb.ioc.config.ScanConfig;
 import com.lucky.jacklamb.ioc.enums.IocCode;
 import com.lucky.jacklamb.quartz.ann.QuartzJobs;
 import com.lucky.jacklamb.rest.LSON;
 import com.lucky.jacklamb.rest.XStreamAllowType;
 import com.lucky.jacklamb.sqlcore.mapper.xml.MapperXMLParsing;
 import com.lucky.jacklamb.utils.base.Assert;
 import com.lucky.jacklamb.utils.base.LuckyUtils;
 import com.lucky.jacklamb.utils.reflect.AnnotationUtils;
 import com.lucky.jacklamb.utils.reflect.ClassUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;

 import javax.websocket.Endpoint;
 import javax.websocket.server.ServerApplicationConfig;
 import javax.websocket.server.ServerEndpoint;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 import java.util.stream.Stream;

 /**
 * 包扫描的基类
 * @author fk-7075
 *
 */
public abstract class Scan implements ComponentFilter {

	 private static final Logger log= LogManager.getLogger(Scan.class);
//	 private static final InternalComponents internalComponents=InternalComponents.getInternalComponents();
	 protected LSON lson;
	/**
	 * Map<iocCode,iocType>
	 */
	protected Map<String, Set<Class<?>>> componentClassMap;
	/**
	 * Controller组件
	 */
	protected Set<Class<?>> controllerClass;
	/**
	 * Service组件
	 */
	protected Set<Class<?>> serviceClass;
	/**
	 * Repository组件
	 */
	protected Set<Class<?>> repositoryClass;
	/**
	 * Component组件
	 */
	protected Set<Class<?>> componentClass;
	/**
	 * Aspect组件
	 */
	protected Set<Class<?>> aspectClass;
	/**
	 * WebSocket组件
	 */
	protected Set<Class<?>> webSocketClass;
	/** 配置类@Bean */
	protected Set<Class<?>> beanClass;
	/**
	 * 配置类修改器
	 */
	protected ApplicationConfig appConfig;
	/**
	 * 全局配置类
	 */
	private AppConfig configuration;
	protected Set<Class<?>> pojoClass;
	protected Set<Class<?>> deserializationXStream;
	protected boolean isFirst=true;
	private static final Class<? extends Annotation>[] COMPONENT_ANNS=
			 new Class[]{
					 Component.class,ControllerExceptionHandler.class,LuckyServlet.class,
					 LuckyFilter.class,LuckyListener.class,Conversion.class,QuartzJobs.class
			 };
	private static final Class<? extends Annotation>[] CONTROLLER_ANNS=
			 new Class[]{
					 Controller.class,CallController.class,LuckyClient.class
			 };

	private static final Class<? extends Annotation>[] REPOSITORY_ANNS=
			 new Class[]{
					 Repository.class,Mapper.class
			 };
	
	public Scan() {
		lson=new LSON();
		componentClassMap=new HashMap<>(16);
		controllerClass=new HashSet<>(16);
		serviceClass=new HashSet<>(16);
		repositoryClass=new HashSet<>(16);
		componentClass=new HashSet<>(16);
		aspectClass=new HashSet<>(16);
		webSocketClass=new HashSet<>(16);
		pojoClass=new HashSet<>(16);
		deserializationXStream=new HashSet<>(16);
		beanClass=new HashSet<>(16);
	}

	public Set<Class<?>> getPojoClass() {
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
			autoScan();
		}else {
			suffixScan();
		}
//		aspectClass.addAll(internalComponents.getDirectClass());
		componentClassMap.put("controller", controllerClass);
		componentClassMap.put("service", serviceClass);
		componentClassMap.put("repository", repositoryClass);
		componentClassMap.put("component", componentClass);
		componentClassMap.put("aspect", aspectClass);
		componentClassMap.put("websocket", webSocketClass);
		componentClassMap.put("xStream",deserializationXStream);
		componentClassMap.put("bean",beanClass);
	}
	
	public Set<Class<?>> getComponentClass(String iocCode){
		return componentClassMap.get(iocCode);
	}
	
	/**
	 * 后缀扫描
	 */
	public void  suffixScan() {
		ScanConfig scanConfig = configuration.getScanConfig();
		controllerClass=loadComponent(scanConfig.getControllerPackSuffix());
		serviceClass=loadComponent(scanConfig.getServicePackSuffix());
		repositoryClass=loadComponent(scanConfig.getRepositoryPackSuffix());
		componentClass=loadComponent(scanConfig.getComponentPackSuffix());
		aspectClass=loadComponent(scanConfig.getAspectPackSuffix());
		webSocketClass=loadComponent(scanConfig.getWebSocketPackSuffix());
		pojoClass=loadComponent(scanConfig.getPojoPackSuffix());
		beanClass=loadComponent(scanConfig.getBeanPackSuffix());
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
		if(filter(fileClass)){
			return;
		}
		if(AnnotationUtils.isExist(fileClass,XStreamAllowType.class)){
			deserializationXStream.add(fileClass);
		}
		if(AnnotationUtils.isExistOrByArray(fileClass,CONTROLLER_ANNS)) {
			controllerClass.add(fileClass);
		} else if(fileClass.isAnnotationPresent(Service.class)) {
			serviceClass.add(fileClass);
		} else if(AnnotationUtils.isExistOrByArray(fileClass,REPOSITORY_ANNS)) {
			repositoryClass.add(fileClass);
		} else if(AnnotationUtils.isExistOrByArray(fileClass,COMPONENT_ANNS)) {
			componentClass.add(fileClass);
		} else if(fileClass.isAnnotationPresent(Table.class)) {
			pojoClass.add(fileClass);
		} else if(fileClass.isAnnotationPresent(Aspect.class)|| AopPoint.class.isAssignableFrom(fileClass)) {
			aspectClass.add(fileClass);
		}else if(fileClass.isAnnotationPresent(Configuration.class)){
			beanClass.add(fileClass);
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
			List<Method> beanMethods = ClassUtils.getMethodByAnnotation(componentClass,Bean.class);
			Object cfgObj = componentClass.newInstance();
			Class<?> returnType;
			Object config;
			StringBuilder info;
			for (Method method : beanMethods) {
				returnType = method.getReturnType();
				IocCode iocCode = method.getAnnotation(Bean.class).iocCode();
				method.setAccessible(true);
				if (LuckyConfig.class.isAssignableFrom(returnType)) {
					config = method.invoke(cfgObj);
				} else if(InjectionAopPoint.class.isAssignableFrom(returnType)){
					InjectionAopPoint iap =(InjectionAopPoint) method.invoke(cfgObj);
					boolean b = AspectAOP.addIAPoint(iap);
				}else if (iocCode == IocCode.CONTROLLER) {
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
				}else if(iocCode==IocCode.CONF_BEAN){
					config = method.invoke(cfgObj);
					if (returnType == Class.class) {
						Class ctrlClass = (Class) config;
						beanClass.add(ctrlClass);
					} else if (returnType == Class[].class) {
						Class[] ctrlClasses = (Class[]) config;
						Stream.of(ctrlClasses).forEach(beanClass::add);
					} else {
						throw new RuntimeException("尝试创建一个@Configuration组件失败！不合法的返回值类型" + returnType + ",合法的返回值类型为Class和Class[]，错误位置：" + method);
					}
				}
			}
		}
	}

	private String getBeanName(Class<?> currClass,Method currMethod){
		Bean beanAnn= AnnotationUtils.get(currMethod,Bean.class);
		if(Assert.isBlank(beanAnn.value())){
			return LuckyUtils.TableToClass1(currClass.getSimpleName())+"$"+currMethod.getName();
		}
		return beanAnn.value();
	}
	
	/**
	 * 后缀扫描
	 * @param suffixs
	 * @return
	 */
	public abstract Set<Class<?>> loadComponent(List<String> suffixs);
	
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

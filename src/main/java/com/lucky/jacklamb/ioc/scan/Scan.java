 package com.lucky.jacklamb.ioc.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lucky.jacklamb.httpclient.registry.RegistrationController;
import com.lucky.jacklamb.httpclient.service.FeignClientController;
import com.lucky.jacklamb.ioc.config.ApplicationConfig;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ScanConfig;
import com.lucky.jacklamb.utils.LuckyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

 /**
 * 包扫描的基类
 * @author fk-7075
 *
 */
public abstract class Scan {

	 private static final Logger log= LogManager.getLogger(Scan.class);
	
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
	
	protected boolean isFirst=true;
	
	public Scan() {

		componentClassMap=new HashMap<>();
		controllerClass=new ArrayList<>();
		serviceClass=new ArrayList<>();
		repositoryClass=new ArrayList<>();
		componentClass=new ArrayList<>();
		aspectClass=new ArrayList<>();
		webSocketClass=new ArrayList<>();
		pojoClass=new ArrayList<>();

	}

	public List<Class<?>> getPojoClass() {
		return pojoClass;
	}

	public void init() {
		configuration=AppConfig.getAppConfig();
		if(configuration.getServiceConfig().isRegistrycenter()){
			controllerClass.add(RegistrationController.class);
		}else if(configuration.getServiceConfig().getServiceName()!=null&&!configuration.getServiceConfig().isRegistrycenter()){
			controllerClass.add(FeignClientController.class);
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
	 * 后缀扫描
	 * @param suffixs
	 * @return
	 */
	public abstract List<Class<?>> loadComponent(List<String> suffixs);
	
	/**
	 * 自动扫描
	 */
	public abstract void autoScan();

}

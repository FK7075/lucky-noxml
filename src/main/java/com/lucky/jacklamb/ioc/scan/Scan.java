 package com.lucky.jacklamb.ioc.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lucky.jacklamb.ioc.config.ApplicationConfig;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ScanConfig;
import com.lucky.jacklamb.utils.LuckyUtils;

/**
 * 包扫描的基类
 * @author fk-7075
 *
 */
public abstract class Scan {
	
	private static Logger log=Logger.getLogger(Scan.class);
	
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
	
	protected boolean isFirst=true;
	
	public Scan() {
		componentClassMap=new HashMap<>();
		controllerClass=new ArrayList<>();
		serviceClass=new ArrayList<>();
		repositoryClass=new ArrayList<>();
		componentClass=new ArrayList<>();
		aspectClass=new ArrayList<>();
		webSocketClass=new ArrayList<>();
	}
	
	public void init() {
		configuration=AppConfig.getAppConfig();
		if(configuration.getScanConfig().getScanMode()==com.lucky.jacklamb.enums.Scan.AUTO_SCAN) {
			log.info(LuckyUtils.time()+"   LUCKY-SCAN-MODE => AUTO_SCAN");
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
		StringBuilder sb=new StringBuilder(LuckyUtils.time()+"   LUCKY-SCAN-MODE => SUFFIX_SCAN\n");
		sb.append(LuckyUtils.time()+"   [controller-pack-suffix ] "+scanConfig.getControllerPackSuffix()+"\n")
		.append(LuckyUtils.time()+"   [service-pack-suffix    ] "+scanConfig.getServicePackSuffix()+"\n")
		.append(LuckyUtils.time()+"   [repository-pack-suffix ] "+scanConfig.getRepositoryPackSuffix()+"\n")
		.append(LuckyUtils.time()+"   [component-pack-suffix  ] "+scanConfig.getComponentPackSuffix()+"\n")
		.append(LuckyUtils.time()+"   [aspect-pack-suffix     ] "+scanConfig.getAspectPackSuffix()+"\n")
		.append(LuckyUtils.time()+"   [websocket-pack-suffix  ] "+scanConfig.getWebSocketPackSuffix()+"\n")
		.append(LuckyUtils.time()+"   [pojo-pack-suffix       ] "+scanConfig.getPojoPackSuffix());
		log.info(sb.toString());
		controllerClass=loadComponent(scanConfig.getControllerPackSuffix());
		serviceClass=loadComponent(scanConfig.getServicePackSuffix());
		repositoryClass=loadComponent(scanConfig.getRepositoryPackSuffix());
		componentClass=loadComponent(scanConfig.getComponentPackSuffix());
		aspectClass=loadComponent(scanConfig.getAspectPackSuffix());
		webSocketClass=loadComponent(scanConfig.getWebSocketPackSuffix());
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

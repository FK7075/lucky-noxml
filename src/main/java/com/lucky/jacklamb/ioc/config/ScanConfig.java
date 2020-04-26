package com.lucky.jacklamb.ioc.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lucky.jacklamb.enums.Logo;
import com.lucky.jacklamb.enums.Scan;

/**
 * IOC组件的默认位置后缀配置
 * 
 * @author DELL
 *
 */
public class ScanConfig implements LuckyConfig {
	
	private static ScanConfig scanfig;
	
	/**
	 * 设置扫描模式
	 */
	private Scan scanMode;

	/**
	 * Controller组件所在包的后缀
	 */
	private List<String> controllerPackSuffix;

	/**
	 * Service组件所在包的后缀
	 */
	private List<String> servicePackSuffix;

	/**
	 * Repository组件所在包的后缀
	 */
	private List<String> repositoryPackSuffix;

	/**
	 * Aspect组件所在包的后缀
	 */
	private List<String> aspectPackSuffix;

	/**
	 * 普通组件所在包的后缀
	 */
	private List<String> componentPackSuffix;
	
	/**
	 * WebSocket组件所在的包后缀
	 */
	private List<String> webSocketPackSuffix;

	/**
	 * 实体类所在包的后缀
	 */
	private List<String> pojoPackSuffix;

	/**
	 * 设置Lucky运行时的Logo
	 */
	private Logo logo;

	/**
	 * 设置自定义的Logo
	 */
	private String customLogo;
	
	/**
	 * 配置sql的ini文件的位置
	 */
	private String sqlIniPath;
	

	public String getSqlIniPath() {
		return sqlIniPath;
	}
	
	/**
	 * 设置配置sql的ini文件
	 * @param sqlIniPath
	 */
	public void setSqlIniPath(String sqlIniPath) {
		this.sqlIniPath = sqlIniPath;
	}
	public ScanConfig getScanfig() {
		return scanfig;
	}
	public void setScanfig(ScanConfig scanfig) {
		ScanConfig.scanfig = scanfig;
	}

	private ScanConfig() {
		controllerPackSuffix = new ArrayList<>();
		servicePackSuffix = new ArrayList<>();
		repositoryPackSuffix = new ArrayList<>();
		aspectPackSuffix = new ArrayList<>();
		componentPackSuffix = new ArrayList<>();
		pojoPackSuffix = new ArrayList<>();
		webSocketPackSuffix = new ArrayList<>();
	}

	public String getCustomLogo() {
		return customLogo;
	}

	/**
	 * 设置一个自定义Logo
	 * 
	 * @param customLogo
	 */
	public void setCustomLogo(String customLogo) {
		this.customLogo = customLogo;
	}
	
	public List<String> getWebSocketPackSuffix() {
		return webSocketPackSuffix;
	}
	
	public void setWebSocketPackSuffix(List<String> webSocketPackSuffix) {
		this.webSocketPackSuffix = webSocketPackSuffix;
	}
	
	/**
	 * 添加一个装载WebSocket组件的包后缀
	 * 
	 * @param suffix
	 */
	public void addWebSocketPackSuffix(String... suffix) {
		webSocketPackSuffix.addAll(Arrays.asList(suffix));
	}

	/**
	 * 清空原有的配置后，添加一个装载WebSocket组件的包后缀
	 * 
	 * @param suffix
	 */
	public void emptyAddWebSocketPackSuffix(String... suffix) {
		controllerPackSuffix.clear();
		addWebSocketPackSuffix(suffix);
	}
	public Logo getLogo() {
		return logo;
	}

	/**
	 * 在Lucky中选择一个Logo
	 * 
	 * @param logo
	 */
	public void setLogo(Logo logo) {
		this.logo = logo;
	}

	public List<String> getControllerPackSuffix() {
		return controllerPackSuffix;
	}

	/**
	 * 添加一个装载Controller组件的包后缀
	 * 
	 * @param suffix
	 */
	public void addControllerPackSuffix(String... suffix) {
		controllerPackSuffix.addAll(Arrays.asList(suffix));
	}

	/**
	 * 清空原有的配置后，添加一个装载Controller组件的包后缀
	 * 
	 * @param suffix
	 */
	public void emptyAddControllerPackSuffix(String... suffix) {
		controllerPackSuffix.clear();
		controllerPackSuffix.addAll(Arrays.asList(suffix));
	}

	public void setControllerPackSuffix(List<String> controllerPackSuffix) {
		this.controllerPackSuffix = controllerPackSuffix;
	}

	public List<String> getServicePackSuffix() {
		return servicePackSuffix;
	}

	/**
	 * 添加一个装载Controller组件的包后缀
	 * 
	 * @param suffix
	 */
	public void addServicePackSuffix(String... suffix) {
		servicePackSuffix.addAll(Arrays.asList(suffix));
	}

	/**
	 * 清空原有的配置后，添加一个装载Service组件的包后缀
	 * 
	 * @param suffix
	 */
	public void emptyAddServicePackSuffix(String... suffix) {
		servicePackSuffix.clear();
		servicePackSuffix.addAll(Arrays.asList(suffix));
	}

	public void setServicePackSuffix(List<String> servicePackSuffix) {
		this.servicePackSuffix = servicePackSuffix;
	}

	public List<String> getRepositoryPackSuffix() {
		return repositoryPackSuffix;
	}

	/**
	 * 添加一个装载Repository组件的包后缀
	 * 
	 * @return
	 */
	public void addRepositoryPackSuffix(String... suffix) {
		repositoryPackSuffix.addAll(Arrays.asList(suffix));
	}

	/**
	 * 清空原有的配置后，添加一个装载Repository组件的包后缀
	 * 
	 * @param suffix
	 */
	public void emptyAddRepositoryPackSuffix(String... suffix) {
		repositoryPackSuffix.clear();
		repositoryPackSuffix.addAll(Arrays.asList(suffix));
	}

	public void setRepositoryPackSuffix(List<String> repositoryPackSuffix) {
		this.repositoryPackSuffix = repositoryPackSuffix;
	}

	public List<String> getAspectPackSuffix() {
		return aspectPackSuffix;
	}

	/**
	 * 添加一个装载Aspect组件的包后缀
	 * 
	 * @param suffix
	 */
	public void addAspectPackSuffix(String... suffix) {
		aspectPackSuffix.addAll(Arrays.asList(suffix));
	}

	/**
	 * 清空原有的配置后，添加一个装载Aspect组件的包后缀
	 * 
	 * @param suffix
	 */
	public void emptyAddAspectPackSuffix(String... suffix) {
		aspectPackSuffix.clear();
		aspectPackSuffix.addAll(Arrays.asList(suffix));
	}

	public void setAspectPackSuffix(List<String> AspectPackSuffix) {
		this.aspectPackSuffix = AspectPackSuffix;
	}

	public List<String> getComponentPackSuffix() {
		return componentPackSuffix;
	}

	/**
	 * 添加一个装载普通组件的包后缀
	 * 
	 * @param suffix
	 */
	public void addComponentPackSuffix(String... suffix) {
		componentPackSuffix.addAll(Arrays.asList(suffix));
	}

	/**
	 * 清空原有的配置后，添加一个装载普通组件的包后缀
	 * 
	 * @param suffix
	 */
	public void emptyAddComponentPackSuffix(String... suffix) {
		componentPackSuffix.clear();
		componentPackSuffix.addAll(Arrays.asList(suffix));
	}

	public void setComponentPackSuffix(List<String> componentPackSuffix) {
		this.componentPackSuffix = componentPackSuffix;
	}

	public List<String> getPojoPackSuffix() {
		return pojoPackSuffix;
	}

	public void setPojoPackSuffix(List<String> pojoPackSuffix) {
		this.pojoPackSuffix = pojoPackSuffix;
	}

	/**
	 * 添加一个装载pojo实体组件的包后缀
	 * 
	 * @param suffix
	 */
	public void addPojoPackSuffix(String... suffix) {
		pojoPackSuffix.addAll(Arrays.asList(suffix));
	}

	/**
	 * 清空原有的配置后，添加一个装载pojo实体组件的包后缀
	 * 
	 * @param suffix
	 */
	public void emptyAddPojoPackSuffix(String... suffix) {
		pojoPackSuffix.clear();
		pojoPackSuffix.addAll(Arrays.asList(suffix));
	}
	
	public Scan getScanMode() {
		return scanMode;
	}
	
	/**
	 * 设置扫描模式(默认为自动扫描)
	 * @param scanMode
	 */
	public void setScanMode(Scan scanMode) {
		this.scanMode = scanMode;
	}
	
	public static ScanConfig defaultScanConfig() {
		if (scanfig == null) {
			scanfig = new ScanConfig();
			scanfig.addControllerPackSuffix("controller");
			scanfig.addServicePackSuffix("service");
			scanfig.addRepositoryPackSuffix("dao", "repository", "mapper");
			scanfig.addComponentPackSuffix("component", "bean","exceptionhander","conversion");
			scanfig.addWebSocketPackSuffix("websocket");
			scanfig.addAspectPackSuffix("aspect","aop");
			scanfig.addPojoPackSuffix("pojo", "entity");
			scanfig.setSqlIniPath("appconfig.ini");
			scanfig.setScanMode(Scan.AUTO_SCAN);
			scanfig.setLogo(Logo.LUCKY);
		}
		return scanfig;
	}


}

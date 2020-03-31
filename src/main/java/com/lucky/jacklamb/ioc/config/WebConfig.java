package com.lucky.jacklamb.ioc.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class WebConfig  implements LuckyConfig  {
	
	private static WebConfig webConfig;
	
	/**
	 * 请求参数编码格式
	 */
	private String encoding;
	
	/**
	 * 静态资源映射配置
	 */
	private Map<String,String> staticHander;
	
	/**
	 * 全局的响应前后缀配置
	 */
	private List<String> handerPrefixAndSuffix;
	
	/**
	 * 是否开启Lucky的静态资源管理器
	 */
	private boolean openStaticResourceManage;
	
	/**
	 * 是否开启post请求的请求类型转换
	 */
	private boolean postChangeMethod;
	
	/**
	 * 全局资源的IP限制
	 */
	private List<String> globalResourcesIpRestrict;
	
	/**
	 * 静态资源的IP限制
	 */
	private List<String> staticResourcesIpRestrict;
	
	/**
	 * 指定资源的IP限制
	 */
	private Map<String,List<String>> specifiResourcesIpRestrict;
	
	
	private WebConfig() {
		encoding="ISO-8859-1";
		openStaticResourceManage=false;
		postChangeMethod=false;
		handerPrefixAndSuffix=new ArrayList<>();;
		handerPrefixAndSuffix.add("");handerPrefixAndSuffix.add("");
		staticHander=new HashMap<>();
		globalResourcesIpRestrict=new ArrayList<>();
		staticResourcesIpRestrict=new ArrayList<>();
		specifiResourcesIpRestrict=new HashMap<>();
		
	}
	
	public static WebConfig defauleWebConfig() {
		if(webConfig==null)
			webConfig=new WebConfig();
		return webConfig;
	}

	public String getEncoding() {
		return encoding;
	}
	
	public boolean isOpenStaticResourceManage() {
		return openStaticResourceManage;
	}
	
	public List<String> getGlobalResourcesIpRestrict() {
		return globalResourcesIpRestrict;
	}

	public void addGlobalResourcesIpRestrict(String... ips) {
		Stream.of(ips).forEach(globalResourcesIpRestrict::add);
	}

	public List<String> getStaticResourcesIpRestrict() {
		return staticResourcesIpRestrict;
	}

	public void addStaticResourcesIpRestrict(String... ips) {
		Stream.of(ips).forEach(staticResourcesIpRestrict::add);
	}

	public Map<String, List<String>> getSpecifiResourcesIpRestrict() {
		return specifiResourcesIpRestrict;
	}

	public void setSpecifiResourcesIpRestrict(Map<String, List<String>> specifiResourcesIpRestrict) {
		this.specifiResourcesIpRestrict = specifiResourcesIpRestrict;
	}

	/**
	 * 是否开启静态文件处理器(默认关闭 false)
	 * @param openStaticResourceManage
	 */
	public void openStaticResourceManage(boolean openStaticResourceManage) {
		this.openStaticResourceManage = openStaticResourceManage;
	}

	/**
	 * 设置解析url请求的编码格式(默认"ISO-8859-1")
	 * @param encoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Map<String, String> getStaticHander() {
		return staticHander;
	}

	/**
	 * 添加一个静态资源的映射
	 * @param requestMapping 请求路径
	 * @param staticPesources 响应资源
	 * @return
	 */
	public WebConfig addStaticHander(String requestMapping,String staticPesources) {
		staticHander.put(requestMapping, staticPesources);
		return this;
	}

	public List<String> getHanderPrefixAndSuffix() {
		return handerPrefixAndSuffix;
	}

	/**
	 * 设置一个全局的资源转发/重定向的前后缀
	 * @param prefix 前缀("/WEB-INF/jsp/")
	 * @param suffix 后缀(".jsp")
	 */
	public void setHanderPrefixAndSuffix(String prefix,String suffix) {
		handerPrefixAndSuffix.clear();
		handerPrefixAndSuffix.add(prefix);
		handerPrefixAndSuffix.add(suffix);
	}

	public boolean isPostChangeMethod() {
		return postChangeMethod;
	}

	/**
	 * 是否开启POST请求变换(在POST请求下使用_method改变请求的类型！[_method=get/post/put/delete])
	 * @param postChangeMethod
	 */
	public void postChangeMethod(boolean postChangeMethod) {
		this.postChangeMethod = postChangeMethod;
	}
	
	
	
}

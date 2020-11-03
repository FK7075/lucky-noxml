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
	 * 设置单个文件大小为限制1M(单位：kb)
	 */
	private long multipartMaxFileSize;

	/**
	 * 总上传的数据大小也为10M(单位：kb)
	 */
	private long multipartMaxRequestSize;
	
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
	 * 静态文件的根目录
	 */
	private String webRoot;
	
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

	/**
	 * 连接超时时间
	 */
	private int connectTimeout;

	/**
	 *连接请求超时时间
	 */
	private int connectionRequestTimeout;

	/**
	 *socket超时时间
	 */
	private int socketTimeout;

	private WebConfig() {
		webRoot="${classpath}/templates";
		multipartMaxFileSize=1*1024;
		multipartMaxRequestSize=10*1024;
		encoding="UTF-8";
		openStaticResourceManage=false;
		postChangeMethod=false;
		handerPrefixAndSuffix=new ArrayList<>();;
		handerPrefixAndSuffix.add("");handerPrefixAndSuffix.add("");
		staticHander=new HashMap<>();
		globalResourcesIpRestrict=new ArrayList<>();
		staticResourcesIpRestrict=new ArrayList<>();
		specifiResourcesIpRestrict=new HashMap<>();
		connectTimeout=5000;
		connectionRequestTimeout=1000;
		socketTimeout=5000;
	}
	
	public static WebConfig defauleWebConfig() {
		if(webConfig==null)
			webConfig=new WebConfig();
		return webConfig;
	}

	public long getMultipartMaxFileSize() {
		return multipartMaxFileSize;
	}

	/**
	 * 设置单个文件大小限制(单位：kb)
	 * @param multipartMaxFileSize
	 */
	public void setMultipartMaxFileSize(long multipartMaxFileSize) {
		this.multipartMaxFileSize = multipartMaxFileSize;
	}

	public long getMultipartMaxRequestSize() {
		return multipartMaxRequestSize;
	}

	/**
	 * 设置总文件大小限制(单位：kb)
	 * @param multipartMaxRequestSize
	 */
	public void setMultipartMaxRequestSize(long multipartMaxRequestSize) {
		this.multipartMaxRequestSize = multipartMaxRequestSize;
	}

	public String getWebRoot() {
		return webRoot;
	}

	/**
	 * 设置静态文件的根目录
	 * 写法如下
	 *     1.${classpath}/xxx ：classpath下的某个文件夹 默认：${classpath}/webapps
	 *     2.${user.dir}/xxx  ：System.getProperty("user.dir")下的某个文件夹
	 *     3.${docBase}/xxx   : Tomcat的docBase下的某个文件夹
	 *     4.绝对路径
	 * @param webRoot
	 */
	public void setWebRoot(String webRoot) {
		webRoot=webRoot.endsWith("/")?webRoot.substring(0,webRoot.length()-1):webRoot;
		this.webRoot = webRoot;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getConnectionRequestTimeout() {
		return connectionRequestTimeout;
	}

	public void setConnectionRequestTimeout(int connectionRequestTimeout) {
		this.connectionRequestTimeout = connectionRequestTimeout;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
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

	public void setPrefix(String prefix){
		handerPrefixAndSuffix.set(0,prefix);
	}

	public void setSuffix(String suffix){
		handerPrefixAndSuffix.set(1,suffix);
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

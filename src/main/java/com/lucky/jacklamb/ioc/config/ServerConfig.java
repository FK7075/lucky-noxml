package com.lucky.jacklamb.ioc.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import com.lucky.jacklamb.annotation.mvc.LuckyFilter;
import com.lucky.jacklamb.annotation.mvc.LuckyServlet;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.servlet.core.LuckyDispatcherServlet;
import com.lucky.jacklamb.start.FilterMapping;
import com.lucky.jacklamb.start.ServletMapping;
import com.lucky.jacklamb.utils.base.LuckyUtils;

public class ServerConfig implements LuckyConfig  {

	public ServerConfig() {
		servletlist=new ArrayList<>();
		listeners=new HashSet<>();
		filterlist=new ArrayList<>();
	}
	
	private static ServerConfig serverConfig;
	
	private int port;
	
	private boolean autoDeploy;
	
	private boolean reloadable;
	
	private Integer closePort;
	
	private String shutdown;
	
	private int sessionTimeout;
	
	public static String projectPath;
	
	private String contextPath;
	
	private String webapp;
	
	private String URIEncoding;
	
	private String docBase;
	
	private String baseDir;

	private boolean autoCreateWebapp;

	private String requestTargetAllow;
	
	private List<ServletMapping> servletlist;
	
	private List<FilterMapping> filterlist;
	
	private Set<EventListener> listeners;

	public boolean isAutoCreateWebapp() {
		return autoCreateWebapp;
	}

	public void autoCreateWebapp(boolean autoCreateWebapp) {
		this.autoCreateWebapp = autoCreateWebapp;
	}

	public boolean isAutoDeploy() {
		return autoDeploy;
	}

	public void setAutoDeploy(boolean autoDeploy) {
		this.autoDeploy = autoDeploy;
	}

	public boolean isReloadable() {
		return reloadable;
	}

	public void setReloadable(boolean reloadable) {
		this.reloadable = reloadable;
	}

	public String getURIEncoding() {
		return URIEncoding;
	}

	public void setURIEncoding(String uRIEncoding) {
		URIEncoding = uRIEncoding;
	}

	public String getDocBase() {
		return docBase;
	}
	
	public int getSessionTimeout() {
		return sessionTimeout;
	}
	
	public Integer getClosePort() {
		return closePort;
	}
	
	public String getShutdown() {
		return shutdown;
	}

	public String getBaseDir() {
		return baseDir;
	}


	public String getRequestTargetAllow() {
		return requestTargetAllow;
	}

	/**
	 * 配置Tomcat中URL可以使用的特殊字符"|{}[]"
	 * @param requestTargetAllow
	 */
	public void setRequestTargetAllow(String requestTargetAllow) {
		this.requestTargetAllow = requestTargetAllow;
	}

	/**
	 * 设置一个Tomcat的临时文件夹(相对项目路径)
	 * @param baseDir
	 */
	public void setBaseDir(String baseDir) {
		if(baseDir.startsWith("/")) {
			this.baseDir = projectPath+baseDir.substring(1);
		}else {
			this.baseDir = projectPath+baseDir;
		}
	}
	
	/**
	 * 设置一个Tomcat的临时文件夹(绝对路径)
	 * @param ap_baseDir
	 */
	public void setApBaseDir(String ap_baseDir) {
		this.baseDir = ap_baseDir;
	}
	

	/**
	 * 设置一个用于关闭Tomcat服务的指令
	 * @param shutdown
	 */
	public void setShutdown(String shutdown) {
		this.shutdown = shutdown;
	}

	/**
	 * 设置一个用于关闭Tomcat服务的端口
	 * @param closePort
	 */
	public void setClosePort(Integer closePort) {
		this.closePort = closePort;
	}

	/**
	 * 设置Session超时时间
	 * @param sessionTimeout
	 */
	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	/**
	 * 设置一个静态文件的储存库(绝对路径)
	 * @param ap_docBase
	 */
	public void setApDocBase(String ap_docBase) {
		this.docBase = ap_docBase;
	}
	
	/**
	 * 设置一个静态文件的储存库(System.getProperty("user.dir")的相对路径)
	 * @param docbase
	 */
	public void setDocBase(String docbase) {
		if(docbase.startsWith("/")) {
			this.docBase=projectPath+docbase.substring(1);
		}else {
			this.docBase=projectPath+docbase;
		}
	}

	public int getPort() {
		return port;
	}

	/**
	 * 设置Tomcat服务器的启动端口
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getWebapp() {
		return webapp;
	}

	public void setWebapp(String webapp) {
		this.webapp = webapp;
	}
	
	public List<ServletMapping> getServletlist() {
		return servletlist;
	}
	
	
	public Set<EventListener> getListeners() {
		return listeners;
	}
	
	public void addListener(EventListener listener) {
		listeners.add(listener);
	}

	public void addServlet(HttpServlet servlet,String...mappings) {
		String servletName=LuckyUtils.TableToClass1(servlet.getClass().getSimpleName());
		Set<String> maps;
		if(mappings.length==0) {
			maps=new HashSet<>();
			maps.add("/"+servletName);
		}else {
			maps=new HashSet<>(Arrays.asList(mappings));
		}
		ServletMapping servletMapping=new ServletMapping(maps,servletName,servlet);
		servletlist.add(servletMapping);
	}

	public void addServlet(HttpServlet servlet,int loadOnStartup,String...mappings) {
		String servletName=LuckyUtils.TableToClass1(servlet.getClass().getSimpleName());
		Set<String> maps;
		if(mappings.length==0) {
			maps=new HashSet<>();
			maps.add("/"+servletName);
		}else {
			maps=new HashSet<>(Arrays.asList(mappings));
		}
		ServletMapping servletMapping=new ServletMapping(maps,servletName,servlet,loadOnStartup);
		servletlist.add(servletMapping);
	}

	public List<FilterMapping> getFilterlist() {
		return filterlist;
	}

	public void addFilter(Filter filter,String...mappings) {
		String filterName=LuckyUtils.TableToClass1(filter.getClass().getSimpleName());
		Set<String> maps;
		if(mappings.length==0) {
			maps=new HashSet<>();
			maps.add("/"+filterName);
		}else {
			maps=new HashSet<>(Arrays.asList(mappings));
		}
		FilterMapping filterMapping=new FilterMapping(maps,filterName,filter);
		filterlist.add(filterMapping);
	}
	
	public static ServerConfig defaultServerConfig() {
		if(serverConfig==null) {
			serverConfig=new ServerConfig();
			serverConfig.setPort(8080);
			serverConfig.setClosePort(null);
			serverConfig.setShutdown(null);
			serverConfig.setSessionTimeout(30);
			serverConfig.setWebapp("/WebContent/");
			projectPath=System.getProperty("user.dir").replaceAll("\\\\", "/")+"/";
			serverConfig.addServlet(new LuckyDispatcherServlet(),0, "/");
			serverConfig.setContextPath("");
			serverConfig.setApBaseDir(System.getProperty("java.io.tmpdir")+"tomcat/");
			serverConfig.setDocBase("webapp/");
			serverConfig.setURIEncoding("UTF-8");
			serverConfig.setAutoDeploy(false);
			serverConfig.setReloadable(false);
			serverConfig.autoCreateWebapp(false);
			serverConfig.setRequestTargetAllow("|{}[]");
		}
		return serverConfig;
	}
	
	/**
	 * 注解版Servlet注册
	 */
	private void servletInit() {
		List<Object> servlets = ApplicationBeans.createApplicationBeans().getBeans(HttpServlet.class);
		ServletMapping servletMap;
		HttpServlet servlet;
		Set<String> smapping;
		LuckyServlet annServlet;
		for(Object servletObj:servlets) {
			servlet=(HttpServlet) servletObj;
			annServlet=servlet.getClass().getAnnotation(LuckyServlet.class);
			smapping=new HashSet<>(Arrays.asList(annServlet.value()));
			servletMap=new ServletMapping(smapping,LuckyUtils.TableToClass1(servlet.getClass().getSimpleName()),servlet,annServlet.loadOnStartup());
			servletlist.add(servletMap);
		}
	}
	
	/**
	 * 注解版Filter注册
	 */
	private void filterInit() {
		List<Object> filters = ApplicationBeans.createApplicationBeans().getBeans(Filter.class);
		FilterMapping filterMap;
		Filter filter;
		Set<String> fmapping;
		LuckyFilter annFilter;
		for(Object filterObj:filters) {
			filter=(Filter) filterObj;
			annFilter=filter.getClass().getAnnotation(LuckyFilter.class);
			fmapping=new HashSet<>(Arrays.asList(annFilter.value()));
			filterMap=new FilterMapping(fmapping,LuckyUtils.TableToClass1(filter.getClass().getSimpleName()),filter);
			filterlist.add(filterMap);
		}
	}
	
	/**
	 * 注解版Listener注册
	 */
	private void listenerInit() {
		List<Object> listeners = ApplicationBeans.createApplicationBeans().getBeans(EventListener.class);
		listeners.stream().forEach(a->this.listeners.add((EventListener)a));
	}
	
	public void init() {
		listenerInit();
		servletInit();
		filterInit();
	}

}
	

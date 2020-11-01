package com.lucky.jacklamb.ioc.config;

import com.lucky.jacklamb.annotation.mvc.LuckyFilter;
import com.lucky.jacklamb.annotation.mvc.LuckyServlet;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.servlet.core.LuckyDispatcherServlet;
import com.lucky.jacklamb.start.FilterMapping;
import com.lucky.jacklamb.start.ServletMapping;
import com.lucky.jacklamb.utils.base.LuckyUtils;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.*;

public class ServerConfig implements LuckyConfig  {

	private static final String f= "/";

	public ServerConfig() {
		servletList =new ArrayList<>();
		listeners=new HashSet<>();
		filterList =new ArrayList<>();
	}
	
	private static ServerConfig serverConfig;
	
	private int port;
	
	private boolean autoDeploy;
	
	private boolean reloadable;
	
	private Integer closePort;
	
	private String shutdown;
	
	private int sessionTimeout;
	
	private String contextPath;
	
	private String webapp;
	
	private String URIEncoding;
	
	private String docBase;
	
	private String baseDir;

	private String requestTargetAllow;
	
	private List<ServletMapping> servletList;
	
	private List<FilterMapping> filterList;
	
	private Set<EventListener> listeners;

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
	 *   1.${user.dir}/XXX System.getProperty("user.dir")下的某个文件夹
	 *   2.${java.io.tmpdir}/XXX 系统临时文件夹下的某个文件夹
	 *   3.XXX 文件夹的绝对路径
	 * @param baseDir
	 */
	public void setBaseDir(String baseDir) {
		if(baseDir.startsWith("${user.dir}")){
			baseDir=baseDir.substring(11);
			this.baseDir=System.getProperty("user.dir")+baseDir;
		}else if(baseDir.startsWith("${java.io.tmpdir}")){
			baseDir=baseDir.substring(17);
			baseDir=baseDir.startsWith("/")?baseDir.substring(1):baseDir;
			String s = System.getProperty("java.io.tmpdir");
			s=s.endsWith(File.separator)?s:s+File.separator;
			this.baseDir=s+baseDir;
		}else{
			this.baseDir=baseDir;
		}
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
	 * 设置一个静态文件的储存库
	 *   1.${user.dir}/XXX System.getProperty("user.dir")下的某个文件夹
	 *   2.${java.io.tmpdir}/XXX 系统临时文件夹下的某个文件夹
	 *   3.XXX 文件夹的绝对路径
	 * @param docbase
	 */
	public void setDocBase(String docbase) {
		if(docbase.startsWith("${user.dir}")){
			docbase=docbase.substring(11);
			docBase=System.getProperty("user.dir")+docbase;
		}else if(docbase.startsWith("${java.io.tmpdir}")){
			docbase=docbase.substring(17);
			docbase=docbase.startsWith("/")?docbase.substring(1):docbase;
			docBase=System.getProperty("java.io.tmpdir")+docbase;
		}else{
			docBase=docbase;
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
		serverConfig.setBaseDir("${java.io.tmpdir}/tomcat."+serverConfig.getPort()+"/");
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
	
	public List<ServletMapping> getServletList() {
		return servletList;
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
		servletList.add(servletMapping);
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
		servletList.add(servletMapping);
	}

	public List<FilterMapping> getFilterList() {
		return filterList;
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
		filterList.add(filterMapping);
	}
	
	public static ServerConfig defaultServerConfig() {
		if(serverConfig==null) {
			serverConfig=new ServerConfig();
			serverConfig.setPort(8080);
			serverConfig.setClosePort(null);
			serverConfig.setShutdown(null);
			serverConfig.setSessionTimeout(30);
			serverConfig.setWebapp("/WebContent/");
			serverConfig.addServlet(new LuckyDispatcherServlet(),0, "/");
			serverConfig.setContextPath("");
			serverConfig.setURIEncoding("UTF-8");
			serverConfig.setAutoDeploy(false);
			serverConfig.setReloadable(false);
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
			servletList.add(servletMap);
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
			filterList.add(filterMap);
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

	public static void main(String[] args) {
		System.out.println(System.getProperty("java.io.tmpdir"));

	}
}
	

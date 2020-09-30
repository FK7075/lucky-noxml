package com.lucky.jacklamb.servlet;

import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.servlet.core.Model;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LuckyWebContext {
	
	private static final ThreadLocal<LuckyWebContext> context=new ThreadLocal<LuckyWebContext>();
	
	public static LuckyWebContext getCurrentContext() {
		return (LuckyWebContext) context.get();
	}

	public static LuckyWebContext createContext() {
		return new LuckyWebContext();
	}
	
	public static void setContext(LuckyWebContext context1) {
		context.set(context1);
	}
	
	public static void clearContext() {
		context.set(null);
	}
	
	private HttpServletRequest request=null;
	private HttpServletResponse response=null;
	private ServletContext application=null;
	private HttpSession session=null;
	private RequestMethod requestMethod=null;
	private ServletConfig servletConfig=null;

	public RequestMethod getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(RequestMethod requestMethod) {
		this.requestMethod = requestMethod;
	}

	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	public ServletContext getApplication() {
		return application;
	}

	public void setApplication(ServletContext application) {
		this.application = application;
	}

	public HttpSession getSession() {
		return session;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
	
	
}

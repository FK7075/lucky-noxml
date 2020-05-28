package com.lucky.jacklamb.mapping;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lucky.jacklamb.annotation.ioc.Controller;
import com.lucky.jacklamb.annotation.mvc.CrossOrigin;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.ControllerAndMethod;
import com.lucky.jacklamb.ioc.URLAndRequestMethod;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.servlet.LuckyWebContext;
import com.lucky.jacklamb.servlet.Model;

/**
 * url解析，将url映射为ControllerAndMethod对象,并且负责一些关于请求转化与判定的事务,以及跨域问题的解决
 * 
 * @author fk-7075
 *
 */
public class UrlParsMap {
	

	/**
	 * 跨域访问配置
	 * @param request Request对象
	 * @param response Response对象
	 * @param come ControllerAndMethod对象
	 */
	public void setCross(HttpServletRequest request, HttpServletResponse response, ControllerAndMethod come) {
		if (come.getController().getClass().isAnnotationPresent(CrossOrigin.class)) {
			CrossOrigin crso = come.getController().getClass().getAnnotation(CrossOrigin.class);
			String url = request.getHeader("Origin");
			String[] url_v = crso.value();
			String[] url_o = crso.origins();
			if ((url_v.length != 0 && url_o.length != 0)
					&& (!Arrays.asList(url_v).contains(url) && !Arrays.asList(url_o).contains(url)))
				url = "fk-xfl-wl";
			String isCookie = "false";
			if (crso.allowCredentials())
				isCookie = "true";
			response.setHeader("Access-Control-Allow-Origin", url);
			response.setHeader("Access-Control-Allow-Methods", crso.method());
			response.setHeader("Access-Control-Max-Age", crso.maxAge() + "");
			response.setHeader("Access-Control-Allow-Headers", crso.allowedHeaders());
			response.setHeader("Access-Control-Allow-Credentials", isCookie);
			response.setHeader("XDomainRequestAllowed", "1");
		}
		if (come.getMethod().isAnnotationPresent(CrossOrigin.class)) {
			CrossOrigin crso = come.getMethod().getAnnotation(CrossOrigin.class);
			String url = request.getHeader("Origin");
			String[] url_v = crso.value();
			String[] url_o = crso.origins();
			if ((url_v.length != 0 && url_o.length != 0)
					&& (!Arrays.asList(url_v).contains(url) && !Arrays.asList(url_o).contains(url)))
				url = "fk-xfl-cl";
			String isCookie = "false";
			if (crso.allowCredentials())
				isCookie = "true";
			response.setHeader("Access-Control-Allow-Origin", url);
			response.setHeader("Access-Control-Allow-Methods", crso.method());
			response.setHeader("Access-Control-Max-Age", crso.maxAge() + "");
			response.setHeader("Access-Control-Allow-Headers", crso.allowedHeaders());
			response.setHeader("Access-Control-Allow-Credentials", isCookie);
			response.setHeader("XDomainRequestAllowed", "1");
		}
	}

	/**
	 * 根据POST请求参数"_method"的值改变请求的类型
	 * @param request 对象
	 * @param method 当前的请求类型
	 * @param postChange 是否有权限执行改变
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public RequestMethod chagenMethod(HttpServletRequest request, HttpServletResponse response, RequestMethod method,boolean postChange)
			throws UnsupportedEncodingException {
		request.setCharacterEncoding("utf8");
		response.setCharacterEncoding("utf8");
		response.setHeader("Content-Type", "text/html;charset=utf-8");
		if (postChange&&method == RequestMethod.POST) {
			String hihMeth = request.getParameter("_method");
			if (hihMeth != null) {
				if ("POST".equalsIgnoreCase(hihMeth))
					return RequestMethod.POST;
				else if ("GET".equalsIgnoreCase(hihMeth))
					return RequestMethod.GET;
				else if ("PUT".equalsIgnoreCase(hihMeth))
					return RequestMethod.PUT;
				else if ("DELETE".equalsIgnoreCase(hihMeth))
					return RequestMethod.DELETE;
			} else {
				return method;
			}
		}
		return method;
	}
	
	/**
	 * 根据一个请求的URL找到一个与之对应的ControllerAndMethod,如果找不到对应则返回null
	 * @param url 当前请求的URL
	 * @return ControllerAndMethod对象
	 * @throws IOException 
	 */
	public ControllerAndMethod pars(Model model,String url,RequestMethod requestMethod) throws IOException {
		ControllerAndMethod come;
		URLAndRequestMethod iocURM=getURLAndRequestMethod(model,url,requestMethod);
		if(iocURM==null)
			return null;
		come = ApplicationBeans.createApplicationBeans().getHanderMethods().get(iocURM);
		come.setUrl(iocURM.getUrl());
		come.setRestKV(getRestKV(iocURM.getUrl(), url));
		Class<?> controllerClass=come.getController().getClass();
		if(controllerClass.getSimpleName().contains("$$EnhancerByCGLIB$$"))
			controllerClass=controllerClass.getSuperclass();
		Controller cont=controllerClass.getAnnotation(Controller.class);
		List<String> globalprefixAndSuffix=AppConfig.getAppConfig().getWebConfig().getHanderPrefixAndSuffix();
 		come.setPrefix(globalprefixAndSuffix.get(0));
		come.setSuffix(globalprefixAndSuffix.get(1));
		if(!"".equals(cont.prefix()))
			come.setPrefix(cont.prefix());
		if(!"".equals(cont.suffix()))
			come.setSuffix(cont.suffix());
		return come;
	}

	/**
	 * 解析出一个url中的所有Rest参数，封装到一个Map中
	 * @param mapstr 请求映射的模板
	 * @param currurl 符合模板的一个Url
	 * @return
	 */
	public Map<String,String> getRestKV(String mapstr,String currurl){
		Map<String,String> restKV=new HashMap<>();
		String[] mapArray=participle(mapstr);
		String[] urlArray=participle(currurl);
		if(mapstr.endsWith("}*")){
			int index=mapArray.length-1;
			String value="";
			for(int i=index;i<urlArray.length;i++)
				value=value+"/"+urlArray[i];
			String lastParam=mapArray[mapArray.length-1];
			restKV.put(lastParam.substring(2,lastParam.length()-2),value);
			for(int i=0;i<index;i++){
				if(mapArray[i].startsWith("#{")&&mapArray[i].endsWith("}")) {
					restKV.put(mapArray[i].substring(2,mapArray[i].length()-1).trim(), urlArray[i]);
				}
			}
		}
		for(int i=0;i<mapArray.length;i++) {
			if(mapArray[i].startsWith("#{")&&mapArray[i].endsWith("}")) {
				restKV.put(mapArray[i].substring(2,mapArray[i].length()-1).trim(), urlArray[i]);
			}
		}
		return restKV;
	}
	
	public URLAndRequestMethod getURLAndRequestMethod(Model model,String currUrl,RequestMethod currRequestMethod) throws IOException {
		URLAndRequestMethod urm= new URLAndRequestMethod();
		urm.setUrl(currUrl);
		urm.addMethod(currRequestMethod);
		List<URLAndRequestMethod> urlList = ApplicationBeans.createApplicationBeans().getHanderMethods().getUrlList();
		return urm.findUrl(model, urlList);
	}
	
	private String[] participle(String url) {
		String[] split = url.split("/");
		List<String> list=new ArrayList<>();
		Stream.of(split).filter(a->!"".equals(a)).forEach(list::add);
		String[] rest=new String[list.size()];
		list.toArray(rest);
		return rest;
	}

	/**
	 * 为上下文对象封装HttpServletRequest和HttpServletResponse对象
	 * @param model
	 */
	public void setLuckyWebContext(Model model) {
		LuckyWebContext luckyWebContext = LuckyWebContext.createContext();
		luckyWebContext.setRequest(model.getRequest());
		luckyWebContext.setResponse(model.getResponse());
		luckyWebContext.setSession(model.getSession());
		luckyWebContext.setApplication(model.getServletContext());
		LuckyWebContext.setContext(luckyWebContext);
	}

	/**
	 * 清除上下文内容
	 */
	public void closeLuckyWebContext() {
		LuckyWebContext.clearContext();
	}

	/**
	 * 为Controller注入Model HttpSession ServletRequest ServletResponse ServletContext属性
	 * @param obj controller对象
	 * @param model controller方法
	 */
	public void autowReqAdnResp(Object obj, Model model) {
		try {
			ApplicationBeans.iocContainers.autowReqAdnResp(obj, model);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

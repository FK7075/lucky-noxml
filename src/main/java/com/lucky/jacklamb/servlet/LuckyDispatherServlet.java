package com.lucky.jacklamb.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.lucky.jacklamb.annotation.mvc.Download;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.ControllerAndMethod;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;
import com.lucky.jacklamb.ioc.exception.LuckyExceptionDispose;
import com.lucky.jacklamb.mapping.AnnotationOperation;
import com.lucky.jacklamb.mapping.UrlParsMap;
import com.lucky.jacklamb.utils.Jacklabm;

@MultipartConfig
public class LuckyDispatherServlet extends HttpServlet {
	
	private static final long serialVersionUID = 3808567874497317419L;
	private static Logger log=Logger.getLogger(LuckyDispatherServlet.class);
	private AnnotationOperation anop;
	private WebConfig webCfg;
	private UrlParsMap urlParsMap;
	private ResponseControl responseControl;
	

	public void init(ServletConfig config) {
		ApplicationBeans.createApplicationBeans();
		anop = new AnnotationOperation();
		webCfg=AppConfig.getAppConfig().getWebConfig();
		urlParsMap=new UrlParsMap();
		responseControl=new ResponseControl();
	}

	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.luckyResponse(req, resp,RequestMethod.DELETE);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.luckyResponse(req, resp,RequestMethod.PUT);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.luckyResponse(req, resp,RequestMethod.GET);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp){
		this.luckyResponse(req,resp,RequestMethod.POST);
	}
	
	private void luckyResponse(HttpServletRequest req, HttpServletResponse resp,RequestMethod requestMethod) {
		Model model = null;
		Method method=null;
		Object controllerObj=null;
		Object[] args = null;
		try {
			String encoding=webCfg.getEncoding();
			requestMethod=urlParsMap.chagenMethod(req,resp,requestMethod,webCfg.isPostChangeMethod());
			String uri = req.getRequestURI();
			uri=java.net.URLDecoder.decode(new String(uri.getBytes(encoding), req.getCharacterEncoding()), req.getCharacterEncoding());
			model=new Model(req,resp,this.getServletConfig(),requestMethod,encoding);
			urlParsMap.setLuckyWebContext(model);
			String context = req.getContextPath();
			String path = uri.replace(context, "");
			String currIp=req.getRemoteAddr();
			//全局资源的IP限制
			if(!webCfg.getGlobalResourcesIpRestrict().isEmpty()&&!webCfg.getGlobalResourcesIpRestrict().contains(currIp)) {
				model.writer(Jacklabm.exception("HTTP Status 403 Blocking Access","不合法的请求ip："+currIp,"该ip地址没有被注册，服务器拒绝响应！"));
				log.info("403 : 不合法的请求ip："+currIp+"该ip地址没有被注册，服务器拒绝响应！");
				return;
			}
			//指定资源的IP限制
			if(!webCfg.getSpecifiResourcesIpRestrict().isEmpty()&&(webCfg.getSpecifiResourcesIpRestrict().containsKey(path)&&!webCfg.getSpecifiResourcesIpRestrict().get(path).contains(currIp))) {
				model.writer(Jacklabm.exception("HTTP Status 403 Blocking Access","不合法的请求ip："+currIp,"该ip地址没有被注册，服务器拒绝响应！"));
				log.info("403 : 不合法的请求ip："+currIp+"该ip地址没有被注册，服务器拒绝响应！");
				return;
			}
			if(webCfg.isOpenStaticResourceManage()&&StaticResourceManage.isLegalRequest(webCfg,currIp,resp,path)) {
				//静态资源处理
				log.debug("STATIC-REQUEST [静态资源请求]  ["+requestMethod+"]  #SR#=> "+uri);
				StaticResourceManage.response(req, resp, uri);
				return;
			}
			if (path.endsWith(".lucky")||path.endsWith(".do")||path.endsWith(".xfl")||path.endsWith(".fk")||path.endsWith(".cad")||path.endsWith(".lcl")) {
				//Lucky默认可以使用的后缀
				path = path.substring(0, path.lastIndexOf("."));
			}
			if(webCfg.getStaticHander().containsKey(path)) {
				//扫描并执行配置中的映射
				String forwardurl=webCfg.getHanderPrefixAndSuffix().get(0)+webCfg.getStaticHander().get(path)+webCfg.getHanderPrefixAndSuffix().get(1);
				req.getRequestDispatcher(forwardurl).forward(req, resp);
			}else {
				ControllerAndMethod controllerAndMethod = urlParsMap.pars(model,path,requestMethod);
				if(controllerAndMethod==null)
					return;
				if(!controllerAndMethod.ipExistsInRange(currIp)||!controllerAndMethod.ipISCorrect(currIp)) {
					model.writer(Jacklabm.exception("HTTP Status 403 Blocking Access","不合法的请求ip："+currIp,"该ip地址没有被注册，服务器拒绝响应！"));
					log.info("403 : 不合法的请求ip："+currIp+"该ip地址没有被注册，服务器拒绝响应！");
					return;
				}
				else {
					log.info("CURR-REQUEST ==> ["+requestMethod+"] "+uri);
					model.setRestMap(controllerAndMethod.getRestKV());
					urlParsMap.setCross(req,resp, controllerAndMethod);
					method = controllerAndMethod.getMethod(); 
					boolean isDownload = method.isAnnotationPresent(Download.class);
					controllerObj=controllerAndMethod.getController();
					urlParsMap.autowReqAdnResp(controllerObj,model);
					Object obj1 = new Object();
					args = (Object[]) anop.getControllerMethodParam(model,method);
					obj1 = method.invoke(controllerObj, args);
					if (isDownload == true)//下载操作
						anop.download(model, method);
					responseControl.jump(model,controllerAndMethod, method, obj1);
				}
			}
		} catch (Throwable e) {
			log.error("Lucky异常处理机制捕获到LuckyDispatherServlet异常, Caused by: "+e.getCause());
			LuckyExceptionDispose luckyExceptionDispose=new LuckyExceptionDispose();
			luckyExceptionDispose.initialize(model, controllerObj, method, args);
			luckyExceptionDispose.exceptionHand();
			if(e instanceof InvocationTargetException) {
				luckyExceptionDispose.exceptionRole(e.getCause());
			}else {
				luckyExceptionDispose.exceptionRole(e);
			}
		}finally {
			urlParsMap.closeLuckyWebContext();
		}
	}
}

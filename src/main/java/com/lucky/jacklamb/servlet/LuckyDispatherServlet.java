package com.lucky.jacklamb.servlet;

import com.lucky.jacklamb.annotation.mvc.Download;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.file.utils.FileCopyUtils;
import com.lucky.jacklamb.httpclient.HttpClientCall;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.ControllerAndMethod;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ServiceConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;
import com.lucky.jacklamb.ioc.exception.LuckyExceptionDispose;
import com.lucky.jacklamb.mapping.AnnotationOperation;
import com.lucky.jacklamb.mapping.UrlParsMap;
import com.lucky.jacklamb.utils.Jacklabm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@MultipartConfig
public class LuckyDispatherServlet extends HttpServlet {
	
	private static final long serialVersionUID = 3808567874497317419L;
	private static final Logger log= LogManager.getLogger(LuckyDispatherServlet.class);
	private AnnotationOperation anop;
	private WebConfig webCfg;
	private UrlParsMap urlParsMap;
	private ResponseControl responseControl;

	@Override
	public void init(){
		init(null);
	}

	@Override
	public void init(ServletConfig config) {
		ApplicationBeans.createApplicationBeans();
		ServiceConfig service=AppConfig.getAppConfig().getServiceConfig();
		//存在[Service]配置，配置类型为服务时，将此服务注册到注册中心
		if(service.getServiceName()!=null&&!service.isRegistrycenter()){
			try {
				String url=service.getServiceUrl().endsWith("/")?service.getServiceUrl()+"register":service.getServiceUrl()+"/register";
				Map<String,String> param=new HashMap<>();
				param.put("serviceName",service.getServiceName());
				param.put("port",AppConfig.getAppConfig().getServerConfig().getPort()+"");
				HttpClientCall.postCall(url,param);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		anop = new AnnotationOperation();
		webCfg=AppConfig.getAppConfig().getWebConfig();
		urlParsMap=new UrlParsMap();
		responseControl=new ResponseControl();
	}



	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp){
		this.luckyResponse(req, resp,RequestMethod.DELETE);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp){
		this.luckyResponse(req, resp,RequestMethod.PUT);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp){
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
			String currIp=model.getIpAddr();
			if("/favicon.ico".equals(uri)){
				resp.setContentType("image/x-icon");
				URL icoFile = ApplicationBeans.class.getClassLoader().getResource("/favicon.ico");
				if(icoFile!=null){
					FileCopyUtils.copyToServletOutputStream(resp,new File(icoFile.getPath()));
					return;
				}
				FileCopyUtils.copyToServletOutputStream(resp,ApplicationBeans.class.getResourceAsStream("/favicon.ico"));
				return;
			}
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
					log.debug("CURR-REQUEST ==> ["+requestMethod+"] "+path);
					model.setRestMap(controllerAndMethod.getRestKV());
					urlParsMap.setCross(req,resp, controllerAndMethod);
					method = controllerAndMethod.getMethod(); 
					boolean isDownload = method.isAnnotationPresent(Download.class);
					controllerObj=controllerAndMethod.getController();
					urlParsMap.autowReqAdnResp(controllerObj,model);
					Object obj1;
					args =anop.getControllerMethodParam(model,controllerObj.getClass(),method);
					obj1 = method.invoke(controllerObj, args);
					if (isDownload == true)//下载操作
						anop.download(model, method);
					responseControl.jump(model,controllerAndMethod, method, obj1);
				}
			}
		} catch (Throwable e) {
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

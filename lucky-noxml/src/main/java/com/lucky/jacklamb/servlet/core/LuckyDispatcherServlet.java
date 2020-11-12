package com.lucky.jacklamb.servlet.core;

import com.lucky.jacklamb.annotation.mvc.Download;
import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.ioc.ControllerAndMethod;
import com.lucky.jacklamb.servlet.exceptionhandler.DispatchServletExceptionInterceptor;
import com.lucky.jacklamb.servlet.staticsource.StaticResourceManage;
import com.lucky.jacklamb.utils.base.StaticFile;
import com.lucky.jacklamb.utils.file.FileUtils;
import com.lucky.jacklamb.utils.file.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@MultipartConfig
public class LuckyDispatcherServlet extends BaseServlet {
    private final static String ICO ="/favicon.ico";

    private static final Logger log = LogManager.getLogger(LuckyDispatcherServlet.class);

    @Override
    public void luckyResponse(HttpServletRequest req, HttpServletResponse resp, RequestMethod requestMethod) {
        Model model = null;
        Method method = null;
        Object controllerObj = null;
        Object[] args = null;
        try {
            String encoding = webCfg.getEncoding();
            requestMethod = urlParsMap.chagenMethod(req, resp, requestMethod, webCfg.isPostChangeMethod());
            String uri = req.getRequestURI();
            if(uri.contains(";")){
                uri=uri.substring(0,uri.indexOf(";"));
            }
            uri = java.net.URLDecoder.decode(new String(uri.getBytes(encoding), req.getCharacterEncoding()), req.getCharacterEncoding());
            model = new Model(req, resp, this.getServletConfig(), requestMethod, encoding);
            urlParsMap.setLuckyWebContext(model);
            String context = req.getContextPath();
            String path = uri.replace(context, "");
            String currIp = model.getIpAddr();
            if (ICO.equals(uri)) {
                resp.setContentType("image/x-icon");
                InputStream favStream = Resources.getInputStream(StaticFile.USER_ICO_FILE);
                if (favStream != null) {
                    FileUtils.preview(model, favStream, ICO);
                    return;
                }
                FileUtils.preview(model, Resources.getInputStream(StaticFile.ICO_FILE), ICO);
                return;
            }
            //全局资源的IP限制
            if (!webCfg.getGlobalResourcesIpRestrict().isEmpty() && !webCfg.getGlobalResourcesIpRestrict().contains(currIp)) {
                model.error(Code.REFUSED, "该ip地址没有被注册，服务器拒绝响应！", "不合法的请求ip：" + currIp);
                log.info("403 : 不合法的请求ip：" + currIp + "该ip地址没有被注册，服务器拒绝响应！");
                return;
            }
            //指定资源的IP限制
            if (!webCfg.getSpecifiResourcesIpRestrict().isEmpty() && (webCfg.getSpecifiResourcesIpRestrict().containsKey(path) && !webCfg.getSpecifiResourcesIpRestrict().get(path).contains(currIp))) {
                model.error(Code.REFUSED, "该ip地址没有被注册，服务器拒绝响应！", "不合法的请求ip：" + currIp);
                log.info("403 : 不合法的请求ip：" + currIp + "该ip地址没有被注册，服务器拒绝响应！");
                return;
            }
            if (webCfg.isOpenStaticResourceManage() && StaticResourceManage.isLegalRequest(webCfg, currIp, resp, path)) {
                try {
                    if (StaticResourceManage.resources(model, uri)) {
                        //静态资源处理
                        log.debug("STATIC-REQUEST [静态资源请求]  [" + requestMethod + "]  #SR#=> " + uri);
                        StaticResourceManage.response(model, uri);
                        return;
                    }else{
                        model.error(Code.NOTFOUND,"服务器中找不到资源文件 "+uri+"！","找不到资源 "+uri);
                        return;
                    }
                } catch (Exception e) {
                    model.error(e, Code.ERROR);
                    return;
                }

            }
            if (path.endsWith(".lucky") || path.endsWith(".do") || path.endsWith(".xfl") || path.endsWith(".fk") || path.endsWith(".cad") || path.endsWith(".lcl")) {
                //Lucky默认可以使用的后缀
                path = path.substring(0, path.lastIndexOf("."));
            }
            if (webCfg.getStaticHander().containsKey(path)) {
                //扫描并执行配置中的映射
                String forwardurl = webCfg.getHanderPrefixAndSuffix().get(0) + webCfg.getStaticHander().get(path) + webCfg.getHanderPrefixAndSuffix().get(1);
                req.getRequestDispatcher(forwardurl).forward(req, resp);
            } else {
                ControllerAndMethod controllerAndMethod = urlParsMap.pars(model, path, requestMethod);
                if (controllerAndMethod == null)
                    return;
                if (!controllerAndMethod.ipExistsInRange(currIp) || !controllerAndMethod.ipISCorrect(currIp)) {
                    model.error(Code.REFUSED, "该ip地址没有被注册，服务器拒绝响应！", "不合法的请求ip：" + currIp);
                    log.info("403 : 不合法的请求ip：" + currIp + "该ip地址没有被注册，服务器拒绝响应！");
                    return;
                } else {
                    log.debug("CURR-REQUEST ==> [" + requestMethod + "] " + path);
                    model.setRestMap(controllerAndMethod.getRestKV());
                    urlParsMap.setCross(req, resp, controllerAndMethod);
                    method = controllerAndMethod.getMethod();
                    boolean isDownload = method.isAnnotationPresent(Download.class);
                    controllerObj = controllerAndMethod.getController();
                    urlParsMap.autowReqAdnResp(controllerObj, model);
                    Object obj1;
                    args = anop.getControllerMethodParam(model, controllerObj.getClass(), method);
                    obj1 = method.invoke(controllerObj, args);
                    if (isDownload == true)//下载操作
                        anop.download(model, method);
                    responseControl.jump(model, controllerAndMethod.getRest(), method, obj1,controllerAndMethod.getPreAndSuf());
                }
            }
        } catch (Throwable e) {
            /*
                全局异常处理
                1.创建DispatchServlet异常拦截器
                2.初始化DispatchServlet异常拦截器,(属性初始化+异常处理器的注册)
                3.获取当前异常，并执行统一异常处理
             */
            DispatchServletExceptionInterceptor exceptionInterceptor = new DispatchServletExceptionInterceptor();
            exceptionInterceptor.initialize(model, controllerObj, method, args);
            while (true){
                if(e instanceof InvocationTargetException ||
                   e.getClass()==RuntimeException.class){
                    e=e.getCause();
                }else {
                    break;
                }
            }
            exceptionInterceptor.unifiedExceptionHandler(e);
        } finally {
            urlParsMap.closeLuckyWebContext();
        }
    }
}

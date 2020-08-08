package com.lucky.jacklamb.servlet.core;

import com.lucky.jacklamb.annotation.mvc.Download;
import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.file.utils.FileCopyUtils;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.ControllerAndMethod;
import com.lucky.jacklamb.servlet.exceptionhandler.LuckyExceptionDispose;
import com.lucky.jacklamb.servlet.staticsource.StaticResourceManage;
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
            uri = java.net.URLDecoder.decode(new String(uri.getBytes(encoding), req.getCharacterEncoding()), req.getCharacterEncoding());
            model = new Model(req, resp, this.getServletConfig(), requestMethod, encoding);
            urlParsMap.setLuckyWebContext(model);
            String context = req.getContextPath();
            String path = uri.replace(context, "");
            String currIp = model.getIpAddr();
            if ("/favicon.ico".equals(uri)) {
                resp.setContentType("image/x-icon");
                InputStream favStream = ApplicationBeans.class.getResourceAsStream("/ico/favicon.ico");
                if (favStream != null) {
                    FileCopyUtils.preview(model, favStream,"favicon.ico");
                    return;
                }
                FileCopyUtils.preview(model, ApplicationBeans.class.getResourceAsStream("/lucky-config/static/favicon.ico"), "favicon.ico");
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
            LuckyExceptionDispose luckyExceptionDispose = new LuckyExceptionDispose();
            luckyExceptionDispose.initialize(model, controllerObj, method, args);
            luckyExceptionDispose.exceptionHand();
            if (e instanceof InvocationTargetException) {
                luckyExceptionDispose.exceptionRole(e.getCause());
            } else {
                luckyExceptionDispose.exceptionRole(e);
            }
        } finally {
            urlParsMap.closeLuckyWebContext();
        }
    }
}

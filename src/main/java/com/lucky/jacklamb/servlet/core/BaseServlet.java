package com.lucky.jacklamb.servlet.core;

import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.ComponentIOC;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;
import com.lucky.jacklamb.quartz.ann.Job;
import com.lucky.jacklamb.quartz.ann.QuartzJobs;
import com.lucky.jacklamb.servlet.ResponseControl;
import com.lucky.jacklamb.servlet.ServerStartRun;
import com.lucky.jacklamb.servlet.mapping.AnnotationOperation;
import com.lucky.jacklamb.servlet.mapping.UrlParsMap;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.MethodUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {

    protected static final Logger log = LogManager.getLogger(BaseServlet.class);
    protected AnnotationOperation anop;
    protected WebConfig webCfg;
    protected UrlParsMap urlParsMap;
    protected ResponseControl responseControl;

    public void initRun() {
        ApplicationBeans.iocContainers.getControllerIOC().getServerStartRuns()
                .stream().sorted(Comparator.comparing(ServerStartRun::getPriority)).forEach((a) -> {
            log.info("@InitRun ==> Running \"{priority=[" + a.getPriority() + "], id=" + a.getComponentName() + ", Method=" + a.getControllerMethod() + "\"}");
            a.runAdd();
        });
    }

    public void jobRun(){
        final Map<String, Object> appMap = ApplicationBeans.iocContainers.getAppIOC().getAppMap();
        for(Map.Entry<String,Object> entry:appMap.entrySet()){
            Object app = entry.getValue();
            Class<?> appClass = app.getClass().getSuperclass();
            if(appClass.isAnnotationPresent(QuartzJobs.class)){
                Method[] jobMethods = appClass.getDeclaredMethods();
                for (Method jobMethod : jobMethods) {
                    if(jobMethod.isAnnotationPresent(Job.class)&&jobMethod.getAnnotation(Job.class).serverStartRun()){
                        jobMethod.setAccessible(true);
                        log.info("@Job \" "+appClass.getName()+"."+jobMethod.getName()+" \" Start Running......");
                        MethodUtils.invoke(app,jobMethod);
                    }
                }
            }
        }
    }

    @Override
    public void init(ServletConfig config) {
        ApplicationBeans.createApplicationBeans();
        anop = new AnnotationOperation();
        webCfg = AppConfig.getAppConfig().getWebConfig();
        urlParsMap = new UrlParsMap();
        responseControl = new ResponseControl();
        jobRun();
        initRun();
    }

    @Override
    public void destroy() {
        ApplicationBeans.iocContainers.getControllerIOC().getServerCloseRuns()
                .stream().sorted(Comparator.comparing(ServerStartRun::getPriority)).forEach((a) -> {
            log.info("@CloseRun ==> Running \"{priority=[" + a.getPriority() + "], id=" + a.getComponentName() + ", Method=" + a.getControllerMethod() + "\"}");
            a.runAdd();
        });
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        this.luckyResponse(req, resp, RequestMethod.DELETE);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        this.luckyResponse(req, resp, RequestMethod.PUT);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        this.luckyResponse(req, resp, RequestMethod.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        this.luckyResponse(req, resp, RequestMethod.POST);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.luckyResponse(req, resp, RequestMethod.HEAD);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.luckyResponse(req, resp, RequestMethod.OPTIONS);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.luckyResponse(req, resp, RequestMethod.TRACE);
    }

    protected abstract void luckyResponse(HttpServletRequest req, HttpServletResponse resp, RequestMethod post);

}

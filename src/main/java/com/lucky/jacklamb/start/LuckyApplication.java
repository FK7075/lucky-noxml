package com.lucky.jacklamb.start;

import java.io.File;
import java.util.Comparator;

import com.lucky.jacklamb.servlet.ServerStartRun;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.websocket.server.WsSci;

import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;
import static com.lucky.jacklamb.start.RunParam.*;

public class LuckyApplication {

    private static Logger log;

    static {
        System.setProperty("log4j.skipJansi","false");
        log= LogManager.getLogger(LuckyApplication.class);
    }


    /**
     * 在使用java -jar运行jar包时，Lucky会使用JarFile的方法去遍历并筛选classpath下所有的.class文件来寻找组件。
     * 效率较高，但是规定applicationClass类必须写在最外层的包下
     *
     * @param applicationClass
     */
    public static void run(Class<?> applicationClass,String[] args) {
        for (String arg : args) {
            String[] mainKV = arg.split("=");
            if(isRunParam(mainKV[0].trim())){
                System.setProperty(mainKV[0].trim(),mainKV[1]);
            }
        }
        doShutDownWork();
        AppConfig.applicationClass = applicationClass;
        new Thread(()->run(),"main-lucky").start();
    }

    private static void run() {
        ServerConfig serverCfg = AppConfig.getAppConfig().getServerConfig();
        Tomcat tomcat = new Tomcat();
        String runPort = System.getProperty(SERVER_PORT);
        if(runPort!=null)
            tomcat.setPort(Integer.parseInt(runPort));
        else
            tomcat.setPort(serverCfg.getPort());
        tomcat.setBaseDir(serverCfg.getBaseDir());
        tomcat.getHost().setAutoDeploy(serverCfg.isAutoDeploy());
        if (serverCfg.getClosePort() != null)
            tomcat.getServer().setPort(serverCfg.getClosePort());
        if (serverCfg.getShutdown() != null)
            tomcat.getServer().setShutdown(serverCfg.getShutdown());
        StandardContext context = new StandardContext();
        context.setSessionTimeout(serverCfg.getSessionTimeout());
        context.setPath(serverCfg.getContextPath());
        context.setReloadable(serverCfg.isReloadable());
        String docBase = serverCfg.getDocBase();
        if(docBase!=null){
            File docFile=new File(docBase);
            if(!docFile.exists())
                docFile.mkdirs();
            context.setDocBase(docBase);
        }
        context.setSessionCookieName("JackLamb.Lucky.Tomcat");
        context.addLifecycleListener(new Tomcat.DefaultWebXmlListener());
        context.addLifecycleListener(new Tomcat.FixContextListener());
        context.addServletContainerInitializer(new LuckyServletContainerInitializer(), null);
        context.addServletContainerInitializer(new WsSci(), ApplicationBeans.createApplicationBeans().getWebSocketSet());
        if (serverCfg.getRequestTargetAllow() != null)
            System.setProperty("tomcat.util.http.parser.HttpParser.requestTargetAllow", serverCfg.getRequestTargetAllow());
        tomcat.getHost().addChild(context);
        try {
            tomcat.getConnector();
            tomcat.init();
            tomcat.start();
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }


    private static void doShutDownWork() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                ApplicationBeans.iocContainers.getControllerIOC().getServerCloseRuns()
                        .stream().sorted(Comparator.comparing(ServerStartRun::getPriority)).forEach((a)->{
                    log.info("@CloseRun ==> Running \"{priority=["+a.getPriority()+"], id="+a.getComponentName()+", Method="+a.getControllerMethod()+"\"}");
                    a.runAdd();
                });
            }
        });

    }

}

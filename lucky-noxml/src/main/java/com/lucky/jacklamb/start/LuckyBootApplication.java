package com.lucky.jacklamb.start;

import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;
import com.lucky.jacklamb.servlet.ServerStartRun;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
import com.lucky.jacklamb.thymeleaf.utils.ThymeleafConfig;
import com.lucky.jacklamb.thymeleaf.utils.ThymeleafListener;
import com.lucky.jacklamb.utils.base.Assert;
import com.lucky.jacklamb.utils.base.JackLamb;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.tomcat.websocket.server.WsSci;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Comparator;

import static com.lucky.jacklamb.start.RunParam.SERVER_PORT;
import static com.lucky.jacklamb.start.RunParam.isRunParam;

public class LuckyBootApplication {

    private static Logger log;
    private static final RuntimeMXBean mxb = ManagementFactory.getRuntimeMXBean();
    private static final boolean isEnabled= ThymeleafConfig.getConf().isEnabled();

    static {
        System.setProperty("log4j.skipJansi","false");
    }


    /**
     * 在使用java -jar运行jar包时，Lucky会使用JarFile的方法去遍历并筛选classpath下所有的.class文件来寻找组件。
     * 效率较高，但是规定applicationClass类必须写在最外层的包下
     *
     * @param applicationClass
     */
    public static void run(Class<?> applicationClass,String[] args) {
        long start = System.currentTimeMillis();
        AppConfig.applicationClass = applicationClass;
        log= LogManager.getLogger(applicationClass);
        new Preprocessor(applicationClass).init();
        JackLamb.welcome();
        String pid = mxb.getName().split("@")[0];
        ThreadContext.put("pid", pid);
        String classpath= Assert.isNotNull(applicationClass.getClassLoader().getResource(""))
                ?applicationClass.getClassLoader().getResource("").getPath():applicationClass.getResource("").getPath();
        log.info("Starting {} on localhost with PID {} ( {} started by {} in {} )"
                ,applicationClass.getSimpleName()
                ,pid
                ,classpath
                ,System.getProperty("user.name")
                ,System.getProperty("user.dir"));
        for (String arg : args) {
            String[] mainKV = arg.split("=");
            if(isRunParam(mainKV[0].trim())){
                System.setProperty(mainKV[0].trim(),mainKV[1]);
            }
        }
        doShutDownWork();
        run(applicationClass,start);
    }

    private static void run(Class<?> applicationClass, long start) {

        ServerConfig serverCfg = AppConfig.getAppConfig().getServerConfig();
        if(isEnabled){
            serverCfg.addListener(new ThymeleafListener());
        }
        Tomcat tomcat = new Tomcat();
        String runPort = System.getProperty(SERVER_PORT);
        if(runPort!=null) {
            serverCfg.setPort(Integer.parseInt(runPort));
        }
        tomcat.setPort(serverCfg.getPort());
        tomcat.setBaseDir(serverCfg.getBaseDir());
        tomcat.getHost().setAutoDeploy(serverCfg.isAutoDeploy());
        if (serverCfg.getClosePort() != null) {
            tomcat.getServer().setPort(serverCfg.getClosePort());
        }
        if (serverCfg.getShutdown() != null) {
            tomcat.getServer().setShutdown(serverCfg.getShutdown());
        }
        StandardContext context = new StandardContext();
        context.setSessionTimeout(serverCfg.getSessionTimeout());
        context.setPath(serverCfg.getContextPath());
        context.setReloadable(serverCfg.isReloadable());
        String docBase = serverCfg.getDocBase();
        if(docBase!=null){
            File docFile=new File(docBase);
            if(!docFile.exists()) {
                docFile.mkdirs();
            }
            context.setDocBase(docBase);
        }
        context.setSessionCookieName("JackLamb.Lucky.Tomcat");
        context.addLifecycleListener(new Tomcat.DefaultWebXmlListener());
        context.addLifecycleListener(new Tomcat.FixContextListener());
        context.addServletContainerInitializer(new LuckyServletContainerInitializer(), null);
        context.addServletContainerInitializer(new WsSci(), ApplicationBeans.createApplicationBeans().getWebSocketSet());
        if (serverCfg.getRequestTargetAllow() != null) {
            System.setProperty("tomcat.util.http.parser.HttpParser.requestTargetAllow", serverCfg.getRequestTargetAllow());
        }
        tomcat.getHost().addChild(context);

        try {
            tomcat.getConnector();
            tomcat.init();
            tomcat.start();
            long end = System.currentTimeMillis();
            log.info("Started {} in {} seconds (JVM running for {})"
                    ,applicationClass.getSimpleName()
                    ,(end-start)/1000.0
                    ,mxb.getUptime()/1000.0);
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }


    private static void doShutDownWork() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ApplicationBeans.iocContainers.getControllerIOC().getServerCloseRuns()
                    .stream().sorted(Comparator.comparing(ServerStartRun::getPriority)).forEach((a)->{
                log.info("@CloseRun ==> Running \"{priority=["+a.getPriority()+"], id="+a.getComponentName()+", Method="+a.getControllerMethod()+"\"}");
                a.runAdd();
            });
            LuckyDataSource.close();
        }));
    }

}

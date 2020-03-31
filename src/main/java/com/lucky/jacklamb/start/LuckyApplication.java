package com.lucky.jacklamb.start;

import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.log4j.Logger;
import org.apache.tomcat.websocket.server.WsSci;

import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;
import com.lucky.jacklamb.utils.LuckyUtils;

public class LuckyApplication {
	
	private static Logger log=Logger.getLogger(LuckyApplication.class);

	
	/**
	 * 在使用java -jar运行jar包时，Lucky会使用JarFile的方法去遍历并筛选classpath下所有的.class文件来寻找组件。
	 * 效率较高，但是规定applicationClass类必须写在最外层的包下
	 * @param applicationClass
	 */
	public static void run(Class<?> applicationClass) {
		AppConfig.applicationClass=applicationClass;
		run();
	}
	
	private static void run() {
		ServerConfig serverCfg=AppConfig.getAppConfig().getServerConfig();
		long start= System.currentTimeMillis();
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(serverCfg.getPort());
		tomcat.setBaseDir(serverCfg.getBaseDir());
		tomcat.getHost().setAutoDeploy(serverCfg.isAutoDeploy());
		tomcat.getServer().setPort(serverCfg.getClosePort());
		tomcat.getServer().setShutdown(serverCfg.getShutdown());
        StandardContext context =new StandardContext();
        context.setSessionTimeout(serverCfg.getSessionTimeout());
        context.setPath(serverCfg.getContextPath());
        context.setReloadable(serverCfg.isReloadable());
        String docBase = serverCfg.getDocBase();
		File doc=new File(docBase);
		if(!doc.isDirectory())
			doc.mkdirs();
        context.setDocBase(docBase);
        context.setSessionCookieName("Lucky-Tomcat");
        context.addLifecycleListener(new Tomcat.DefaultWebXmlListener());
        context.addLifecycleListener(new Tomcat.FixContextListener());
        context.addServletContainerInitializer(new WsSci(), ApplicationBeans.createApplicationBeans().getWebSocketSet());
        context.addServletContainerInitializer(new LuckyServletContainerInitializer(), null);
        tomcat.getHost().addChild(context);
		try {
			tomcat.init();
			tomcat.start();
			long end= System.currentTimeMillis();
			StringBuilder sb=new StringBuilder();
			sb.append(LuckyUtils.time()+"  Tomcat-SessionTimeOut   : " +serverCfg.getSessionTimeout()+"min\n")
			  .append(LuckyUtils.time()+"  Tomcat-Shutdown-Port    : "+serverCfg.getClosePort()+"\n")
			  .append(LuckyUtils.time()+"  Tomcat-Shutdown-Command : "+serverCfg.getShutdown()+"\n")
			  .append(LuckyUtils.time()+"  Tomcat-BaseDir          : "+serverCfg.getBaseDir()+"\n")
			  .append(LuckyUtils.time()+"  Tomcat-DocBase          : "+docBase+"\n")
			  .append(LuckyUtils.time()+"  Tomcat-ContextPath      : \""+serverCfg.getContextPath()+"\"\n")
			  .append(LuckyUtils.time()+"  Tomcat-Start [http-nio-"+serverCfg.getPort()+"],"+"Tomcat启动成功！用时"+(end-start)+"ms!");
			log.info(sb.toString());
			tomcat.getServer().await();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
	}

}

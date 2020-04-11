package com.lucky.jacklamb.start;

import java.io.File;

import com.lucky.jacklamb.exception.NotFindDocBaseFolderException;
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
		if(!doc.isDirectory()){
			if(!serverCfg.isAutoCreateWebapp()){
				System.err.println(LuckyUtils.time()+"  ###");
				System.err.println(LuckyUtils.time()+"  LUVKY-TOMCAT-DOCBASE ==>[ NOT FOUND ]");
				System.err.println(LuckyUtils.time()+"  [ ==WARNING==]：找不到Tomcat的DocBase文件夹：{ "+docBase+"}");
				System.err.println(LuckyUtils.time()+"  [ == PROMPT== ]：请手动创建该文件夹，或者增加配置信息「 \"autoCreateWebapp=true\" 」,添加之后Lucky在下次启动时将自动创建！");
				System.err.println(LuckyUtils.time()+"  LUVKY-TOMCAT-DOCBASE ==>[ NOT FOUND ]");
				System.err.println(LuckyUtils.time()+"  ###");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else{
				doc.mkdirs();
				context.setDocBase(docBase);
			}
		}else{
			context.setDocBase(docBase);
		}
        context.setSessionCookieName("Lucky-Tomcat");
        context.addLifecycleListener(new Tomcat.DefaultWebXmlListener());
        context.addLifecycleListener(new Tomcat.FixContextListener());
        context.addServletContainerInitializer(new WsSci(), ApplicationBeans.createApplicationBeans().getWebSocketSet());
        context.addServletContainerInitializer(new LuckyServletContainerInitializer(start), null);
        tomcat.getHost().addChild(context);
		try {
			tomcat.init();
			tomcat.start();
			tomcat.getServer().await();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
	}

}

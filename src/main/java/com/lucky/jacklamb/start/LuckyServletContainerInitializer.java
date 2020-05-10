package com.lucky.jacklamb.start;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import com.lucky.jacklamb.utils.LuckyUtils;
import org.apache.log4j.Logger;

import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;

public class LuckyServletContainerInitializer implements ServletContainerInitializer {
	
	public final ServerConfig serverCfg=AppConfig.getAppConfig().getServerConfig();

	private long start;
	
	public static Logger log=Logger.getLogger(LuckyServletContainerInitializer.class);
	
	public LuckyServletContainerInitializer(long start) {
		this.start=start;
		serverCfg.init();
	}

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		ApplicationBeans.createApplicationBeans();
		ServletRegistration.Dynamic servlet;
		FilterRegistration.Dynamic filter;
		String[] mapping;
		for(ServletMapping sm:serverCfg.getServletlist()) {
			servlet=ctx.addServlet(sm.getServletName(), sm.getServlet());
			mapping=new String[sm.getRequestMapping().size()];
			mapping=sm.getRequestMapping().toArray(mapping);
			servlet.addMapping(mapping);
			log.info("@Servlet \"[name="+sm.getServletName()+" mapping="+Arrays.toString(mapping)+" class="+sm.getServlet()+"]\"");
		}
		
		for(FilterMapping fm:serverCfg.getFilterlist()) {
			filter=ctx.addFilter(fm.getFilterName(), fm.getFilter());
			mapping=new String[fm.getRequestMapping().size()];
			mapping=fm.getRequestMapping().toArray(mapping);
			filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true,mapping);
			log.info("@Filter \"[name="+fm.getFilterName()+" mapping="+Arrays.toString(mapping)+" class="+ fm.getFilter()+"]\"");
		}
		
		for(EventListener l:serverCfg.getListeners()) {
			ctx.addListener(l);
			log.info("@Listener \"[class="+l+"]\"");
		}
		long end= System.currentTimeMillis();
		log.info("Tomcat-SessionTimeOut \"" +serverCfg.getSessionTimeout()+"min\"");
		log.info("Tomcat-Shutdown-Port \"" +serverCfg.getClosePort()+"\"");
		log.info("Tomcat-Shutdown-Command \"" +serverCfg.getShutdown()+"\"");
		log.info("Tomcat-BaseDir \"" +serverCfg.getBaseDir()+"\"");
		log.info("Tomcat-DocBase \"" +serverCfg.getDocBase()+"\"");
		log.info("Tomcat-ContextPath : \"" +serverCfg.getContextPath()+"\"");
		log.info("Tomcat-Start [http-nio-"+serverCfg.getPort()+"],"+"Tomcat启动成功！用时"+(end-start)+"ms!");
	}

}

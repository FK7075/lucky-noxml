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

import org.apache.log4j.Logger;

import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;

public class LuckyServletContainerInitializer implements ServletContainerInitializer {
	
	public final ServerConfig serverCfg=AppConfig.getAppConfig().getServerConfig();
	
	public static Logger log=Logger.getLogger(LuckyServletContainerInitializer.class);
	
	public LuckyServletContainerInitializer() {
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
			log.info("@Servlet    => [name="+sm.getServletName()+" mapping="+Arrays.toString(mapping)+" class="+sm.getServlet()+"]");
		}
		
		for(FilterMapping fm:serverCfg.getFilterlist()) {
			filter=ctx.addFilter(fm.getFilterName(), fm.getFilter());
			mapping=new String[fm.getRequestMapping().size()];
			mapping=fm.getRequestMapping().toArray(mapping);
			filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true,mapping);
			log.info("@Filter     => [name="+fm.getFilterName()+" mapping="+Arrays.toString(mapping)+" class="+ fm.getFilter()+"]");
		}
		
		for(EventListener l:serverCfg.getListeners()) {
			ctx.addListener(l);
			log.info("@Listener   => [class="+l+"]");
		}

	}

}

package com.lucky.jacklamb.thymeleaf.utils;

import com.lucky.jacklamb.annotation.mvc.LuckyListener;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.thymeleaf.template.ClasspathTemplateResolver;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.templateresolver.ITemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;
import java.util.Set;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/1 3:23 上午
 */
@LuckyListener
public class ThymeleafListener implements ServletContextListener {

   public final static ThymeleafConfig conf=ThymeleafConfig.getConf();
   public final static List<Object> dialects= ApplicationBeans.createApplicationBeans().getBeans(IDialect.class);

    public void contextInitialized(ServletContextEvent sce) {
        TemplateEngine engine = templateEngine(sce.getServletContext());
        for (Object obj : dialects) {
            engine.addDialect((IDialect)obj);
        }
        TemplateEngineUtil.storeTemplateEngine(sce.getServletContext(), engine);
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }

    private TemplateEngine templateEngine(ServletContext servletContext) {
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver(servletContext));
        return engine;
    }

    private ITemplateResolver templateResolver(ServletContext servletContext) {
        ClasspathTemplateResolver resolver = new ClasspathTemplateResolver();
        resolver.setPrefix(conf.getPrefix());
        resolver.setCharacterEncoding(conf.getEncoding());
        resolver.setSuffix(conf.getSuffix());
        resolver.setCacheable(conf.isCache());
        resolver.setTemplateMode(conf.getModel());
        return resolver;
    }


}

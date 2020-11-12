package com.lucky.jacklamb.authority.shiro.lucky;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.lucky.jacklamb.annotation.ioc.Bean;
import com.lucky.jacklamb.aop.expandpoint.LuckyShiroAccessControlPoint;
import com.lucky.jacklamb.authority.shiro.conf.LuckyFilterChainResolverFactory;
import com.lucky.jacklamb.authority.shiro.conf.LuckyShiroFilter;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

/**
 *
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/12 9:39 下午
 */
public abstract class LuckyShiroConfigBean {

    //使Shiro注解生效的配置
    @Bean
    public LuckyShiroAccessControlPoint shiroAccessControlPoint(){
        return new LuckyShiroAccessControlPoint();
    }

    //Thymeleaf整合Shiro标签库
    @Bean
    public ShiroDialect shiroDialect(){
        return new ShiroDialect();
    }

    //安全管理器
    @Bean
    public SecurityManager securityManager(){
        DefaultWebSecurityManager securityManager=new DefaultWebSecurityManager();
        //缓存管理器
        if(cacheManager()!=null) {
            securityManager.setCacheManager(cacheManager());
        }
        //授权管理器
        if(realm()!=null){
            securityManager.setRealm(realm());
        }
        //会话管理器
        if(sessionManager()!=null){
            securityManager.setSessionManager(sessionManager());
        }
        //记住我管理器
        if(rememberMeManager()!=null){
            securityManager.setRememberMeManager(rememberMeManager());
        }
        return securityManager;
    }

    //Web过滤器
    @Bean
    public void luckyShiroFilter(DefaultWebSecurityManager securityManager){
        ServerConfig server= AppConfig.getDefaultServerConfig();
        server.addFilter(new LuckyShiroFilter(securityManager,resolverFactory()), "/*");
    }

    protected abstract LuckyFilterChainResolverFactory resolverFactory();

    protected abstract RememberMeManager rememberMeManager();

    protected abstract DefaultWebSessionManager sessionManager();

    protected abstract Realm realm();

    protected abstract CacheManager cacheManager();

}

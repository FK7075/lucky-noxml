package com.lucky.jacklamb.authority.shiro.conf;

import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.Nameable;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

import javax.servlet.Filter;
import java.util.Map;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/11/10 11:18
 */
public class LuckyShiroFilter extends AbstractShiroFilter {

    public LuckyShiroFilter(WebSecurityManager webSecurityManager, LuckyFilterChainResolverFactory resolverFactory){
        super();
        if (webSecurityManager == null) {
            throw new IllegalArgumentException("WebSecurityManager property cannot be null.");
        }
        FilterChainManager manager = resolverFactory.createFilterChainManager();
        PathMatchingFilterChainResolver chainResolver = new PathMatchingFilterChainResolver();
        chainResolver.setFilterChainManager(manager);

        setSecurityManager(webSecurityManager);

        if (chainResolver != null) {
            setFilterChainResolver(chainResolver);
        }
    }
}

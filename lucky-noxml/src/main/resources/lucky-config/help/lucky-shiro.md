# Lucky整合Shiro
```java
/**
* Lucky整合Shiro的配置类
*/
@Configuration
public class ShiroConfigBeans {

    @Bean
    public void shiroInit(SecurityManager securityManager){
        SecurityUtils.setSecurityManager(securityManager);
    }

    //密码验证器
    public CredentialsMatcher credentialsMatcher(){
        //自定义的密码验证器
        return new MyMatcher();
    }

    //缓存管理器
    public CacheManager cacheManager(){
        //缓存管理器
        return new MemoryConstrainedCacheManager();
    }

    //权限验证器
    public AuthorizingRealm realm(){
        IniRealm iniRealm = new IniRealm("classpath:shiro.ini");
        //给权限验证器配置上自定义的密码验证器
        iniRealm.setCredentialsMatcher(credentialsMatcher());
        return iniRealm;
    }

    //Cookie配置
    public SimpleCookie rememberMeCookie() {
        //这个参数是cookie的名称，对应前端的checkbox的name=rememberMe
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        //cookie生效时间为10秒
        simpleCookie.setMaxAge(10);
        return simpleCookie;
    }

    //Cookie管理器
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        return  cookieRememberMeManager;
    }


    //将Shiro的安全管理器注入到容器
    @Bean
    public SecurityManager securityManager(){
        DefaultWebSecurityManager securityManager= new DefaultWebSecurityManager();
        securityManager.setRealm(realm());
        securityManager.setCacheManager(cacheManager());
        securityManager.setRememberMeManager(rememberMeManager());
        return securityManager;
    }


    
    //配置ShiroDialect，用于Thymeleaf和Shiro标签配合使用
    @Bean
    public ShiroDialect getShiroDialect() {
        return new ShiroDialect();
    }

    //注入Lucky的Shiro代理，使Shiro的权限注解生效
    @Bean
    public LuckyShiroAccessControlPoint luckyShiroAccessControlPoint(){
        return new LuckyShiroAccessControlPoint();
    }
    
        //配置资源的过滤链
        @Bean
        public LuckyFilterChainResolverFactory resolverFactory(){
            LuckyFilterChainResolverFactory factory=new LuckyFilterChainResolverFactory();
            Map<String, String> map = new HashMap<>();
            //登出
            map.put("/logout", "logout");
            map.put("/sys/in","anon");
            //对所有用户认证
            map.put("/**", "user");
    //        //登录
            factory.setLoginUrl("/login");
    //        //首页
    //        factory.setSuccessUrl("/index");
    //        //错误页面，认证不通过跳转
    //        factory.setUnauthorizedUrl("/error");
            factory.setFilterChainDefinitionMap(map);
            return factory;
        }

    //将LuckyShiroFilter注册到Tomcat容器使其生效
    @Bean
    public void addFilter(LuckyFilterChainResolverFactory resolverFactory,DefaultWebSecurityManager securityManager){
        ServerConfig server= AppConfig.getDefaultServerConfig();
        //构建LuckyShiroFilter需要Shiro的安全管理器和资源过滤链
        server.addFilter(new LuckyShiroFilter(securityManager,resolverFactory), "/*");
    }


}
```
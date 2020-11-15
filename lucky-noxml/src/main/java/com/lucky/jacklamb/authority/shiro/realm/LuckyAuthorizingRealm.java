package com.lucky.jacklamb.authority.shiro.realm;

import com.lucky.jacklamb.authority.shiro.conf.ShiroConfig;
import com.lucky.jacklamb.authority.shiro.entity.SysUser;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.HashSet;
import java.util.Set;

/**
 * Lucky-Shiro的授权中心
 * @author fk7075
 * @version 1.0
 * @date 2020/10/28 10:18
 */
public class LuckyAuthorizingRealm extends AuthorizingRealm {

    private final static Logger log= LogManager.getLogger(LuckyAuthorizingRealm.class);
    private static ShiroConfig shiroConfig=ShiroConfig.getShiroConfig();
    private static SqlCore sqlCore=shiroConfig.getSqlCore();

    /**
     * 授权
     * 将认证通过的角色和权限信息设置到对应的用户主体上
     * @param principals 主体信息
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        AuthorizationInfo authorizationInfo=new SimpleAuthorizationInfo();
        SysUser dbUser= (SysUser) principals.getPrimaryPrincipal();
        Set<String> roles=new HashSet<>();
        dbUser.getRoles().stream().forEach(r->roles.add(r.getName()));

        return null;
    }

    /**
     * 登录认证
     * @param token 登录信息
     * @return 认证信息
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken= (UsernamePasswordToken) token;
        String username=upToken.getUsername();
        String password=new String(upToken.getPassword());
        String sql="";
        SysUser dbUser=sqlCore.getObject(SysUser.class,sql,username);
        if(dbUser==null){
            UnknownAccountException e = new UnknownAccountException("用户名不存在");
            log.error(e);
            throw e;
        }
        if(!dbUser.equalsPassword(password)){
            CredentialsException e=new CredentialsException("密码错误");
            log.error(e);
            throw e;
        }
        if(dbUser.getStatus()==1){
            DisabledAccountException e= new DisabledAccountException("账号被禁用");
            log.error(e);
            throw e;
        }
        if(dbUser.getStatus()==2){
            LockedAccountException e= new LockedAccountException("账号被锁定");
            log.error(e);
            throw e;
        }
        log.debug("认证成功! USER:[{}],PASSWORD:[******]",username);
        SimpleAuthenticationInfo info=
                new SimpleAuthenticationInfo(dbUser,token.getCredentials(),getName());
        return info;
    }
}

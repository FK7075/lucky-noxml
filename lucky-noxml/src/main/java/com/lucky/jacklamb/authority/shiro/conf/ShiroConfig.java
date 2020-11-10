package com.lucky.jacklamb.authority.shiro.conf;

import com.lucky.jacklamb.authority.shiro.entity.SysResource;
import com.lucky.jacklamb.authority.shiro.entity.SysRole;
import com.lucky.jacklamb.authority.shiro.entity.SysUser;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.util.PojoManage;


/**
 * @author fk7075
 * @version 1.0
 * @date 2020/10/28 10:46
 */
public class ShiroConfig {

    private static ShiroConfig shiroConf=null;
    private SqlCore sqlCore;
    private String dbname="defaultDB";
    private String permissionSplit=",";
    private Class<?> sysUserClass= SysUser.class;
    private Class<?> sysRoleClass= SysRole.class;
    private Class<?> sysResourceClass= SysResource.class;
    private String loginSql="SELECT `user_id`,`username`,`password`,`status`,`deleted` FROM `sys_user` WHERE username=?";

    private ShiroConfig(){}

    public static ShiroConfig getShiroConfig(){
        if(shiroConf==null){
            shiroConf=new ShiroConfig();
        }
        return shiroConf;
    }

    public SqlCore getSqlCore() {
        return sqlCore;
    }

    public String getDbname() {
        return dbname;
    }

    public Class<?> getSysUserClass() {
        return sysUserClass;
    }

    public String getSysUserTableName(){
        return PojoManage.getTable(sysUserClass,dbname);
    }

    public Class<?> getSysRoleClass() {
        return sysRoleClass;
    }

    public String getSysRoleTableName(){
        return PojoManage.getTable(sysRoleClass,dbname);
    }

    public Class<?> getSysResourceClass() {
        return sysResourceClass;
    }

    public String getSysResourceTableName(){
        return PojoManage.getTable(sysResourceClass,dbname);
    }

    public String getPermissionSplit() {
        return permissionSplit;
    }


}

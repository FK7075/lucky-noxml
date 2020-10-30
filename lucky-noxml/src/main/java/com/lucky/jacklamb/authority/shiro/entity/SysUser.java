package com.lucky.jacklamb.authority.shiro.entity;

import com.lucky.jacklamb.annotation.orm.Column;
import com.lucky.jacklamb.annotation.orm.Id;
import com.lucky.jacklamb.annotation.orm.Table;
import com.lucky.jacklamb.annotation.orm.jpa.ManyToMany;
import com.lucky.jacklamb.enums.PrimaryType;
import com.lucky.jacklamb.md5.MD5Utils;

import java.util.Set;
import java.util.UUID;

/**
 * 用户表
 * @author fk7075
 * @version 1.0
 * @date 2020/10/22 16:17
 */
@Table("sys_user")
public class SysUser extends SysBase {

    /** 用户表ID*/
    @Id(value = "user_id",type = PrimaryType.AUTO_INT,length = 11)
    private Integer userId;

    /** 用户名*/
    @Column(length = 50)
    private String username;

    /** 登录密码*/
    @Column(length = 100)
    private String password;

    /** 盐*/
    @Column(length = 50)
    private String salt;

    /** 账户状态 0-正常(DEF) 1-禁用 2-锁定*/
    @Column(length = 2)
    private Integer status=0;

    /** 删除状态 0-正常(DEF) 1-删除*/
    @Column(length = 2)
    private Integer deleted=0;

    @ManyToMany(joinTable = "sys_user_role",joinColumnThis = "user_id",joinColumnTo = "role_id")
    private Set<SysRole> roles;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Set<SysRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<SysRole> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }



    /**
     * 设置密码并进行加密
     * @param password 密码明文
     */
    public void setPassword(String password) {
        salt= MD5Utils.md5UpperCase(UUID.randomUUID().toString(),"@1234",1);
        this.password = MD5Utils.md5UpperCase(password,salt,10);
    }

    /**
     * 密码比较
     * @param inputPass 输入的密码
     * @return 比对结果
     */
    public boolean equalsPassword(String inputPass){
        return password.equals(MD5Utils.md5UpperCase(inputPass,salt,10));
    }
}

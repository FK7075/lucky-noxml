package com.lucky.jacklamb.authority.shiro.entity;

import com.lucky.jacklamb.annotation.orm.Column;
import com.lucky.jacklamb.annotation.orm.Id;
import com.lucky.jacklamb.annotation.orm.Table;
import com.lucky.jacklamb.annotation.orm.jpa.ManyToMany;
import com.lucky.jacklamb.enums.PrimaryType;

import java.util.Set;

/**
 * 角色表
 * @author fk7075
 * @version 1.0
 * @date 2020/10/22 16:30
 */
@Table("sys_role")
public class SysRole extends SysBase {

    /** 角色ID*/
    @Id(value = "role_id",type = PrimaryType.AUTO_INT,length = 11)
    private Integer roleId;

    /** 角色名*/
    @Column(length = 100)
    private String name;

    /** 角色描述*/
    @Column(length = 100)
    private String remark;

    @ManyToMany(joinTable = "sys_role_resource",joinColumnThis = "role_id",joinColumnTo = "resource_id")
    private Set<SysResource> resources;

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Set<SysResource> getResources() {
        return resources;
    }

    public void setResources(Set<SysResource> resources) {
        this.resources = resources;
    }
}

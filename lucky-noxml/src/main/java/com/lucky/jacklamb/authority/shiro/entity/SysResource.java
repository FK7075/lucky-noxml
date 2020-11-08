package com.lucky.jacklamb.authority.shiro.entity;

import com.lucky.jacklamb.annotation.orm.Column;
import com.lucky.jacklamb.annotation.orm.Id;
import com.lucky.jacklamb.annotation.orm.Table;
import com.lucky.jacklamb.enums.PrimaryType;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/10/22 16:41
 */
@Table("sys_resource")
public class SysResource extends SysBase implements NodeModel<Integer>{

    /** 资源ID*/
    @Id(value = "resource_id",type = PrimaryType.AUTO_INT,length = 11)
    private Integer resourceId;

    /** 资源名称*/
    private String name;

    /** 资源的父级资源ID*/
    @Column(value = "parent_id",length = 11)
    private Integer parentId=-1;

    /** 资源路径*/
    @Column(value = "path",length = 10)
    private String path;

    /** 资源类型*/
    private String type;

    /** 资源图标*/
    private String icon;

    /** 资源的连接*/
    private String url;

    /** 该资源对应的权限标识符*/
    @Column(length = 500)
    private String permission;

    /** 排序字段*/
    @Column(value = "order_num",length = 11)
    private Integer orderNum;

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getId() {
        return getResourceId();
    }

    @Override
    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}


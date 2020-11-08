package com.lucky.jacklamb.authority.shiro.entity;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/8 9:42 下午
 */
public interface NodeModel<ID> {

    /**
     * 返回当前节点的ID
     * @return 当前节点的ID
     */
    ID getId();

    /**
     * 返回当前节点的父节点的ID
     * @return 当前节点的父节点的ID
     */
    ID getParentId();
}

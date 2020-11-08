package com.lucky.jacklamb.authority.shiro.terr;

import com.lucky.jacklamb.authority.shiro.entity.SysResource;

import java.util.List;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/8 10:53 下午
 */
public class SysResourceNode<T extends SysResource> extends NodeUtils<T,Integer>{

    public SysResourceNode(List<T> dates) {
        super(dates);
    }
}

package com.lucky.jacklamb.authority.shiro.entity;

import com.lucky.jacklamb.annotation.orm.Column;
import com.lucky.jacklamb.utils.base.LuckyUtils;

import java.sql.Timestamp;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/10/28 10:07
 */
public class SysBase {

    /** 创建时间*/
    @Column("create_time")
    protected Timestamp createTime= LuckyUtils.getTimestamp();;

    /** 最近一次更新时间*/
    @Column("update_time")
    protected Timestamp updateTime= LuckyUtils.getTimestamp();

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}

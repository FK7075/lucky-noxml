package com.lucky.jacklamb.enums.jpa;

/** 用来描述实体间的级联操作关系 */
public enum CascadeType {

    /** Cascade all operations */
    ALL,

    /** Cascade persist operation */
    PERSIST,

    /**
     * 级联更新（合并）操作
     *
     * 当当前实体中的数据改变，与它有映射关系的实体也会跟着被更新。
     */
    MERGE,

    /**
     * 级联删除
     *
     * 删除当前实体时，与它有映射关系的实体也会跟着被删除。
     */
    REMOVE,

    /** Cascade refresh operation */
    REFRESH,

    /**
     * Cascade detach operation
     *
     * @since Java Persistence 2.0
     *
     */
    DETACH
}

package com.lucky.jacklamb.sqlcore.datasource.enums;

import com.lucky.jacklamb.sqlcore.datasource.c3p0.C3p0DataSource;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
import com.lucky.jacklamb.sqlcore.datasource.hikaricp.HikariCPDataSource;
import com.lucky.jacklamb.sqlcore.exception.PoolTypeUnableToIdentifyException;

/**
 * 数据库连接池枚举工厂
 * @author fk7075
 * @version 1.0.0
 * @date 2020/6/15 11:29 下午
 */
public enum Pool {

    C3P0("c3p0"),
    HIKARICP("HikariCP");

    private String strPoolType;

    Pool(String strPoolType) {
        this.strPoolType = strPoolType;
    }

    public LuckyDataSource getDataSource(){
        if("c3p0".equals(strPoolType))
            return new C3p0DataSource();
        return new HikariCPDataSource();
    }

    public static LuckyDataSource getDataSource(String strPoolType){
        if(strPoolType==null)
            return HIKARICP.getDataSource();
        if("c3p0".equalsIgnoreCase(strPoolType))
            return C3P0.getDataSource();
        if("HikariCP".equalsIgnoreCase(strPoolType))
            return HIKARICP.getDataSource();
        throw new PoolTypeUnableToIdentifyException(strPoolType);
    }
}

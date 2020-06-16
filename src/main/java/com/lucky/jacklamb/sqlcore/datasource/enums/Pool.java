package com.lucky.jacklamb.sqlcore.datasource.enums;

import com.lucky.jacklamb.sqlcore.datasource.c3p0.C3p0DataSource;
import com.lucky.jacklamb.sqlcore.datasource.c3p0.C3p0DataSourceManage;
import com.lucky.jacklamb.sqlcore.datasource.factory.DataSourceManage;
import com.lucky.jacklamb.sqlcore.datasource.factory.LuckyDataSource;
import com.lucky.jacklamb.sqlcore.datasource.hikaricp.HikariCPDataSource;
import com.lucky.jacklamb.sqlcore.datasource.hikaricp.HikaroCPDataSourceManage;

/**
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

    public String strPoolType(){
        return this.strPoolType;
    }

    public LuckyDataSource getDataSource(){
        if("c3p0".equals(strPoolType))
            return new C3p0DataSource();
        return new HikariCPDataSource();
    }

    public DataSourceManage getDataSourceManage(){
        if("c3p0".equals(strPoolType))
            return new C3p0DataSourceManage();
        return new HikaroCPDataSourceManage();
    }
}

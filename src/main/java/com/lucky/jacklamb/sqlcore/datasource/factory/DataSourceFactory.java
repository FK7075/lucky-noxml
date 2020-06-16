package com.lucky.jacklamb.sqlcore.datasource.factory;

import com.lucky.jacklamb.sqlcore.datasource.c3p0.C3p0DataSource;
import com.lucky.jacklamb.sqlcore.datasource.hikaricp.HikariCPDataSource;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/6/16 1:10 上午
 */
public abstract class DataSourceFactory {

    public static LuckyDataSource getDataSource(String poolType){
        if("c3p0".equalsIgnoreCase(poolType))
            return new C3p0DataSource();
        else if("HikariCP".equalsIgnoreCase(poolType))
            return new HikariCPDataSource();
        System.out.println("错误的poolType: ["+poolType+"],Lucky将采用默认的数据库连接池！");
        return new HikariCPDataSource();
    }

    public static LuckyDataSource getDefDataSource(){
        return new HikariCPDataSource();
    }
}

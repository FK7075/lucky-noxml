package com.lucky.jacklamb.sqlcore.datasource.hikaricp;

import com.lucky.jacklamb.exception.NoDataSourceException;
import com.lucky.jacklamb.sqlcore.datasource.DataSourceManage;
import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;
import com.lucky.jacklamb.sqlcore.datasource.c3p0.C3p0DataSource;
import com.lucky.jacklamb.sqlcore.datasource.enums.Pool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HikaroCPDataSourceManage extends DataSourceManage {

    private static List<HikariCPDataSource> datalist;
    private static Map<String, HikariDataSource> dbMap;

    static {
        init();
    }

    public static void init() {
        dbMap = new HashMap<>();
        datalist = ReaderInI.getAllDataSource().stream().filter(a-> Pool.HIKARICP==a.getPoolType()).map(a->(HikariCPDataSource)a).collect(Collectors.toList());
        for (HikariCPDataSource data : datalist) {
            HikariConfig hikariCfg=new HikariConfig();
            hikariCfg.setDriverClassName(data.getDriverClass());
            hikariCfg.setUsername(data.getUsername());
            hikariCfg.setJdbcUrl(data.getJdbcUrl());
            hikariCfg.setPassword(data.getPassword());
            hikariCfg.setConnectionTimeout(data.getConnectionTimeout());
            hikariCfg.setMaximumPoolSize(data.getMaximumPoolSize());
            HikariDataSource ds = new HikariDataSource(hikariCfg);
            dbMap.put(data.getDbname(), ds);
        }
    }

    @Override
    public Connection getConnection(String name) {
        Connection connection;
        HikariDataSource ds = null;
        try {
            ds = dbMap.get(name);
            connection = ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NoDataSourceException("错误的数据库连接[databaseURL:" + ds.getJdbcUrl() + "] 或 错误用户名和密码[username:" + ds.getUsername() + "  password=" + ds.getPassword() + "]");
        }
        return connection;
    }
}

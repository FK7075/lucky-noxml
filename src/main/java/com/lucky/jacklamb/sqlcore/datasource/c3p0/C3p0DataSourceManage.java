package com.lucky.jacklamb.sqlcore.datasource.c3p0;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.lucky.jacklamb.exception.NoDataSourceException;
import com.lucky.jacklamb.sqlcore.datasource.DataSourceManage;
import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;
import com.lucky.jacklamb.sqlcore.datasource.enums.Pool;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3p0DataSourceManage extends DataSourceManage {

    private static List<C3p0DataSource> datalist;
    private static Map<String, ComboPooledDataSource> dbMap;

    static {
        if (dbMap == null)
            init();
    }

    public static void init() {
        dbMap = new HashMap<>();
        datalist = ReaderInI.getAllDataSource().stream().filter(a-> Pool.C3P0==a.getPoolType()).map(a->(C3p0DataSource)a).collect(Collectors.toList());
        for (C3p0DataSource data : datalist) {
            ComboPooledDataSource db = new ComboPooledDataSource();
            try {
                db.setDriverClass(data.getDriverClass());
            } catch (PropertyVetoException e) {
                throw new NoDataSourceException("找不到数据库的驱动程序" + data.getDriverClass());
            }
            db.setJdbcUrl(data.getJdbcUrl());
            db.setUser(data.getUsername());
            db.setPassword(data.getPassword());
            db.setAcquireIncrement(data.getAcquireIncrement());
            db.setInitialPoolSize(data.getInitialPoolSize());
            db.setMaxPoolSize(data.getMaxPoolSize());
            db.setMinPoolSize(data.getMinPoolSize());
            db.setMaxIdleTime(data.getMaxidleTime());
            db.setMaxStatements(data.getMaxStatements());
            db.setMaxConnectionAge(data.getMaxConnectionAge());
            db.setCheckoutTimeout(data.getCheckoutTimeout());
            db.setMaxStatementsPerConnection(data.getMaxStatementsPerConnection());
            dbMap.put(data.getDbname(), db);
        }
    }

    @Override
    public  Connection getConnection(String name) {
        Connection connection;
        ComboPooledDataSource comboPooledDataSource = null;
        try {
            comboPooledDataSource = dbMap.get(name);
            connection = comboPooledDataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NoDataSourceException("错误的数据库连接[databaseURL:" + comboPooledDataSource.getJdbcUrl() + "] 或 错误用户名和密码[username:" + comboPooledDataSource.getUser() + "  password=" + comboPooledDataSource.getPassword() + "]");
        }
        return connection;
    }

}

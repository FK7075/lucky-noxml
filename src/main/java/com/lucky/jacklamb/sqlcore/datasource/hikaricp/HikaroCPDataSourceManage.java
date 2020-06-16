package com.lucky.jacklamb.sqlcore.datasource.hikaricp;

import com.lucky.jacklamb.exception.NoDataSourceException;
import com.lucky.jacklamb.sqlcore.datasource.factory.DataSourceManage;
import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;
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
            hikariCfg.setJdbcUrl(data.getJdbcUrl());
            hikariCfg.setUsername(data.getUsername());
            hikariCfg.setPassword(data.getPassword());
            hikariCfg.setAutoCommit(data.getAutoCommit());
            hikariCfg.setConnectionTimeout(data.getConnectionTimeout());
            hikariCfg.setIdleTimeout(data.getIdleTimeout());
            hikariCfg.setMaxLifetime(data.getMaxLifetime());
            hikariCfg.setConnectionTestQuery(data.getConnectionTestQuery());//Object
            hikariCfg.setMinimumIdle(data.getMinimumIdle());
            hikariCfg.setMaximumPoolSize(data.getMaximumPoolSize());
            hikariCfg.setMetricRegistry(data.getMetricRegistry());//Object
            if(data.getHealthCheckRegistry()!=null)
                hikariCfg.setHealthCheckProperties(data.getHealthCheckRegistry());//Object
            if(data.getPoolName()!=null)
                hikariCfg.setPoolName(data.getPoolName());
            hikariCfg.setIsolateInternalQueries(data.getIsolateInternalQueries());
            hikariCfg.setAllowPoolSuspension(data.getAllowPoolSuspension());
            hikariCfg.setReadOnly(data.getReadOnly());
            hikariCfg.setRegisterMbeans(data.getRegisterMbeans());
            hikariCfg.setConnectionInitSql(data.getConnectionInitSql());
            hikariCfg.setLeakDetectionThreshold(data.getLeakDetectionThreshold());
            hikariCfg.setDataSource(data.getDataSource());
            hikariCfg.setSchema(data.getSchema());
            hikariCfg.setThreadFactory(data.getThreadFactory());
            hikariCfg.setScheduledExecutor(data.getScheduledExecutorService());
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

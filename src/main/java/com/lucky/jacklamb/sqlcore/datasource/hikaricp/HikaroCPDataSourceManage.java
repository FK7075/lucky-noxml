package com.lucky.jacklamb.sqlcore.datasource.hikaricp;

import com.lucky.jacklamb.exception.NoDataSourceException;
import com.lucky.jacklamb.sqlcore.datasource.DataSourceManage;
import com.lucky.jacklamb.sqlcore.datasource.ReadIni;
import com.lucky.jacklamb.sqlcore.datasource.c3p0.C3p0DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HikaroCPDataSourceManage extends DataSourceManage {

    private static List<C3p0DataSource> datalist;
    private static Map<String, HikariDataSource> dbMap;
//    private static Map<String,String> hikaroCPDercirClassNames;

    static {
//        try {
//            InputStreamReader reader = new InputStreamReader(ApplicationBeans.class.getResourceAsStream("/config/hikarocp.json"), "UTF-8");
//            Type type=new TypeToken<Map<String,String>>(){}.getType();
//            hikaroCPDercirClassNames=new Gson().fromJson(reader,type);
            init();
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }


    }

    public static void init() {
        dbMap = new HashMap<>();
        datalist = ReadIni.getAllDataSource().stream().filter(a->"HikariCP".equalsIgnoreCase(a.getPoolType())).collect(Collectors.toList());
        for (C3p0DataSource data : datalist) {
            HikariConfig hikariCfg=new HikariConfig();
            hikariCfg.setDriverClassName(data.getDriverClass());
            hikariCfg.setUsername(data.getUser());
            hikariCfg.setJdbcUrl(data.getJdbcUrl());
            hikariCfg.setPassword(data.getPassword());
            hikariCfg.setConnectionTimeout(data.getCheckoutTimeout());
            hikariCfg.setMaximumPoolSize(data.getMaxPoolSize());
            HikariDataSource ds = new HikariDataSource(hikariCfg);
            dbMap.put(data.getName(), ds);
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

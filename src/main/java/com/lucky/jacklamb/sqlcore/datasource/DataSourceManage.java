package com.lucky.jacklamb.sqlcore.datasource;

import com.lucky.jacklamb.sqlcore.datasource.c3p0.C3p0DataSourceManage;
import com.lucky.jacklamb.sqlcore.datasource.hikaricp.HikaroCPDataSourceManage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public abstract class DataSourceManage {

    public abstract Connection getConnection(String name);

    public static void release(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("资源关闭错误！");
        }
    }

    public static DataSourceManage getDataSourceManage(String dbname){
        String poolType=ReadIni.getDataSource(dbname).getPoolType();
        if("c3p0".equalsIgnoreCase(poolType))
            return new C3p0DataSourceManage();
        if("HikariCP".equalsIgnoreCase(poolType))
            return new HikaroCPDataSourceManage();
        throw new RuntimeException("目前不支持该数据库连接池["+poolType+"],请使用c3p0或者HikariCP");
    }
}

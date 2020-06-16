package com.lucky.jacklamb.sqlcore.datasource.abs;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public abstract class DataSourceManage {

    protected static List<? extends LuckyDataSource> datalist;
    protected static Map<String, DataSource> dbMap;

    public abstract Connection getConnection();

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
}

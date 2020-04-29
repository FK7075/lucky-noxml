package com.lucky.jacklamb.sqlcore.c3p0;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lucky.jacklamb.exception.NoDataSourceException;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3p0Util {
	
	private static List<DataSource> datalist;
	private static Map<String,ComboPooledDataSource> dbMap;

	static {
		if(dbMap==null)
			init();
	}
	
	public static void init() {
		dbMap=new HashMap<>();
		datalist=ReadIni.getAllDataSource();
		for(DataSource data:datalist) {
			ComboPooledDataSource db=new ComboPooledDataSource();
			try {
				db.setDriverClass(data.getDriverClass());
			} catch (PropertyVetoException e) {
				throw new NoDataSourceException("找不到数据库的驱动程序"+data.getDriverClass());
			}
			db.setJdbcUrl(data.getJdbcUrl());
			db.setUser(data.getUser());
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
			dbMap.put(data.getName(), db);
		}
	}
	
	public static Connection getConnecion(String name){
		Connection connection;
		ComboPooledDataSource comboPooledDataSource = null;
		try {
			comboPooledDataSource = dbMap.get(name);
			connection = comboPooledDataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new NoDataSourceException("错误的数据库连接[databaseURL:"+comboPooledDataSource.getJdbcUrl()+"] 或 错误用户名和密码[username:"+comboPooledDataSource.getUser()+"  password="+comboPooledDataSource.getPassword()+"]");
		}
		return connection;
	}
	
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

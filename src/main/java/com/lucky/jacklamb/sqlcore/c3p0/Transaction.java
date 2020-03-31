 package com.lucky.jacklamb.sqlcore.c3p0;

import java.sql.Connection;
import java.sql.SQLException;
/**
 * Lucky的事务处理类
 * @author fk-7075
 *
 */
public class Transaction {
	private Connection conn;
	public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}
	
	//提交事务
	public void commit() {
		try {
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//事务回滚
	public void rollback() {
		try {
			conn.rollback();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

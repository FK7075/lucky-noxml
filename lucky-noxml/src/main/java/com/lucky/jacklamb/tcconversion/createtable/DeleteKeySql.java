package com.lucky.jacklamb.tcconversion.createtable;

import com.lucky.jacklamb.sqlcore.util.PojoManage;
import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
import com.lucky.jacklamb.sqlcore.jdbc.core.SqlOperation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeleteKeySql {

	private String dbname;
	private SqlOperation sqlop;
	private String databasename;
	private List<String> delkeysql = new ArrayList<>();
	private List<Class<?>> classlist;

	public String getDatabasename() {
		return databasename;
	}

	public void setDatabasename(String databasename) {
		this.databasename = databasename;
	}

	public List<String> getDelkeysql() {
		return delkeysql;
	}

	public void setDelkeysql(List<String> delkeysql) {
		this.delkeysql = delkeysql;
	}

	/**
	 * 建表时删除
	 */
	public List<String> deleteKeySql() {
		List<String> deleteKeySQL=new ArrayList<>();
		for (Class<?> clazz : classlist) {
			try {
				String table = PojoManage.getTable(clazz,dbname);
				String sql = "SHOW CREATE TABLE " + table;
				ResultSet rs = sqlop.getResultSet(sql);
				List<String> keyList=new ArrayList<>();
				if (rs != null) {
					while (rs.next()) {
						String info = rs.getString(2);
						while (info.contains("CONSTRAINT")) {
							int index = info.indexOf("CONSTRAINT");
							int end = info.indexOf("FOREIGN");
							keyList.add(info.substring(index + 12, end - 2));
							info = info.replaceFirst("CONSTRAINT", "");
							info = info.replaceFirst("FOREIGN", "");
						}
					}
				}
				LuckyDataSource.release(rs,null,null);
				for (String wkey : keyList) {
					String sqlStr = "ALTER TABLE " + table + " DROP FOREIGN KEY " + wkey;
					deleteKeySQL.add(sqlStr);
				}
			} catch (SQLException e) {

			}
		}
		return deleteKeySQL;
	}

	/**
	 * 得到数据库的名字和删除数据库所有表外键的sql语句集合并封装到属性中
	 */
	public DeleteKeySql(String dbname,List<Class<?>> classlist) {
		sqlop = new SqlOperation(ReaderInI.getDataSource(dbname).getConnection(),dbname);
		this.dbname=dbname;
		this.classlist = classlist;
	}

	/**
	 * 删除所有外键
	 */
	public void deleteKey1() {
		for (String sql : this.delkeysql) {
			sqlop.setSql(sql);
		}
	}

}
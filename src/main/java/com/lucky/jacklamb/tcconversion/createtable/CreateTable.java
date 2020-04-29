package com.lucky.jacklamb.tcconversion.createtable;

import com.lucky.jacklamb.sqlcore.c3p0.ReadIni;
import com.lucky.jacklamb.sqlcore.c3p0.SqlOperation;

import java.util.List;

public class CreateTable {
	private SqlOperation sqlop;
	private List<Class<?>> classlist;
	private String dbname;
	
	public CreateTable(String dbname) {
		this.dbname=dbname;
		classlist=ReadIni.getDataSource(dbname).getCaeateTable();
		sqlop = new SqlOperation();
	}

	public void creatTable() {
		DeleteKeySql dtlkeysql = new DeleteKeySql(dbname,classlist);
			for (Class<?> str : classlist) {
				String sql = CreateTableSql.getCreateTable(dbname,str);
				sqlop.setSql(dbname,sql);
			}
			dtlkeysql.deleteKey();// 删除所有现有的外键
			for (Class<?> str : classlist) {
				List<String> sqls = CreateTableSql.getForeignKey(str);
				if (sqls != null) {
					for (String st : sqls) {
						sqlop.setSql(dbname,st);
					}
				}
				List<String> indexKey = CreateTableSql.getIndexKey(str);
				for(String index:indexKey) {
					sqlop.setSql(dbname,index);
				}
			}
	}
}

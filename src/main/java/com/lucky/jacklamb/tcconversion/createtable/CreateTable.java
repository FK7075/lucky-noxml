package com.lucky.jacklamb.tcconversion.createtable;

import java.util.List;

import com.lucky.jacklamb.sqlcore.c3p0.ReadIni;
import com.lucky.jacklamb.sqlcore.c3p0.SqlOperation;
import com.lucky.jacklamb.utils.LuckyManager;

public class CreateTable {
	private SqlOperation sqlop;
	private List<Class<?>> classlist;
	private String dbname;
	
	public CreateTable(String dbname) {
		this.dbname=dbname;
		classlist=ReadIni.getDataSource(dbname).getCaeateTable();
		sqlop = LuckyManager.getSqlOperation(dbname);
	}

	public void creatTable() {
		DeleteKeySql dtlkeysql = new DeleteKeySql(dbname,classlist);
			for (Class<?> str : classlist) {
				String sql = CreateTableSql.getCreateTable(dbname,str);
				sqlop.setSql(sql);
			}
			dtlkeysql.deleteKey();// 删除所有现有的外键
			for (Class<?> str : classlist) {
				List<String> sqls = CreateTableSql.getForeignKey(str);
				if (sqls != null) {
					for (String st : sqls) {
						sqlop.setSql(st);
					}
				}
				List<String> indexKey = CreateTableSql.getIndexKey(str);
				for(String index:indexKey) {
					sqlop.setSql(index);
				}
			}
	}
}

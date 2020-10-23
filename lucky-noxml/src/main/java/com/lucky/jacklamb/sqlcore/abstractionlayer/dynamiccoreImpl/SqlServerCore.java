package com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl;

import java.util.Collection;
import java.util.List;

import com.lucky.jacklamb.query.QueryBuilder;
import com.lucky.jacklamb.sqlcore.createtable.CreateTableSqlGenerate;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlGroup;

public final class SqlServerCore extends SqlCore {

	public SqlServerCore(String dbname) {
		super(dbname);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SqlGroup getSqlGroup() {
		return null;
	}

	@Override
	public void createJavaBean() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createJavaBean(String srcPath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createJavaBeanByTable(String... tables) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createJavaBeanSrc(String srcPath, String... tables) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CreateTableSqlGenerate getCreateTableSqlGenerate() {
		return null;
	}

	@Override
	public <T> List<T> query(QueryBuilder queryBuilder, Class<T> resultClass, String... expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int insertByCollection(Collection<T> collection) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNextId(Object pojo) {
		// TODO Auto-generated method stub
		
	}


}

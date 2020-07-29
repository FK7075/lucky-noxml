package com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl;

import java.util.Collection;
import java.util.List;

import com.lucky.jacklamb.query.QueryBuilder;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;

public final class AccessSqlCore extends SqlCore {

	public AccessSqlCore(String dbname) {
		super(dbname);
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
	public void createTable() {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> List<T> getPageList(T t, int index, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> query(QueryBuilder queryBuilder, Class<T> resultClass, String... expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int insertBatchByCollection(Collection<T> collection) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNextId(Object pojo) {
		// TODO Auto-generated method stub
		
	}

}

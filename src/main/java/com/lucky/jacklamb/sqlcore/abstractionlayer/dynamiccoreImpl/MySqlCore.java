package com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import com.lucky.jacklamb.query.ObjectToJoinSql;
import com.lucky.jacklamb.query.QueryBuilder;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlGroup;
import com.lucky.jacklamb.sqlcore.util.BatchInsert;
import com.lucky.jacklamb.sqlcore.util.PojoManage;
import com.lucky.jacklamb.tcconversion.createtable.CreateTable;
import com.lucky.jacklamb.tcconversion.reverse.TableToJava;
import com.lucky.jacklamb.utils.reflect.FieldUtils;

@SuppressWarnings("unchecked")
public final class MySqlCore extends SqlCore {
	
	
	private TableToJava tableToJava;
	
	public MySqlCore(String dbname) {
		super(dbname);
		tableToJava=new TableToJava(dbname);
	}

	@Override
	public SqlGroup getSqlGroup() {
		return new MySqlGroup();
	}

	@Override
	public void createJavaBean() {
		tableToJava.generateJavaSrc();
	}

	@Override
	public void createJavaBean(String srcPath) {
		tableToJava.generateJavaSrc(srcPath);
	}

	@Override
	public void createJavaBeanByTable(String... tables) {
		tableToJava.b_generateJavaSrc(tables);
	}

	@Override
	public void createJavaBeanSrc(String srcPath, String... tables) {
		tableToJava.a_generateJavaSrc(srcPath, tables);
		
	}

	@Override
	public void createTable() {
		CreateTable ct = new CreateTable(dbname);
		ct.createTable();
	}

	@Override
	public void createTable(Class<?> tableClass) {
		CreateTable ct = new CreateTable(dbname);
		ct.createTable(tableClass);
	}

	@Override
	public <T> List<T> query(QueryBuilder queryBuilder, Class<T> resultClass, String... expression) {
		queryBuilder.setDbname(getDbName());
		queryBuilder.setWheresql(new MySqlGroup());
		ObjectToJoinSql join = new ObjectToJoinSql(queryBuilder);
		String sql = join.getJoinSql(expression);
		Object[] obj = join.getJoinObject();
		return getList(resultClass, sql, obj);
	}

	@Override
	public <T> int insertByCollection(Collection<T> collection) {
		if(collection.isEmpty())
			return -1;
		setUUID(collection);
		BatchInsert bbi=new BatchInsert(collection,dbname);
		return statementCore.update(bbi.getInsertSql(), bbi.getInsertObject());
	}

	/**
	 * 设置自增主键
	 * @param pojo
	 */
	@Override
	public void setNextId(Object pojo) {
		Class<?> pojoClass=pojo.getClass();
		String sql="SELECT auto_increment FROM information_schema.`TABLES` WHERE TABLE_SCHEMA=? AND table_name=?";
		int nextid= statementCore.getObject(int.class, sql, PojoManage.getDatabaseName(dbname),PojoManage.getTable(pojoClass,getDbName()))-1;
		Field idf=PojoManage.getIdField(pojoClass);
		FieldUtils.setValue(pojo,idf,nextid);
	}
}

class MySqlGroup extends SqlGroup{

	@Override
	public String sqlGroup(String res, String onsql, String andsql, String like, String sort) {
		if(!andsql.contains("WHERE")&&!"".equals(like)) {
			like=" WHERE "+like;
		}
		if(page==null&&rows==null) {
			return "SELECT "+res+" FROM " + onsql + andsql+like+sort;
		}else {
			return "SELECT "+res+" FROM " + onsql + andsql+like+sort+" LIMIT "+(page-1)*rows+","+rows;
		}
	}
	
}


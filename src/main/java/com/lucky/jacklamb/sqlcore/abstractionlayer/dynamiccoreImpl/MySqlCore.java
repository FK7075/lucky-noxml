package com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.lucky.jacklamb.query.ObjectToJoinSql;
import com.lucky.jacklamb.query.QueryBuilder;
import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.SqlGroup;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.BatchInsert;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PojoManage;
import com.lucky.jacklamb.tcconversion.createtable.CreateTable;
import com.lucky.jacklamb.tcconversion.reverse.TableToJava;

@SuppressWarnings("unchecked")
public final class MySqlCore extends SqlCore {
	
	
	private TableToJava tableToJava;
	
	public MySqlCore(String dbname) {
		super(dbname);
		tableToJava=new TableToJava(dbname);
	}

	@Override
	public void createJavaBean() {
		log.debug("Run ==> createJavaBean()");
		tableToJava.generateJavaSrc();
	}

	@Override
	public void createJavaBean(String srcPath) {
		log.debug("Run ==> createJavaBean([String]"+srcPath+")");
		tableToJava.generateJavaSrc(srcPath);
	}

	@Override
	public void createJavaBean(String... tables) {
		log.debug("Run ==> createJavaBean([String...]"+Arrays.toString(tables)+")");
		tableToJava.b_generateJavaSrc(tables);
	}

	@Override
	public void createJavaBean(String srcPath, String... tables) {
		log.debug("Run ==> createJavaBean([String]"+srcPath+",[String...]"+Arrays.toString(tables)+")");
		tableToJava.a_generateJavaSrc(srcPath, tables);
		
	}

	@Override
	public void createTable() {
		log.debug("Run ==> createTable()");
		CreateTable ct = new CreateTable(dbname);
		ct.creatTable();
		
	}

	@Override
	public <T> List<T> getPageList(T t, int page, int size) {
		log.debug("Run ==> getPageList([T]"+t+",[int]"+page+",[int]"+size+")");
		QueryBuilder queryBuilder=new QueryBuilder();
		queryBuilder.limit(page,size);
		queryBuilder.setWheresql(new MySqlGroup());
		queryBuilder.addObject(t);
		return (List<T>) query(queryBuilder,t.getClass());
	}

	@Override
	public <T> List<T> query(QueryBuilder queryBuilder, Class<T> resultClass, String... expression) {
		log.debug("Run ==> query([QueryBuilder]"+queryBuilder+",[Class<T>]"+resultClass+",[String...]"+Arrays.toString(expression)+")");
		queryBuilder.setWheresql(new MySqlGroup());
		ObjectToJoinSql join = new ObjectToJoinSql(queryBuilder);
		String sql = join.getJoinSql(expression);
		Object[] obj = join.getJoinObject();
		return getList(resultClass, sql, obj);
	}

	@Override
	public <T> boolean insertBatchByCollection(Collection<T> list) {
		log.debug("Run ==> insertBatchByCollection([Collection<T>]"+list+")");
		BatchInsert bbi=new BatchInsert(list);
		return statementCore.update(bbi.getInsertSql(), bbi.getInsertObject());
	}

	/**
	 * 设置自增主键
	 * @param pojo
	 */
	@Override
	public void setNextId(Object pojo) {
		log.debug("Run ==> setNextId([Object]"+pojo+")");
		Class<?> pojoClass=pojo.getClass();
		String sql="SELECT auto_increment FROM information_schema.`TABLES` WHERE TABLE_SCHEMA=? AND table_name=?";
		int nextid= statementCore.getObject(int.class, sql, PojoManage.getDatabaseName(dbname),PojoManage.getTable(pojoClass))-1;
		Field idf=PojoManage.getIdField(pojoClass);
		idf.setAccessible(true);
		try {
			idf.set(pojo, nextid);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
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


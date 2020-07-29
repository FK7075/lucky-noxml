package com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import com.lucky.jacklamb.query.ObjectToJoinSql;
import com.lucky.jacklamb.query.QueryBuilder;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlGroup;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.BatchInsert;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PojoManage;

@SuppressWarnings("unchecked")
public final class PostgreSqlCore extends SqlCore {

	public PostgreSqlCore(String dbname) {
		super(dbname);
		// TODO Auto-generated constructor stub
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
	public <T> List<T> getPageList(T t, int page, int size) {
		QueryBuilder queryBuilder=new QueryBuilder();
		queryBuilder.limit(page,size);
		queryBuilder.setWheresql(new PostgreSqlGroup());
		queryBuilder.addObject(t);
		return (List<T>) query(queryBuilder,t.getClass());
	}

	@Override
	public <T> List<T> query(QueryBuilder queryBuilder, Class<T> resultClass, String... expression) {
		queryBuilder.setWheresql(new PostgreSqlGroup());
		ObjectToJoinSql join = new ObjectToJoinSql(queryBuilder);
		String sql = join.getJoinSql(expression);
		Object[] obj = join.getJoinObject();
		return getList(resultClass, sql, obj);
	}

	@Override
	public <T> boolean insertBatchByCollection(Collection<T> collection) {
		setUUID(collection);
		BatchInsert bbi=new BatchInsert(collection);
		return statementCore.update(bbi.getInsertSql(), bbi.getInsertObject());
	}

	@Override
	public void setNextId(Object pojo) {
		Class<?> pojoClass=pojo.getClass();
		String sql="SELECT last_value FROM "+PojoManage.getTable(pojoClass)+"_"+PojoManage.getIdString(pojoClass)+"_seq";
		int nextid= statementCore.getObject(int.class, sql);
		Field idf=PojoManage.getIdField(pojoClass);
		idf.setAccessible(true);
		try {
			idf.set(pojo, nextid);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

class PostgreSqlGroup extends SqlGroup{

	@Override
	public String sqlGroup(String res, String onsql, String andsql, String like, String sort) {
		if(!andsql.contains("WHERE")&&!"".equals(like)) {
			like=" WHERE "+like;
		}
		if(page==null&&rows==null) {
			return "SELECT "+res+" FROM " + onsql + andsql+like+sort;
		}else {
			return "SELECT "+res+" FROM " + onsql + andsql+like+sort+" LIMIT "+rows+" OFFSET "+(page-1)*rows;
		}
	}
	
}
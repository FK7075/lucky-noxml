package com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl;

import java.util.Collection;
import java.util.List;

import com.lucky.jacklamb.query.ObjectToJoinSql;
import com.lucky.jacklamb.query.QueryBuilder;
import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.SqlGroup;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.BatchInsert;

@SuppressWarnings("unchecked")
public final class OracleCore extends SqlCore {

	public OracleCore(String dbname) {
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
	public void createJavaBean(String... tables) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createJavaBean(String srcPath, String... tables) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createTable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> List<T> getPageList(T t, int page, int rows) {
		QueryBuilder queryBuilder=new QueryBuilder();
		queryBuilder.setWheresql(new OracleSqlGroup());
		queryBuilder.addObject(t);
		queryBuilder.limit(page, rows);
		return (List<T>) query(queryBuilder,t.getClass());
	}

	@Override
	public <T> List<T> query(QueryBuilder queryBuilder, Class<T> resultClass, String... expression) {
		queryBuilder.setWheresql(new OracleSqlGroup());
		ObjectToJoinSql join = new ObjectToJoinSql(queryBuilder);
		String sql = join.getJoinSql(expression);
		Object[] obj = join.getJoinObject();
		return getList(resultClass, sql, obj);
	}

	@Override
	public <T> boolean insertBatchByCollection(Collection<T> collection) {
		BatchInsert bbi=new BatchInsert(collection);
		return statementCore.update(bbi.OrcaleInsetSql(), bbi.getInsertObject());
	}

	@Override
	public void setNextId(Object pojo) {
		// TODO Auto-generated method stub
		
	}


}

class OracleSqlGroup extends SqlGroup{

	@Override
	public String sqlGroup(String res, String onsql, String andsql, String like, String sort) {
		if(!andsql.contains("WHERE")&&!"".equals(like)) {
			like=" WHERE "+like;
		}
		if(page==null&&rows==null) {
			return "SELECT "+res+" FROM " + onsql + andsql+like+sort;
		}else {
			int start=(page-1)*rows;
			int end=start+rows-1;
			return " SELECT * FROM (SELECT lucy.*,ROWNUM jack FROM (SELECT "+res+" FROM " + onsql + andsql+like+sort+") lucy WHERE ROWNUM<="+end+") WHERE jack>="+start;
		}
	}
	
}

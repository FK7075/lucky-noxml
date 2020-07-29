package com.lucky.jacklamb.sqlcore.jdbc.core;

import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;

/**
 * 结果集自动包装类
 *
 * @author fk-7075
 *
 */

public class DefaultSqlActuator extends SqlActuator{

	private LuckyDataSource dataSource;

	public DefaultSqlActuator(String dbname) {
		super(dbname);
	}


	@Override
	public <T> List<T> autoPackageToList(Class<T> c, String sql, Object... obj) {
		SqlAndParams sp=new SqlAndParams(sql,obj);
		if(isCache) {
			return queryCache(sp,c);
		}
		Connection connection = dataSource.getConnection();
		SqlOperation sqlOperation=new SqlOperation(connection,dataSource.getDbname());
		List<T> result = sqlOperation.autoPackageToList(c, sp.precompileSql, sp.params);
		LuckyDataSource.release(null,null,connection);
		return result;
	}

	@Override
	public int update(String sql, Object...obj) {
		SqlAndParams sp=new SqlAndParams(sql,obj);
		if(isCache) {
			clear();
		}
		Connection connection = dataSource.getConnection();
		SqlOperation sqlOperation=new SqlOperation(connection,dbname);
		int result = sqlOperation.setSql(sp.precompileSql, sp.params);
		LuckyDataSource.release(null,null,connection);
		return result;
	}

	@Override
	public <T> List<T> autoPackageToListMethod(Class<T> c, Method method,String sql, Object[] obj) {
		SqlAndParams sp=new SqlAndParams(method,sql,obj);
		if(isCache) {
			return queryCache(sp,c);
		}
		Connection connection = dataSource.getConnection();
		SqlOperation sqlOperation=new SqlOperation(connection,dbname);
		List<T> result = sqlOperation.autoPackageToList(c, sp.precompileSql, sp.params);
		LuckyDataSource.release(null,null,connection);
		return result;
	}

	@Override
	public int updateMethod(Method method, String sql, Object[]obj) {
		SqlAndParams sp=new SqlAndParams(method,sql,obj);
		if(isCache)
			clear();
		Connection connection = dataSource.getConnection();
		SqlOperation sqlOperation=new SqlOperation(connection,dbname);
		int result = sqlOperation.setSql(sp.precompileSql, sp.params);
		LuckyDataSource.release(null,null,connection);
		return result;
	}

	@Override
	public int[] updateBatch(String sql,Object[][] obj) {
		if(isCache) {
			clear();
		}
		Connection connection = dataSource.getConnection();
		SqlOperation sqlOperation=new SqlOperation(connection,dbname);
		int[] result = sqlOperation.setSqlBatch(sql, obj);
		LuckyDataSource.release(null,null,connection);
		return result;
	}

	@Override
	public int[] updateBatch(String...completeSqls){
		if(isCache) {
			clear();
		}
		Connection connection = dataSource.getConnection();
		SqlOperation sqlOperation=new SqlOperation(connection,dbname);
		int[] result = sqlOperation.setSqlBatch(completeSqls);
		LuckyDataSource.release(null,null,connection);
		return result;
	}

}

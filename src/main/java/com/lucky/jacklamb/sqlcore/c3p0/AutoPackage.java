package com.lucky.jacklamb.sqlcore.c3p0;

import com.lucky.jacklamb.conversion.util.MethodUtils;
import com.lucky.jacklamb.servlet.mapping.regula.Regular;
import com.lucky.jacklamb.sqlcore.abstractionlayer.cache.LuckyLRUCache;
import com.lucky.jacklamb.sqlcore.abstractionlayer.exception.LuckySqlGrammarMistakesException;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.CreateSql;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 结果集自动包装类
 *
 * @author fk-7075
 *
 */
public class AutoPackage {

	private String dbname;

	private boolean isCache;

	private LuckyLRUCache<String,List<?>> lruCache;

	private SqlOperation sqlOperation;

	public AutoPackage(String dbname) {
		this.dbname=dbname;
		isCache=ReadIni.getDataSource(dbname).isCache();
		//如果用户开启了缓存配置，测初始化一个LRU缓存
		if(isCache)
			lruCache=new LuckyLRUCache<>(ReadIni.getDataSource(dbname).getCacheCapacity());
		sqlOperation=new SqlOperation();
		//保证dbname的数据库连接池能提前创建，避免第一次执行数据库操作时才创建连接池
		C3p0Util.release(null,null,C3p0Util.getConnecion(dbname));
	}

	/**
	 * 自动将结果集中的内容封装起来
	 *
	 * @param c 封装类的Class对象
	 * @param sql 预编译的sql语句
	 * @param obj 替换占位符的数组
	 * @return 返回一个泛型的List集合
	 */
	public <T> List<T> autoPackageToList(Class<T> c, String sql, Object... obj) {
		SqlAndParams sp=new SqlAndParams(sql,obj);
		if(isCache)
			return queryCache(sp,c);
		return sqlOperation.autoPackageToList(dbname, c, sp.precompileSql, sp.params);
	}

	public boolean update(String sql, Object...obj) {
		SqlAndParams sp=new SqlAndParams(sql,obj);
		boolean result = sqlOperation.setSql(dbname, sp.precompileSql, sp.params);
		if(isCache)
			clear();
		return result;
	}

	public <T> List<T> autoPackageToListMethod(Class<T> c, Method method,String sql, Object[] obj) {
		SqlAndParams sp=new SqlAndParams(method,sql,obj);
		if(isCache)
			return queryCache(sp,c);
		return sqlOperation.autoPackageToList(dbname, c, sp.precompileSql, sp.params);
	}

	public boolean updateMethod(Method method, String sql, Object[]obj) {
		SqlAndParams sp=new SqlAndParams(method,sql,obj);
		boolean result = sqlOperation.setSql(dbname, sp.precompileSql, sp.params);
		if(isCache)
			clear();
		return result;
	}

	public boolean updateBatch(String sql,Object[][] obj) {
		if(isCache)
			clear();
		return sqlOperation.setSqlBatch(dbname,sql, obj);
	}

	public boolean updateBatch(String...completeSqls){
		if(isCache)
			clear();
		return sqlOperation.setSqlBatch(dbname,completeSqls);
	}

	/**
	 * 从LRU缓存中查询结果
	 * @param sp
	 * @param c
	 * @param <T>
	 * @return
	 */
	public <T> List<T> queryCache(SqlAndParams sp,Class<T> c){
		String completeSql= CreateSql.getCompleteSql(sp.precompileSql,sp.params);
		if(lruCache.containsKey(completeSql)){
			return (List<T>) lruCache.get(completeSql);
		}else{
			List<?> result = sqlOperation.autoPackageToList(dbname, c, sp.precompileSql, sp.params);
			lruCache.put(completeSql,result);
			return (List<T>) result;
		}
	}

	public void clear(){
		lruCache.clear();
	}

}

class SqlAndParams{

	String precompileSql;

	Object[] params;

	public SqlAndParams(String haveNumSql,Object[] params){
		numSql(haveNumSql,params);
	}

	public SqlAndParams(Method method, String haveNumSql, Object[] params){
		List<String> names = Regular.getArrayByExpression(haveNumSql, Regular.$SQL);
		if(names.isEmpty()){
			numSql(haveNumSql,params);
		}else{
			nameSQl(method,names,haveNumSql,params);
		}
	}

	private void nameSQl(Method method,List<String> names,String haveNumSql, Object[] params){
		precompileSql=haveNumSql.replaceAll(Regular.$SQL,"?");
		this.params=validation(method,names,params,haveNumSql);
	}


	private void numSql(String haveNumSql,Object[] params){
		List<String> nums = Regular.getArrayByExpression(haveNumSql, Regular.NUMSQL);
		if(nums.isEmpty()){
			precompileSql=haveNumSql;
			this.params=params;
		}else{
			precompileSql=haveNumSql.replaceAll(Regular.NUMSQL,"?");
			this.params=validation(nums,params,haveNumSql);
		}
	}

	private Object[] validation(List<String> nums,Object[] params,String haveNumSql){
		int index;
		int size=nums.size();
		Object[] targetParams=new Object[size];
		for (int i=0;i<size;i++) {
			index=Integer.parseInt(nums.get(i).substring(1));
			if(0<index&&index<=params.length){
				targetParams[i]=params[index-1];
			}else{
				throw new LuckySqlGrammarMistakesException(haveNumSql,index);
			}
		}
		return targetParams;
	}

	private Object[] validation(Method method,List<String> names,Object[] params,String haveNumSql){
		try {
			Map<String,Object> methodParamsNV= MethodUtils.getMethodParamsNV(method,params);
			int size=names.size();
			String name;
			Object[] targetParams=new Object[size];
			for (int i = 0; i < size; i++) {
				name=names.get(i).substring(1);
				if(methodParamsNV.containsKey(name)){
					targetParams[i]=methodParamsNV.get(name);
				}else {
					throw new LuckySqlGrammarMistakesException(method,haveNumSql,name);
				}
			}
			return targetParams;
		} catch (IOException e) {
			throw new LuckySqlGrammarMistakesException(method,e);
		}
	}
}

package com.lucky.jacklamb.sqlcore.datasource.core;

import com.lucky.jacklamb.utils.reflect.MethodUtils;
import com.lucky.jacklamb.servlet.mapping.regula.Regular;
import com.lucky.jacklamb.sqlcore.abstractionlayer.cache.LRUCache;
import com.lucky.jacklamb.sqlcore.abstractionlayer.exception.LuckySqlGrammarMistakesException;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.CreateSql;
import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 结果集自动包装类
 *
 * @author fk-7075
 *
 */

@SuppressWarnings("all")
public class AutoPackage {

	private LuckyDataSource dataSource;

	private boolean isCache;

	private LRUCache<String,List<?>> lruCache;

	private SqlOperation sqlOperation;

	public AutoPackage(String dbname) {
		this.dataSource=ReaderInI.getDataSource(dbname);
		isCache= ReaderInI.getDataSource(dbname).getCache();
		//如果用户开启了缓存配置，测初始化一个LRU缓存
		if(isCache)
			lruCache=new LRUCache<>(ReaderInI.getDataSource(dbname).getCacheCapacity());
		sqlOperation=new SqlOperation();
		//初始化数据源
		dataSource.init();
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
		return sqlOperation.autoPackageToList(dataSource, c, sp.precompileSql, sp.params);
	}

	public boolean update(String sql, Object...obj) {
		SqlAndParams sp=new SqlAndParams(sql,obj);
		boolean result = sqlOperation.setSql(dataSource, sp.precompileSql, sp.params);
		if(isCache)
			clear();
		return result;
	}

	public <T> List<T> autoPackageToListMethod(Class<T> c, Method method,String sql, Object[] obj) {
		SqlAndParams sp=new SqlAndParams(method,sql,obj);
		if(isCache)
			return queryCache(sp,c);
		return sqlOperation.autoPackageToList(dataSource, c, sp.precompileSql, sp.params);
	}

	public boolean updateMethod(Method method, String sql, Object[]obj) {
		SqlAndParams sp=new SqlAndParams(method,sql,obj);
		boolean result = sqlOperation.setSql(dataSource, sp.precompileSql, sp.params);
		if(isCache)
			clear();
		return result;
	}

	public boolean updateBatch(String sql,Object[][] obj) {
		if(isCache)
			clear();
		return sqlOperation.setSqlBatch(dataSource,sql, obj);
	}

	public boolean updateBatch(String...completeSqls){
		if(isCache)
			clear();
		return sqlOperation.setSqlBatch(dataSource,completeSqls);
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
			List<?> result = sqlOperation.autoPackageToList(dataSource, c, sp.precompileSql, sp.params);
			lruCache.put(completeSql,result);
			return (List<T>) result;
		}
	}

	public void clear(){
		lruCache.clear();
	}

}

class SqlAndParams{

	private final String S ="?s";
	private final String E ="?e";
	private final String C ="?c";
	private final String In="?C";

	String precompileSql;

	Object[] params;

	public SqlAndParams(String haveNumSql,Object[] params){
		numSql(haveNumSql,params);
		finalProce();
	}

	public SqlAndParams(Method method, String haveNumSql, Object[] params){
		List<String> names = Regular.getArrayByExpression(haveNumSql, Regular.$SQL);
		if(names.isEmpty()){
			numSql(haveNumSql,params);
		}else{
			nameSQl(method,names,haveNumSql,params);
		}
		finalProce();
	}

	public void finalProce(){
		if(precompileSql.contains(In)||precompileSql.contains(S)||
			precompileSql.contains(E)||precompileSql.contains(C)){
			List<Integer> indexs=new ArrayList<>();
			getIndexParamMap(precompileSql,indexs,"?","@");
			Map<Integer,Integer> indexMap=new HashMap<>();
			for (int i = 0; i < indexs.size(); i++) {
				indexMap.put(indexs.get(i),i);
			}
			dealithW_esc(indexMap,E);dealithW_esc(indexMap,S);dealithW_esc(indexMap,C);
		}
	}

	public void dealithW_esc(Map<Integer,Integer>indexMap,String target){
		List<Integer> escIndex=new ArrayList<>();
		getIndexParamMap(precompileSql,escIndex,target,"@X");
		precompileSql=precompileSql.replaceAll("\\\\"+target,"?");
		for (Integer index : escIndex) {
			int idx=indexMap.get(index);
			if(S.equals(target)){
				params[idx]=params[idx]+"%";
			}else if(E.equals(target)){
				params[idx]="%"+params[idx];
			}else if(C.equals(target)){
				params[idx]="%"+params[idx]+"%";
			}
		}
	}

	public void dealithW_C(){

	}

	//得到每个？的位置
	public void getIndexParamMap(String sql,List<Integer> indexs,String target,String replace){
		if(!sql.contains(target)){
			return;
		}
		String sqlCopy=sql;
		indexs.add(sqlCopy.indexOf(target));
		if("?".equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?",replace);
		}else if(S.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?s",replace);
		}else if(E.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?e",replace);
		}else if(C.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?c",replace);
		}else if(S.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?C",replace);
		}else{
			throw new RuntimeException("错误的参数："+target+",正确的参数为[?,?s,?e,?c,?C]");
		}

		getIndexParamMap(sqlCopy,indexs,target,replace);
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
				name=names.get(i).substring(2);
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

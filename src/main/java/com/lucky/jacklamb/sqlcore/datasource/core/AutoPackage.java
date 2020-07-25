package com.lucky.jacklamb.sqlcore.datasource.core;

import com.lucky.jacklamb.utils.reflect.MethodUtils;
import com.lucky.jacklamb.utils.regula.Regular;
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

	public SqlAndParams(){};

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

	/*最后处理，处理预编译Sql中的特殊参数[?s,?e,?c,?C]
	    ---------------------------------------------------------------------------
	    ?e  : EndingWith    ->LIKE %param      以param结尾
	        SELECT * FROM table WHERE name LIKE ?e       Params["Jack"]
	                             |
	                             V
	        SELECT * FROM table WHERE name LIKE ?        Params["%Jack"]
        ----------------------------------------------------------------------------
	    ?s  : StartingWith  ->LIKE param%      以param开头
	    ?c  : Containing    ->LIKE %param%     包含param
	    -----------------------------------------------------------------------------
	    ?C  : In            ->In [Collection]  条件范围为Collection集合
	        SELECT * FROM table WHERE age IN ?C           Params[Collection[1,2,3,4]]
	        	                 |
	                             V
	        SELECT * FROM table WHERE age IN (?,?,?,?)    Params[1,2,3,4]
	    -----------------------------------------------------------------------------
	 */
	public void finalProce(){
		if(precompileSql.contains(In)||precompileSql.contains(S)||
			precompileSql.contains(E)||precompileSql.contains(C)){
			List<Integer> indexs=new ArrayList<>();
			setQuestionMarkIndex(precompileSql,indexs,"?");
			Map<Integer,Integer> indexMap=new HashMap<>();
			for (int i = 0; i < indexs.size(); i++) {
				indexMap.put(indexs.get(i),i);
			}
			dealithW_s(indexMap);dealithW_e(indexMap);dealithW_c(indexMap);
			precompileSql=precompileSql.replaceAll("\\?l","?");
			dealithW_C(indexMap);
		}
	}

	//处理?s,同dealithW_c
	public void dealithW_s(Map<Integer,Integer>indexMap){
		List<Integer> escIndex=new ArrayList<>();
		setQuestionMarkIndex(precompileSql,escIndex,S);
		precompileSql=precompileSql.replaceAll("\\?s","?l");
		for (Integer index : escIndex) {
			int idx=indexMap.get(index);
			params[idx]=params[idx]+"%";
		}
	}

    //处理?e,同dealithW_s
	public void dealithW_e(Map<Integer,Integer>indexMap){
		List<Integer> escIndex=new ArrayList<>();
		setQuestionMarkIndex(precompileSql,escIndex,E);
		precompileSql=precompileSql.replaceAll("\\?e","?l");
		for (Integer index : escIndex) {
			int idx=indexMap.get(index);
			params[idx]="%"+params[idx];
		}
	}

	/*处理?c
	    indexMap:每个?在SQL中的位置(KEY)与(=>)?对应参数在参数数组中的位置(VALUE)所组成的Map<Integer,Integer>
	    1.得到所有?c在SQl中出现的位置所组成的集合(escIndex)
	    2.使用?l替换掉所有的?c
	    3.遍历(escIndex),使用(indexMap)将escIndex翻译为对应参数数组的位置(idx)
	    4.使用(idx)拿到并修改原参数 ==>params[idx]="%"+params[idx]+"%";
	 */
	public void dealithW_c(Map<Integer,Integer>indexMap){
		List<Integer> escIndex=new ArrayList<>();
		setQuestionMarkIndex(precompileSql,escIndex,C);
		precompileSql=precompileSql.replaceAll("\\?c","?l");
		for (Integer index : escIndex) {
			int idx=indexMap.get(index);
			params[idx]="%"+params[idx]+"%";
		}
	}

	/*处理?C  (IN操作类似的范围限定操作)
	    indexMap:?在SQL中的位置(KEY)与(=>)?对应参数在参数数组中的位置(VALUE)
	     1.得到所有?C在SQl中出现的位置所组成的集合(escIndex)
	     2.如果(escIndex)中没有元素则结束，否则进入第三步
	     3.遍历(escIndex),将使用(indexMap)将escIndex翻译为对应参数数组的位置(idx)
	     4.使用(idx)拿到原参数，并将其强转为Collection类型，如果出现异常会抛出一个RuntimeException
	     5.将Collection转化为数组(collArray),转化后将(idx)和(collArray)添加到Map(inCollectionMap)中 ==> inCollectionMap.put(idx,collArray)
	     6.得到一个包含(collArray)长度个数的? ==>  (?,?,?,?) ,然后使用这个替换掉原SQL中的第一个?C
	     7.生产新的参数数组Object[] newParams；
	        新数组长度=原数组长度+原数组中所有集合参数的元素个数和-集合参数的个数
	        newParamsSize=params.length+S(inCollectionMap.value.length)-inCollectionMap.size
	     8.使用原数组和inCollectionMap数组填充新数组
	     9.将新数组赋值给原数组

	     ------------------------------------------------------------------------------------------------------------------------------
	      SQL : SELECT * FROM table WHERE f0=? AND f1=? AND f2 IN ?C AND f3=? OR f4=? AND f5 IN ?C f6 NOT IN ?C
	      Params               [0,1,2C,3,4,5C,6C]    l=7

	      inCollectionMap                            lm=3
	            2 -> [a,b,c,d]                       l2=4
	            5 -> [x,y,z]                         l5=3
	            6 -> [t,q,m,j]                       l6=4
	      Object[] newParams = new Object[l+l2+l5+l6-lm]
	                             |
	                             V
	      NEW-SQL    : SELECT * FROM table WHERE f0=? AND f1=? AND f2 IN (?,?,?,?) AND f3=? OR f4=? AND f5 IN (?,?,?) f6 NOT IN (?,?,?,?)
	      NEW-Params :        [0,1,a,b,c,d,3,4,x,y,z,t,q,m,j]
	 */
	public void dealithW_C(Map<Integer,Integer>indexMap){
		List<Integer> escIndex=new ArrayList<>();
		setQuestionMarkIndex(precompileSql,escIndex,In);
		if(!escIndex.isEmpty()){
			Map<Integer,Object[]> inCollectionMap=new HashMap<>();
			Collection collections;
			int newArraySize=0;
			Object[] collArray;
			for (Integer index : escIndex) {
				int idx=indexMap.get(index);
				try{
                    collections=(Collection)params[idx];
                }catch (Exception e){
				    throw new RuntimeException("SQL操作符 \"IN\" 对应的参数类型必须为Collection的子类！错误的类型:"+params[idx].getClass(),e);
                }
				int collSize=collections.size();
				newArraySize+=collSize;
				collArray=new Object[collSize];
				precompileSql=precompileSql.replaceFirst("\\?C",getMark(collSize));
				int i=0;
				for (Object coll : collections) {
					collArray[i]=coll;
					i++;
				}
				inCollectionMap.put(idx,collArray);
			}
			newArraySize=params.length+newArraySize-inCollectionMap.size();
			Object[] newParams=new Object[newArraySize];
			int p=0;
			for(int i=0;i<params.length;i++){
				if(inCollectionMap.containsKey(i)){
					for(Object obj:inCollectionMap.get(i)){
						newParams[p]=obj;
						p++;
					}
				}else{
					newParams[p]=params[i];
					p++;
				}
			}
			params=newParams;
		}
	}

	public String getMark(int num){
		StringBuilder s=new StringBuilder(" (");
		for(int i=0;i<num;i++){
			s.append("?,");
		}
		return s.substring(0,s.length()-1)+") ";
	}

	//得到每个？在SQL中的位置，组成一个集合
	public void setQuestionMarkIndex(String sql, List<Integer> indexs, String target){
		if(!sql.contains(target)){
			return;
		}
		String sqlCopy=sql;
		indexs.add(sqlCopy.indexOf(target));
		if("?".equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?","@");
		}else if(S.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?s","@L");
		}else if(E.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?e","@L");
		}else if(C.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?c","@L");
		}else if(In.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?C","@L");
		}else{
			throw new RuntimeException("错误的参数："+target+",正确的参数为[?,?s,?e,?c,?C]");
		}

		setQuestionMarkIndex(sqlCopy,indexs,target);
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

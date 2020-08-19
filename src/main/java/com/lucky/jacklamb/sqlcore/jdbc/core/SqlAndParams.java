package com.lucky.jacklamb.sqlcore.jdbc.core;

import com.lucky.jacklamb.sqlcore.abstractionlayer.exception.LuckySqlGrammarMistakesException;
import com.lucky.jacklamb.utils.reflect.MethodUtils;
import com.lucky.jacklamb.utils.regula.Regular;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class SqlAndParams{

	/** ?s <=> XX LIKE YY% */
	private final String START ="?s";
	/** ?e <=> XX LIKE %YY */
	private final String END ="?e";
	/** ?c <=> XX LIKE %YY% */
	private final String CONTAIN ="?c";
	/** ?C <=> (?,?,?,?) */
	private final String In="?C";
	/** ?D <=> 动态SQL */
	private final String DYSQL="?D";

	String precompileSql;

	Object[] params;

	public SqlAndParams(){};

	public SqlAndParams(String haveNumSql,Object[] params){
		numSql(haveNumSql,params);
		finalProce(null);
	}

	public SqlAndParams(Method method, String haveNumSql, Object[] params){
		List<String> names = Regular.getArrayByExpression(haveNumSql, Regular.$SQL);
		if(names.isEmpty()){
			numSql(haveNumSql,params);
		}else{
			nameSQl(method,names,haveNumSql,params);
		}
		finalProce(method);
	}

	public Map<Integer,Integer> getIndexMap(){
		List<Integer> indexs=new ArrayList<>();
		setQuestionMarkIndex(precompileSql,indexs,"?");
		Map<Integer,Integer> indexMap=new HashMap<>();
		for (int i = 0; i < indexs.size(); i++) {
			indexMap.put(indexs.get(i),i);
		}
		return indexMap;
	}

	/*最后处理，处理预编译Sql中的特殊参数[?s,?e,?c,?C,?D]
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
	    ?D	: DySql         -> DynamicSqlWrapper 动态SQL生成规则
	    	SELECT * FROM table ?D				   Params[(map)->{if(map.get("name")==null){return new SP("WHERE name>?",map.get("name"))}else{return new SP()}}]
	    						 |
	    						 V
	    	SELECT * FROM table  OR
	    	SELECT * FROM table WHERE name=?              Params["jack"]
	 */
	public void finalProce(Method method){
		if(precompileSql.contains(In)||precompileSql.contains(START)||
			precompileSql.contains(END)||precompileSql.contains(CONTAIN)||
			precompileSql.contains(DYSQL)){
			Map<String,Object> DY_DATA=new HashMap<>();
			if(method!=null){
				DY_DATA=MethodUtils.getMethodParamsNV(method,params);
			}else{
				for (int i = 0,j=params.length; i < j; i++) {
					DY_DATA.put(i+"",params[i]);
				}
			}
			Map<Integer,Integer> indexMap=getIndexMap();
			dealithW_D(DY_DATA);
			indexMap=getIndexMap();
			dealithW_s(indexMap);dealithW_e(indexMap);dealithW_c(indexMap);
			precompileSql=precompileSql.replaceAll("\\?l","?");
			indexMap=getIndexMap();
			dealithW_C(indexMap);
		}
	}

	//处理?s,同dealithW_c
	public void dealithW_s(Map<Integer,Integer>indexMap){
		List<Integer> escIndex=new ArrayList<>();
		setQuestionMarkIndex(precompileSql,escIndex, START);
		precompileSql=precompileSql.replaceAll("\\?s","?l");
		for (Integer index : escIndex) {
			int idx=indexMap.get(index);
			params[idx]=params[idx]+"%";
		}
	}

    //处理?e,同dealithW_s
	public void dealithW_e(Map<Integer,Integer>indexMap){
		List<Integer> escIndex=new ArrayList<>();
		setQuestionMarkIndex(precompileSql,escIndex, END);
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
		setQuestionMarkIndex(precompileSql,escIndex, CONTAIN);
		precompileSql=precompileSql.replaceAll("\\?c","?l");
		for (Integer index : escIndex) {
			int idx=indexMap.get(index);
			params[idx]="%"+params[idx]+"%";
		}
	}

	public void dealithW_D(Map<String,Object> DY_DATA){
		while (true){
			if(!precompileSql.contains("?D")){
				break;
			}
			Map<Integer, Integer> indexMap = getIndexMap();
			List<Integer> escIndex=new ArrayList<>();
			setQuestionMarkIndex(precompileSql,escIndex,DYSQL);
			DynamicSqlWrapper dySqlWar;
			SP sp;
			int idx=0;
			try{
				idx=indexMap.get(escIndex.get(0));
				dySqlWar=(DynamicSqlWrapper)params[idx];
			}catch (Exception e){
				throw new RuntimeException("SQL操作符 \"?D\" 对应的参数类型必须为"+DynamicSqlWrapper.class+"！错误的类型:"+params[idx].getClass(),e);
			}
			sp=dySqlWar.dySql(DY_DATA);
			precompileSql=precompileSql.replaceFirst("\\?D",sp.getpSql());
			Object[] newParams=new Object[params.length+sp.getParams().size()-1];
			for (int i=0,j=newParams.length;i<j;i++){
				if(i<idx){
					newParams[i]=params[i];
				}else if(i>=idx&&i<(idx+sp.getParams().size())){
					newParams[i]=sp.getParams().get(i-idx);
				}else{
					newParams[i]=params[i-idx];
				}
			}
			params=newParams;
		}
//		List<Integer> escIndex=new ArrayList<>();
//		Map<Integer,Integer>indexMap;
//		setQuestionMarkIndex(precompileSql,escIndex,In);
//		if(!escIndex.isEmpty()){
//			DynamicSqlWrapper dySqlWar;
//			SP sp;
//			for (Integer index : escIndex) {
//				int idx=indexMap.get(index);
//				try{
//					dySqlWar=(DynamicSqlWrapper)params[idx];
//				}catch (Exception e){
//					throw new RuntimeException("SQL操作符 \"?D\" 对应的参数类型必须为"+DynamicSqlWrapper.class+"！错误的类型:"+params[idx].getClass(),e);
//				}
//				sp=dySqlWar.dySql(DY_DATA);
//				precompileSql.replaceFirst("\\?D",sp.getpSql());
//			}
//		}

	}

	/*处理?C  (IN操作类似的范围限定操作)
	    indexMap:?在SQL中的位置(KEY)与(=>)?对应参数在参数数组中的位置(VALUE)
	     1.得到所有?C在SQl中出现的位置所组成的集合(escIndex)
	     2.如果(escIndex)中没有元素则结束，否则进入第三步
	     3.遍历(escIndex),将使用(indexMap)将escIndex翻译为对应参数数组的位置(idx)
	     4.使用(idx)拿到原参数，并将其强转为Collection类型，如果出现异常会抛出一个RuntimeException
	     5.将Collection转化为数组(collArray),转化后将(idx)和(collArray)添加到Map(inCollectionMap)中 ==> inCollectionMap.put(idx,collArray)
	     6.得到一个包含(collArray)长度个数的? ==>  (?,?,?,?) ,然后使用这个替换掉原SQL中的第一个?CONTAIN
	     7.生产新的参数数组Object[] newParams；
	        新数组长度=原数组长度+原数组中所有集合参数的元素个数和-集合参数的个数
	        newParamsSize=params.length+START(inCollectionMap.value.length)-inCollectionMap.size
	     8.使用原数组和inCollectionMap数组填充新数组
	     9.将新数组赋值给原数组

	     ------------------------------------------------------------------------------------------------------------------------------
	      SQL : SELECT * FROM table WHERE f0=? AND f1=? AND f2 IN ?C AND f3=? OR f4=? AND f5 IN ?CONTAIN f6 NOT IN ?CONTAIN
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
				    throw new RuntimeException("SQL操作符 \"?C\" 对应的参数类型必须为Collection的子类！错误的类型:"+params[idx].getClass(),e);
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
		}else if(START.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?s","@L");
		}else if(END.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?e","@L");
		}else if(CONTAIN.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?c","@L");
		}else if(In.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?C","@L");
		}else if(DYSQL.equals(target)){
			sqlCopy=sqlCopy.replaceFirst("\\?D","@L");
		}else{
			throw new RuntimeException("错误的参数："+target+",正确的参数为[?,?s,?e,?c,?CONTAIN]");
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
	}
}

package com.lucky.jacklamb.sqlcore.c3p0;

import com.lucky.jacklamb.mapping.Regular;
import com.lucky.jacklamb.sqlcore.abstractionlayer.exception.LuckySqlGrammarMistakesException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 结果集自动包装类
 * 
 * @author fk-7075
 *
 */
public class AutoPackage {

	private String dbname;

	private SqlOperation sqlOperation;

	
	public AutoPackage(String dbname) {
		this.dbname=dbname;
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
		return sqlOperation.autoPackageToList(dbname,c,sp.precompileSql,sp.params);
	}

	public boolean update(String sql,Object...obj) {
		SqlAndParams sp=new SqlAndParams(sql,obj);
		return sqlOperation.setSql(dbname,sp.precompileSql,sp.params);
	}
	
	public boolean updateBatch(String sql,Object[][] obj) {
		return sqlOperation.setSqlBatch(dbname,sql, obj);
	}

	public boolean updateBatch(String...completeSqls){
		return sqlOperation.setSqlBatch(dbname,completeSqls);
	}



}

class SqlAndParams{

	String precompileSql;

	Object[] params;

	public SqlAndParams(String haveNumSql,Object[] params){
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
			if(0<index&&index<=size){

			}else{
				throw new LuckySqlGrammarMistakesException(haveNumSql);
			}
		}
		return targetParams;
	}

}

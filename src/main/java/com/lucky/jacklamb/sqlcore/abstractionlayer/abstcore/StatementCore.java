package com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore;

import java.util.List;

/**
 * 关系型数据库通用的Sql语句操作
 * @author fk-7075
 *
 */
public interface StatementCore {
	
	/**
	 * SQL查询
	 * @param c
	 * 包装类的Class
	 * @param sql
	 * 预编译的sql语句
	 * @param obj
	 * @return
	 */
	public <T> List<T> getList(Class<T> c, String sql, Object... obj);
	
	/**
	 * 预编译SQL方式获得单一对象
	 * @param c
	 * @param sql
	 * @param obj
	 * @return
	 */
	public <T> T getObject(Class<T> c,String sql,Object...obj);
	
	/**
	 * 预编译SQL非查询操作
	 * @param sql
	 * @param obj
	 * @return
	 */
	public boolean update(String sql,Object...obj);

	
	
	/**
	 * 批量SQL非查询操作
	 * @param sql
	 * 模板预编译SQL语句
	 * @param obj
	 * 填充占位符的一组组对象数组组成的二维数组
	 * [[xxx],[xxx],[xxx]]
	 * @return
	 */
	public boolean updateBatch(String sql,Object[][] obj);
	
	/**
	 * 清空缓存
	 */
	public void clear();
	

	

}

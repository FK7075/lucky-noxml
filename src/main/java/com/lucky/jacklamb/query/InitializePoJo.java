package com.lucky.jacklamb.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 初始化信息对象
 * @author fk7075
 *
 */
public class InitializePoJo {
	
	/**
	 * 分页查询的包装器
	 */
	private Class<?> clzz;
	/**
	 * 包含查询信息的pojo
	 */
	private List<Object> pojos=new ArrayList<>();
	/**
	 * 查询的预编译SQL
	 */
	private String sql;
	/**
	 * 填充占位符的对象
	 */
	private List<Object> sqlobj=new ArrayList<>();
	
	
	/**
	 * 得到一个空的初始化信息对象
	 */
	public InitializePoJo() {
		
	}

	/**
	 * 得到一个包含分页包装器的初始化信息对象
	 * @param clzz 分页包装器
	 */
	public InitializePoJo(Class<?> clzz) {
		this.clzz = clzz;
	}
	
	/**
	 * 得到一个包含分页包装器和包含查询信息的pojo的初始化信息对象
	 * @param clzz 分页包装器
	 * @param pojos 包含分页查询信息的pojos
	 */
	public InitializePoJo(Class<?> clzz, Object...pojos) {
		this.clzz = clzz;
		this.pojos = Arrays.asList(pojos);
	}
	
	/**
	 * 
	 * @param clzz 分页包装器
	 * @param sql 分页前的预编译SQL
	 * @param sqlobj 填充占位符的对象
	 */
	public InitializePoJo(Class<?> clzz, String sql, Object...sqlobj) {
		this.clzz = clzz;
		this.sql = sql;
		this.sqlobj = Arrays.asList(sqlobj);
	}


	public Class<?> getClzz() {
		return clzz;
	}
	public void setClzz(Class<?> clzz) {
		this.clzz = clzz;
	}
	public List<Object> getPojos() {
		return pojos;
	}
	public void setPojos(Object...pojos) {
		this.pojos = Arrays.asList(pojos);
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public List<Object> getSqlobj() {
		return sqlobj;
	}
	public void setSqlobj(Object...sqlobj) {
		this.sqlobj = Arrays.asList(sqlobj);
	}
	
	
	

}

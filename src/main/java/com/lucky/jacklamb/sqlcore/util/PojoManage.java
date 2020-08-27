package com.lucky.jacklamb.sqlcore.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.lucky.jacklamb.annotation.orm.*;
import com.lucky.jacklamb.enums.PrimaryType;
import com.lucky.jacklamb.exception.NotFindFlieException;
import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;
import com.lucky.jacklamb.utils.reflect.ClassUtils;

/**
 * 实体类管理工具
 * @author fk-7075
 *
 */
public class PojoManage {
	
	public static String getIpPort(String dbname){
		String url = ReaderInI.getDataSource(dbname).getJdbcUrl();
		return url.substring(url.indexOf("//"),url.lastIndexOf("/")+1);
		
	}
	
	/**
	 * 获取当前数据源对应数据库的类型
	 * @param dbname
	 * @return
	 */
	public static String getDatabaseType(String dbname) {
		String jdbcDriver= ReaderInI.getDataSource(dbname).getDriverClass();
		if(jdbcDriver.contains("mysql"))
			return "MYSQL";
		if(jdbcDriver.contains("db2"))
			return "DB2";
		if(jdbcDriver.contains("oracle"))
			return "ORACLE";
		if(jdbcDriver.contains("postgresql"))
			return "POSTGRESQL";
		if(jdbcDriver.contains("sqlserver"))
			return "SQL SERVER";
		if(jdbcDriver.contains("sybase"))
			return "SYBASE";
		if(jdbcDriver.contains("access"))
			return "ACCESS";
		return null;
	}
	
	/**
	 * 获取当前数据源对应数据库的名字
	 * @param dbname
	 * @return
	 */
	public static String getDatabaseName(String dbname) {
		String url = ReaderInI.getDataSource(dbname).getJdbcUrl();
		String databasename=url.substring((url.lastIndexOf("/")+1),url.length());
		if(databasename.contains("?")) {
			databasename=databasename.substring(0, databasename.indexOf("?"));
		}
		return databasename;
	}
	
	/**
	 * 得到该实体类属性对应的数据库映射
	 * @param field
	 * @return
	 */
	public static String getTableField(Field field) {
		if(field.isAnnotationPresent(Column.class)) {
			Column coumn=field.getAnnotation(Column.class);
			if("".equals(coumn.value()))
				return field.getName();
			return coumn.value();
		}else if(field.isAnnotationPresent(Id.class)) {
			Id id=field.getAnnotation(Id.class);
			if("".equals(id.value()))
				return field.getName();
			return id.value().toLowerCase();
		}else if(field.isAnnotationPresent(Key.class)) {
			Key key=field.getAnnotation(Key.class);
			if("".equals(key.value()))
				return field.getName();
			return key.value();
		}else if(field.isAnnotationPresent(NoColumn.class)){
			return "";
		}else{
			return field.getName();
		}
	}
	
	/**
	 * 得到该字段是否可以为null的配置
	 * @param field
	 * @return
	 */
	public static boolean allownull(Field field) {
		if(field.isAnnotationPresent(Column.class)) {
			return field.getAnnotation(Column.class).allownull();
		}else if(field.isAnnotationPresent(Key.class)) {
			return field.getAnnotation(Key.class).allownull();
		}else {
			return true;
		}
	}
	
	/**
	 * 得到属性的长度配置
	 * @param field
	 * @return
	 */
	public static int getLength(Field field) {
		if(field.isAnnotationPresent(Id.class)) {
			return field.getAnnotation(Id.class).length();
		}else if(field.isAnnotationPresent(Key.class)) {
			return field.getAnnotation(Key.class).length();
		}else if(field.isAnnotationPresent(Column.class)) {
			return field.getAnnotation(Column.class).length();
		}else {
			return 100;
		}
	}
	
	/**
	 * 得到该实体类的Id属性
	 * @param pojoClass
	 * @return
	 */
	public static Field getIdField(Class<?> pojoClass) {
		Field[] pojoFields=ClassUtils.getAllFields(pojoClass);
		for(Field field:pojoFields) {
			if(field.isAnnotationPresent(Id.class)) {
				return field;
			}
		}
		throw new NotFindFlieException("没有找到"+pojoClass.getName()+"的Id属性，请检查该类的ID属性上是否有配置@Id注解.");
	}
	
	/**
	 * 得到该实体类的映射表名
	 * @param pojoClass
	 * @return
	 */
	public static String getTable(Class<?> pojoClass) {
		if(pojoClass.isAnnotationPresent(Table.class)) {
			Table table=pojoClass.getAnnotation(Table.class);
			if("".equals(table.value())){
				return pojoClass.getSimpleName().toLowerCase();
			}
			return table.value();
		}else {
			return pojoClass.getSimpleName().toLowerCase();
		}
	}
	
	/**
	 * 得到该实体对应表的级联删除信息
	 * @param pojoClass
	 * @return
	 */
	public static boolean cascadeDelete(Class<?> pojoClass) {
		if(pojoClass.isAnnotationPresent(Table.class)) {
			Table table=pojoClass.getAnnotation(Table.class);
			return table.cascadeDelete();
		}
		return false;
	}
	
	/**
	 * 得到该实体对应表的级更新除信息
	 * @param pojoClass
	 * @return
	 */
	public static boolean cascadeUpdate(Class<?> pojoClass) {
		if(pojoClass.isAnnotationPresent(Table.class)) {
			Table table=pojoClass.getAnnotation(Table.class);
			return table.cascadeUpdate();
		}
		return false;
	}
	
	/**
	 * 得到该实体类的映射主键名
	 * @param pojoClass
	 * @return
	 */
	public static String getIdString(Class<?> pojoClass) {
		Field idField = getIdField(pojoClass);
		Id id = idField.getAnnotation(Id.class);
		if("".equals(id.value()))
			return idField.getName();
		return id.value();
	}
	

	/**
	 * 得到该实体类的所有映射外键名与属性组成的Map
	 * @param pojoClass
	 * @return
	 */
	public static Map<Field,Class<?>> getKeyFieldMap(Class<?> pojoClass){
		Map<Field,Class<?>> keys=new HashMap<>();
		Field[] pojoFields= ClassUtils.getAllFields(pojoClass);
		for(Field field:pojoFields) {
			if(field.isAnnotationPresent(Key.class)) {
				Key key=field.getAnnotation(Key.class);
				keys.put(field, key.pojo());
			}
		}
		return keys;
	}
	
	/**
	 * 外键对应类反推外键属性
	 * @param clap 主表类
	 * @param clak 外键表类
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Field classToField(Class<?> clap,Class<?> clak) {
		List<Field> clapKeyFields = (List<Field>) getKeyFields(clap, true);
		for(Field field: clapKeyFields) {
			Key key=field.getAnnotation(Key.class);
			if(key.pojo().equals(clak))
				return field;
		}
		return null;
	}
	
	/**
	 * 得到该实体对应表的外键信息
	 * @param pojoClass
	 * @param iskey true(返回外键属性集合)/false(返回外键对应的的实体Class)
	 * @return
	 */
	public static List<?> getKeyFields(Class<?> pojoClass,boolean iskey){
		Map<Field,Class<?>> keyAdnField=getKeyFieldMap(pojoClass);
		List<Field> keys=new ArrayList<>();
		List<Class<?>> clzzs=new ArrayList<>();
		for(Entry<Field,Class<?>> entry:keyAdnField.entrySet()) {
			keys.add(entry.getKey());
			clzzs.add(entry.getValue());
		}
		if(iskey)
			return keys;
		else
			return clzzs;
	}
	
	/**
	 * 判断该实体对应表的主键类型(自增int主键/UUID主键/普通主键)
	 * @param pojoClass
	 * @return
	 */
	public static PrimaryType getIdType(Class<?> pojoClass) {
		Field idF=getIdField(pojoClass);
		Id id=idF.getAnnotation(Id.class);
		return id.type();
	}
	
	/**
	 * 得到设置主键索引的信息
	 * @param pojoClass
	 * @return
	 */
	public static String primary(Class<?> pojoClass) {
		if(pojoClass.isAnnotationPresent(Table.class)) {
			Table table=pojoClass.getAnnotation(Table.class);
			return table.primary();
		}else {
			return "";
		}
	}
	
	/**
	 * 得到设置普通索引的信息
	 * @param pojoClass
	 * @return
	 */
	public static String[] index(Class<?> pojoClass) {
		if(pojoClass.isAnnotationPresent(Table.class)) {
			Table table=pojoClass.getAnnotation(Table.class);
			return table.index();
		}else {
			return new String[0];
		}
	}
	
	/**
	 * 得到设置唯一值索引的信息
	 * @param pojoClass
	 * @return
	 */
	public static String[] unique(Class<?> pojoClass) {
		if(pojoClass.isAnnotationPresent(Table.class)) {
			Table table=pojoClass.getAnnotation(Table.class);
			return table.unique();
		}else {
			return new String[0];
		}
	}
	
	/**
	 * 得到设置全文索引的信息
	 * @param pojoClass
	 * @return
	 */
	public static String[] fulltext(Class<?> pojoClass) {
		if(pojoClass.isAnnotationPresent(Table.class)) {
			Table table=pojoClass.getAnnotation(Table.class);
			return table.fulltext();
		}else {
			return new String[0];
		}
	}

	/**
	 * 得到表的别名，在连接操作时使用
	 * @param pojoClass
	 * @return
	 */
	public static String tableAlias(Class<?> pojoClass){
		if(pojoClass.isAnnotationPresent(Table.class)){
			String alias=pojoClass.getAnnotation(Table.class).alias();
			if(!"".equals(alias))
				return alias;
			return getTable(pojoClass);
		}
		return getTable(pojoClass);
	}

	/**
	 * 别名，From语句后使用
	 * @param pojoClass
	 * @return
	 */
	public static String selectFromTableAlias(Class<?> pojoClass){
		if(tableAlias(pojoClass).equals(getTable(pojoClass)))
			return getTable(pojoClass);
		return getTable(pojoClass)+" "+tableAlias(pojoClass);
	}
}

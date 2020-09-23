package com.lucky.jacklamb.tcconversion.createtable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.lucky.jacklamb.enums.PrimaryType;
import com.lucky.jacklamb.sqlcore.util.PojoManage;
import com.lucky.jacklamb.tcconversion.typechange.JDBChangeFactory;
import com.lucky.jacklamb.tcconversion.typechange.TypeConversion;

/**
 * 生成建表语句的类
 * 
 * @author fk-7075
 *
 */
@SuppressWarnings("unchecked")
public class CreateTableSql {

	/**
	 * 根据类的Class信息生成建表语句
	 * @param clzz 目标类的Class
	 * @return
	 */
	public static String getCreateTable(String dbname,Class<?> clzz) {
		TypeConversion jDChangeFactory = JDBChangeFactory.jDBChangeFactory(dbname);
		StringBuilder sql=new StringBuilder("CREATE TABLE IF NOT EXISTS ");
		sql.append(PojoManage.getTable(clzz,dbname)).append(" (");
		Field[] fields = clzz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			String fieldType=fields[i].getType().getSimpleName();
			if (i < fields.length - 1) {
				if (PojoManage.getTableField(dbname,fields[i]).equals(PojoManage.getIdString(clzz,dbname))) {
					sql.append(PojoManage.getIdString(clzz,dbname)).append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append("(").append(PojoManage.getLength(fields[i],dbname)).append(")")
					.append(" NOT NULL ").append(isAutoInt(clzz,dbname)).append(" PRIMARY KEY,");
				} else if (!("double".equals(jDChangeFactory.javaTypeToDb(fieldType))
						|| "datetime".equals(jDChangeFactory.javaTypeToDb(fieldType))
						|| "date".equals(jDChangeFactory.javaTypeToDb(fieldType)))) {
					sql.append(PojoManage.getTableField(dbname,fields[i])).append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append("(").append(PojoManage.getLength(fields[i],dbname)).append(") ")
					.append(allownull(fields[i],dbname)).append(",");
				} else {
					sql.append(PojoManage.getTableField(dbname,fields[i])).append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append(allownull(fields[i],dbname)).append(",");
				}
			} else {
				if (PojoManage.getTableField(dbname,fields[i]).equals(PojoManage.getIdString(clzz,dbname))) {
					sql.append(PojoManage.getTableField(dbname,fields[i])).append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append("(").append(PojoManage.getLength(fields[i],dbname)).append(")")
					.append(" NOT NULL AUTO_INCREMENT PRIMARY KEY");
				} else if (!("double".equals(jDChangeFactory.javaTypeToDb(fieldType))
						|| "datetime".equals(jDChangeFactory.javaTypeToDb(fieldType))
						|| "date".equals(jDChangeFactory.javaTypeToDb(fieldType)))) {
					sql.append(PojoManage.getTableField(dbname,fields[i])).append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append("(").append(PojoManage.getLength(fields[i],dbname)).append(") ")
					.append(allownull(fields[i],dbname));
				} else {
					sql.append(PojoManage.getTableField(dbname,fields[i])).append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append(allownull(fields[i],dbname));
				}
			}
		}
		sql.append(") ENGINE=InnoDB DEFAULT CHARSET=UTF8");
		return sql.toString();
	}

	/**
	 * 生成添加外键的sql语句集合
	 * @param clzz
	 * 目标类的Class
	 * @return
	 */
	public static List<String> getForeignKey(Class<?> clzz,String dbname) {
		List<String> stlist = new ArrayList<String>();
		List<Field> keys = (List<Field>) PojoManage.getKeyFields(clzz, dbname,true);
		if (keys.isEmpty()) {
			return stlist;
		} else {
			List<Class<?>> cs = (List<Class<?>>) PojoManage.getKeyFields(clzz, dbname,false);
			for (int i = 0; i < cs.size(); i++) {
				String sql = "ALTER TABLE " + PojoManage.getTable(clzz,dbname) + " ADD CONSTRAINT " + getRandomStr()
						+ " FOREIGN KEY (" + PojoManage.getTableField(dbname,keys.get(i)) + ") REFERENCES " + PojoManage.getTable(cs.get(i),dbname) + "("
						+ PojoManage.getIdString(cs.get(i),dbname) + ")"+isCascadeDel(cs.get(i),dbname)+isCascadeUpd(cs.get(i),dbname);
				stlist.add(sql);
			}
			return stlist;
		}
	}
	
	/**
	 * 生成添加索引的sql语句集合
	 * @param clzz
	 * @return
	 */
	public static List<String> getIndexKey(Class<?> clzz,String dbname){
		String table_name=PojoManage.getTable(clzz,dbname);
		List<String> indexlist = new ArrayList<String>();
		String primary = PojoManage.primary(clzz,dbname);
		String[] indexs = PojoManage.index(clzz,dbname);
		String[] fulltextes = PojoManage.fulltext(clzz,dbname);
		String[] uniques = PojoManage.unique(clzz,dbname);
		if(!"".equals(primary)){
			String p_key="ALTER TABLE "+table_name+" ADD PRIMARY KEY("+primary+")";
			indexlist.add(p_key);
		}
		addAll(indexlist,table_name,indexs,"INDEX");
		addAll(indexlist,table_name,fulltextes,"FULLTEXT");
		addAll(indexlist,table_name,uniques,"UNIQUE");
		return indexlist;
	}

	/**
	 * 拼接该实体中需要配置的所有索引信息
	 * @param indexlist
	 * @param tablename
	 * @param indexs
	 * @param type
	 */
	private static void addAll(List<String> indexlist, String tablename, String[] indexs, String type) {
		String key="ALTER TABLE "+tablename+" ADD ";
		for(String index:indexs) {
			String indexkey;
			if("INDEX".equals(type)) {
				indexkey=key+type+" "+getRandomStr()+"(";
			} else {
				indexkey=key+type+"(";
			}
			 indexkey+=index+")";
			indexlist.add(indexkey);
		}
		
	}

	/**
	 * 生成外键名
	 * @return
	 */
	private static String getRandomStr() {
		return UUID.randomUUID().toString().replaceAll("-","");
	}
	
	/**
	 * 设置主键类型
	 * @param clzz
	 * @return
	 */
	private static String isAutoInt(Class<?> clzz,String dbname) {
		PrimaryType idType = PojoManage.getIdType(clzz,dbname);
		if(idType==PrimaryType.AUTO_INT) {
			return "AUTO_INCREMENT";
		}
		return "";
	}
	
	/**
	 * 设置级联删除
	 * @param clzz
	 * @return
	 */
	private static String isCascadeDel(Class<?> clzz,String dbname) {
		if(PojoManage.cascadeDelete(clzz,dbname)) {
			return " ON DELETE CASCADE";
		}
		return "";
	}
	
	/**
	 * 设置级联更新
	 * @param clzz
	 * @return
	 */
	private static String isCascadeUpd(Class<?> clzz,String dbname) {
		if(PojoManage.cascadeUpdate(clzz,dbname)) {
			return " ON UPDATE CASCADE";
		}
		return "";
	}
	
	/**
	 * 是否允许为NULL
	 * @param field
	 * @return
	 */
	private static String allownull(Field field,String dbname) {
		if(PojoManage.allownull(field,dbname)) {
			return " DEFAULT NULL ";
		}
		return " NOT NULL ";
	}
	
}


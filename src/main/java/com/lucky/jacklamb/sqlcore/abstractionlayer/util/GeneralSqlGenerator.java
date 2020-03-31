package com.lucky.jacklamb.sqlcore.abstractionlayer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.lucky.jacklamb.query.QFilter;

/**
 * 通用SQL生成器
 * @author DELL
 *
 */
public class GeneralSqlGenerator {
	
	/**
	 * id查询的SQL
	 * @param c
	 * @return
	 */
	public String getOneSql(Class<?> c) {
		StringBuilder sql=new StringBuilder("SELECT ");
		sql.append(new QFilter(c).lines()).append(" FROM ")
		.append(PojoManage.getTable(c)).append(" WHERE ")
		.append(PojoManage.getIdString(c)).append(" =?");
		return sql.toString();
	}
	
	/**
	 * id删除的SQL
	 * @param c
	 * @return
	 */
	public String deleteOneSql(Class<?> c) {
		StringBuilder sql=new StringBuilder("DELETE FROM ");
		sql.append(PojoManage.getTable(c)).append(" WHERE ")
		.append(PojoManage.getIdString(c)).append(" =?");
		return sql.toString();
	}
	
	/**
	 * 批量id删除的SQL
	 * @param c
	 * @param ids
	 * @return
	 */
	public String deleteIn(Class<?> c,Object...ids) {
		boolean first=true;
		StringBuilder sql=new StringBuilder("DELETE FROM ");
		sql.append(PojoManage.getTable(c)).append(" WHERE ")
		.append(PojoManage.getIdString(c)).append(" IN ");
		for(int i=0;i<ids.length;i++) {
			if(first) {
				sql.append("(?");
				first=false;
			}else {
				sql.append(",?");
			}
		}
		sql.append(")");
		return sql.toString();
	}
	
	/**
	 * 简单对象COUNT的SQL
	 * @param pojo
	 * @return
	 */
	public PrecompileSqlAndObject singleCount(Object pojo) {
		StringBuilder sql=new StringBuilder("SELECT COUNT(");
		sql.append(PojoManage.getIdString(pojo.getClass())).append(")").append(" FROM ").append(PojoManage.getTable(pojo.getClass()));
		PrecompileSqlAndObject psaq=singleWhere(pojo);
		psaq.setPrecompileSql(sql.append(psaq.getPrecompileSql()).toString());
		return psaq;
	}
	
	/**
	 * 简单对象查询的SQL
	 * @param pojo
	 * @return
	 */
	public PrecompileSqlAndObject singleSelect(Object pojo) {
		Class<?> objClass=pojo.getClass();
		StringBuilder sql=new StringBuilder("SELECT ");
		sql.append(new QFilter(objClass).lines()).append(" FROM ").append(PojoManage.getTable(objClass));
		PrecompileSqlAndObject psaq=singleWhere(pojo);
		psaq.setPrecompileSql(sql.append(psaq.getPrecompileSql()).toString());
		return psaq;
	}

	/**
	 * 简单对象删除的SQL
	 * @param pojo
	 * @return
	 */
	public PrecompileSqlAndObject singleDelete(Object pojo) {
		StringBuilder sql=new StringBuilder("DELETE FROM ");
		sql.append(PojoManage.getTable(pojo.getClass()));
		PrecompileSqlAndObject psaq=singleWhere(pojo);
		psaq.setPrecompileSql(sql.append(psaq.getPrecompileSql()).toString());
		return psaq;
	}
	
	/**
	 * 简单对象添加的SQL
	 * @param pojo
	 * @return
	 */
	public PrecompileSqlAndObject singleInsert(Object pojo){
		PrecompileSqlAndObject psaq=new PrecompileSqlAndObject();
		FieldAndValue fv=new FieldAndValue(pojo);
		boolean first=true;
		StringBuilder insertSql=new StringBuilder("INSERT INTO ");
		insertSql.append(PojoManage.getTable(pojo.getClass())).append("(");
		StringBuilder valuesSql=new StringBuilder(" VALUES(");
		Map<String, Object> fvMap = fv.getFieldNameAndValue();
		for(Entry<String, Object> entry:fvMap.entrySet()) {
			if(first) {
				insertSql.append(entry.getKey());
				valuesSql.append("?");
				first=false;
			}else {
				insertSql.append(",").append(entry.getKey());
				valuesSql.append(",?");
			}
			psaq.addObjects(entry.getValue());
		}
		psaq.setPrecompileSql(insertSql.append(")").append(valuesSql.append(")")).toString());
		return psaq;
	}
	
	/**
	 * 简单对象更新的SQL
	 * @param pojo
	 * @param conditions
	 * @return
	 */
	public PrecompileSqlAndObject singleUpdate(Object pojo,String...conditions) {
		PrecompileSqlAndObject psao=new PrecompileSqlAndObject();
		FieldAndValue fv=new FieldAndValue(pojo);
		StringBuilder updateSql=new StringBuilder("UPDATE ");
		updateSql.append(PojoManage.getTable(pojo.getClass())).append(" SET ");
		StringBuilder whereSql=new StringBuilder();
		Map<String, Object> fvMap = fv.getFieldNameAndValue();
		if(conditions.length==0) {
			boolean first=true;
			whereSql.append(" WHERE ").append(PojoManage.getIdString(pojo.getClass())).append("=?");
			for(Entry<String,Object> entry:fvMap.entrySet()) {
				if(!fv.getIdField().equals(entry.getKey())) {
					if(first) {
						updateSql.append(entry.getKey()).append("=?");
						first=false;
					}else {
						updateSql.append(",").append(entry.getKey()).append("=?");
					}
					psao.addObjects(entry.getValue());
				}
			}
			psao.setPrecompileSql(updateSql.append(whereSql).toString());
			psao.addObjects(fv.getIdValue());
			return psao;
		}else {
			boolean setfirst=true,wherefirst=true;
			List<Object> whereObject=new ArrayList<>();
			if(fv.containsFields(conditions)) {
				for(Entry<String,Object> entry:fvMap.entrySet()) {
					if(Arrays.asList(conditions).contains(entry.getKey())) {//WHERE
						if(wherefirst) {
							whereSql.append(" WHERE ").append(entry.getKey()).append("=?");
							wherefirst=false;
						}else {
							whereSql.append(" AND ").append(entry.getKey()).append("=?");
						}
						whereObject.add(entry.getValue());
					}else {//SET
						if(setfirst) {
							updateSql.append(entry.getKey()).append("=?");
							setfirst=false;
						}else {
							updateSql.append(",").append(entry.getKey()).append("=?");
						}
						psao.addObjects(entry.getValue());
					}
				}
				psao.addAllObjects(whereObject);
				psao.setPrecompileSql(updateSql.append(whereSql).toString());
				return psao;
			}
			return null;
		}
	}
	
	
	/**
	 * 简单对象操作的通用WHER片段SQL
	 * @param pojo
	 * @return
	 */
	public PrecompileSqlAndObject singleWhere(Object pojo) {
		FieldAndValue fv=new FieldAndValue(pojo);
		PrecompileSqlAndObject psaq=new PrecompileSqlAndObject();
		StringBuilder sql=new StringBuilder();
		boolean first=true;
		for(Entry<String, Object> entry:fv.getFieldNameAndValue().entrySet()) {
			if(first) {
				sql.append(" WHERE ").append(entry.getKey()).append("=?");
				first=false;
			}else {
				sql.append(" AND ").append(entry.getKey()).append("=?");
			}
			psaq.addObjects(entry.getValue());
		}
		psaq.setPrecompileSql(sql.toString());
		return psaq;
	}
	
}



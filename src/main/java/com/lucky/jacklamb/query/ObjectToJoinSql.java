package com.lucky.jacklamb.query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlGroup;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PojoManage;

public class ObjectToJoinSql{

	/**
	 * 连接查询的连接方式
	 */
	private String join;

	/**
	 * 需要操作的对象
	 */
	private Object[] obj;

	/**
	 * 返回列
	 */
	private String result;

	/**
	 * 排序条件
	 */
	private String sort;

	/**
	 * 模糊条件
	 */
	private String like;

	private SqlGroup sqlGroup;

	public ObjectToJoinSql(QueryBuilder query) {
		this.join = query.getJoin().getJoin();
		this.obj = query.getObjectArray();
		this.sort=query.getSort();
        this.like=query.getLike();
        this.sqlGroup=query.getWheresql();
        this.result=query.getResult();
	}

	/**
	 * 得到AND 部分的SQL
	 * @return
	 */
	private String andFragment() {
		String sql = "";
		int p = 0;
		for (int i = 0; i < obj.length; i++) {
			Class<?> clzz = obj[i].getClass();
			Field[] fields = clzz.getDeclaredFields();
			for (int j = 0; j < fields.length; j++) {
				fields[j].setAccessible(true);
				Object fk;
				try {
					fk = fields[j].get(obj[i]);
					if (fk != null) {
						if (p == 0) {
							sql += " WHERE " + PojoManage.tableAlias(clzz) + "." + PojoManage.getTableField(fields[j])
									+ "=?";
							p++;
						} else {
							sql += " AND " + PojoManage.tableAlias(clzz) + "." + PojoManage.getTableField(fields[j])
									+ "=?";

						}
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return sql;
	}

	/**
	 * 得到连接操作的查询条件
	 * @return
	 */
	public Object[] getJoinObject() {
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < obj.length; i++) {
			Field[] fields = obj[i].getClass().getDeclaredFields();
			try {
				for (int j = 0; j < fields.length; j++) {
					fields[j].setAccessible(true);
					Object object = fields[j].get(obj[i]);
					if (object != null) {
						list.add(object);
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return list.toArray();
	}

	/**
	 * 得到连接操作的SQL
	 * 
	 * @return
	 */
	public String getJoinSql(String...expression) {
	    String where=andFragment();
	    if(!"".equals(where)&&!"".equals(like))
	        like=" AND "+like;
	    return sqlGroup.sqlGroup(result, getOnSql(expression), andFragment(), like, sort);
	}




	
	/**
	 * 根据连接表达式确定连接方式
	 * @param expression
	 * @return
	 */
	public String getOnSql(String...expression) {
		String expre="";
		if(expression.length==0||"".equals(expression[0])) {
			for(Object object:obj) {
				expre+=PojoManage.getTable(object.getClass())+"-->";
			}
			expre=expre.substring(0,expre.length()-3);
		}else {
			expre=expression[0];
		}
		String onsql="";
		List<ClassControl> parsExpression = parsExpression(expre);
		for(int i=0;i<parsExpression.size();i++) {
			if(i==0) {
				onsql+=PojoManage.selectFromTableAlias(parsExpression.get(0).getClzz());
			}else {
				onsql+=" "+join+" "+PojoManage.selectFromTableAlias(parsExpression.get(i).getClzz())+" ON "+getEquation(parsExpression.get(i).getClzz(),parsExpression.get(i-1-parsExpression.get(i).getSite()).getClzz());
			}
		}
		return onsql;
	}

	/**
	 * 两个Class确定连接等式
	 * @param clax
	 * @param clay
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getEquation(Class<?> clax,Class<?> clay) {
		try {
			List<Class<?>> claxKeyClasss = (List<Class<?>>) PojoManage.getKeyFields(clax, false);
			if(claxKeyClasss.contains(clay))
				return PojoManage.tableAlias(clax)+"."+PojoManage.getTableField(PojoManage.classToField(clax, clay))+"="+PojoManage.tableAlias(clay)+"."+PojoManage.getIdString(clay);
			return PojoManage.tableAlias(clay)+"."+PojoManage.getTableField(PojoManage.classToField(clay, clax))+"="+PojoManage.tableAlias(clax)+"."+PojoManage.getIdString(clax);
		}catch(Exception e) {
			throw new RuntimeException(clax.getName()+" 与  "+clay.getName()+"不存在'主外键关系',请检查您的相关配置(@Key,@Id，连接查询表达式['->' '--' '<?>'] )....",e);
		}
	}
	
	/**
	 * 解析表达式
	 * @param expression
	 * @return
	 */
	public List<ClassControl> parsExpression(String expression){
		 List<ClassControl> cctlist=new ArrayList<>();
		String order=expression.replaceAll("\\b<\\S*?>\\b", ",").replaceAll("-->", ",").replaceAll("--", ",").toLowerCase();
		String symbol=expression.toLowerCase();
		String[] splitName = order.split(",");
		for(String name:splitName) {
			symbol=symbol.replaceAll(name, ",");
			ClassControl cctl=new ClassControl();
			Stream.of(obj).map(obj->obj.getClass()).filter(c->name.equals(PojoManage.getTable(c))).forEach(cctl::setClzz);
			cctlist.add(cctl);
		}
		String[] symArr=symbol.split(",");
		for(int i=0;i<symArr.length;i++) {
			cctlist.get(i).setSite(symbolToInt(symArr[i]));
		}
		return cctlist;
	}
	
	public int symbolToInt(String symbol) {
		if("".equals(symbol)) {
			return -1;
		}else if("--".equals(symbol)) {
			return 1;
		}else if("-->".equals(symbol)) {
			return 0;
		}else if(symbol.startsWith("<")&&symbol.endsWith(">")) {
			symbol=symbol.replaceAll("<", "").replaceAll(">", "");
			return Integer.parseInt(symbol);
		}else {
			return -2;
		}
		
	}
}

class ClassControl{
	
	private Class<?> clzz;
	
	private int site=-1;
	

	public Class<?> getClzz() {
		return clzz;
	}

	public void setClzz(Class<?> clzz) {
		this.clzz = clzz;
	}

	public int getSite() {
		return site;
	}

	public void setSite(int site) {
		this.site = site;
	}

	@Override
	public String toString() {
		return "ClassControl [clzz=" + clzz + ", site=" + site + "]";
	}
	
	
}

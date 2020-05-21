package com.lucky.jacklamb.sqlcore.c3p0;

import com.lucky.jacklamb.annotation.orm.NoPackage;
import com.lucky.jacklamb.conversion.util.ClassUtils;
import com.lucky.jacklamb.exception.AutoPackageException;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PojoManage;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.SqlLog;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC相关操作类
 * @author fk-7075
 *
 */
public class SqlOperation {

	/**
	 * 实现对表的曾刪改操作
	 * @param sql（预编译的sql语句）
	 * @param obj（替换占位符的数组）
	 * @return boolean
	 */
	public boolean setSql(String dbname,String sql, Object...obj) {
		Connection conn=null;
		PreparedStatement ps=null;
		SqlLog log=new SqlLog(dbname);
		boolean isOk=false;
		try {
			conn=C3p0Util.getConnecion(dbname);
			log.isShowLog(sql, obj);
			ps = conn.prepareStatement(sql);
			if (obj != null) {
				for (int i = 0; i < obj.length; i++) {
					ps.setObject(i + 1, obj[i]);
				}
			}
			int i = ps.executeUpdate();
			if (i != 0)
				isOk = true;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			C3p0Util.release(null, ps, conn);
		}
		return isOk;
	}
	
	/**
	 * 增删改操作批处理
	 * @param sql
	 * 预编译的SQL语句
	 * @param obj
	 * 填充占位符的一个有一个数组组成的二维数组
	 * @return
	 */
	public boolean setSqlBatch(String dbname,String sql,Object[]... obj) {
		Connection conn=null;
		PreparedStatement ps=null;
		SqlLog log=new SqlLog(dbname);
		boolean isOk=false;
		try {
			conn=C3p0Util.getConnecion(dbname);
			log.isShowLog(sql, obj);
			ps = conn.prepareStatement(sql);
			if(obj==null||obj.length==0) {
				ps.executeUpdate();
				isOk=true;
			}else {
				for(int i=0;i<obj.length;i++) {
					for(int j=0;j<obj[i].length;j++) {
						ps.setObject(j+1, obj[i][j]);
					}
					ps.addBatch();
				}
				ps.executeBatch();
				isOk=true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isOk;
	}

	/**
	 * 返回结果集
	 * @param sql
	 * @param obj
	 * @return
	 */
	public ResultSet getResultSet(String dbname,String sql, Object...obj) {
		Connection conn=null;
		PreparedStatement ps=null;
		SqlLog log=new SqlLog(dbname);
		ResultSet rs=null;
		try {
			conn=C3p0Util.getConnecion(dbname);
			log.isShowLog(sql, obj);
			ps = conn.prepareStatement(sql);
			if (obj != null) {
				for (int i = 0; i < obj.length; i++) {
					ps.setObject(i + 1, obj[i]);
				}
			}
			rs = ps.executeQuery();
		} catch (SQLException e) {
			throw new RuntimeException("SQL语法错误！错误的SQL:"+sql,e);
		}
		return rs;
	}

	/**
	 *
	 * @param dbname 数据源名称
	 * @param c 包装类的Class对象
	 * @param sql 预编译的sql语句
	 * @param obj 替换占位符的数组
	 * @param <T>
	 * @return
	 */
	public <T> List<T> autoPackageToList(String dbname,Class<T> c, String sql, Object... obj) {
		Connection conn=null;
		PreparedStatement ps=null;
		SqlLog log=new SqlLog(dbname);
		ResultSet rs=null;
		try {
			conn=C3p0Util.getConnecion(dbname);
			log.isShowLog(sql, obj);
			ps = conn.prepareStatement(sql);
			if (obj != null) {
				for (int i = 0; i < obj.length; i++) {
					ps.setObject(i + 1, obj[i]);
				}
			}
			rs = ps.executeQuery();
		} catch (SQLException e) {
			throw new RuntimeException("SQL语法错误！错误的SQL:"+sql,e);
		}
		List<T> collection=new ArrayList<>();
		if(c.getClassLoader()!=null) {
			Field[] fields = ClassUtils.getAllFields(c);
			Object object = null;
			try {
				while (rs.next()) {
					object = c.newInstance();
					for (Field f : fields) {
						if(f.isAnnotationPresent(NoPackage.class))
							continue;
						if (f.getType().getClassLoader()!=null) {
							Class<?> cl=f.getType();
							Field[] fils= ClassUtils.getAllFields(cl);
							Object onfk=cl.newInstance();
							for (Field ff : fils) {
								if(ff.isAnnotationPresent(NoPackage.class))
									continue;
								String field_tab= PojoManage.getTableField(ff);
								if (isExistColumn(rs, field_tab)) {
									ff.setAccessible(true);
									ff.set(onfk, rs.getObject(field_tab));
								}
							}
							f.setAccessible(true);
							f.set(object, onfk);
						} else {
							String field_tab=PojoManage.getTableField(f);
							if (isExistColumn(rs, field_tab)) {
								f.setAccessible(true);
								f.set(object, rs.getObject(field_tab));
							}
						}
					}
					collection.add((T) object);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new AutoPackageException("表类映射错误，无法自动包装查询结果！请检查检查映射配置。包装类：Class:"+c.getName()+"   SQl:"+sql);
			}finally {
				C3p0Util.release(rs,ps,conn);
			}
		}else {
			try {
				while(rs.next()) {
					collection.add((T) JavaConversion.strToBasic(rs.getObject(1).toString(), c));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				C3p0Util.release(rs,ps,conn);
			}
		}
		return collection;
	}



	/**
	 * 判断结果集中是否有指定的列
	 * @param rs 结果集对象
	 * @param columnName  类名
	 * @return 结果集中有指定的列则反true
	 */
	public boolean isExistColumn(ResultSet rs, String columnName) {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int size=metaData.getColumnCount();
			for(int i=1;i<=size;i++) {
				if(columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
					return true;
				}
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	

}

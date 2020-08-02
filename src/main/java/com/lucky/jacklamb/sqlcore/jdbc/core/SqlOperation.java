package com.lucky.jacklamb.sqlcore.jdbc.core;

import com.lucky.jacklamb.annotation.orm.NoPackage;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.exception.AutoPackageException;
import com.lucky.jacklamb.sqlcore.abstractionlayer.exception.LuckySqlOperationException;
import com.lucky.jacklamb.sqlcore.util.PojoManage;
import com.lucky.jacklamb.sqlcore.util.SqlLog;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
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
@SuppressWarnings("unchecked")
public class SqlOperation {

	private Connection conn;

	private String dbname;

	public SqlOperation(Connection conn, String dbname) {
		this.conn = conn;
		this.dbname = dbname;
	}

	/**
	 * 实现对表的曾刪改操作
	 * @param sql（预编译的sql语句）
	 * @param obj（替换占位符的数组）
	 * @return boolean
	 */
	public int setSql(String sql, Object...obj) {
		PreparedStatement ps=null;
		try {
			new SqlLog(dbname).isShowLog(sql, obj);
			ps = conn.prepareStatement(sql);
			if (obj != null) {
				for (int i = 0; i < obj.length; i++) {
					ps.setObject(i + 1, obj[i]);
				}
			}
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new LuckySqlOperationException(sql,obj,e);
		} finally {
			LuckyDataSource.release(null,ps,null);
		}
	}

	/**
	 * 增删改操作批处理
	 * @param sql 预编译的SQL语句
	 * @param obj 预编译的SQL语句
	 * @return
	 */
	public int[] setSqlBatch(String sql,Object[]... obj) {
		PreparedStatement ps=null;
		try {
			new SqlLog(dbname).isShowLog(sql, obj);
			ps = conn.prepareStatement(sql);
			if(obj==null||obj.length==0) {
				int[] result={ps.executeUpdate()};
				return result;
			}else {
				for(int i=0;i<obj.length;i++) {
					for(int j=0;j<obj[i].length;j++) {
						ps.setObject(j+1, obj[i][j]);
					}
					ps.addBatch();
				}
				return  ps.executeBatch();
			}
		} catch (SQLException e) {
			throw new LuckySqlOperationException(sql,obj,e);
		}finally {
			LuckyDataSource.release(null,ps,null);
		}
	}

	/**
	 * SQL批量执行
	 * @param sqls 一系列完整SQL组成的数组
	 * @return
	 */
	public int[] setSqlBatch(String...sqls){
		Statement ps=null;
		try {
			new SqlLog(dbname).isShowLog(sqls);
			ps = conn.createStatement();
			for (String sql : sqls) {
				ps.addBatch(sql);
			}
			return ps.executeBatch();
		} catch (SQLException e) {
			throw new LuckySqlOperationException(sqls,e);
		}finally {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 执行SQL返回结果集ResultSet
	 * @param sql 预编译SQL
	 * @param obj 预编译SQl执行参数
	 * @return
	 */
	public ResultSet getResultSet(String sql, Object...obj) {
		try {
			new SqlLog(dbname).isShowLog(sql, obj);
			PreparedStatement ps = conn.prepareStatement(sql);
			if (obj != null) {
				for (int i = 0; i < obj.length; i++) {
					ps.setObject(i + 1, obj[i]);
				}
			}
			return ps.executeQuery();
		} catch (SQLException e) {
			throw new LuckySqlOperationException(sql,obj,e);
		}
	}

	/**
	 *
	 * @param c 包装类的Class对象
	 * @param sql 预编译的sql语句
	 * @param obj 替换占位符的数组
	 * @param <T>
	 * @return
	 */
	public <T> List<T> autoPackageToList(Class<T> c, String sql, Object... obj) {
		PreparedStatement ps=null;
		SqlLog log=new SqlLog(dbname);
		ResultSet rs=null;
		try {
			log.isShowLog(sql, obj);
			ps = conn.prepareStatement(sql);
			if (obj != null) {
				for (int i = 0; i < obj.length; i++) {
					ps.setObject(i + 1, obj[i]);
				}
			}
			rs = ps.executeQuery();
		} catch (SQLException e) {
			throw new LuckySqlOperationException(sql,obj,e);
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
				throw new AutoPackageException("表类映射错误，无法自动包装查询结果！请检查检查映射配置。包装类：Class:"+c.getName()+"   SQl:"+sql,e);
			}finally {
				LuckyDataSource.release(rs,ps,null);
			}
		}else {
			try {
				while(rs.next()) {
					collection.add((T) JavaConversion.strToBasic(rs.getObject(1).toString(), c));
				}
			} catch (SQLException e) {
				throw new LuckySqlOperationException(sql,obj,e);
			}finally {
				LuckyDataSource.release(rs,ps,null);
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
			throw new LuckySqlOperationException(e);
		}
	}
}

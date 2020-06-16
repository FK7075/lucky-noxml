package com.lucky.jacklamb.sqlcore.datasource.core;

import com.lucky.jacklamb.annotation.orm.NoPackage;
import com.lucky.jacklamb.conversion.util.ClassUtils;
import com.lucky.jacklamb.exception.AutoPackageException;
import com.lucky.jacklamb.sqlcore.abstractionlayer.exception.LuckySqlOperationException;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PojoManage;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.SqlLog;
import com.lucky.jacklamb.sqlcore.datasource.abs.DataSourceManage;
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


	/**
	 * 实现对表的曾刪改操作
	 * @param dataSource 数据源
	 * @param sql（预编译的sql语句）
	 * @param obj（替换占位符的数组）
	 * @return boolean
	 */
	public boolean setSql(LuckyDataSource dataSource,String sql, Object...obj) {
		Connection conn=null;
		PreparedStatement ps=null;
		boolean isOk=false;
		try {
			conn= dataSource.getConnection();
			new SqlLog(dataSource.getDbname()).isShowLog(sql, obj);
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
			throw new LuckySqlOperationException(sql,obj,e);
		} finally {
			DataSourceManage.release(null, ps, conn);
		}
		return isOk;
	}

	/**
	 * 增删改操作批处理
	 * @param dataSource 数据源
	 * @param sql 预编译的SQL语句
	 * @param obj 预编译的SQL语句
	 * @return
	 */
	public boolean setSqlBatch(LuckyDataSource dataSource,String sql,Object[]... obj) {
		Connection conn=null;
		PreparedStatement ps=null;
		boolean isOk=false;
		try {
			conn= dataSource.getConnection();
			new SqlLog(dataSource.getDbname()).isShowLog(sql, obj);
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
			throw new LuckySqlOperationException(sql,obj,e);
		}finally {
			DataSourceManage.release(null, ps, conn);
			return isOk;
		}
	}

	/**
	 * SQL批量执行
	 * @param dataSource 数据源
	 * @param sqls 一系列完整SQL组成的数组
	 * @return
	 */
	public boolean setSqlBatch(LuckyDataSource dataSource,String...sqls){
		Connection conn=null;
		Statement ps;
		try {
			conn= dataSource.getConnection();
			new SqlLog(dataSource.getDbname()).isShowLog(sqls);
			ps = conn.createStatement();
			for (String sql : sqls) {
				ps.addBatch(sql);
			}
			ps.executeBatch();
			return true;
		} catch (SQLException e) {
			throw new LuckySqlOperationException(sqls,e);
		}finally {
			DataSourceManage.release(null, null, conn);
		}
	}

	/**
	 * 执行SQL返回结果集ResultSet
	 * @param dataSource 数据源
	 * @param sql 预编译SQL
	 * @param obj 预编译SQl执行参数
	 * @return
	 */
	public ResultSet getResultSet(LuckyDataSource dataSource,String sql, Object...obj) {
		try {
			Connection conn= dataSource.getConnection();
			new SqlLog(dataSource.getDbname()).isShowLog(sql, obj);
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
	 * @param dataSource 数据源
	 * @param c 包装类的Class对象
	 * @param sql 预编译的sql语句
	 * @param obj 替换占位符的数组
	 * @param <T>
	 * @return
	 */
	public <T> List<T> autoPackageToList(LuckyDataSource dataSource,Class<T> c, String sql, Object... obj) {
		Connection conn=null;
		PreparedStatement ps=null;
		SqlLog log=new SqlLog(dataSource.getDbname());
		ResultSet rs=null;
		try {
			conn= dataSource.getConnection();
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
				DataSourceManage.release(rs,ps,conn);
			}
		}else {
			try {
				while(rs.next()) {
					collection.add((T) JavaConversion.strToBasic(rs.getObject(1).toString(), c));
				}
			} catch (SQLException e) {
				throw new LuckySqlOperationException(sql,obj,e);
			}finally {
				LuckyDataSource.release(rs,ps,conn);
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

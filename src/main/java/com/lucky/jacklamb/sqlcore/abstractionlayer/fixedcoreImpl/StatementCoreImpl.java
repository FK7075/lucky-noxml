package com.lucky.jacklamb.sqlcore.abstractionlayer.fixedcoreImpl;

import java.lang.reflect.Method;
import java.util.List;

import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.StatementCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.CreateSql;
import com.lucky.jacklamb.sqlcore.jdbc.core.DefaultSqlActuator;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;

@SuppressWarnings("unchecked")
public final class StatementCoreImpl implements StatementCore {
	
	private String dbname;
	
	private CreateSql createSql;
	
	protected DefaultSqlActuator autopackage;
	
	
	public StatementCoreImpl(LuckyDataSource dataSource) {
		this.createSql= new CreateSql();
		this.dbname=dataSource.getDbname();
		this.autopackage=new DefaultSqlActuator(dbname);
	}
	
	
	@Override
	public <T> List<T> getList(Class<T> c, String sql, Object... obj) {
		List<?> result=null;
		return (List<T>) autopackage.autoPackageToList(c, sql, obj);
	}

	@Override
	public <T> List<T> getListMethod(Class<T> c,Method method, String sql, Object[] obj) {
		List<?> result;
		return (List<T>) autopackage.autoPackageToListMethod(c,method, sql, obj);
	}

	@Override
	public <T> T getObject(Class<T> c, String sql, Object... obj) {
		List<T> list = getList(c,sql,obj);
		if(list==null||!list.isEmpty())
			return list.get(0);
		return null;
	}

	@Override
	public <T> T getObjectMethod(Class<T> c,Method method, String sql, Object[] obj) {
		List<T> list = getListMethod(c,method,sql,obj);
		if(list==null||!list.isEmpty())
			return list.get(0);
		return null;
	}

	@Override
	public int update(String sql, Object... obj) {
		return autopackage.update(sql, obj);
	}

	@Override
	public int updateMethod(Method method, String sql, Object[] obj) {
		return autopackage.updateMethod(method,sql, obj);
	}

	@Override
	public int[] updateBatch(String sql, Object[][] obj) {
		return autopackage.updateBatch(sql, obj);
	}

	@Override
	public int[] updateBatch(String... completeSqls) {
		return autopackage.updateBatch(completeSqls);
	}


	@Override
	public void clear() {
		autopackage.clear();
	}

}

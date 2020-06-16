package com.lucky.jacklamb.sqlcore.abstractionlayer.fixedcoreImpl;

import java.lang.reflect.Method;
import java.util.List;

import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.StatementCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.CreateSql;
import com.lucky.jacklamb.sqlcore.datasource.core.AutoPackage;
import com.lucky.jacklamb.sqlcore.datasource.factory.LuckyDataSource;

@SuppressWarnings("unchecked")
public final class StatementCoreImpl implements StatementCore {
	
	private String dbname;
	
	private CreateSql createSql;
	
	protected AutoPackage autopackage;
	
	
	public StatementCoreImpl(LuckyDataSource dataSource) {
		this.createSql= new CreateSql();
		this.dbname=dataSource.getDbname();
		this.autopackage=new AutoPackage(dbname);
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
	public boolean update(String sql, Object... obj) {
		return autopackage.update(sql, obj);
	}

	@Override
	public boolean updateMethod(Method method, String sql, Object[] obj) {
		return autopackage.updateMethod(method,sql, obj);
	}

	@Override
	public boolean updateBatch(String sql, Object[][] obj) {
		return autopackage.updateBatch(sql, obj);
	}

	@Override
	public boolean updateBatch(String... completeSqls) {
		return autopackage.updateBatch(completeSqls);
	}


	@Override
	public void clear() {
		autopackage.clear();
	}

}

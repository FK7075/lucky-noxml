package com.lucky.jacklamb.sqlcore.abstractionlayer.fixedcoreImpl;

import java.lang.reflect.Method;
import java.util.List;

import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.StatementCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.cache.LuckyCache;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.CreateSql;
import com.lucky.jacklamb.sqlcore.c3p0.AutoPackage;
import com.lucky.jacklamb.sqlcore.c3p0.DataSource;

@SuppressWarnings("unchecked")
public final class StatementCoreImpl implements StatementCore {
	
	private String dbname;
	
	private boolean isCache;
	
	private LuckyCache cache;
	
	private CreateSql createSql;
	
	protected AutoPackage autopackage;
	
	
	public StatementCoreImpl(DataSource dataSource) {
		this.createSql= new CreateSql();
		this.cache=LuckyCache.getLuckyCache();
		this.dbname=dataSource.getName();
		this.isCache=dataSource.isCache();
		this.autopackage=new AutoPackage(dbname);
	}
	
	
	@Override
	public <T> List<T> getList(Class<T> c, String sql, Object... obj) {
		List<?> result=null;
		if(isCache) {
			if (!cache.contains(dbname,createSql.getSqlString(sql, obj))) {
				result = autopackage.autoPackageToList(c, sql, obj);
				cache.add(dbname,createSql.getSqlString(sql, obj), result);
			} else {
				result = cache.get(dbname,createSql.getSqlString(sql, obj));
			}
			return (List<T>) result;
		}
		return (List<T>) autopackage.autoPackageToList(c, sql, obj);
	}

	@Override
	public <T> List<T> getListMethod(Class<T> c,Method method, String sql, Object[] obj) {
		List<?> result;
		if(isCache) {
			if (!cache.contains(dbname,createSql.getSqlString(sql, obj))) {
				result = autopackage.autoPackageToList(c, sql, obj);
				cache.add(dbname,createSql.getSqlString(sql, obj), result);
			} else {
				result = cache.get(dbname,createSql.getSqlString(sql, obj));
			}
			return (List<T>) result;
		}
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
		if(isCache)
			cache.empty(dbname);
		return autopackage.updateMethod(sql, obj);
	}

	@Override
	public boolean updateMethod(Method method, String sql, Object[] obj) {
		if(isCache)
			cache.empty(dbname);
		return autopackage.updateMethod(method,sql, obj);
	}

	@Override
	public boolean updateBatch(String sql, Object[][] obj) {
		if(isCache)
			cache.empty(dbname);
		return autopackage.updateBatch(sql, obj);
	}

	@Override
	public boolean updateBatch(String... completeSqls) {
		if(isCache)
			cache.empty(dbname);
		return autopackage.updateBatch(completeSqls);
	}


	@Override
	public void clear() {
		cache.empty(dbname);
		
	}

}

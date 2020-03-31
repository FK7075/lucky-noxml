package com.lucky.jacklamb.sqlcore.abstractionlayer.fixedcoreImpl;

import java.util.Collection;
import java.util.List;

import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.GeneralObjectCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.StatementCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.GeneralSqlGenerator;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PrecompileSqlAndObject;

@SuppressWarnings("unchecked")
public final class GeneralObjectCoreImpl implements GeneralObjectCore {
	
	private GeneralSqlGenerator gcg;
	
	private StatementCore statementCore;
	
	
	public GeneralObjectCoreImpl(StatementCore statementCore) {
		gcg=new GeneralSqlGenerator();
		this.statementCore=statementCore;
	}
	
	public <T> T getOne(Class<T> c, Object id) {
		String ysql = gcg.getOneSql(c);
		return statementCore.getObject(c, ysql, id);
	}

	public <T> T getObject(T t) {
		PrecompileSqlAndObject select = gcg.singleSelect(t);
		String ysql = select.getPrecompileSql();
		Object[] objects=select.getObjects().toArray();
		return (T) statementCore.getObject(t.getClass(), ysql, objects);
	}

	public <T> List<T> getList(T t) {
		PrecompileSqlAndObject select = gcg.singleSelect(t);
		String ysql = select.getPrecompileSql();
		Object[] objects=select.getObjects().toArray();
		return (List<T>) statementCore.getList(t.getClass(), ysql, objects);
	}

	public <T> int count(T t) {
		PrecompileSqlAndObject select = gcg.singleCount(t);
		String ysql = select.getPrecompileSql();
		Object[] objects=select.getObjects().toArray();
		return statementCore.getObject(int.class, ysql, objects);
	}

	public <T> boolean delete(T t) {
		PrecompileSqlAndObject delete = gcg.singleDelete(t);
		return statementCore.update(delete.getPrecompileSql(), delete.getObjects().toArray());
	}

	public <T> boolean update(T t,String...conditions) {
		PrecompileSqlAndObject update = gcg.singleUpdate(t,conditions);
		return statementCore.update(update.getPrecompileSql(), update.getObjects().toArray());
	}

	public boolean deleteBatchByArray(Object... obj) {
		for(Object o:obj)
			delete(o);
		return true;
	}
	
	public <T> boolean deleteBatchByCollection(Collection<T> collection) {
		for(T o:collection)
			delete(o);
		return true;
	}

	public boolean updateBatchByArray(Object... obj) {
		for(Object o:obj)
			update(o);
		return true;
	}

	public <T> boolean updateBatchByCollection(Collection<T> collection) {
		for(Object o:collection)
			update(o);
		return true;
	}

	public boolean delete(Class<?> clazz, Object id) {
		String ysql = gcg.deleteOneSql(clazz);
		return statementCore.update(ysql, id);
	}

	public boolean deleteBatchByID(Class<?> clazz, Object... ids) {
		String ysql =gcg.deleteIn(clazz, ids);
		return statementCore.update(ysql, ids);
	}

	@Override
	public <T> boolean insert(T pojo) {
		PrecompileSqlAndObject insert=gcg.singleInsert(pojo);
		return statementCore.update(insert.getPrecompileSql(), insert.getObjects().toArray());
	}

}

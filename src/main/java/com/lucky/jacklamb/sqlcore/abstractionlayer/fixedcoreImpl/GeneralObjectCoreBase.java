package com.lucky.jacklamb.sqlcore.abstractionlayer.fixedcoreImpl;

import java.lang.reflect.Field;
import java.util.*;

import com.lucky.jacklamb.annotation.orm.Id;
import com.lucky.jacklamb.utils.reflect.FieldUtils;
import com.lucky.jacklamb.enums.PrimaryType;
import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.GeneralObjectCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.StatementCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.UniqueSqlCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.CreateSql;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.GeneralSqlGenerator;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PojoManage;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PrecompileSqlAndObject;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;

@SuppressWarnings("unchecked")
public abstract class GeneralObjectCoreBase implements GeneralObjectCore, UniqueSqlCore {
	
	private GeneralSqlGenerator gcg;
	
	protected StatementCore statementCore;

	protected LuckyDataSource dataSource;

	protected String dbname;
	
	
	public GeneralObjectCoreBase(String dbname) {
		this.dbname=dbname;
		gcg=new GeneralSqlGenerator();
		this.dataSource= ReaderInI.getDataSource(dbname);
		this.statementCore=new StatementCoreImpl(dataSource);
	}

	@Override
	public <T> T getOne(Class<T> c, Object id) {
		String ysql = gcg.getOneSql(c);
		return statementCore.getObject(c, ysql, id);
	}

	@Override
	public <T> T getObject(T t) {
		PrecompileSqlAndObject select = gcg.singleSelect(t);
		String ysql = select.getPrecompileSql();
		Object[] objects=select.getObjects().toArray();
		return (T) statementCore.getObject(t.getClass(), ysql, objects);
	}

	@Override
	public <T> List<T> getList(T t) {
		PrecompileSqlAndObject select = gcg.singleSelect(t);
		String ysql = select.getPrecompileSql();
		Object[] objects=select.getObjects().toArray();
		return (List<T>) statementCore.getList(t.getClass(), ysql, objects);
	}

	@Override
	public <T> int count(T t) {
		PrecompileSqlAndObject select = gcg.singleCount(t);
		String ysql = select.getPrecompileSql();
		Object[] objects=select.getObjects().toArray();
		return statementCore.getObject(int.class, ysql, objects);
	}

	@Override
	public <T> boolean delete(T t) {
		PrecompileSqlAndObject delete = gcg.singleDelete(t);
		return statementCore.update(delete.getPrecompileSql(), delete.getObjects().toArray());
	}

	@Override
	public <T> boolean update(T t,String...conditions) {
		PrecompileSqlAndObject update = gcg.singleUpdate(t,conditions);
		return statementCore.update(update.getPrecompileSql(), update.getObjects().toArray());
	}

	@Override
	public boolean deleteBatchByArray(Object... obj) {
		List<Object> objects = Arrays.asList(obj);
		return deleteBatchByCollection(objects);
	}

	@Override
	public <T> boolean deleteBatchByCollection(Collection<T> collection) {
		PrecompileSqlAndObject delete;
		List<String> completeSqls=new ArrayList<>();
		for (T t : collection) {
			delete = gcg.singleDelete(t);
			completeSqls.add(CreateSql.getCompleteSql(delete.getPrecompileSql(),delete.getObjects().toArray()));
		}
		String[] sqls=new String[completeSqls.size()];
		completeSqls.toArray(sqls);
		return statementCore.updateBatch(sqls);
	}

	@Override
	public boolean updateBatchByArray(Object... obj) {
		List<Object> objects = Arrays.asList(obj);
		return updateBatchByCollection(objects);
	}

	@Override
	public <T> boolean updateBatchByCollection(Collection<T> collection) {
		PrecompileSqlAndObject update;
		List<String> completeSqls=new ArrayList<>();
		for (T t : collection) {
			update = gcg.singleUpdate(t);
			completeSqls.add(CreateSql.getCompleteSql(update.getPrecompileSql(),update.getObjects().toArray()));
		}
		String[] sqls=new String[completeSqls.size()];
		completeSqls.toArray(sqls);
		return statementCore.updateBatch(sqls);
	}

	@Override
	public boolean delete(Class<?> clazz, Object id) {
		String ysql = gcg.deleteOneSql(clazz);
		return statementCore.update(ysql, id);
	}

	@Override
	public boolean deleteByIdIn(Class<?> clazz, Object[] ids) {
		String ysql =gcg.deleteIn(clazz, ids);
		return statementCore.update(ysql, ids);
	}

	@Override
	public <T> List<T> getByIdIn(Class<T> clazz, Object[] ids) {
		String ysql =gcg.selectIn(clazz, ids);
		return statementCore.getList(clazz,ysql, ids);
	}

	@Override
	public <T> boolean insert(T pojo) {
		PrecompileSqlAndObject insert=gcg.singleInsert(pojo);
		return statementCore.update(insert.getPrecompileSql(), insert.getObjects().toArray());
	}

	@Override
	public <T> boolean insertSetIdBatchByArray(Object... obj) {
		return insertBatchByCollection(Arrays.asList(obj));
	}

	@Override
	public <T> boolean insertBatchByCollection(Collection<T> collection) {
		PrecompileSqlAndObject insert;
		List<String> completeSqls=new ArrayList<>();
		for (T t : collection) {
			insert=gcg.singleInsert(t);
			completeSqls.add(CreateSql.getCompleteSql(insert.getPrecompileSql(),insert.getObjects().toArray()));
		}
		setUUID(completeSqls);
		String[] sqls=new String[completeSqls.size()];
		completeSqls.toArray(sqls);
		return statementCore.updateBatch(sqls);
	}

	protected <T> void setUUID(Collection<T> collection){
		Class<?> pClass=null;
		boolean isUUID=false;
		Field idField=null;
		for (T t : collection) {
			if (pClass == null) {
				pClass = t.getClass();
				idField = PojoManage.getIdField(pClass);
				isUUID = idField.getAnnotation(Id.class).type() == PrimaryType.AUTO_UUID;
			}
			if (isUUID && FieldUtils.getValue(t, idField) == null)
				FieldUtils.setValue(t, idField, UUID.randomUUID().toString());
		}
	}
}

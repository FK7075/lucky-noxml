package com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore;

import com.lucky.jacklamb.enums.PrimaryType;
import com.lucky.jacklamb.mapper.LuckyMapperProxy;
import com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl.MySqlCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.fixedcoreImpl.GeneralObjectCoreImpl;
import com.lucky.jacklamb.sqlcore.abstractionlayer.fixedcoreImpl.StatementCoreImpl;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PojoManage;
import com.lucky.jacklamb.sqlcore.c3p0.DataSource;
import com.lucky.jacklamb.sqlcore.c3p0.ReadIni;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 对所有关系型数据库操作的抽象，本抽象类聚合对StatementCore接口和GeneralObjectCore接口的所有实现，
 * 对UniqueSqlCore接口的方法留给其子类去实现
 * @author fk-7075
 *
 */
public abstract class SqlCore implements UniqueSqlCore {
	
	protected Logger log =Logger.getLogger(MySqlCore.class);
	
	protected String dbname;
	
	protected DataSource dataSource;

	protected StatementCore statementCore;
	
	protected GeneralObjectCore generalObjectCore;
	
	
	public SqlCore(String dbname) {
		this.dbname=dbname;
		this.dataSource=ReadIni.getDataSource(dbname);
		this.statementCore=new StatementCoreImpl(dataSource);
		this.generalObjectCore=new GeneralObjectCoreImpl(this.statementCore);
	}
	
	/**
	 * ID查询
	 * @param pojoClass
	 * 包装类的Class
	 * @param id
	 * @return
	 */
	public <T> T getOne(Class<T> pojoClass, Object id) {
		log.debug("Run ==> getOne([Class<T>]"+pojoClass+",[Object]"+id+")");
		return generalObjectCore.getOne(pojoClass, id);
	}
	
	/**
	 * 对象方式获得单个对象
	 * @param pojo
	 * @return
	 */
	public <T> T getObject(T pojo) {
		log.debug("Run ==> getObject([T]"+pojo+")");
		return generalObjectCore.getObject(pojo);
	}
	
	/**
	 * 对象查询
	 * @param pojo
	 * 对象
	 * @return
	 */
	public <T> List<T> getList(T pojo){
		log.debug("Run ==> getList([T]"+pojo+")");
		return generalObjectCore.getList(pojo);
	}
	
	/**
	 * 查询class对应表中得所有数据
	 * @param clzz
	 * @return
	 */
	public <T> List<T> getList(Class<T> clzz){
		log.debug("Run ==> getList([Class<T>]"+clzz+")");
		return generalObjectCore.getList(clzz);
	}
	
	/**
	 * 条件数据统计
	 * @param pojo
	 * @return
	 */
	public <T> int count(T pojo) {
		log.debug("Run ==> count([T]"+pojo+")");
		return generalObjectCore.count(pojo);
	}
	
	/**
	 * 总数统计
	 * @param clzz
	 * @return
	 */
	public <T> int count(Class<T> clzz) {
		log.debug("Run ==> count([Class<T>]"+clzz+")");
		return generalObjectCore.count(clzz);
	}
	
	
	/**
	 * 删除数据
	 * @param pojo
	 * 包含删除信息的包装类的对象
	 * @return
	 */
	public <T> boolean delete(T pojo) {
		log.debug("Run ==> delete([T]"+pojo+")");
		return generalObjectCore.delete(pojo);
	}
	
	/**
	 * 跟新操作
	 * @param pojo 实体类对象
	 * @param conditions 作为更新条件的字段(支持多值，缺省默认使用Id字段作为更新条件)
	 * @return
	 */
	public <T> boolean updateByPojo(T pojo,String...conditions) {
		log.debug("Run ==> updateByPojo([T]"+pojo+",[String...]"+Arrays.toString(conditions)+")");
		return generalObjectCore.update(pojo,conditions);
	}
	
	/**
	 * 批量删除-数组模式
	 * @param pojos
	 * 包含删除信息的对象数组
	 * @return
	 */
	public boolean deleteBatchByArray(Object...pojos) {
		log.debug("Run ==> deleteBatchByArray([Object...]"+pojos+")");
		return generalObjectCore.deleteBatchByArray(pojos);
	}
	
	
	/**
	 * 批量更新-数组模式
	 * @param pojos
	 * 包含更新信息的对象数组
	 * @return
	 */
	public boolean updateBatchByArray(Object...pojos) {
		log.debug("Run ==> updateBatchByArray([Object...]"+Arrays.toString(pojos)+")");
		return generalObjectCore.updateBatchByArray(pojos);
	}
	
	/**
	 * 批量删除-集合模式
	 * @param pojoCollection 要操作的对象所组成的集合
	 * @return false or true
	 */
	public <T> boolean deleteBatchByCollection(Collection<T> pojoCollection) {
		log.debug("Run ==> deleteBatchByCollection([Collection<T>]"+pojoCollection+")");
		return generalObjectCore.deleteBatchByCollection(pojoCollection);
	}
	
	
	/**
	 * 批量更新-集合模式
	 * @param pojoCollection 要操作的对象所组成的集合
	 * @return false or true
	 */
	public <T> boolean updateBatchByCollection(Collection<T> pojoCollection) {
		log.debug("Run ==> updateBatchByCollection([Collection<T>]"+pojoCollection+")");
		return generalObjectCore.updateBatchByCollection(pojoCollection);
	}
	
	/**
	 * SQL查询
	 * @param pojoClass
	 * 包装类的Class
	 * @param sql
	 * 预编译的sql语句
	 * @param obj
	 * @return
	 */
	public <T> List<T> getList(Class<T> pojoClass, String sql, Object... obj){
		log.debug("Run ==> getList([Class<T>]"+pojoClass+",[String]"+sql+",[Object...]"+Arrays.toString(obj)+")");
		return statementCore.getList(pojoClass, sql, obj);
	}
	
	/**
	 * 预编译SQL方式获得单一对象
	 * @param pojoClass
	 * @param sql
	 * @param obj
	 * @return
	 */
	public <T> T getObject(Class<T> pojoClass,String sql,Object...obj) {
		log.debug("Run ==> getObject([Class<T>]"+pojoClass+",[String]"+sql+",[Object...]"+Arrays.toString(obj)+")");
		return statementCore.getObject(pojoClass, sql, obj);
	}
	
	/**
	 * 预编译SQL非查询操作
	 * @param sql
	 * @param obj
	 * @return
	 */
	public boolean update(String sql,Object...obj) {
		log.debug("Run ==> update([String]"+sql+",[Object...]"+Arrays.toString(obj)+")");
		return statementCore.update(sql, obj);
	}
	
	/**
	 * id删除
	 * @param pojoClass
	 * 所操作类
	 * @param id
	 * id值
	 * @return
	 */
	public boolean delete(Class<?> pojoClass,Object id) {
		log.debug("Run ==> delete([Class<?>]"+pojoClass+",[Object]"+id+")");
		return generalObjectCore.delete(pojoClass, id);
	}
	
	/**
	 * 批量ID删除
	 * @param pojoClass 要操作表对应类的Class
	 * @param ids 要删除的id所组成的集合
	 * @return
	 */
	public boolean deleteBatchByID(Class<?> pojoClass,Object...ids) {
		log.debug("Run ==> deleteBatchByID([Class<?>]"+pojoClass+",[Object...]"+Arrays.toString(ids)+")");
		return generalObjectCore.deleteBatchByID(pojoClass, ids);
	}
	
	
	/**
	 * 批量SQL非查询操作
	 * @param sql
	 * 模板预编译SQL语句
	 * @param obj
	 * 填充占位符的一组组对象数组组成的二维数组
	 * [[xxx],[xxx],[xxx]]
	 * @return
	 */
	public boolean updateBatch(String sql,Object[][] obj) {
		log.debug("Run ==> updateBatch([String]"+sql+",[Object[][]]"+Arrays.toString(obj)+")");
		return statementCore.updateBatch(sql, obj);
	}
	
	/**
	 * 得到当前SqlCore对象对应的数据源的dbname
	 * @return
	 */
	public String getDbName() {
		log.debug("Run ==> getDbName()");
		return dataSource.getName();
	}
	
	/**
	 * 清空缓存
	 */
	public final void clear() {
		log.debug("Run ==> clear()");
		statementCore.clear();
		
	}

	/**
	 * Mapper接口式开发,返回该接口的代理对象
	 * @param clazz Mapper接口的Class
	 * @return Mapper接口的代理对象
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMapper(Class<T> clazz) {
		log.debug("Run ==> getMapper([Class<T>]"+clazz+")");
		LuckyMapperProxy mapperProxy = new LuckyMapperProxy(this);
		Object obj = null;
		try {
			obj = mapperProxy.getMapperProxyObject(clazz);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (T) obj;
	}

	@Override
	public <T> boolean insert(T t) {
		log.debug("Run ==> insert([T]"+t+")");
		if(PojoManage.getIdType(t.getClass())==PrimaryType.AUTO_UUID)
			setNextUUID(t);
		generalObjectCore.insert(t);
		return true;
	}
	
	@Override
	public <T> boolean insertSetId(T t) {
		log.debug("Run ==> insertSetId([T]"+t+")");
		insert(t);
		if(PojoManage.getIdType(t.getClass())==PrimaryType.AUTO_INT)
			setNextId(t);
		return true;
	}

	@Override
	public boolean insertBatchByArray(Object... obj) {
		log.debug("Run ==> insertBatchByArray([Object...]"+Arrays.toString(obj)+")");
		for(Object pojo:obj) {
			insert(pojo);
		}
		return true;
	}
	
	@Override
	public boolean insertSetIdBatchByArray( Object... obj) {
		log.debug("Run ==> insertSetIdBatchByArray([Object...]"+Arrays.toString(obj)+")");
		for(Object pojo:obj) {
			insertSetId(pojo);
		}
		return true;
	}
	
	public void setNextUUID(Object pojo) {
		log.debug("Run ==> setNextUUID([Object]"+pojo+")");
		Field idField=PojoManage.getIdField(pojo.getClass());
		idField.setAccessible(true);
		try {
			idField.set(pojo, UUID.randomUUID().toString());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

package com.lucky.jacklamb.sqlcore.mapper;

import com.lucky.jacklamb.annotation.orm.mapper.*;
import com.lucky.jacklamb.query.QueryBuilder;

import java.util.Collection;
import java.util.List;

/**
 * 单表操作的Mapper接口模板，可以用来简化Mapper接口开发
 * @author fk-7075
 * @param <T> 表对应的实体类
 */
public interface LuckyMapper<T> {
	
	/**
	 * 根据ID得到一条记录
	 * @param id 主键id
	 * @return T
	 */
	@Select(byid=true)
	public T selectById(Object id);
	
	/**
	 * 根据ID删除一条记录
	 * @param id 主键id
	 * @return
	 */
	@Delete(byid=true)
	public boolean deleteById(Object id);
	
	/**
	 * 对象删除
	 * @param pojo
	 * @return
	 */
	@Delete
	public boolean delete(T pojo);

	/**
	 * 查询操作
	 * @param pojo 包含查询信息的pojo对象
	 * @return 对应类型的查询结果
	 */
	@Select
	public T select(T pojo);
	
	/**
	 * 查询操作
	 * @param pojo 包含查询信息的pojo对象
	 * @return  对应类型集合的查询结果
	 */
	@Select
	public List<T> selectList(T pojo);
	
	/**
	 * 查询Class对应表的所有数据
	 * @return
	 */
	public List<T> selectList();
	
	/**
	 * 更新操作
	 * @param pojo 包含更新信息的pojo对象
	 * @return 
	 */
	@Update
	public boolean update(T pojo);
	
	/**
	 * 添加操作，并自动获取自增ID
	 * @param pojo 包含添加信息的pojo对象
	 * @return
	 */
	@Insert(setautoId=true)
	public boolean insertAutoID(T pojo);
	
	/**
	 * 添加操作
	 * @param pojo 包含添加信息的pojo对象
	 * @return
	 */
	@Insert
	public boolean insert(T pojo);
	
	/**
	 * 批量添加操作
	 * @param pojos 包含添加信息的List[pojo]集合
	 * @return
	 */
	@Insert(batch=true)
	public boolean batchInsert(Collection<T> pojos);

	/**
	 * 批量更新
	 * @param pojos 包含添加信息的List[pojo]集合
	 * @return
	 */
	@Update(batch = true)
	public boolean batchUpdate(Collection<T> pojos);

	/**
	 * 批量删除
	 * @param pojos 包含添加信息的List[pojo]集合
	 * @return
	 */
	@Delete(batch = true)
	public boolean batchDelete(Collection<T> pojos);
	
	/**
	 * 分页操作
	 * @param pojo 包含查询信息的pojo对象
	 * @param page 页码
	 * @param rows 每页显示的条数
	 * @return
	 */
	@Query(limit=true)
	public List<T> selectLimit(T pojo,int page,int rows);
	
	/**
	 * QueryBuilder查询模式
	 * @param queryBuilder QueryBuilder对象
	 * @return
	 */
	@Query(queryBuilder=true)
	public List<T> query(QueryBuilder queryBuilder);
	
	/**
	 * Count操作
	 * @param pojo 包含查询信息的pojo对象
	 * @return
	 */
	@Count
	public int count(T pojo);
	
	/**
	 * 总数统计
	 * @return
	 */
	public int count();

	/**
	 * 创建啊实体对应的数据库表
	 */
	public void createTable();

	/**
	 * 根据ID批量删除数据
	 * @param ids
	 * @return
	 */
	public boolean deleteByIdIn(List<?> ids);

	/**
	 * 根据ID批量查询数据
	 * @param ids
	 * @return
	 */
	public List<T> selectByIdIn(List<?> ids);
	
}
package com.lucky.jacklamb.mapper;

import java.util.Collection;
import java.util.List;

import com.lucky.jacklamb.annotation.orm.mapper.Count;
import com.lucky.jacklamb.annotation.orm.mapper.Delete;
import com.lucky.jacklamb.annotation.orm.mapper.Insert;
import com.lucky.jacklamb.annotation.orm.mapper.Query;
import com.lucky.jacklamb.annotation.orm.mapper.Select;
import com.lucky.jacklamb.annotation.orm.mapper.Update;
import com.lucky.jacklamb.query.QueryBuilder;

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
	 * @param pojoClass
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
	 * @param pojoClass
	 * @return
	 */
	public int count();
	
}

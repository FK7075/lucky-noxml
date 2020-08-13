package com.lucky.jacklamb.sqlcore.activerecord;

import com.lucky.jacklamb.annotation.orm.NoColumn;
import com.lucky.jacklamb.annotation.orm.NoPackage;
import com.lucky.jacklamb.query.QueryBuilder;
import com.lucky.jacklamb.sqlcore.jdbc.SqlCoreFactory;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.util.PojoManage;
import com.lucky.jacklamb.utils.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;


/**
 * ActiveRecord操作模式的基本实体类
 * @author fk7075
 * @date 2020/8/13 9:35
 * @version 1.0
 */
public abstract class BaseEntity<Entity> {

    @NoPackage
    @NoColumn
    private SqlCore sqlCore;

    @NoPackage
    @NoColumn
    private final Field idField= PojoManage.getIdField(this.getClass());

    public BaseEntity(){
        sqlCore= SqlCoreFactory.createSqlCore();
    }

    protected void setSqlCore(SqlCore sqlCore){
        this.sqlCore=sqlCore;
    }

    /**
     * 设置数据源内核
     * @param dbName
     */
    protected void setDbName(String dbName){
        sqlCore=SqlCoreFactory.createSqlCore(dbName);
    }

    /**
     * 创建实体对应的数据库表
     */
    protected void createTable(){
        sqlCore.createTable(this.getClass());
    }

    /**
     * 添加一条记录
     * @param entity 实体
     * @return
     */
    protected int insert(Entity entity){
        return sqlCore.insert(entity);
    }

    /**
     * 将当前实体添加到数据库
     * @return
     */
    protected int insert(){
        return sqlCore.insert(this);
    }

    /**
     * 批量添加
     * @param entities
     * @return
     */
    protected int insert(Collection<Entity> entities){
        return sqlCore.insertByCollection(entities);
    }

    /**
     * 添加并返回ID
     * @param entity
     * @return
     */
    protected int insertSetId(Entity entity){
        return sqlCore.insertSetId(entity);
    }

    /**
     * 添加并返回ID
     * @return
     */
    protected int insertSetId(){
        return sqlCore.insertSetId(this);
    }

    /**
     * ID删除
     * @param id 字段ID
     * @return
     */
    protected int deleteById(Object id){
        return sqlCore.delete(this.getClass(),id);
    }

    /**
     * 使用当前ID的ID删除
     * @return
     */
    protected int deleteById(){
        return sqlCore.delete(this.getClass(),FieldUtils.getValue(this,idField));
    }

    /**
     * 实体删除
     * @param entity 要删除的实体
     * @return
     */
    protected int delete(Entity entity){
        return sqlCore.delete(entity);
    }

    /**
     * 使用当前实体的实体删除
     * @return
     */
    protected int delete(){
        return sqlCore.delete(this);
    }

    /**
     * 批量删除
     * @param entities
     * @return
     */
    protected int delete(Collection<Entity> entities){
        return sqlCore.deleteByCollection(entities);
    }

    /**
     * 根据ID的批量删除
     * @param ids 要删除数据的ID集合
     * @return
     */
    protected int batchDeleteById(Object ...ids){
        return sqlCore.deleteByIdIn(this.getClass(),ids);
    }

    /**
     * 实体更新
     * @param entity 实体
     * @return
     */
    protected int update(Entity entity){
        return sqlCore.update(entity);
    }

    /**
     * 使用当前实体的实体更新
     * @return
     */
    protected int update(){
        return sqlCore.update(this);
    }

    /**
     * 实体更新，指定更新条件
     * @param entity 实体
     * @param conditions WHERE条件字段名
     * @return
     */
    protected int updateByColumn(Entity entity,String...conditions){
        return sqlCore.update(entity,conditions);
    }


    /**
     * 使用当前实体的实体更新，指定更新条件
     * @param conditions WHERE条件字段名
     * @return
     */
    protected int updateByColumn(String...conditions){
        return sqlCore.update(this,conditions);
    }


    /**
     * ID查询
     * @param id 字段ID
     * @return
     */
    protected Entity selectById(Object id){
        return (Entity) sqlCore.getOne(this.getClass(),id);
    }

    /**
     * 使用当前ID的ID查询
     * @return
     */
    protected Entity selectById(){
        return (Entity) sqlCore.getOne(this.getClass(), FieldUtils.getValue(this,idField));
    }

    /**
     * 实体查询,返回单一对象
     * @param entity 要查询的实体
     * @return
     */
    protected Entity selectOne(Entity entity){
        return sqlCore.getObject(entity);
    }

    /**
     * 使用当前实体的实体查询,返回单一对象
     * @return
     */
    protected Entity selectOne(){
        return (Entity) sqlCore.getObject(this);
    }

    /**
     * 实体查询,返回List集合
     * @param entity 要查询的实体
     * @return
     */
    protected List<Entity> select(Entity entity){
        return sqlCore.getList(entity);
    }

    /**
     * 使用当前实体的实体查询,返回List集合
     * @return
     */
    protected List<Entity> select(){
        return (List<Entity>) sqlCore.getList(this);
    }

    /**
     * 全表内容查询
     * @return
     */
    protected List<Entity> selectAll(){
        return (List<Entity>) sqlCore.getList(this.getClass());
    }

    /**
     * 分页查询
     * @param entity 实体
     * @param page 页码
     * @param rows 每页记录数
     * @return
     */
    protected List<Entity> limit(Entity entity,int page,int rows){
        return sqlCore.getPageList(entity,page,rows);
    }

    /**
     * 使用当前实体的分页查询
     * @param page 页码
     * @param rows 每页记录数
     * @return
     */
    protected List<Entity> limit(int page,int rows){
        return (List<Entity>) sqlCore.getPageList(this,page,rows);
    }

    /**
     * 查询实体Count
     * @param entity 实体
     * @return
     */
    protected int selectCount(Entity entity){
        return sqlCore.count(entity);
    }

    /**
     * 使用当前实体的查询实体Count
     * @return
     */
    protected int selectCount(){
        return sqlCore.count(this);
    }

    /**
     * 查询全表的Count
     * @return
     */
    protected int count(){
        return sqlCore.count(this.getClass());
    }

    /**
     * QueryBuilder查询
     * @param queryBuilder QueryBuilder对象
     * @return
     */
    protected List<Entity> query(QueryBuilder queryBuilder){
        return (List<Entity>) sqlCore.query(queryBuilder,this.getClass());
    }


    /**
     * 预编译SQL查询
     * @param sql 预编译SQL
     * @param params SQl参数
     * @return
     */
    protected List<Entity> query(String sql,Object...params){
        return (List<Entity>) sqlCore.getList(this.getClass(),sql,params);
    }

    /**
     * 预编译SQL的非查询操作
     * @param sql 预编译SQL
     * @param params SQl参数
     * @return
     */
    protected int edit(String sql,Object...params){
        return sqlCore.updateBySql(sql,params);
    }




}

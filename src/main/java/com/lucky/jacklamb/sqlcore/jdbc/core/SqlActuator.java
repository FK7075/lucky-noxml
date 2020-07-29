package com.lucky.jacklamb.sqlcore.jdbc.core;

import com.lucky.jacklamb.sqlcore.abstractionlayer.cache.LRUCache;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.CreateSql;
import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;

public abstract class SqlActuator {

    protected LuckyDataSource dataSource;

    protected String dbname;

    protected LRUCache<String,List<?>> lruCache;

    protected boolean isCache;

    public SqlActuator(String dbname) {
        this.dataSource=ReaderInI.getDataSource(dbname);
        isCache= ReaderInI.getDataSource(dbname).getCache();
        //如果用户开启了缓存配置，测初始化一个LRU缓存
        if(isCache)
            lruCache=new LRUCache<>(ReaderInI.getDataSource(dbname).getCacheCapacity());
        //初始化数据源
        dataSource.init();
    }

    /**
     * 自动将查询结果集中的内容封装起来
     * @param c 封装类的Class对象
     * @param sql 预编译的sql语句
     * @param obj 替换占位符的数组
     * @return 返回一个泛型的List集合
     */
    public abstract <T> List<T> autoPackageToList(Class<T> c, String sql, Object... obj);

    /**
     * 执行一个非查询语句，返回此次操作影响的行数
     * @param sql 预编译的sql语句
     * @param obj 替换占位符的数组
     * @return 受影响的行数
     */
    public abstract int update(String sql, Object...obj);

    /**
     *  自动将查询结果集中的内容封装起来
     * @param c 封装类的Class对象
     * @param method findBy语法方法
     * @param sql 预编译的sql语句
     * @param obj 替换占位符的数组
     * @param <T>
     * @return
     */
    public abstract <T> List<T>  autoPackageToListMethod(Class<T> c, Method method, String sql, Object[] obj);

    /**
     * 执行一个非查询语句，返回此次操作影响的行数
     * @param method findBy语法方法
     * @param sql 预编译的sql语句
     * @param obj 替换占位符的数组
     * @return
     */
    public abstract int updateMethod(Method method, String sql, Object[]obj);

    /**
     * 基于PreparedStatement的批量操作
     * @param sql 预编译SQL
     * @param obj 替换占位符的数组
     * @return
     */
    public abstract int[] updateBatch(String sql,Object[][] obj);

    /**
     * 基于Statement的批量操作
     * @param completeSqls 完整的SQL语句集合
     * @return
     */
    public abstract int[] updateBatch(String...completeSqls);


    /**
     * 从LRU缓存中查询结果
     * @param sp
     * @param c
     * @param <T>
     * @return
     */
    public <T> List<T> queryCache(SqlAndParams sp,Class<T> c){
        Connection connection = dataSource.getConnection();
        String completeSql= CreateSql.getCompleteSql(sp.precompileSql,sp.params);
        if(lruCache.containsKey(completeSql)){
            return (List<T>) lruCache.get(completeSql);
        }else{
            SqlOperation sqlOperation=new SqlOperation(connection,dataSource.getDbname());
            List<?> result = sqlOperation.autoPackageToList(c, sp.precompileSql, sp.params);
            lruCache.put(completeSql,result);
            return (List<T>) result;
        }
    }

    /**
     * 清空缓存
     */
    public void clear(){
        lruCache.clear();
    }
}

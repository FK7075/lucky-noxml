package com.lucky.jacklamb.sqlcore.jdbc.core;

import com.lucky.jacklamb.sqlcore.abstractionlayer.cache.LRUCache;
import com.lucky.jacklamb.sqlcore.abstractionlayer.transaction.Transaction;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.CreateSql;
import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SqlActuator {

    protected LuckyDataSource dataSource;

    protected String dbname;

    protected static Map<String,LRUCache<String,List<?>>> lruCache=new HashMap<>();

    protected boolean isCache;

    public SqlActuator(String dbname) {
        this.dbname=dbname;
        this.dataSource=ReaderInI.getDataSource(dbname);
        isCache= ReaderInI.getDataSource(dbname).getCache();
        //初始化数据源
        dataSource.init();
        //如果用户开启了缓存配置，测初始化一个LRU缓存
        if(isCache&&!lruCache.containsKey(dbname)){
            LRUCache<String,List<?>> dbCache=new LRUCache<>(ReaderInI.getDataSource(dbname).getCacheCapacity());
            lruCache.put(dbname,dbCache);
        }
//            lruCache=new LRUCache<>(ReaderInI.getDataSource(dbname).getCacheCapacity());
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
    public abstract <T> List<T> queryCache(SqlAndParams sp,Class<T> c);

    public abstract Transaction openTransaction();

    public abstract Transaction openTransaction(int isolationLevel);

    /**
     * 清空缓存
     */
    public void clear(){
        lruCache.get(dbname).clear();
    }
}

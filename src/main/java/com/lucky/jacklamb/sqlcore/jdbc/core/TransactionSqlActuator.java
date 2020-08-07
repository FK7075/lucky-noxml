package com.lucky.jacklamb.sqlcore.jdbc.core;

import com.lucky.jacklamb.sqlcore.abstractionlayer.transaction.JDBCTransaction;
import com.lucky.jacklamb.sqlcore.abstractionlayer.transaction.Transaction;
import com.lucky.jacklamb.sqlcore.util.CreateSql;

import java.lang.reflect.Method;
import java.util.List;

public class TransactionSqlActuator extends SqlActuator {

    private Transaction tr;

    public Transaction openTransaction() {
        tr.open();
        return tr;
    }

    @Override
    public Transaction openTransaction(int isolationLevel) {
        tr.open(isolationLevel);
        return tr;
    }

    public TransactionSqlActuator(String dbname){
        super(dbname);
        tr= new JDBCTransaction(dataSource.getConnection());
    }

    @Override
    public <T> List<T> autoPackageToList(Class<T> c, String sql, Object... obj) {
        SqlAndParams sp=new SqlAndParams(sql,obj);
        if(isCache) {
            return queryCache(sp,c);
        }
        SqlOperation sqlOperation=new SqlOperation(tr.getConnection(),dbname);
        List<T> result = sqlOperation.autoPackageToList(c, sp.precompileSql, sp.params);
        return result;
    }

    @Override
    public int update(String sql, Object... obj) {
        SqlAndParams sp=new SqlAndParams(sql,obj);
        if(isCache) {
            clear();
        }
        SqlOperation sqlOperation=new SqlOperation(tr.getConnection(),dbname);
        int result = sqlOperation.setSql(sp.precompileSql, sp.params);
        return result;
    }

    @Override
    public <T> List<T> autoPackageToListMethod(Class<T> c, Method method, String sql, Object[] obj) {
        SqlAndParams sp=new SqlAndParams(method,sql,obj);
        if(isCache) {
            return queryCache(sp,c);
        }
        SqlOperation sqlOperation=new SqlOperation(tr.getConnection(),dbname);
        List<T> result = sqlOperation.autoPackageToList(c, sp.precompileSql, sp.params);
        return result;
    }

    @Override
    public int updateMethod(Method method, String sql, Object[] obj) {
        SqlAndParams sp=new SqlAndParams(method,sql,obj);
        if(isCache)
            clear();
        SqlOperation sqlOperation=new SqlOperation(tr.getConnection(),dbname);
        int result = sqlOperation.setSql(sp.precompileSql, sp.params);
        return result;
    }

    @Override
    public int[] updateBatch(String sql, Object[][] obj) {
        if(isCache) {
            clear();
        }
        SqlOperation sqlOperation=new SqlOperation(tr.getConnection(),dbname);
        int[] result = sqlOperation.setSqlBatch(sql, obj);
        return result;
    }

    @Override
    public int[] updateBatch(String... completeSqls) {
        if(isCache) {
            clear();
        }
        SqlOperation sqlOperation=new SqlOperation(tr.getConnection(),dbname);
        int[] result = sqlOperation.setSqlBatch(completeSqls);
        return result;
    }

    @Override
    public <T> List<T> queryCache(SqlAndParams sp, Class<T> c) {
        String completeSql= CreateSql.getCompleteSql(sp.precompileSql,sp.params);
        if(lruCache.get(dbname).containsKey(completeSql)){
            return (List<T>) lruCache.get(dbname).get(completeSql);
        }else{
            SqlOperation sqlOperation=new SqlOperation(tr.getConnection(),dbname);
            List<?> result = sqlOperation.autoPackageToList(c, sp.precompileSql, sp.params);
            lruCache.get(dbname).put(completeSql,result);
            return (List<T>) result;
        }
    }
}
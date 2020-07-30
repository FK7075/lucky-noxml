package com.lucky.jacklamb.aop.expandpoint;

import com.lucky.jacklamb.annotation.orm.mapper.Mapper;
import com.lucky.jacklamb.aop.proxy.Chain;
import com.lucky.jacklamb.aop.proxy.Point;
import com.lucky.jacklamb.ioc.ComponentIOC;
import com.lucky.jacklamb.sqlcore.abstractionlayer.transaction.Transaction;
import com.lucky.jacklamb.sqlcore.jdbc.SqlCoreFactory;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 事务扩展
 * @author DELL
 *
 */
public class TransactionPoint extends Point {

    private Map<Field,Object> oldFieldMapperMap;

    private static final Logger log= LogManager.getLogger(TransactionPoint.class);

    @Override
    public Object proceed(Chain chain) throws Throwable {
        if(method.isAnnotationPresent(com.lucky.jacklamb.annotation.aop.Transaction.class)){
            int isolationLevel = method.getAnnotation(com.lucky.jacklamb.annotation.aop.Transaction.class).isolationLevel();
            return transactionResult(chain,isolationLevel);
        }
        if(targetClass.isAnnotationPresent(com.lucky.jacklamb.annotation.aop.Transaction.class)){
            int isolationLevel = method.getAnnotation(com.lucky.jacklamb.annotation.aop.Transaction.class).isolationLevel();
            return transactionResult(chain,isolationLevel);
        }
        return chain.proceed();
    }

    public synchronized Object transactionResult(Chain chain,int isolationLevel){
        init();
        //开启事务
        List<Transaction> transactions = replace(isolationLevel);
        try{
            //执行方法
            Object result = chain.proceed();
            //提交事务
            transactions.stream().forEach(tr->tr.commit());
            return result;
        }catch (Throwable e){
            //回滚
            transactions.stream().forEach(tr->tr.rollback());
            log.error("\""+method+"\" 方法执行异常，已触发事务的回滚机制.....",e);
            throw new RuntimeException(e);
        }finally {
            recovery();
        }
    }

    //替换，将所有Mapper的内核替换为带事务的内核
    private List<Transaction> replace(int isolationLevel){
        Map<String,SqlCore> dbCores=new HashMap<>();
        for(Map.Entry<Field,Object> entry:oldFieldMapperMap.entrySet()){
            Class<?> mapperClass = entry.getKey().getType();
            SqlCore trCore;
            String dbname;
            if(SqlCore.class.isAssignableFrom(mapperClass)){
                dbname=((SqlCore)entry.getValue()).getDbName();
                if(!dbCores.containsKey(dbname)){
                    trCore= SqlCoreFactory.createTransactionSqlCore(dbname);
                    dbCores.put(dbname,trCore);
                }else{
                    trCore=dbCores.get(dbname);
                }
                FieldUtils.setValue(aspectObject,entry.getKey(),trCore);
            }else{
                dbname=mapperClass.getAnnotation(Mapper.class).dbname();
                if(!dbCores.containsKey(dbname)){
                    trCore= SqlCoreFactory.createTransactionSqlCore(dbname);
                    dbCores.put(dbname,trCore);
                }else{
                    trCore=dbCores.get(dbname);
                }
                FieldUtils.setValue(aspectObject,entry.getKey(),trCore.getMapper(mapperClass));
            }
        }
        return dbCores.keySet().stream().map((k)->{
            if(isolationLevel==-1){
                return dbCores.get(k).openTransaction();
            }else{
                return dbCores.get(k).openTransaction(isolationLevel);
            }
        }).collect(Collectors.toList());
    }

    //代理开始前的初始化，将类中原始的Mapper保存在全局Map中
    private void init(){
        oldFieldMapperMap=new HashMap<>();
        Field[] allFields= ClassUtils.getAllFields(targetClass);
        for (Field field : allFields) {
            Class<?> type = field.getType();
            if(type.isAnnotationPresent(Mapper.class)||SqlCore.class.isAssignableFrom(type)){
                oldFieldMapperMap.put(field, FieldUtils.getValue(aspectObject,field));
            }
        }
    }

    //代理结束后的恢复，根据之前保存的Mapper，将属性恢复回最开始的样子
    private void recovery(){
        for(Map.Entry<Field,Object> entry:oldFieldMapperMap.entrySet()){
            FieldUtils.setValue(aspectObject,entry.getKey(),entry.getValue());
        }
    }
}

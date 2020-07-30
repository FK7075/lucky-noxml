package com.lucky.jacklamb.aop.expandpoint;

import com.lucky.jacklamb.annotation.orm.mapper.Mapper;
import com.lucky.jacklamb.aop.proxy.Chain;
import com.lucky.jacklamb.aop.proxy.Point;
import com.lucky.jacklamb.sqlcore.abstractionlayer.transaction.Transaction;
import com.lucky.jacklamb.sqlcore.jdbc.SqlCoreFactory;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
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

    /*
        事务机制原理
        一.基于SqlCore对象的实现
            1.默认情况下的SqlCore是不支持事务操作的单例对象，每一次数据库操作都会开启一个新的连接
                --SqlCore sqlCore=SqlCoreFactory.createSqlCore(dbname);
            2.如果你需要让SqlCore支持事务操作，请使用：
                --SqlCore sqlCore=SqlCoreFactory.createTransactionSqlCore(dbname)；
             此时的SqlCore是支持事务的多利对象，一组数据库操作将使用同一个Connection，此时可以通过
             SqlCore对象的openTransaction()方法得到一个Transaction对象，通过该对象可以实现提交回滚
             以及设置事务的隔离级别的操作
         二.基于@Transaction注解的实现
            1.IOC容器初始化时代理发生器PointRunFactory会收集到所有被@Transaction注解标注的类，并使用TransactionPoint类
            中的方法对该类执行一个代理，TransactionPoint本质是一个环绕增强，最后代理对象会被注册到IOC容器中
            2.事务代理的执行逻辑：
                默认情况下IOC容器中的所有SqlCore对象都是不支持事务的，所以容器中所有有SqlCore对象产生的Mapper代理对象也都是
                不支持事务的，所以代理逻辑的流程如下：
                a.找到该真实类的所有SqlCore属性和Mapper属性，找到后将其保存
                b.通过a中找到的SqlCore对象与Mapper对象得到一个可以产出所有Mapper和替代有SqlCore的TransactionSqlCore
                c.将真实对象中的属性的引用指向这些TransactionSqlCore和由这些TransactionSqlCore产生的TransactionMapper对象
                d.c步骤完成后真实类中的所有不支持事务的SqlCore便已都成功的换成了支持事务的SqlCore，然后就是执行真实对象的方法了，
                但是在执行前先要开启事务管理，开启后再执行真实方法，执行完毕后再由这些Transaction对象将事务提交到各自的数据库中。
                去过在执行过程中出现异常，则执行回滚操作！
                e.事务操作结束后，将真实对象恢复为最初的状态（这样做是为了不影响其他没有被@Transaction注解所标注的方法的执行）。

     */

    private Map<Field,Object> oldFieldMapperMap;

    private static final Logger log= LogManager.getLogger(TransactionPoint.class);

    @Override
    public Object proceed(Chain chain) throws Throwable {
        if(method.isAnnotationPresent(com.lucky.jacklamb.annotation.aop.Transaction.class)){
            int isolationLevel = method.getAnnotation(com.lucky.jacklamb.annotation.aop.Transaction.class).isolationLevel();
            return transactionResult(chain,isolationLevel);
        }
        if(targetClass.isAnnotationPresent(com.lucky.jacklamb.annotation.aop.Transaction.class)){
            int isolationLevel = targetClass.getAnnotation(com.lucky.jacklamb.annotation.aop.Transaction.class).isolationLevel();
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
            //属性还原
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

package com.lucky.jacklamb.aop.expandpoint;

import com.lucky.jacklamb.annotation.orm.mapper.Mapper;
import com.lucky.jacklamb.aop.core.AopChain;
import com.lucky.jacklamb.aop.core.AopPoint;
import com.lucky.jacklamb.aop.proxy.TargetMethodSignature;
import com.lucky.jacklamb.exception.TransactionPerformException;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.quartz.ann.QuartzJobs;
import com.lucky.jacklamb.sqlcore.abstractionlayer.transaction.Transaction;
import com.lucky.jacklamb.sqlcore.jdbc.SqlCoreFactory;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 注解式事务扩展
 * @author fk7075
 * @version 1.0.0
 */
public class TransactionPoint extends AopPoint {

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
                a.找到该真实类的所有IOC组件属性，找到后将其保存到ThreadLocal<Map<Field,Object>>中
                b.从a中的属性中找以及属性对象中所嵌套的SqlCore对象，然后将原先的不支持事务的SolCore替换为支持事务操作的SqlCore，并生产新的
                  对象，然后应这些新的SqlCore(支持事务)对象替换掉原先的属性值
                c.b步骤完成后真实类中的所有不支持事务的SqlCore便已都成功的换成了支持事务的SqlCore，然后就是执行真实对象的方法了，
                但是在执行前先要开启事务管理，开启后再执行真实方法，执行完毕后再由这些Transaction对象将事务提交到各自的数据库中。
                如果在执行过程中出现异常，则执行回滚操作！
                d.事务操作结束后使用ThreadLocal<Map<Field,Object>>将真实对象恢复为最初的状态（这样做是为了不影响其他没有被@Transaction注解所标注的方法的执行）。

     */

    private final ThreadLocal<Map<Field,Object>> thlSourceFieldKVMap=new ThreadLocal();

    private static final Logger log= LogManager.getLogger(TransactionPoint.class);

    private static final Method[] objectMethod=Object.class.getDeclaredMethods();

    @Override
    public Object proceed(AopChain chain) throws Throwable {
        TargetMethodSignature targetMethodSignature = tlTargetMethodSignature.get();
        Method method=targetMethodSignature.getCurrMethod();
        Class<?> targetClass=targetMethodSignature.getTargetClass();
        //当前方法上存在@Transaction，执行事务代理
        if(method.isAnnotationPresent(com.lucky.jacklamb.annotation.aop.Transaction.class)){
            int isolationLevel = method.getAnnotation(com.lucky.jacklamb.annotation.aop.Transaction.class).isolationLevel();
            return transactionResult(chain,targetMethodSignature,isolationLevel);
        }

        //没有被@Transaction注解标注的继承自Object的方法不执行代理
        for (Method m : objectMethod) {
            if(m.equals(method))
                return chain.proceed();
        }

        //当前方法上不存在@Transaction，但是当前方法的类上存在@Transaction，同样执行事务代理
        if(targetClass.isAnnotationPresent(com.lucky.jacklamb.annotation.aop.Transaction.class)){
            int isolationLevel = targetClass.getAnnotation(com.lucky.jacklamb.annotation.aop.Transaction.class).isolationLevel();
            return transactionResult(chain,targetMethodSignature,isolationLevel);
        }
        //当前方法和类上都不存在@Transaction，执行原始逻辑(不进行事务代理)
        return chain.proceed();
    }

    //事务的执行流程
    public Object transactionResult(AopChain chain, TargetMethodSignature tms, int isolationLevel){
        //数据备份
        backup(tms);
        //替换核心，并开启事务
        List<Transaction> transactions = replaceCoreAndOpenTransaction(tms,isolationLevel);
        try{
            //执行真实方法
            Object result = chain.proceed();
            //提交事务
            transactions.stream().forEach(tr->tr.commit());
            return result;
        }catch (Throwable e){
            //回滚
            transactions.stream().forEach(tr->tr.rollback());
            String ERR="事务方法执行异常，已触发事务的回滚机制。错误位置：\""+tms.getCurrMethod()+"\"";
            log.error(ERR,e);
            throw new TransactionPerformException(e,ERR);
        }finally {
            //数据还原
            recovery(tms);
        }
    }

    //替换，将所真实对象的所有属性(包含所有属性的嵌套属性)的SqlCore替换为支持事务操作的SqlCore，并将真实对象的引用指向该对象
    private List<Transaction> replaceCoreAndOpenTransaction(TargetMethodSignature tms,int isolationLevel){
        Map<Field,Object> oldFieldMapperMap=thlSourceFieldKVMap.get();
        Object aspectObject=tms.getAspectObject();
        Map<String,SqlCore> dbCores=new HashMap<>();
        for(Map.Entry<Field,Object> entry:oldFieldMapperMap.entrySet()){
            Class<?> fieldClass = entry.getKey().getType();
            SqlCore trCore;
            String dbname;
            if(SqlCore.class.isAssignableFrom(fieldClass)){
                dbname=((SqlCore)entry.getValue()).getDbName();
                if(!dbCores.containsKey(dbname)){
                    trCore= SqlCoreFactory.createTransactionSqlCore(dbname);
                    dbCores.put(dbname,trCore);
                }else{
                    trCore=dbCores.get(dbname);
                }
                FieldUtils.setValue(aspectObject,entry.getKey(),trCore);
            }else if(fieldClass.isAnnotationPresent(Mapper.class)){
                dbname=fieldClass.getAnnotation(Mapper.class).dbname();
                if(!dbCores.containsKey(dbname)){
                    trCore= SqlCoreFactory.createTransactionSqlCore(dbname);
                    dbCores.put(dbname,trCore);
                }else{
                    trCore=dbCores.get(dbname);
                }
                FieldUtils.setValue(aspectObject,entry.getKey(),trCore.getMapper(fieldClass));
            }else{
                FieldUtils.setValue(aspectObject,entry.getKey(),getTrObject(dbCores,entry.getValue()));
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

    //获取一个全新的且支持事务操作的克隆来替代原来真实对象的原有属性
    private Object getTrObject(Map<String,SqlCore> dbCores,Object fieldObject){
        Class<?> fClass = fieldObject.getClass();
        Object copyFieldObj = copy(fieldObject);
        Field[] allFields = ClassUtils.getAllFields(fClass);
        for (Field field : allFields) {
            Class<?> fieldClass = field.getType();
            Object fieldValue=FieldUtils.getValue(copyFieldObj,field);
            SqlCore trCore;
            String dbname;
            if(SqlCore.class.isAssignableFrom(fieldClass)){
                dbname=((SqlCore)fieldValue).getDbName();
                if(!dbCores.containsKey(dbname)){
                    trCore= SqlCoreFactory.createTransactionSqlCore(dbname);
                    dbCores.put(dbname,trCore);
                }else{
                    trCore=dbCores.get(dbname);
                }
                FieldUtils.setValue(copyFieldObj,field,trCore);
            }else if(fieldClass.isAnnotationPresent(Mapper.class)){
                dbname=fieldClass.getAnnotation(Mapper.class).dbname();
                if(!dbCores.containsKey(dbname)){
                    trCore= SqlCoreFactory.createTransactionSqlCore(dbname);
                    dbCores.put(dbname,trCore);
                }else{
                    trCore=dbCores.get(dbname);
                }
                FieldUtils.setValue(copyFieldObj,field,trCore.getMapper(fieldClass));
            }else if(ApplicationBeans.createApplicationBeans().isIocBean(fieldClass)&&!fieldClass.isAnnotationPresent(QuartzJobs.class)){
                FieldUtils.setValue(copyFieldObj,field,getTrObject(dbCores,fieldValue));
            }
        }
        return copyFieldObj;
    }

    //拷贝对象得到一个新的对象
    private Object copy(Object oldObj){
        Class<?> objClass = oldObj.getClass();
        Field[] allFields = ClassUtils.getAllFields(objClass);
        Object newObj = ClassUtils.newObject(objClass);
        for (Field field : allFields) {
            if(!Modifier.isFinal(field.getModifiers()))
                FieldUtils.setValue(newObj,field,FieldUtils.getValue(oldObj,field));
        }
        return newObj;
    }

    //代理开始前的数据备份，将真实类的原始属性保存在全局变量中
    private void backup(TargetMethodSignature tms){
        Map<Field,Object> oldFieldMapperMap=new HashMap<>();
        Class<?> targetClass=tms.getTargetClass();
        Object aspectObject=tms.getAspectObject();
        Field[] allFields= ClassUtils.getAllFields(targetClass);
        for (Field field : allFields) {
            Class<?> type = field.getType();
            if((ApplicationBeans.createApplicationBeans().isIocBean(type)
            ||SqlCore.class.isAssignableFrom(type))&&!type.isAnnotationPresent(QuartzJobs.class)){
                oldFieldMapperMap.put(field, FieldUtils.getValue(aspectObject,field));
            }
        }
        thlSourceFieldKVMap.set(oldFieldMapperMap);
    }

    //代理结束后的恢复
    private void recovery(TargetMethodSignature tms){
        Map<Field,Object> oldFieldMapperMap=thlSourceFieldKVMap.get();
        Object aspectObject=tms.getAspectObject();
        for(Map.Entry<Field,Object> entry:oldFieldMapperMap.entrySet()){
            FieldUtils.setValue(aspectObject,entry.getKey(),entry.getValue());
        }
    }
}

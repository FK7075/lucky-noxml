package com.lucky.jacklamb.aop.expandpoint;

import com.lucky.jacklamb.annotation.aop.Around;
import com.lucky.jacklamb.annotation.aop.OperateLog;
import com.lucky.jacklamb.aop.core.AopChain;
import com.lucky.jacklamb.aop.proxy.TargetMethodSignature;
import com.lucky.jacklamb.servlet.core.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 日志扩展
 * @author DELL
 *
 */
public abstract class ControllerLogPoint {

    private static final Logger logger= LogManager.getLogger(ControllerLogPoint.class);

    @Around(pointCutClass = "ioc:controller",pointCutAnnotation = OperateLog.class,priority = -1)
    public Object proceed(AopChain chain, TargetMethodSignature ts,Model model) throws Throwable {
        try{
            Object result = chain.proceed();
            insert(ts,model,result);
            return result;
        }catch (Throwable e){
            logger.error("LogPointException",e);
            throw new RuntimeException(e);
        }

    }

    /**
     * 用于将日志信息持久化
     * @param ts 方法的签名信息
     * @param model 请求模型
     * @param result 真实方法的执行结果
     */
    protected abstract void insert(TargetMethodSignature ts,Model model,Object result);
}

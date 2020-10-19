package com.lucky.jacklamb.aop.expandpoint;

import com.lucky.jacklamb.annotation.aop.AfterReturning;
import com.lucky.jacklamb.annotation.aop.OperateLog;
import com.lucky.jacklamb.annotation.aop.Param;
import com.lucky.jacklamb.aop.proxy.TargetMethodSignature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 日志扩展
 * @author DELL
 *
 */
public abstract class ControllerLogPoint {

    private static final Logger log= LogManager.getLogger(ControllerLogPoint.class);

    @AfterReturning(pointCutClass = "ioc:controller", pointCutMethodAnn = OperateLog.class,priority = -1)
    public void proceed(TargetMethodSignature ts, @Param Object result,@Param("runtime")long runtime,OperateLog logAnn){


    }

    /**
     * 用于将日志信息持久化
     * @param ts 方法的签名信息
     */
    protected abstract void insert(TargetMethodSignature ts);
}

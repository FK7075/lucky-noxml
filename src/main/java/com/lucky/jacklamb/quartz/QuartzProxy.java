package com.lucky.jacklamb.quartz;

import com.lucky.jacklamb.cglib.CglibProxy;

public class QuartzProxy {

    public static <T> T getProxy(Class<T> jobClass){
        return CglibProxy.getCglibProxyObject(jobClass,new QuartzMethodInterceptor());
    }
}

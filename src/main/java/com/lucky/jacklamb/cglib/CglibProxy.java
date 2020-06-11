package com.lucky.jacklamb.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

public abstract class CglibProxy {

    public static <T> T getCglibProxyObject(Class<T> clazz, MethodInterceptor methodInterceptor){
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(methodInterceptor);
        return (T) enhancer.create();
    }
}

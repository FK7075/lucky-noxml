package com.lucky.jacklamb.httpclient.luckyclient;

import com.lucky.jacklamb.cglib.CglibProxy;

public class LuckyClientControllerProxy {

    /**
     * 获得LuckyClientController接口的代理对象
     *
     * @param feignClientControllerClass
     * @param <T>
     * @return
     */
    public static <T> T getLuckyClientControllerProxyObject(Class<T> feignClientControllerClass) {
        return CglibProxy.getCglibProxyObject(feignClientControllerClass, new LuckyClientMethodInterceptor(feignClientControllerClass));
    }


}

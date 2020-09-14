package com.lucky.jacklamb.utils.websocket;

import com.lucky.jacklamb.aop.core.AopProxyFactory;
import com.lucky.jacklamb.aop.core.PointRunFactory;
import com.lucky.jacklamb.ioc.IOCContainers;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/14 16:20
 */
public abstract class BaseWebSocket {

    public BaseWebSocket(){
        IOCContainers.injection(this);
    }
}
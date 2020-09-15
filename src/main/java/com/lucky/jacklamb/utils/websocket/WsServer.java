package com.lucky.jacklamb.utils.websocket;

import com.lucky.jacklamb.ioc.IOCContainers;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/15 16:49
 */
public abstract class WsServer {

    public WsServer(){
        IOCContainers.injection(this);
    }
}

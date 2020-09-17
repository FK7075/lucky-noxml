package com.lucky.jacklamb.utils.websocket;

import com.lucky.jacklamb.ioc.IOCContainers;

import javax.websocket.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 编程式的WebSocket服务
 *
 * @author fk7075
 * @version 1.0
 * @date 2020/9/14 16:20
 */
public abstract class WsServer extends Endpoint {

    public WsServer(){
        IOCContainers.injection(this);
    }

}
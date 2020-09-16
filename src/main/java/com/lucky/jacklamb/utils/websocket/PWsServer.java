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
public abstract class PWsServer extends Endpoint {

    public PWsServer(){
        IOCContainers.injection(this);
    }

    protected static Map<String, PWsServer> clients = new ConcurrentHashMap<>();

    protected Session currSession;

    protected String currClientId;

    public Session getCurrSession() {
        return currSession;
    }

    public String getCurrClientId() {
        return currClientId;
    }

    public static PWsServer getClient(String clientId){
        return clients.get(clientId);
    }

}
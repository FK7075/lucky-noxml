package com.lucky.jacklamb.utils.websocket;

import com.lucky.jacklamb.ioc.IOCContainers;

import javax.websocket.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  前端展示base64格式的图片
 *  <img src={`data:;base64,${base64String}`}  />
 * @author fk7075
 * @version 1.0
 * @date 2020/9/14 16:20
 */
public class BaseWsServer extends Endpoint {

    protected static Map<String,BaseWsServer> clients = new ConcurrentHashMap<>();

    protected Session currSession;

    protected String currClientId;

    public Session getCurrSession() {
        return currSession;
    }

    public String getCurrClientId() {
        return currClientId;
    }

    public BaseWsServer(){
        IOCContainers.injection(this);
    }

    public static BaseWsServer getClient(String clientId){
        return clients.get(clientId);
    }


    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

        Map<String, Object> userProperties = endpointConfig.getUserProperties();
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {

            }
        });
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        super.onError(session, throwable);
    }

}
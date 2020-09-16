package com.lucky.jacklamb.utils.websocket;

import com.lucky.jacklamb.ioc.IOCContainers;
import com.lucky.jacklamb.ioc.Injection;

/**
 * 注解式的WebSocket服务<br/>
 * 1.@ServerEndpoint：标注类<br/>
 * 2.@OnOpen：连接时执行<br/>
 * 3.@OnClose：连接断开时执行<br/>
 * 4.@OnMessage：接受到消息后执行<br/>
 * @author fk7075
 * @version 1.0
 * @date 2020/9/15 16:49
 */
public abstract class AWsServer extends Injection {

}

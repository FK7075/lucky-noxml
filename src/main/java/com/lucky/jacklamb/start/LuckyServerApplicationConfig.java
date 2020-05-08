package com.lucky.jacklamb.start;

import java.util.Set;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.log4j.Logger;

public class LuckyServerApplicationConfig implements ServerApplicationConfig {
	
	private static Logger log=Logger.getLogger(LuckyServerApplicationConfig.class);

	
	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scan) {
		for(Class<?> clzz:scan) {
			if(clzz.isAnnotationPresent(ServerEndpoint.class)) {
				log.info("@WebSocket : [mapping="+clzz.getAnnotation(ServerEndpoint.class).value()+" class="+clzz+"]");
			}
		}
		return scan;
	}

	@Override
	public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> scan) {
		// TODO Auto-generated method stub
		return null;
	}

}

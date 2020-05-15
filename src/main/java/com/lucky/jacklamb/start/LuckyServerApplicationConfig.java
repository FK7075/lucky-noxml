package com.lucky.jacklamb.start;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

public class LuckyServerApplicationConfig implements ServerApplicationConfig {

	private static final Logger log= LogManager.getLogger(LuckyServerApplicationConfig.class);

	
	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scan) {
		for(Class<?> clzz:scan) {
			if(clzz.isAnnotationPresent(ServerEndpoint.class)) {
				log.info("@WebSocket \"[mapping="+clzz.getAnnotation(ServerEndpoint.class).value()+" class="+clzz+"]\"");
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

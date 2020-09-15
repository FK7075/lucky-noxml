package com.lucky.jacklamb.start;

import com.lucky.jacklamb.annotation.mvc.LEndpoint;
import com.lucky.jacklamb.utils.base.LuckyUtils;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
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
		Set<ServerEndpointConfig> serverEndpointConfigs=new HashSet<>();
		for (Class<? extends Endpoint> aClass : scan) {
			ServerEndpointConfig serverEndpointConfig;
			if(aClass.isAnnotationPresent(LEndpoint.class)){
				LEndpoint lep=aClass.getAnnotation(LEndpoint.class);
				serverEndpointConfig = ServerEndpointConfig.Builder
						.create(aClass, lep.value())
						.configurator(ClassUtils.newObject(lep.configurator()))
						.decoders(Arrays.asList(lep.decoders()))
						.encoders(Arrays.asList(lep.encoders()))
						.subprotocols(Arrays.asList(lep.subprotocols()))
						.build();

			}else{
				serverEndpointConfig = ServerEndpointConfig.Builder
						.create(aClass, LuckyUtils.TableToClass1(aClass.getSimpleName()))
						.build();
			}
			serverEndpointConfigs.add(serverEndpointConfig);
		}
		return serverEndpointConfigs;
	}

}

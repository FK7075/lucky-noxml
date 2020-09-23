package com.lucky.jacklamb.annotation.mvc;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.server.ServerEndpointConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/15 18:11
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LEndpoint {

    /**
     * URI or URI-template that the annotated class should be mapped to.
     * @return The URI or URI-template that the annotated class should be mapped
     *         to.
     */
    String value();

    String[] subprotocols() default {};

    Class<? extends Decoder>[] decoders() default {};

    Class<? extends Encoder>[] encoders() default {};

    public Class<? extends ServerEndpointConfig.Configurator> configurator()
    default ServerEndpointConfig.Configurator.class;
}

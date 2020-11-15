package com.lucky.jacklamb.ioc.auto.factory;

import java.util.Set;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/14 6:04 上午
 */
public interface ComponentFactory {

    Set<Class<?>> factory();
}

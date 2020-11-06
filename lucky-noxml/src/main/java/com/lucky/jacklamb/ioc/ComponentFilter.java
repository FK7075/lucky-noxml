package com.lucky.jacklamb.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/11/6 9:24
 */
public interface ComponentFilter {

    default boolean filter(Class<?> aClass){
        int mo = aClass.getModifiers();
        if(Annotation.class.isAssignableFrom(aClass)){
            return true;
        }
        return false;
    }
}

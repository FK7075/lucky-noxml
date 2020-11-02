package com.lucky.jacklamb.exception;

import java.lang.reflect.Field;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/3 12:46 上午
 */
public class AutoInjectionException extends RuntimeException{

    public AutoInjectionException(Class<?> aClass, Field field,Throwable e){
        super(String.format("自动注入异常！在为%s注入%s类型属性\"%s\"时出现异常",aClass.getName(),field.getType().getName(),field.getName())
                ,e);


    }
}

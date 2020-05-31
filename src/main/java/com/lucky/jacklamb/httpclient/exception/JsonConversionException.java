package com.lucky.jacklamb.httpclient.exception;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/5/31 3:29 上午
 */
public class JsonConversionException extends RuntimeException{

    public JsonConversionException(Class<?> targetClass,String jsonString,Throwable e){
        super("JSON转换异常！JSON : "+jsonString+", Class : "+targetClass.getName(),e);
    }

}

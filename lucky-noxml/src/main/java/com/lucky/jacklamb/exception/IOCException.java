package com.lucky.jacklamb.exception;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/7/28 2:05 上午
 */
public class IOCException extends RuntimeException{

    public IOCException(Throwable e){
        super("IOC容器初始化错误！",e);
    }
}

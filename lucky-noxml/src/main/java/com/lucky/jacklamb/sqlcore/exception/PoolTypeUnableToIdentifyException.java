package com.lucky.jacklamb.sqlcore.exception;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/6/17 1:06 上午
 */
public class PoolTypeUnableToIdentifyException extends RuntimeException {

    public PoolTypeUnableToIdentifyException(String errPoolType){
        super("无法识别的数据库连接池：'"+errPoolType+"' ，请使用c3p0或者HikariCP");
    }
}

package com.lucky.jacklamb.sqlcore.abstractionlayer.exception;

public class LuckySqlOperationException extends RuntimeException {

    public LuckySqlOperationException(Throwable e){
        super(e);
    }
}

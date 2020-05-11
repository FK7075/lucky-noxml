package com.lucky.jacklamb.exception;

import java.util.Arrays;

public class IllegalParameterException extends RuntimeException {

    public IllegalParameterException(String errorStr,String[] regulars){
        super("与约定格式不符的错误字符串："+errorStr);
        System.err.println("约定的正则表达式："+ Arrays.toString(regulars)+", ERROR-STRING: "+errorStr);
    }
}

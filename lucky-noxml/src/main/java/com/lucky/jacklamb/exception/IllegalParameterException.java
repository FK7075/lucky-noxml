package com.lucky.jacklamb.exception;

import com.lucky.jacklamb.servlet.core.Model;

import java.util.Arrays;

public class IllegalParameterException extends Exception {

    public IllegalParameterException(Model model,String paramName, String errorStr, String[] regulars){
        super("不合法的参数["+paramName+" = "+errorStr+"] 该参数值不符合正则约束："+Arrays.toString(regulars));
    }
}

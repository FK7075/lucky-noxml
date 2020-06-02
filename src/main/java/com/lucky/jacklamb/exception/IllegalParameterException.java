package com.lucky.jacklamb.exception;

import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.servlet.Model;
import com.lucky.jacklamb.utils.Jacklabm;

import java.util.Arrays;

public class IllegalParameterException extends RuntimeException {

    public IllegalParameterException(Model model,String paramName, String errorStr, String[] regulars){
        super("与约定格式不符的错误字符串："+errorStr);
        model.error(Code.REFUSED,"不合法的参数\""+paramName+"\" : "+errorStr,"该参数不符合正则约束："+Arrays.toString(regulars));
        System.err.println("约定的正则表达式："+ Arrays.toString(regulars)+", ERROR-STRING: "+errorStr);
    }
}

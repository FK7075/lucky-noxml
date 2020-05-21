package com.lucky.jacklamb.conversion.util;

import com.lucky.jacklamb.aop.util.ASMUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class MethodUtils {

    public static Object invoke(Object targetObject,Method method,Object...params){
        try {
            method.setAccessible(true);
            return method.invoke(targetObject,params);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法通过反射机制执行方法！ Method: "+method+", Object: "+targetObject+", Param: "+ Arrays.toString(params),e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("无法通过反射机制执行方法！ Method: "+method+", Object: "+targetObject+", Param: "+ Arrays.toString(params),e);
        }
    }

    public static List<String> getInterfaceParamNames(Method method) throws IOException {
       return ASMUtil.getInterfaceMethodParamNames(method);
    }

    public static String[] getClassParamNames(Method method){
        return ASMUtil.getMethodParamNames(method);
    }

    public static String[] getParamNamesByParameter(Method method){
        Parameter[] parameters = method.getParameters();
        String[] names=new String[parameters.length];
        for(int i=0;i<parameters.length;i++)
            names[i]=parameters[i].getName();
        return names;
    }

}

package com.lucky.jacklamb.conversion.util;

import com.lucky.jacklamb.aop.util.ASMUtil;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;

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

    public static Object[] getRunParam(Method method,String[] StrParam,ApplicationBeans beans){
        Parameter[] parameters = method.getParameters();
        if(parameters.length!=StrParam.length){
            throw new RuntimeException("@InitRun参数错误(runParam提供的参数个数与方法参数列表个数不匹配--[ERROR m:"+parameters.length+" , p:"+StrParam.length+"])，位置："+method);
        }
        Object[] runParams=new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if(StrParam[i].startsWith("ref:")){
                runParams[i]= beans.getBean(StrParam[i].substring(4));
            }else{
                runParams[i]= JavaConversion.strToBasic(StrParam[i],parameters[i].getType());
            }
        }
        return runParams;
    }

}

package com.lucky.jacklamb.utils.reflect;

import com.lucky.jacklamb.aop.util.ASMUtil;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.servlet.mapping.Mapping;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MethodUtils {

    /**
     * 使用反射机制执行方法
     * @param targetObject
     * @param method
     * @param params
     * @return
     */
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

    public static Object invoke(Object targetObject,String methodName,Object[] params){
        try {
            Method method=targetObject.getClass().getDeclaredMethod(methodName,ClassUtils.array2Class(params));
            return invoke(targetObject,method,params);
        } catch (NoSuchMethodException e) {
           throw new RuntimeException(e);
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

    public static Map<String,Object> getInterfaceMethodParamsNV(Method method,Object[] params) throws IOException {
        Map<String,Object> paramMap=new HashMap<>();
        List<String> interParams = ASMUtil.getInterfaceMethodParamNames(method);
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            paramMap.put(Mapping.getParamName(parameters[i],interParams.get(i)),params[i]);
        }
        return paramMap;
    }

    public static Map<String,Object> getClassMethodParamsNV(Method method, Object[] params) throws IOException {
        Map<String,Object> paramMap=new HashMap<>();
        String[] mparams = ASMUtil.getMethodParamNames(method);
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            paramMap.put(Mapping.getParamName(parameters[i],mparams[i]),params[i]);
        }
        return paramMap;
    }

    public static Map<String,Object> getMethodParamsNV(Method method, Object[] params) throws IOException {
        Map<String, Object> interfaceMethodParamsNV = getInterfaceMethodParamsNV(method, params);
        return interfaceMethodParamsNV.isEmpty()?getClassMethodParamsNV(method,params):interfaceMethodParamsNV;
    }

}

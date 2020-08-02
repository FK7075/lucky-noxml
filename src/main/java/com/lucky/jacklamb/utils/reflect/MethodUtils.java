package com.lucky.jacklamb.utils.reflect;

import com.lucky.jacklamb.cglib.ASMUtil;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.servlet.mapping.Mapping;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MethodUtils {

    /**
     * 使用反射机制执行方法
     * @param targetObject 对象实例
     * @param method 要执行的方法的Method
     * @param params 方法执行所需要的参数
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

    /**
     * 使用反射机制执行方法
     * @param targetObject 对象实例
     * @param methodName 要执行的方法的方法名
     * @param params 方法执行所需要的参数
     * @return
     */
    public static Object invoke(Object targetObject,String methodName,Object[] params){
        try {
            Method method=targetObject.getClass().getDeclaredMethod(methodName,ClassUtils.array2Class(params));
            return invoke(targetObject,method,params);
        } catch (NoSuchMethodException e) {
           throw new RuntimeException(e);
        }
    }

    /**
     * interface Method : public void method(String name,Double price)
     *      ->
     * List[name,price]
     * 只有在编译参数-parameters开启后才能生效
     *
     * 通过ASM得到接口中方法的所有参数名
     * @param method 要操作的方法的Method
     * @return
     * @throws IOException
     */
    public static List<String> getInterfaceParamNames(Method method) throws IOException {
       return ASMUtil.getInterfaceMethodParamNames(method);
    }

    /**
     * class Method : public void method(String name,Double price)
     *      ->
     * String[] =[name,price]
     * 不依赖编译参数-parameters
     *
     * 通过ASM得到类方法的所有参数名
     * @param method 要操作的方法的Method
     * @return
     */
    public static String[] getClassParamNames(Method method){
        return ASMUtil.getMethodParamNames(method);
    }

    /**
     * 通过JDK8的Parameter类得到方法的所有参数名
     * @param method 要操作的方法的Method
     * @return
     */
    public static String[] getParamNamesByParameter(Method method){
        Parameter[] parameters = method.getParameters();
        String[] names=new String[parameters.length];
        for(int i=0;i<parameters.length;i++)
            names[i]=parameters[i].getName();
        return names;
    }

    /**
     * 将String类型的参数列表转化为Method方法执行时的Object类型的参数列表
     * @param method 要操作的Method
     * @param StrParam String类型的方法参数列表
     * @return
     */
    public static Object[] getRunParam(Method method,String[] StrParam){
        Parameter[] parameters = method.getParameters();
        if(parameters.length!=StrParam.length){
            throw new RuntimeException("@InitRun参数错误(runParam提供的参数个数与方法参数列表个数不匹配--[ERROR m:"+parameters.length+" , p:"+StrParam.length+"])，位置："+method);
        }
        Object[] runParams=new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if(StrParam[i].startsWith("ref:")){
                runParams[i]= ApplicationBeans.createApplicationBeans().getBean(StrParam[i].substring(4));
            }else{
                runParams[i]= JavaConversion.strToBasic(StrParam[i],parameters[i].getType());
            }
        }
        return runParams;
    }

    /**
     * 接口方法
     * 得到由参数列表的参数名和参数值所组成的Map
     * @param method 接口方法
     * @param params 执行参数
     * @return
     * @throws IOException
     */
    public static Map<String,Object> getInterfaceMethodParamsNV(Method method,Object[] params) throws IOException {
        Map<String,Object> paramMap=new HashMap<>();
        List<String> interParams = ASMUtil.getInterfaceMethodParamNames(method);
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            paramMap.put(Mapping.getParamName(parameters[i],interParams.get(i)),params[i]);
        }
        return paramMap;
    }

    /**
     * 类方法
     * 得到由参数列表的参数名和参数值所组成的Map
     * @param method 类方法
     * @param params 执行参数
     * @return
     * @throws IOException
     */
    public static Map<String,Object> getClassMethodParamsNV(Method method, Object[] params) throws IOException {
        Map<String,Object> paramMap=new HashMap<>();
        String[] mparams = ASMUtil.getMethodParamNames(method);
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            paramMap.put(Mapping.getParamName(parameters[i],mparams[i]),params[i]);
        }
        return paramMap;
    }

    /**
     * 兼容接口方法与类方法
     * 得到由参数列表的参数名和参数值所组成的Map
     * @param method 方法
     * @param params 执行参数
     * @return
     * @throws IOException
     */
    public static Map<String,Object> getMethodParamsNV(Method method, Object[] params) throws IOException {
        Map<String, Object> interfaceMethodParamsNV = getInterfaceMethodParamsNV(method, params);
        return interfaceMethodParamsNV.isEmpty()?getClassMethodParamsNV(method,params):interfaceMethodParamsNV;
    }

    /**
     * 获取方法返回值类型
     * @param method 要操作的Method
     * @return
     */
    public static Class<?> getReturnType(Method method){
        return method.getReturnType();
    }

    /**
     * 获取方法返回值类型的泛型，如果返回值不包含泛型则返回null
     * @param method 要操作的Method
     * @return
     */
    public static Class<?>[] getReturnTypeGeneric(Method method){
        Type type = method.getGenericReturnType();
        return ClassUtils.getGenericType(type);
    }


}

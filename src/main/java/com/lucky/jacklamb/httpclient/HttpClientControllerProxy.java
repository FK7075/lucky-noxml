package com.lucky.jacklamb.httpclient;

import com.lucky.jacklamb.annotation.ioc.CallController;
import com.lucky.jacklamb.aop.util.ASMUtil;
import com.lucky.jacklamb.exception.NotMappingMethodException;
import com.lucky.jacklamb.mapping.Mapping;
import com.lucky.jacklamb.mapping.MappingDetails;
import com.lucky.jacklamb.rest.LSON;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientControllerProxy {

    /**
     * 获得CallController接口的代理对象
     * @param callControllerClass
     * @param <T>
     * @return
     */
    public static <T> T getCallControllerProxyObject(Class<T> callControllerClass){
        final Enhancer enhancer=new Enhancer();
        enhancer.setSuperclass(callControllerClass);
        MethodInterceptor interceptor=(object, method, params, methodProxy)->{
            if(Mapping.isMappingMethod(method)){
                Parameter[] parameters=method.getParameters();
                List<String> paramName= ASMUtil.getInterfaceMethodParamNames(method);
                String key=null;

                Map<String,String> callapiMap=new HashMap<>();

                //获取并封装请求远程接口的参数
                for(int i=0;i<parameters.length;i++) {
                    if(params[i].getClass().getClassLoader()==null){
                        key = Mapping.getParamName(parameters[i],paramName.get(i));
                        callapiMap.put(key, params[i].toString());
                    }else{
                        Field[] fields =params[i].getClass().getDeclaredFields();
                        Object fieldValue;
                        for(Field field:fields){
                            field.setAccessible(true);
                            fieldValue=field.get(params[i]);
                            if(fieldValue!=null)
                                callapiMap.put(field.getName(),fieldValue.toString());
                        }
                    }

                }

                //获取远程接口的地址
                String callControllerApi=Api.getApi(callControllerClass.getAnnotation(CallController.class).value());
                MappingDetails md=Mapping.getMappingDetails(method);
                String methodApi=Api.getApi(md.value);
                if(!callControllerApi.endsWith("/"))
                    callControllerApi+="/";
                if(methodApi.startsWith("/"))
                    methodApi=methodApi.substring(1);
                String apiUrl=callControllerApi+methodApi;

                //调用远程接口
                String callResult=HttpClientCall.call(apiUrl,md.method[0],callapiMap);

                //封装返回结果
                Class<?> returnClass=method.getReturnType();
                if(returnClass.getClassLoader()==null
                        &&!Collection.class.isAssignableFrom(returnClass)
                        &&!Map.class.isAssignableFrom(returnClass)){
                    return JavaConversion.strToBasic(callResult,returnClass);
                }else{
                    return new LSON().toObject(returnClass,callResult);
                }
            }
            throw new NotMappingMethodException("该方法不是Mapping方法，无法执行代理！错误位置："+method);
        };

        enhancer.setCallback(interceptor);
        return (T) enhancer.create();
    }
}

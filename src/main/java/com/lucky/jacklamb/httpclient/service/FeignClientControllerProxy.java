package com.lucky.jacklamb.httpclient.service;

import com.lucky.jacklamb.annotation.ioc.CallController;
import com.lucky.jacklamb.annotation.mvc.FeignClient;
import com.lucky.jacklamb.annotation.mvc.RequestMapping;
import com.lucky.jacklamb.aop.util.ASMUtil;
import com.lucky.jacklamb.conversion.util.FieldUtils;
import com.lucky.jacklamb.exception.NotFoundCallUrlException;
import com.lucky.jacklamb.exception.NotMappingMethodException;
import com.lucky.jacklamb.expression.$Expression;
import com.lucky.jacklamb.httpclient.Api;
import com.lucky.jacklamb.httpclient.HttpClientCall;
import com.lucky.jacklamb.ioc.config.AppConfig;
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

public class FeignClientControllerProxy {

    /**
     * 获得FeignClientController接口的代理对象
     * @param feignClientControllerClass
     * @param <T>
     * @return
     */
    public static <T> T getFeignClientControllerProxyObject(Class<T> feignClientControllerClass){
        final Enhancer enhancer=new Enhancer();
        enhancer.setSuperclass(feignClientControllerClass);
        MethodInterceptor interceptor=(object, method, params, methodProxy)->{
            FeignClient fc=feignClientControllerClass.getAnnotation(FeignClient.class);
            String regUrl= AppConfig.getAppConfig().getServiceConfig().getServiceUrl();
            regUrl=regUrl.endsWith("/")?regUrl:regUrl+"/";
            regUrl=regUrl.substring(0,regUrl.length()-8);
            if(Mapping.isMappingMethod(method)){
                Parameter[] parameters=method.getParameters();
                List<String> paramName= ASMUtil.getInterfaceMethodParamNames(method);
                String key=null;
                Map<String,String> callapiMap=new HashMap<>();
                callapiMap.put("agreement",fc.agreement());
                //获取并封装请求远程接口的参数
                for(int i=0;i<parameters.length;i++) {
                    if(params[i].getClass().getClassLoader()==null){
                        key = Mapping.getParamName(parameters[i],paramName.get(i));
                        callapiMap.put(key, params[i].toString());
                    }else{
                        Field[] fields = FieldUtils.getAllFields(params[i].getClass());
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
                MappingDetails md;
                String apiUrl=feignClientControllerClass.isAnnotationPresent(RequestMapping.class)?feignClientControllerClass.getAnnotation(RequestMapping.class).value():"/";
                apiUrl=apiUrl.endsWith("/")?apiUrl:apiUrl+"/";
                apiUrl=apiUrl.startsWith("/")?apiUrl:"/"+apiUrl;
                String serviceName=fc.value();
                if(Mapping.isMappingMethod(method)){
                    md=Mapping.getMappingDetails(method);
                    String methodUrl=md.value.startsWith("/")?md.value.substring(1):md.value;
                    apiUrl= $Expression.translationSharp(regUrl+serviceName+apiUrl+methodUrl,callapiMap) ;
                }else{
                    throw new NotFoundCallUrlException("该方法不是Mapping方法，无法执行代理！错误位置："+method);
                }



                //调用远程接口
                String callResult=HttpClientCall.call(apiUrl,md.method[0],callapiMap);

                //封装返回结果
                Class<?> returnClass=method.getReturnType();
                return new LSON().toObject(returnClass,callResult);

            }
            throw new NotMappingMethodException("该方法不是Mapping方法，无法执行代理！错误位置："+method);
        };
        enhancer.setCallback(interceptor);
        return (T) enhancer.create();
    }

}

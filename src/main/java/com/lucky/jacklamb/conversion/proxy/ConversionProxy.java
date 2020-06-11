package com.lucky.jacklamb.conversion.proxy;

import com.lucky.jacklamb.cglib.CglibProxy;
import com.lucky.jacklamb.conversion.LuckyConversion;
import com.lucky.jacklamb.conversion.annotation.Conversion;
import com.lucky.jacklamb.conversion.annotation.Mapping;
import com.lucky.jacklamb.conversion.annotation.Mappings;
import com.lucky.jacklamb.conversion.util.ClassUtils;
import com.lucky.jacklamb.conversion.util.EntityAndDto;
import com.lucky.jacklamb.conversion.util.FieldUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 得到一个LuckyConversion接口的实现类Cglib实现
 */
public class ConversionProxy {

    private  static Map<String,Object> conversionMap;

    private static ConversionMethodInterceptor conversionMethodInterceptor;

    /**
     * 得到一个LuckyConversion接口子接口的代理对象
     * @param childInterfaceClass LuckyConversion子接口的Class
     * @param <T>
     * @return
     */
    public static<T extends LuckyConversion> T getLuckyConversion(Class<T> childInterfaceClass){
        if(conversionMap==null)
            conversionMap=new HashMap<>();
        String mapKey=childInterfaceClass.getName();
        if(conversionMap.containsKey(mapKey))
            return (T) conversionMap.get(mapKey);
        Type[] luckyConversionGenericClass=childInterfaceClass.getGenericInterfaces();
        ParameterizedType interfaceType=(ParameterizedType) luckyConversionGenericClass[0];
        Type[] interfacesTypes=interfaceType.getActualTypeArguments();
        Class<? extends LuckyConversion>[] luckyConversionClasses=childInterfaceClass.getAnnotation(Conversion.class).value();
        conversionMethodInterceptor=new ConversionMethodInterceptor(luckyConversionClasses,(Class<?>)interfacesTypes[0],(Class<?>)interfacesTypes[1]);
        T luckyConversion= CglibProxy.getCglibProxyObject(childInterfaceClass,conversionMethodInterceptor);
        conversionMap.put(mapKey,luckyConversion);
        return  luckyConversion;
    }

    public static Map<String,Object> getSourceNameValueMap(Object sourceObject, String initialName) throws IllegalAccessException {
        return conversionMethodInterceptor.getSourceNameValueMap(sourceObject, initialName);
    }
}
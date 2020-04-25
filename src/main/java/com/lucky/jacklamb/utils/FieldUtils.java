package com.lucky.jacklamb.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class FieldUtils {

    public static boolean isArray(Field field){
        return field.getType().isArray();
    }

    public static boolean isCollection(Field field){
        return Collection.class.isAssignableFrom(field.getType());
    }

    public static boolean isMap(Field field){
        return Map.class.isAssignableFrom(field.getType());
    }

    public static Class<?>[] getGenericType(Field field){
        Type type = field.getGenericType();
        if(type!=null && type instanceof ParameterizedType){
            ParameterizedType pt=(ParameterizedType) type;
            Type[] types=pt.getActualTypeArguments();
            Class<?>[] genericType=new Class<?>[types.length];
            for(int i=0;i<types.length;i++)
                genericType[i]=(Class<?>)types[i];
            return genericType;
        }else{
            return null;
        }
    }

    public static boolean isBasicCollection(Field field){
        Class<?>[] genericClasses=getGenericType(field);
        if(genericClasses==null||genericClasses.length!=1)
            return false;
        Class<?> generic=genericClasses[0];
        return generic.getClassLoader()==null;
    }

    public static boolean isBasicSimpleType(Field field){
        Class<?> fieldClass=field.getType();
        if(fieldClass.getClassLoader()!=null)
            return false;
        Class<?>[] genericTypes = getGenericType(field);
        if(genericTypes==null)
            return true;
        for (Class<?> clzz:genericTypes){
            if(clzz.getClassLoader()!=null)
                return false;
        }
        return true;
    }




}

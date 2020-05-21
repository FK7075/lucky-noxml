package com.lucky.jacklamb.conversion.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public abstract class FieldUtils {


    /**
     * 属性是否为数组
     * @param field Field对象
     * @return
     */
    public static boolean isArray(Field field){
        return field.getType().isArray();
    }

    /**
     * 属性是否为集合
     * @param field Field对象
     * @return
     */
    public static boolean isCollection(Field field){
        return Collection.class.isAssignableFrom(field.getType());
    }

    /**
     * 属性是否为Map
     * @param field Field对象
     * @return
     */
    public static boolean isMap(Field field){
        return Map.class.isAssignableFrom(field.getType());
    }

    /**
     * 获取带有泛型的属性的泛型类型,不是泛型属性返回null
     * @param field Field对象
     * @return Class[] OR null
     */
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

    /**
     * 属性是否为基本集合类型(泛型为JDK类型的集合)
     * @param field Field对象
     * @return
     */
    public static boolean isBasicCollection(Field field){
        Class<?>[] genericClasses=getGenericType(field);
        if(genericClasses==null||genericClasses.length!=1)
            return false;
        Class<?> generic=genericClasses[0];
        return generic.getClassLoader()==null;
    }

    /**
     * 是否为基本数据类型(JDK类型，以及泛型为基本类型的JDK泛型类)
     * @param field
     * @return
     */
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


    /**
     * 反射机制获取Field的值
     * @param fieldObject 目标对象
     * @param field Field对象
     * @return
     */
    public static Object getValue(Object fieldObject,Field field){
        try {
            field.setAccessible(true);
            return field.get(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法通过反射机制获取属性值！Field: "+field+", Object: "+fieldObject,e);
        }
    }

    /**
     * 反射机制设置Field的值
     * @param fieldObject
     * @param field
     */
    public static void setValue(Object fieldObject,Field field,Object fieldValue){
        try {
            field.setAccessible(true);
            field.set(fieldObject,fieldValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("无法通过反射机制为属性赋值！Field: "+field+", Object: "+fieldObject+", FieldValue: "+fieldObject,e);
        }
    }

    /**
     * 判断目标类型是否为属性类型的子类
     * @param field Field对象
     * @param targetClass 目标类型
     * @return
     */
    public static boolean isSubclass(Field field,Class<?> targetClass){
        return field.getType().isAssignableFrom(targetClass);
    }

    /**
     * 判断目标类型是否为属性类型的父类
     * @param field Field对象
     * @param targetClass 目标类型
     * @return
     */
    public static boolean isParentClass(Field field,Class<?> targetClass){
        return targetClass.isAssignableFrom(field.getType());
    }

    /**
     * 目标对象是否属于属性对应的类型
     * @param field Field对象
     * @param targetObject 目标对象
     * @return
     */
    public static boolean instanceOf(Field field,Object targetObject){
        return isSubclass(field,targetObject.getClass());
    }

}

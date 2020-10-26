package com.lucky.jacklamb.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Map;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/10/26 16:33
 */
public abstract class AnnotationUtils {

    public static boolean isExist(Class<?> aClass,Class<? extends Annotation> annotationClass){
        return aClass.isAnnotationPresent(annotationClass);
    }
    public static boolean isExist(Method method, Class<? extends Annotation> annotationClass){
        return method.isAnnotationPresent(annotationClass);
    }
    public static boolean isExist(Field field, Class<? extends Annotation> annotationClass){
        return field.isAnnotationPresent(annotationClass);
    }
    public static boolean isExist(Parameter parameter, Class<? extends Annotation> annotationClass){
        return parameter.isAnnotationPresent(annotationClass);
    }


    public static <T extends Annotation> T get(Class<?> aClass,Class<T> annotationClass){
        if(isExist(aClass,annotationClass)){
            return aClass.getAnnotation(annotationClass);
        }
        throw new AnnotationParsingException("获取注解失败 ： 类\""+aClass+"\"并没有被注解[\""+annotationClass+"\"]标注！");
    }

    public static <T extends Annotation> T get(Method method,Class<T> annotationClass){
        if(isExist(method,annotationClass)){
            return method.getAnnotation(annotationClass);
        }
        throw new AnnotationParsingException("获取注解失败 ： 方法\""+method+"\"并没有被注解[\""+annotationClass+"\"]标注！");
    }

    public static  <T extends Annotation> T get(Field field,Class<T> annotationClass){
        if(isExist(field,annotationClass)){
            return field.getAnnotation(annotationClass);
        }
        throw new AnnotationParsingException("获取注解失败 ： 属性\""+field+"\"并没有被注解[\""+annotationClass+"\"]标注！");
    }
    public static <T extends Annotation> T get(Parameter parameter,Class<T> annotationClass){
        if(isExist(parameter,annotationClass)){
            return parameter.getAnnotation(annotationClass);
        }
        throw new AnnotationParsingException("获取注解失败 ： 方法参数\""+parameter+"\"并没有被注解[\""+annotationClass+"\"]标注！");
    }

    public static <Ann extends Annotation> void set(Ann ann, String fileName, Object setValue ){
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(ann);
        Field memberValues = FieldUtils.getDeclaredField(invocationHandler.getClass(),"memberValues");
        memberValues.setAccessible(true);
        Map map = null;
        try {
            map = (Map) memberValues.get(invocationHandler);
            map.put(fileName,setValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

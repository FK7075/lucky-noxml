package com.lucky.jacklamb.utils.reflect;

import com.lucky.jacklamb.utils.base.Assert;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/10/26 16:33
 */
public abstract class AnnotationUtils {

    /**
     * 判断类是否被注解标注
     * @param aClass 目标类的Class
     * @param annotationClass 注解Class
     * @return true/false
     */
    public static boolean isExist(Class<?> aClass,Class<? extends Annotation> annotationClass){
        return aClass.isAnnotationPresent(annotationClass);
    }

    /**
     * 判断方法是否被注解标注
     * @param method 目标方法的Method
     * @param annotationClass 注解Class
     * @return true/false
     */
    public static boolean isExist(Method method, Class<? extends Annotation> annotationClass){
        return method.isAnnotationPresent(annotationClass);
    }

    /**
     * 判断属性是否被注解标注
     * @param field 目标属性的Field
     * @param annotationClass 注解Class
     * @return true/false
     */
    public static boolean isExist(Field field, Class<? extends Annotation> annotationClass){
        return field.isAnnotationPresent(annotationClass);
    }

    /**
     * 判断方法参数是否被注解标注
     * @param parameter 目标方法参数的Parameter
     * @param annotationClass 注解Class
     * @return true/false
     */
    public static boolean isExist(Parameter parameter, Class<? extends Annotation> annotationClass){
        return parameter.isAnnotationPresent(annotationClass);
    }

    /**
     * 判断类是否被注解数组中的某一个标注
     * @param aClass 目标类的Class
     * @param annotationClasses 注解Class数组
     * @return true/false
     */
    public static boolean isExistOrByArray(Class<?> aClass,Class<? extends Annotation>[] annotationClasses){
        for (Class<? extends Annotation> ac : annotationClasses) {
            if(isExist(aClass,ac)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断方法是否被注解数组中的某一个标注
     * @param method 目标方法的Method
     * @param annotationClasses 注解Class数组
     * @return true/false
     */
    public static boolean isExistOrByArray(Method method,Class<? extends Annotation>[] annotationClasses){
        for (Class<? extends Annotation> ac : annotationClasses) {
            if(isExist(method,ac)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断属性是否被注解数组中的某一个标注
     * @param field 目标属性的Field
     * @param annotationClasses 注解Class数组
     * @return true/false
     */
    public static boolean isExistOrByArray(Field field,Class<? extends Annotation>[] annotationClasses){
        for (Class<? extends Annotation> ac : annotationClasses) {
            if(isExist(field,ac)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断方法参数是否被注解数组中的某一个标注
     * @param parameter 目标方法参数的Parameter
     * @param annotationClasses 注解Class数组
     * @return true/false
     */
    public static boolean isExistOrByArray(Parameter parameter,Class<? extends Annotation>[] annotationClasses){
        for (Class<? extends Annotation> ac : annotationClasses) {
            if(isExist(parameter,ac)){
                return true;
            }
        }
        return false;
    }


    /**
     * 得到类上的某个注解的实例
     * @param aClass 目标类的Class
     * @param annotationClass 注解Class
     * @param <T> 注解泛型
     * @return 注解实例
     */
    public static <T extends Annotation> T get(Class<?> aClass,Class<T> annotationClass){
        if(isExist(aClass,annotationClass)){
            return aClass.getAnnotation(annotationClass);
        }
        throw new AnnotationParsingException("获取注解失败 ： 类\""+aClass+"\"并没有被注解[\""+annotationClass+"\"]标注！");
    }

    /**
     * 得到方法上的某个注解的实例
     * @param method 目标方法的Method
     * @param annotationClass 注解Class
     * @param <T> 注解泛型
     * @return 注解实例
     */
    public static <T extends Annotation> T get(Method method,Class<T> annotationClass){
        if(isExist(method,annotationClass)){
            return method.getAnnotation(annotationClass);
        }
        throw new AnnotationParsingException("获取注解失败 ： 方法\""+method+"\"并没有被注解[\""+annotationClass+"\"]标注！");
    }

    /**
     * 得到属性上的某个注解的实例
     * @param field 目标属性的Field
     * @param annotationClass 注解Class
     * @param <T> 注解泛型
     * @return 注解实例
     */
    public static  <T extends Annotation> T get(Field field,Class<T> annotationClass){
        if(isExist(field,annotationClass)){
            return field.getAnnotation(annotationClass);
        }
        throw new AnnotationParsingException("获取注解失败 ： 属性\""+field+"\"并没有被注解[\""+annotationClass+"\"]标注！");
    }

    /**
     * 得到方法参数上的某个注解的实例
     * @param parameter 目标方法参数的Parameter
     * @param annotationClass 注解Class
     * @param <T> 注解泛型
     * @return 注解实例
     */
    public static <T extends Annotation> T get(Parameter parameter,Class<T> annotationClass){
        if(isExist(parameter,annotationClass)){
            return parameter.getAnnotation(annotationClass);
        }
        throw new AnnotationParsingException("获取注解失败 ： 方法参数\""+parameter+"\"并没有被注解[\""+annotationClass+"\"]标注！");
    }

    /**
     * 动态的设置注解某个属性的值
     * @param ann 注解实例
     * @param fileName 需要修改的属性的属性名
     * @param setValue 值
     * @param <Ann>
     */
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

    public static List<Annotation> getAnnotations(Class<?> aClass){
        return Stream.of(aClass.getAnnotations()).filter((a) -> {
            boolean r = a instanceof Retention;
            boolean d = a instanceof Documented;
            boolean t = a instanceof Target;
            return !(r | d | t);
        }).collect(Collectors.toList());
    }

    /**
     * 加强版的类注解标注检查，针对组合注解的检查
     * @param aClass 目标类的Class
     * @param annClass 注解Class
     * @return true/false
     */
    private static Annotation strengthenGet(Class<?> aClass,Class<?extends Annotation> annClass){
        if(isExist(aClass, annClass)){
            return get(aClass,annClass);
        }
        List<Annotation> allAnn = getAnnotations(aClass);
        for (Annotation ann : allAnn) {
            Annotation resultAnn=strengthenGet(ann.annotationType(),annClass);
            if(Assert.isNotNull(resultAnn)){
                return resultAnn;
            }
        }
        return null;
    }

}

package com.lucky.jacklamb.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ClassUtils {


    /**
     * 得到一个类以及所有父类(不包括Object)的所有属性(Field)
     * @param clzz 目标类的Class
     * @return
     */
    public static Field[] getAllFields(Class<?> clzz) {
        if (clzz.getSuperclass() == Object.class) {
            return clzz.getDeclaredFields();
        }
        Field[] clzzFields = clzz.getDeclaredFields();
        Field[] superFields = getAllFields(clzz.getSuperclass());
        return delCoverFields(clzzFields,superFields);
    }

    private static Field[] delCoverFields(Field[] thisFields,Field[] superFields){
        List<Field> delCvoerFields=new ArrayList<>();
        Set<String> coverFieldNames=new HashSet<>();
        for (Field thisField : thisFields) {
            if(thisField.isAnnotationPresent(Cover.class)){
                coverFieldNames.add(thisField.getName());
            }
            delCvoerFields.add(thisField);
        }
        for (Field superField : superFields) {
            if(!coverFieldNames.contains(superField.getName())){
                delCvoerFields.add(superField);
            }
        }
        return delCvoerFields.toArray(new Field[delCvoerFields.size()]);
    }

    /**
     * 得到一个类以及所有父类(不包括Object)的所有方法(Method)
     * @param clzz 目标类的Class
     * @return
     */
    public static Method[] getAllMethod(Class<?> clzz){
        if (clzz.getSuperclass() == Object.class) {
            return clzz.getDeclaredMethods();
        }
        Method[] clzzMethods = clzz.getDeclaredMethods();
        Method[] superMethods = getAllMethod(clzz.getSuperclass());
        return delCoverMethods(clzzMethods,superMethods);
    }

    private static Method[] delCoverMethods(Method[] thisMethods,Method[] superMethods){
        List<Method> delCoverMethods=new ArrayList<>();
        Set<String> coverMethodNames=new HashSet<>();
        for (Method thisMethod : thisMethods) {
            if(thisMethod.isAnnotationPresent(Cover.class)){
                coverMethodNames.add(thisMethod.getName());
            }
            delCoverMethods.add(thisMethod);
        }
        for (Method superMethod : superMethods) {
            if(!coverMethodNames.contains(superMethod.getName())){
                delCoverMethods.add(superMethod);
            }
        }
        return delCoverMethods.toArray(new Method[delCoverMethods.size()]);
    }

    /**
     * 使用反射机制调用构造函数创建一个对象
     * @param tclass 目标对象的Class
     * @param cparams 构造器执行的参数
     * @param <T>
     * @return
     */
    public static <T> T newObject(Class<? extends T> tclass,Object...cparams){
        try {
            Constructor<? extends T> constructor  =tclass.getConstructor(array2Class(cparams));
            constructor.setAccessible(true);
            return constructor.newInstance(cparams);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 将一个Object[]转化为对应类型的Class[]
     * @param objs 要操作的Object[]
     * @return
     */
    public static Class<?>[] array2Class(Object[] objs){
        Class<?>[] paramsClass=new Class<?>[objs.length];
        for (int i = 0; i < objs.length; i++) {
            paramsClass[i]=objs[i].getClass();
        }
        return paramsClass;
    }

    /**
     * 得到某个泛型Type的所有泛型类型
     * @param type 泛型Type
     * @return
     */
    public static Class<?>[] getGenericType(Type type){
        if(type!=null && type instanceof ParameterizedType){
            ParameterizedType pt=(ParameterizedType) type;
            Type[] types=pt.getActualTypeArguments();
            Class<?>[] genericType=new Class<?>[types.length];
            for(int i=0;i<types.length;i++) {
                genericType[i]=(Class<?>)types[i];
            }
            return genericType;
        }else{
            return null;
        }
    }

    public static boolean isBasic(Class<?> clzz){
        return clzz.getClassLoader()==null;
    }

    public static Class<?> getClass(String className){
        try {
            Class<?> aClass = Class.forName(className);
            return aClass;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Field> getFieldByAnnotation(Class<?> clzz, Class<? extends Annotation> annotation){
        Field[] allFields = getAllFields(clzz);
        List<Field> annFields=new ArrayList<>();
        for (Field field : allFields) {
            if(field.isAnnotationPresent(annotation)){
                annFields.add(field);
            }
        }
        return annFields;
    }
}

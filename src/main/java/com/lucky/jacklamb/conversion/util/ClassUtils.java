package com.lucky.jacklamb.conversion.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class ClassUtils {

    /**
     * 得到一个类以及所有父类(不包括Object)的所有属性(Field)
     * @param clzz 目标类的Class
     * @return
     */
    public static Field[] getAllFields(Class<?> clzz) {
        if (clzz.getSuperclass() == Object.class)
            return clzz.getDeclaredFields();
        Field[] clzzFields = clzz.getDeclaredFields();
        Field[] superFields = getAllFields(clzz.getSuperclass());
        int clzzFieldLength = clzzFields.length;
        int superClassFieldLength = superFields.length;
        Field[] allFields = new Field[clzzFieldLength + superClassFieldLength];
        for (int i = 0; i < clzzFieldLength; i++)
            allFields[i] = clzzFields[i];
        for (int i = 0; i < superClassFieldLength; i++)
            allFields[clzzFieldLength + i] = superFields[i];
        return allFields;
    }

    /**
     * 得到一个类以及所有父类(不包括Object)的所有方法(Method)
     * @param clzz 目标类的Class
     * @return
     */
    public static Method[] getAllMethod(Class<?> clzz){
        if (clzz.getSuperclass() == Object.class)
            return clzz.getDeclaredMethods();
        Method[] clzzMethods = clzz.getDeclaredMethods();
        Method[] superMethods = getAllMethod(clzz.getSuperclass());
        int clzzFieldLength = clzzMethods.length;
        int superClassFieldLength = superMethods.length;
        Method[] allMethods = new Method[clzzFieldLength + superClassFieldLength];
        for (int i = 0; i < clzzFieldLength; i++)
            allMethods[i] = clzzMethods[i];
        for (int i = 0; i < superClassFieldLength; i++)
            allMethods[clzzFieldLength + i] = superMethods[i];
        return allMethods;
    }

    public static boolean isBasic(Class<?> clzz){
        return false;
    }
}

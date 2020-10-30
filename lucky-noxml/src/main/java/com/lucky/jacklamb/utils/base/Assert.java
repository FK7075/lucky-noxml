package com.lucky.jacklamb.utils.base;

import java.util.Collection;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/10/29 16:37
 */
public abstract class Assert {

    public static boolean isNull(Object obj){
        return obj==null;
    }

    public static boolean isEmpty(Collection<?> collection){
        return collection==null||collection.isEmpty();
    }

    public static boolean isBlank(String str){
        return str==null||"".equals(str);
    }
}

package com.lucky.jacklamb.sqlcore.datasource.abs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存空间
 * @author fk7075
 * @version 1.0
 * @date 2020/10/27 10:19
 */
public  abstract class DBCacheSpace {

    public static Map<String, Map<String, List<?>>> cacheSpace=new HashMap<>();

    public static void createCacheSpace(Connection connection){
        String key=getKey(connection);
        if(!cacheSpace.containsKey(key)){
            Map<String,List<?>> currSpace=new HashMap<>();
            cacheSpace.put(key,currSpace);
        }
    }

    public static void closeCacheSpace(Connection connection){
        String key=getKey(connection);
        if(cacheSpace.containsKey(key)){
            cacheSpace.remove(key);
        }
    }

    public static Map<String,List<?>> getCacheSpace(Connection connection){
        return cacheSpace.get(getKey(connection));
    }

    public static boolean contains(Connection connection,String sql){
        String key=getKey(connection);
        if(!cacheSpace.containsKey(key)){
            return false;
        }
        return getCacheSpace(connection).containsKey(sql);
    }

    public static List<?> getList(Connection connection,String sql){
        if(contains(connection,sql)){
            return getCacheSpace(connection).get(sql);
        }
        return null;
    }

    public static void setList(Connection connection,String sql,List<?> list){
        getCacheSpace(connection).put(sql,list);
    }

    private static String getKey(Connection connection){
        return Connection.class.getName()+"@"+connection.hashCode();
    }
}

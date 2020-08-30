package com.lucky.jacklamb.redis;

import com.google.gson.reflect.TypeToken;
import com.lucky.jacklamb.rest.LSON;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Type;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/8/27 15:48
 */
public class RedisCode {

    private static LSON lson=new LSON();
    private static Jedis jedis;

    public <T> T getObject(String key,Class<T> tClass){
        return lson.fromJson(tClass,jedis.get(key));
    }

    public Object getObject(String key, Type type){
        return lson.fromJson(type,jedis.get(key));
    }

    public <T> T getObject(String key, TypeToken<T> token){
        return (T) lson.fromJson(token,jedis.get(key));
    }

    public void setObject(String key,Object value){
        jedis.set(key,lson.toJsonByGson(value));
    }


}

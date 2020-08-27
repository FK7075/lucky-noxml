package com.lucky.jacklamb.redis;

import com.lucky.jacklamb.rest.LSON;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/8/27 15:48
 */
public class RedisCode {

    private static LSON lson=new LSON();
    private static Jedis jedis;
    static {
        jedis=new Jedis("192.168.126.128", 6379);
    }

    public <T> T getObject(String key,Class<T> tClass){
        return lson.fromJson(tClass,jedis.get(key));
    }

    public void setObject(String key,Object value){
        jedis.set(key,lson.toJsonByGson(value));
    }


}

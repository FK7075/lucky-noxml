package com.lucky.jacklamb.redis.pojo;

import com.lucky.jacklamb.redis.JedisFactory;
import com.lucky.jacklamb.rest.LSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/29 3:06 下午
 */
public class RString<K,V> extends RedisKey{

    public RString(){}

    @Override
    public void setKey(String newKey) {

    }

    public V set(K k, V v){
        jedis.set(serialization(k), serialization(k));
        return v;
    }

    public V get(K k){
        return (V) deserialization(pojoType,serialization(jedis.get(serialization(k))));
    }


}

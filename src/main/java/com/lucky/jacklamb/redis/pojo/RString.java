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
public class RString<Pojo> extends RedisKey{

    private Type type;

    public RString(String key) {
        super(key);
        type= ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public void setKey(String newKey) {
        this.key = "RString<"+type.getTypeName()+">-["+newKey+"]";
    }

    public void set(Pojo pojo, SetParams setParams){
        jedis.set(key,lson.toJsonByGson(pojo),setParams);
    }

    public void set(Pojo pojo){
        jedis.set(key,lson.toJsonByGson(pojo));
    }
}

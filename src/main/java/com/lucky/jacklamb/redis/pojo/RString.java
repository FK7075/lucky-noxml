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
public class RString<Pojo> implements RedisKey{

    private String key;

    private Type type;

    private Jedis jedis;

    private static LSON lson=new LSON();

    public RString(String key) {
        jedis= JedisFactory.getJedis();
        type= ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.key = "RString<"+type.getTypeName()+">-["+key+"]";
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String newKey) {
        this.key = "RString<"+type.getTypeName()+">-["+key+"]";
    }

    public void set(Pojo pojo, SetParams setParams){
        jedis.set(key,lson.toJsonByGson(pojo),setParams);
    }

    public void set(Pojo pojo){
        jedis.set(key,lson.toJsonByGson(pojo));
    }
}

package com.lucky.jacklamb.redis.pojo;

import com.lucky.jacklamb.redis.JedisFactory;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.serializable.JDKSerializationScheme;
import com.lucky.jacklamb.utils.serializable.SerializationScheme;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/11/12 14:28
 */
public abstract class RedisSerialization {

    protected Jedis jedis;
    protected RKey rkey;
    private SerializationScheme serializationScheme;

    public RedisSerialization(){
        serializationScheme=new JDKSerializationScheme();
        jedis= JedisFactory.getJedis();
        rkey=new RKey(jedis);
    }

    public RKey getRkey() {
        return rkey;
    }

    public void setSerializationScheme(SerializationScheme serializationScheme) {
        this.serializationScheme = serializationScheme;
    }

    public String serialization(Object object){
        try {
            return serializationScheme.serialization(object);
        } catch (IOException e) {
            throw new RuntimeException("序列化异常！",e);
        }
    }

    public Object deserialization(Type objectType, String objectStr){
        try {
            if(objectStr==null){
                return null;
            }
            return serializationScheme.deserialization(objectType,objectStr);
        } catch (Exception e) {
            throw new RuntimeException("反序列化异常！",e);
        }
    }
}

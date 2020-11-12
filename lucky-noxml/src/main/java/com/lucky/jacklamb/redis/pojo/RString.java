package com.lucky.jacklamb.redis.pojo;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/29 3:06 下午
 */
public class RString<K,V> extends RedisKey{

    public RString(){}

    public int timeout=0;

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void setKey(String newKey) {

    }

    public V set(K k, V v){
        String key = serialization(k);
        jedis.set(key, serialization(v));
        pexpire(key);
        return v;
    }

    public V get(K k){
        return (V) deserialization(pojoType,serialization(jedis.get(serialization(k))));
    }

    public V get(String strKey){
        V v = (V) deserialization(pojoType,jedis.get(strKey));
        return v;
    }

    public V set(String strKey,V v){
        jedis.set(strKey,serialization(v));
        pexpire(strKey);
        return v;
    }

    public void pexpire(String key){
        if(timeout>0){
            jedis.expire(key,timeout);
        }
    }


}

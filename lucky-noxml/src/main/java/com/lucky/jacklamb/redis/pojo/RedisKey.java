package com.lucky.jacklamb.redis.pojo;

import com.lucky.jacklamb.md5.MD5Utils;
import com.lucky.jacklamb.redis.JedisFactory;
import com.lucky.jacklamb.utils.serializable.JDKSerializationScheme;
import com.lucky.jacklamb.utils.serializable.SerializationScheme;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/8/31 18:35
 */
public abstract class RedisKey extends RedisSerialization{

    protected String key;
    protected Type type;
    protected Type pojoType;

    public RedisKey(){}

    public RedisKey(String key){
        init(key);
    }

    public RedisKey(String key,int dbNubmer) {
        init(key);
        jedis.select(dbNubmer);
    }

    public RedisKey(String key,int dbNubmer,int seconds) {
        init(key);
        jedis.select(dbNubmer);
        jedis.expire(this.key,seconds);
    }

    public RedisKey(int seconds,String key) {
        init(key);
        jedis.expire(this.key,seconds);
    }

    private void init(String key){
        setType();
        setPojoType();
        setKey(key);
        formatKey();

    }

    /**
     * 获取RedisKey
     * @return
     */
    public String getKey(){
        return this.key;
    }

    /**
     * 重命名key
     * @param newKey
     * @return
     */
    public String rename(String newKey){
        String oldKey=getKey();
        setKey(newKey);
        formatKey();
        return jedis.rename(oldKey,getKey());
    }

    /**
     * 仅当 newkey 不存在时，将 key 改名为 newkey 。
     * @param newKey
     * @return
     */
    public Long renamenx(String newKey){
        String oldKey=getKey();
        setKey(newKey);
        formatKey();
        return jedis.renamenx(oldKey,getKey());
    }

    public abstract void setKey(String newKey);

    public void setPojoType(){

    }

    public void setType(){
        type= ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private void formatKey(){
        if(key!=null)
            key=MD5Utils.md5(key);
    }
}

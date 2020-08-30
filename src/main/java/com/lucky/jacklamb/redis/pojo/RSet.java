package com.lucky.jacklamb.redis.pojo;

import com.lucky.jacklamb.redis.JedisFactory;
import com.lucky.jacklamb.rest.LSON;
import redis.clients.jedis.Jedis;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/29 3:05 下午
 */
public class RSet<Pojo> {

    private String key;

    private Type type;

    private Jedis jedis;

    private static LSON lson=new LSON();

    public RSet(String key) {
        jedis= JedisFactory.getJedis();
        type= ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.key = "RSet<"+type.getTypeName()+">-["+key+"]";
    }

    public void sadd(Pojo...members){
        String[] strSet=new String[members.length];
        for (int i = 0; i < members.length; i++) {
            strSet[i]=lson.toJson(members[i]);
        }
        jedis.sadd(key,strSet);
    }

    public long size(){
        return jedis.scard(key);
    }

    public boolean sismember(Pojo pojo){
        return jedis.sismember(key,lson.toJsonByGson(pojo));
    }

    public Set<Pojo> smemers(){
        Set<Pojo> pojoSet=new HashSet<>();
        Set<String> pojoStrSet = jedis.smembers(key);
        for (String value : pojoStrSet) {
            pojoSet.add((Pojo)lson.fromJson(type,value));
        }
        return pojoSet;
    }

}

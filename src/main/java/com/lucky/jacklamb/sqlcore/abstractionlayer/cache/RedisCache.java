package com.lucky.jacklamb.sqlcore.abstractionlayer.cache;

import com.lucky.jacklamb.annotation.ioc.Value;
import com.lucky.jacklamb.redis.pojo.RHash;

import java.util.List;
import java.util.Map;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/1 14:36
 */
public class RedisCache implements Cache<String,List<Map<String,Object>>>{

    private RHash<String,List<Map<String, Object>>> rHash;

    public RedisCache(String dbname){
        rHash=new RHash<String,List<Map<String, Object>>>(dbname){};
    }

    @Override
    public List<Map<String, Object>> get(String s) {
        return rHash.hget(s);
    }

    @Override
    public List<Map<String, Object>> put(String s, List<Map<String, Object>> maps) {
        rHash.hset(s,maps);
        return maps;
    }

    @Override
    public boolean containsKey(String s) {
        return rHash.hexists(s);
    }

    @Override
    public void clear() {
        rHash.clear();
    }
}

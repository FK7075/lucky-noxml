package com.lucky.jacklamb.authority.shiro.cache;

import com.lucky.jacklamb.redis.pojo.RHash;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.util.*;

/**
 * lUCKY-Shiro缓存
 * @author fk7075
 * @version 1.0
 * @date 2020/11/11 16:15
 */
public class LuckyRedisShiroCache<K,V> implements Cache<K, V> {


    private RHash<K, V> hash;
    private String key;

    public LuckyRedisShiroCache(String key){
        hash =new RHash<K, V>(key){};
        this.key=key;
    }


    @Override
    public V get(K k) throws CacheException {
        if(hash.hexists(k)){

            return hash.hget(k);
        }
        return null;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        hash.hset(k,v);
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        if(hash.hexists(k)){
            V v = get(k);
            hash.hdel(k);
            return v;
        }
        return null;
    }

    @Override
    public void clear() throws CacheException {
        hash.clear();
    }

    @Override
    public int size() {
        return hash.size().intValue();
    }

    @Override
    public Set<K> keys() {
        return hash.hkeys();
    }

    @Override
    public Collection<V> values() {
        return hash.hvals();
    }
}


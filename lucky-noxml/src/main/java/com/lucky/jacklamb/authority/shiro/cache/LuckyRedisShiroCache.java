package com.lucky.jacklamb.authority.shiro.cache;

import com.lucky.jacklamb.redis.pojo.RString;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.util.Collection;
import java.util.Set;

/**
 * lUCKY-Shiro缓存
 * @author fk7075
 * @version 1.0
 * @date 2020/11/11 16:15
 */
public class LuckyRedisShiroCache<K,V> implements Cache<K, V> {


    private RString<K, V> hash;
    private String key;

    public LuckyRedisShiroCache(String key,int timeout_second){
        hash =new RString<K, V>(){};
        hash.setTimeout(timeout_second);
        this.key=key;
    }


    @Override
    public V get(K k) throws CacheException {
        String strKey=getKey(k);
        V v=hash.get(strKey);
        if(v!=null){
            hash.pexpire(strKey);
            return v;
        }
        return null;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        String strKey=getKey(k);
        hash.set(strKey,v);
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        String strKey=getKey(k);
        V v = get(k);
        hash.getRkey().del(strKey);
        return v;
    }

    @Override
    public void clear() throws CacheException {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Set<K> keys() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    private String getKey(K k){
        return key+":"+k.toString();
    }
}


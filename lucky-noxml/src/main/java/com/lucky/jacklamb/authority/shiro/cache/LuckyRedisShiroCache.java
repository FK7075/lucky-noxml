package com.lucky.jacklamb.authority.shiro.cache;

import com.lucky.jacklamb.redis.pojo.RString;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.util.*;

/**
 * LUCKY-SHIRO-REDIS缓存
 * @author fk7075
 * @version 1.0
 * @date 2020/11/11 16:15
 */
public class LuckyRedisShiroCache<K,V> implements Cache<K, V> {


    private RString<K, V> rstring;
    private String key;

    public LuckyRedisShiroCache(String key,int timeout_second){
        rstring =new RString<K, V>(){};
        rstring.setTimeout(timeout_second);
        this.key=key;
    }


    @Override
    public V get(K k) throws CacheException {
        String strKey=getKey(k);
        V v= rstring.get(strKey);
        if(v!=null){
            rstring.pexpire(strKey);
            return v;
        }
        return null;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        String strKey=getKey(k);
        rstring.set(strKey,v);
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        String strKey=getKey(k);
        V v = get(k);
        rstring.getRkey().del(strKey);
        return v;
    }

    @Override
    public void clear() throws CacheException {
        Set<String> keys = strKeys();
        String[] keyArray=new String[keys.size()];
        keys.toArray(keyArray);
        rstring.getRkey().del(keyArray);
    }

    @Override
    public int size() {
        return strKeys().size();
    }

    @Override
    public Set<K> keys() {
        return null;
    }

    @Override
    public Collection<V> values() {
        Set<String> keys = strKeys();
        List<V>  list=new ArrayList<>(keys.size());
        keys.forEach(k->list.add(rstring.get(k)));
        return list;
    }

    private Set<String> strKeys(){
        Set<String> keySet=new HashSet<>();
        rstring.getRkey().keys()
                .stream()
                .filter(k->k.startsWith(key+":"))
                .forEach(keySet::add);
        return keySet;
    }

    private String getKey(K k){
        return key+":"+k.toString();
    }
}


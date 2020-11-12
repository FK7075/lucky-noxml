package com.lucky.jacklamb.authority.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/11/11 17:27
 */
public class LuckyRedisShiroCacheManager implements CacheManager {

    @Override
    public <K, V> Cache<K, V> getCache(String s) throws CacheException {
        return new LuckyRedisShiroCache<K,V>(s);
    }
}

package com.lucky.jacklamb.sqlcore.abstractionlayer.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K,V> extends LinkedHashMap<K,V> {

    private final int MAX_CACHE_SIZE;

    public LRUCache(int cacheSize){
        super((int)Math.ceil(cacheSize / 0.75f) + 1, 0.75f, true);
        MAX_CACHE_SIZE = cacheSize;
    }

    public LRUCache(int cacheSize,float loadFactor){
        super((int)Math.ceil(cacheSize / loadFactor) + 1, loadFactor, true);
        MAX_CACHE_SIZE = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > MAX_CACHE_SIZE;
    }

    @Override
    public synchronized V get(Object key) {
        return super.get(key);
    }

    @Override
    public synchronized V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> m) {
        super.putAll(m);
    }

    @Override
    public synchronized V remove(Object key) {
        return super.remove(key);
    }
}

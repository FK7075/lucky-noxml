package com.lucky.jacklamb.authority.shiro.cache;

import com.lucky.jacklamb.redis.pojo.RHash;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.SimplePrincipalCollection;

import java.util.*;

/**
 * lUCKY-Shiro缓存
 * @author fk7075
 * @version 1.0
 * @date 2020/11/11 16:15
 */
public class LuckyRedisShiroCache implements Cache<SimplePrincipalCollection, SimpleAuthorizationInfo> {


    private RHash<String, ShiroRedisValue> hash;

    public LuckyRedisShiroCache(String key){
        hash=new RHash<String, ShiroRedisValue>(key){};
    }


    @Override
    public SimpleAuthorizationInfo get(SimplePrincipalCollection simplePrincipalCollection) throws CacheException {
        if(hash.hexists(simplePrincipalCollection.toString())){
            return hash.hget(simplePrincipalCollection.toString()).getSimpleAuthorizationInfo();
        }
        return null;
    }

    @Override
    public SimpleAuthorizationInfo put(SimplePrincipalCollection simplePrincipalCollection, SimpleAuthorizationInfo simpleAuthorizationInfo) throws CacheException {
        hash.hset(simplePrincipalCollection.toString(),new ShiroRedisValue(simplePrincipalCollection,simpleAuthorizationInfo));
        return simpleAuthorizationInfo;
    }

    @Override
    public SimpleAuthorizationInfo remove(SimplePrincipalCollection simplePrincipalCollection) throws CacheException {
        SimpleAuthorizationInfo simpleAuthorizationInfo = get(simplePrincipalCollection);
        hash.hdel(simplePrincipalCollection.toString());
        return simpleAuthorizationInfo;
    }

    @Override
    public void clear() throws CacheException {
        hash.clear();
    }

    @Override
    public int size() {
        Long size = hash.size();
        return size.intValue();
    }

    @Override
    public Set<SimplePrincipalCollection> keys() {
        Set<SimplePrincipalCollection> result=new HashSet<>(size());
        List<ShiroRedisValue> values = hash.hvals();
        values.stream().forEach(v->result.add(v.getSimplePrincipalCollection()));
        return result;
    }

    @Override
    public Collection<SimpleAuthorizationInfo> values() {
        Collection<SimpleAuthorizationInfo> result=new ArrayList<>(size());
        List<ShiroRedisValue> values = hash.hvals();
        values.stream().forEach(v->result.add(v.getSimpleAuthorizationInfo()));
        return result;
    }
}

class ShiroRedisValue{

   private SimplePrincipalCollection simplePrincipalCollection;
   private SimpleAuthorizationInfo simpleAuthorizationInfo;

    public ShiroRedisValue(SimplePrincipalCollection simplePrincipalCollection, SimpleAuthorizationInfo simpleAuthorizationInfo) {
        this.simplePrincipalCollection = simplePrincipalCollection;
        this.simpleAuthorizationInfo = simpleAuthorizationInfo;
    }

    public SimplePrincipalCollection getSimplePrincipalCollection() {
        return simplePrincipalCollection;
    }

    public void setSimplePrincipalCollection(SimplePrincipalCollection simplePrincipalCollection) {
        this.simplePrincipalCollection = simplePrincipalCollection;
    }

    public SimpleAuthorizationInfo getSimpleAuthorizationInfo() {
        return simpleAuthorizationInfo;
    }

    public void setSimpleAuthorizationInfo(SimpleAuthorizationInfo simpleAuthorizationInfo) {
        this.simpleAuthorizationInfo = simpleAuthorizationInfo;
    }
}


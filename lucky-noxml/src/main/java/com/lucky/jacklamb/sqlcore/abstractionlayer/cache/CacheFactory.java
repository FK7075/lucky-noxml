package com.lucky.jacklamb.sqlcore.abstractionlayer.cache;

import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;

import java.util.*;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/9/2 1:12 上午
 */
public class CacheFactory {

    public static Cache<String, List<Map<String,Object>>> getCache(String dbname){
        LuckyDataSource dataSource = ReaderInI.getDataSource(dbname);
        String cacheType = dataSource.getCacheType();
        if("Java".equals(cacheType)){
            return new LRUCache<String,List<Map<String,Object>>>(dataSource.getCacheCapacity());
        }else if("Redis".equals(cacheType)) {
            String ceTime = dataSource.getCacheExpiredTime();
            int outTime = (int) JavaConversion.strToBasic(ceTime, int.class, true);
            if(outTime==0){
                return new RedisCache(dbname);
            }
            return new RedisCache(dbname,outTime);
        }else{
            throw new RuntimeException("无法识别的缓存类型\'"+cacheType+"\"!");
        }

    }
}

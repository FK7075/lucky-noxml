package com.lucky.jacklamb.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/8/27 15:23
 */
public class RedisUtils {

    private static Jedis getJdeis(){
        JedisPoolConfig jpc=new JedisPoolConfig();
        JedisPool jpl=new JedisPool(jpc,"127.0.0.1",6379);
        return jpl.getResource();
    }
}

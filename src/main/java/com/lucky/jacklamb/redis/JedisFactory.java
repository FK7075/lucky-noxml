package com.lucky.jacklamb.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/29 7:53 下午
 */
public abstract class JedisFactory {

    public static Jedis getJedis(){
       return RedisUtils.getJedis();
    }

    public static void initJedisPool(){
        RedisUtils.init();
    }

    public static void initShardedJedisPool(){
        ShardedRedisUtils.init();
    }

    public static ShardedJedis getShardedJedis(){
        return ShardedRedisUtils.getShardedJedis();
    }
}

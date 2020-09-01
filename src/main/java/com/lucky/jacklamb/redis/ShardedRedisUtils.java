package com.lucky.jacklamb.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/8/27 15:23
 */
public class ShardedRedisUtils {

    private static final Logger log= LogManager.getLogger(ShardedRedisUtils.class);

    private static ShardedJedisPool pool=null;
    private static RedisConfig redisConfig=null;

    static {
        redisConfig = RedisConfig.getRedisConfig();
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redisConfig.getMaxTotal());
        config.setMaxIdle(redisConfig.getMaxIdle());
        config.setMaxWaitMillis(redisConfig.getMaxWaitMillis());
        config.setTestOnBorrow(redisConfig.isTestOnBorrow());
        config.setTestOnReturn(redisConfig.isTestOnReturn());
        // 集群
        JedisShardInfo jedisShardInfo1 = new JedisShardInfo(redisConfig.getHost(), redisConfig.getPort());
        if(redisConfig.getPassword()!=null){
            jedisShardInfo1.setPassword(redisConfig.getPassword());
        }
        List<JedisShardInfo> list = new LinkedList<>();
        list.add(jedisShardInfo1);
        pool = new ShardedJedisPool(config, list);
    }

    public static ShardedJedis getShardedJedis() {
        return pool.getResource();
    }

    public static void init(){
        log.info("Redis initialized successfully.");
    }
}

package com.lucky.jacklamb.redis;

import com.lucky.jacklamb.ioc.ControllerIOC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/29 7:50 下午
 */
public class RedisUtils {

    private static final Logger log= LogManager.getLogger(RedisUtils.class);

    private static JedisPool jsp=null;
    private static RedisConfig redisConfig=null;

    static {
        log.info("Redis Start Initialization...");
        redisConfig = RedisConfig.getRedisConfig();
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redisConfig.getMaxTotal());
        config.setMaxIdle(redisConfig.getMaxIdle());
        config.setMaxWaitMillis(redisConfig.getMaxWaitMillis());
        config.setTestOnBorrow(redisConfig.isTestOnBorrow());
        config.setTestOnReturn(redisConfig.isTestOnReturn());
        if(redisConfig.getPassword()!=null){
            jsp = new JedisPool(config, redisConfig.getHost(), redisConfig.getPort(),redisConfig.getTimeout(),redisConfig.getPassword());
        }else{
            jsp = new JedisPool(config, redisConfig.getHost(), redisConfig.getPort(),redisConfig.getTimeout());
        }

    }

    public static Jedis getJedis() {
        Jedis jedis = jsp.getResource();
        jedis.select(redisConfig.getDbNumber());
        return jedis;
    }

    public static void init(){
        log.info("Redis Initialized Successfully.");
    }
}

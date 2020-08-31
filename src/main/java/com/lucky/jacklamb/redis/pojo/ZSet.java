package com.lucky.jacklamb.redis.pojo;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/30 4:57 上午
 */
public class ZSet implements RedisKey{

    private String key;
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String newKey) {
        this.key=newKey;
    }
}

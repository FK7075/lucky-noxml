package com.lucky.jacklamb.redis;

import com.lucky.jacklamb.file.ini.IniFilePars;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/29 6:46 下午
 */
public class RedisConfig {

    public static final String SECTION="Redis";

    private static RedisConfig redisConfig;
    private int dbNumber=0;
    private String host;
    private int port;
    private String password;
    private int timeout = 2000;
    private int maxIdle = 300;
    private int maxActive = 600;
    private int maxTotal = 1000;
    private int maxWaitMillis = 1000;
    private int minEvictableIdleTimeMillis = 300000;
    private int numTestsPerEvictionRun = 1024;
    private int timeBetweenEvictionRunsMillis = 30000;
    private boolean testOnBorrow = true;
    private boolean testOnReturn =true;
    private boolean testWhileIdle = false;

    private RedisConfig(){};

    public static RedisConfig getRedisConfig(){
        if(redisConfig==null){
            redisConfig=new RedisConfig();
            readIniConfig();
        }
        return redisConfig;
    }

    private static void readIniConfig(){
        Map<String,String> redisCfg= IniFilePars.getIniFilePars().getSectionMap(SECTION);
        Field[] allFields = ClassUtils.getAllFields(RedisConfig.class);
        for (Field field : allFields) {
            String key = field.getName();
            if(Modifier.isFinal(field.getModifiers()))
                continue;
            if(redisCfg.containsKey(key)){
                FieldUtils.setValue(redisConfig,field, JavaConversion.strToBasic(redisCfg.get(key),field.getType(),true));
            }
        }

    }

    public int getDbNumber() {
        return dbNumber;
    }

    public void setDbNumber(int dbNumber) {
        this.dbNumber = dbNumber;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public void setMaxWaitMillis(int maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }
}


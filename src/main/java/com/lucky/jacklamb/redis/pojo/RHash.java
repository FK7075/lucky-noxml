package com.lucky.jacklamb.redis.pojo;

import com.lucky.jacklamb.redis.JedisFactory;
import com.lucky.jacklamb.rest.LSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/29 2:58 下午
 */
public class RHash<Field,Pojo> {

    private static LSON lson=new LSON();
    private Jedis jedis;
    private Type fieldType;
    private Type pojoType;
    private String key;

    public RHash(String key){
        jedis= JedisFactory.getJedis();
        fieldType= ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        pojoType= ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        this.key = "RHash<"+fieldType.getTypeName()+","+pojoType.getTypeName()+">-["+key+"]";
    }

    public String getKey() {
        return key;
    }

    public void hset(Field field, Pojo pojo){
        jedis.hset(key,lson.toJsonByGson(field),lson.toJsonByGson(pojo));
    }

    public Pojo hget(Field field){
        return (Pojo) lson.fromJson(pojoType,jedis.hget(key,lson.toJsonByGson(field)));
    }

    public boolean hexists(Field field){
        return jedis.hexists(key,lson.toJsonByGson(field));
    }

    public void hdel(Field...fields){
        String[] fieldStrs=new String[fields.length];
        for (int i = 0,j=fields.length; i < j; i++) {
            fieldStrs[i]=lson.toJson(fields[i]);
        }
        jedis.hdel(key,fieldStrs);
    }

    public Set<Field> hkeys(){
        Set<Field> keySet= new HashSet<>();
        Set<String> strKeys = jedis.hkeys(key);
        for (String strKey : strKeys) {
            keySet.add((Field) lson.fromJson(fieldType,strKey));
        }
        return keySet;
    }

    public long size(){
        return jedis.hlen(key);
    }

    public List<Pojo> hmget(Field...fields){
        String[] fieldStrs=new String[fields.length];
        for (int i = 0,j=fields.length; i < j; i++) {
            fieldStrs[i]=lson.toJson(fields[i]);
        }
        return jedis.hmget(key,fieldStrs).stream().map((k)->{
            Pojo pojo= (Pojo) lson.fromJson(pojoType,k);
            return pojo;
        }).collect(Collectors.toList());
    }

    public void hmset(Map<Field,Pojo> map){
        Map<String,String> strMap=new HashMap<>();
        for(Map.Entry<Field,Pojo> entry:map.entrySet()){
            strMap.put(lson.toJsonByGson(entry.getKey()),lson.toJson(entry.getValue()));
        }
        jedis.hmset(key,strMap);
    }

    public List<Pojo> hvals(){
        return jedis.hvals(key).stream().map((p)->{
            Pojo pojo= (Pojo) lson.fromJson(pojoType,p);
            return pojo;
        }).collect(Collectors.toList());
    }

    public void hsetnx(Field field,Pojo pojo){
        jedis.hsetnx(key,lson.toJsonByGson(field),lson.toJsonByGson(pojo));
    }

    public Map<Field,Pojo> hgetall(){
        Map<Field,Pojo> kvMap=new HashMap<>();
        Map<String, String> kvStrMap = jedis.hgetAll(key);
        for(Map.Entry<String,String> entry:kvStrMap.entrySet()){
            kvMap.put((Field) lson.fromJson(fieldType,entry.getKey()),(Pojo) lson.fromJson(pojoType,entry.getValue()));
        }
        return kvMap;
    }

    public ScanResult<Map.Entry<Field,Pojo>> hscan(String cursor){
        return hscan(cursor,new ScanParams());
    }

    public ScanResult<Map.Entry<Field,Pojo>> hscan(String cursor, ScanParams params){
        ScanResult<Map.Entry<String, String>> hscan = jedis.hscan(key, cursor,params);
        List<Map.Entry<String, String>> result = hscan.getResult();
        List<Map.Entry<Field, Pojo>> results = new ArrayList<Map.Entry<Field, Pojo>>();
        for (Map.Entry<String, String> entry : result) {
            results.add(new AbstractMap.SimpleEntry<Field, Pojo>((Field) lson.fromJson(fieldType,entry.getKey()),(Pojo) lson.fromJson(pojoType,entry.getValue())));
        }
        return new ScanResult<Map.Entry<Field,Pojo>>(cursor,results);
    }
}

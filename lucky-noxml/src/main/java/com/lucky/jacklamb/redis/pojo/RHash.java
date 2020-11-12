package com.lucky.jacklamb.redis.pojo;

import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis-Hash
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/29 2:58 下午
 */
public class RHash<Field,Pojo> extends RedisKey {

    public RHash(String key) {
        super(key);
    }

    public RHash(String key, int dbNubmer) {
        super(key, dbNubmer);
    }

    public RHash(String key, int dbNubmer, int seconds) {
        super(key, dbNubmer, seconds);
    }

    public RHash(int seconds, String key) {
        super(seconds, key);
    }


    public void setPojoType(){
        pojoType= ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     * @param field
     * @param increment
     * @return
     */
    public Long hincrBy(Field field,Long increment){
        return jedis.hincrBy(key,serialization(field),increment);
    }

    /**
     * 为哈希表 key 中的指定字段的浮点数值加上增量 increment
     * @param field
     * @param increment
     * @return
     */
    public Double hincrByFloat(Field field,double increment){
        return jedis.hincrByFloat(key,serialization(field),increment);
    }

    @Override
    public void setKey(String newKey) {
        this.key = "RHash<"+ type.getTypeName()+","+pojoType.getTypeName()+">-["+newKey+"]";
        key=key.replaceAll(" ","");
    }

    /**
     * 将哈希表中的字段 field 的值设为 value
     * @param field key字段
     * @param pojo 值
     * @return
     */
    public Long hset(Field field, Pojo pojo){
        return jedis.hset(key,serialization(field),serialization(pojo));
    }

    /**
     * 获取存储在哈希表中指定字段的值。
     * @param field key
     * @return
     */
    public Pojo hget(Field field){
        return (Pojo) deserialization(pojoType,jedis.hget(key,serialization(field)));
    }

    /**
     * 查看哈希表 key 中，指定的字段是否存在。
     * @param field key
     * @return
     */
    public boolean hexists(Field field){
        return jedis.hexists(key,serialization(field));
    }

    /**
     * 删除一个或多个哈希表字段
     * @param fields 要删除字段的keys
     */
    public Long hdel(Field...fields){
        String[] fieldStrs=new String[fields.length];
        for (int i = 0,j=fields.length; i < j; i++) {
            fieldStrs[i]=serialization(fields[i]);
        }
        return jedis.hdel(key,fieldStrs);
    }

    /**
     * 获取所有哈希表中的字段
     * @return
     */
    public Set<Field> hkeys(){
        Set<Field> keySet= new HashSet<>();
        Set<String> strKeys = jedis.hkeys(key);
        for (String strKey : strKeys) {
            keySet.add((Field) deserialization(type,strKey));
        }
        return keySet;
    }

    /**
     * 获取hash表中元素的个数
     * @return
     */
    public Long size(){
        return jedis.hlen(key);
    }

    /**
     * 获取所有给定字段的值
     * @param fields
     * @return
     */
    public List<Pojo> hmget(Field...fields){
        String[] fieldStrs=new String[fields.length];
        for (int i = 0,j=fields.length; i < j; i++) {
            fieldStrs[i]=serialization(fields[i]);
        }
        return jedis.hmget(key,fieldStrs).stream().map((k)->{
            Pojo pojo= (Pojo) deserialization(pojoType,k);
            return pojo;
        }).collect(Collectors.toList());
    }

    /**
     *同时将多个 field-value (域-值)对设置到哈希表 key 中。
     * @param map
     */
    public String hmset(Map<Field,Pojo> map){
        Map<String,String> strMap=new HashMap<>();
        for(Map.Entry<Field,Pojo> entry:map.entrySet()){
            strMap.put(serialization(entry.getKey()),serialization(entry.getValue()));
        }
        return jedis.hmset(key,strMap);
    }

    /**
     * 获取哈希表中所有值。
     * @return
     */
    public List<Pojo> hvals(){
        return jedis.hvals(key).stream().map((p)->{
            Pojo pojo= (Pojo) deserialization(pojoType,p);
            return pojo;
        }).collect(Collectors.toList());
    }

    /**
     * 只有在字段 field 不存在时，设置哈希表字段的值。
     * @param field
     * @param pojo
     * @return
     */
    public Long hsetnx(Field field, Pojo pojo){
        return jedis.hsetnx(key,serialization(field),serialization(pojo));
    }

    /**
     * 获取在哈希表中指定 key 的所有字段和值
     * @return
     */
    public Map<Field,Pojo> hgetall(){
        Map<Field,Pojo> kvMap=new HashMap<>();
        Map<String, String> kvStrMap = jedis.hgetAll(key);
        for(Map.Entry<String,String> entry:kvStrMap.entrySet()){
            kvMap.put((Field) deserialization(type,entry.getKey()),(Pojo) deserialization(pojoType,entry.getValue()));
        }
        return kvMap;
    }

    /**
     * 获取一个用于遍历该hash的迭代器
     * @param cursor 游标
     * @return
     */
    public ScanResult<Map.Entry<Field,Pojo>> hscan(String cursor){
        return hscan(cursor,new ScanParams());
    }

    /**
     * 获取一个用于遍历该hash的迭代器
     * @param cursor 游标
     * @param params
     * @return
     */
    public ScanResult<Map.Entry<Field,Pojo>> hscan(String cursor, ScanParams params){
        ScanResult<Map.Entry<String, String>> hscan = jedis.hscan(key, cursor,params);
        List<Map.Entry<String, String>> result = hscan.getResult();
        List<Map.Entry<Field, Pojo>> results = new ArrayList<Map.Entry<Field, Pojo>>();
        for (Map.Entry<String, String> entry : result) {
            results.add(new AbstractMap.SimpleEntry<Field, Pojo>((Field) deserialization(type,entry.getKey()),(Pojo) deserialization(pojoType,entry.getValue())));
        }
        return new ScanResult<Map.Entry<Field,Pojo>>(cursor,results);
    }

    public void clear(){
        jedis.del(key);
    }
    /**
     * 关闭Redis连接
     */
    public void close(){
        jedis.close();
    }
}

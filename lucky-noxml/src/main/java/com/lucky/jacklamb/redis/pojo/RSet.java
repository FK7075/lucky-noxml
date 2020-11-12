package com.lucky.jacklamb.redis.pojo;

import com.lucky.jacklamb.redis.JedisFactory;
import com.lucky.jacklamb.rest.LSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis-Set
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/29 3:05 下午
 */
public class RSet<Pojo> extends RedisKey{


    public RSet(String key) {
        super(key);
    }

    public RSet(String key, int dbNubmer) {
        super(key, dbNubmer);
    }

    public RSet(String key, int dbNubmer, int seconds) {
        super(key, dbNubmer, seconds);
    }

    public RSet(int seconds, String key) {
        super(seconds, key);
    }

    @Override
    public void setKey(String newKey) {
        this.key = "RSet<"+type.getTypeName()+">-["+newKey+"]";
        key=key.replaceAll(" ","");
    }

    /**
     * 向集合添加一个或多个成员
     * @param members 待添加的元素
     * @return
     */
    public Long sadd(Pojo...members){
        String[] strSet=new String[members.length];
        for (int i = 0; i < members.length; i++) {
            strSet[i]=serialization(members[i]);
        }
        return jedis.sadd(key,strSet);
    }

    /**
     * 获取集合的成员数
     * @return
     */
    public Long size(){
        return jedis.scard(key);
    }

    private String[] getKeys(RSet<Pojo>...otherSets){
        String[] otherKey=new String[otherSets.length+1];
        otherKey[0]=key;
        int index=1;
        for (RSet<Pojo> otherSet : otherSets) {
            otherKey[index]=otherSet.getKey();
        }
        return otherKey;
    }

    private Set<Pojo> getSet(Set<String> jsonSet){
        Set<Pojo> pojoSet=new HashSet<>();
        for (String json:jsonSet){
            pojoSet.add((Pojo) deserialization(type,json));
        }
        return pojoSet;
    }

    /**
     * 返回本集合与其他集合之间的差异。
     * @param otherSets 其他集合
     * @return
     */
    public Set<Pojo> sdiif(RSet<Pojo>...otherSets){
        return getSet(jedis.sdiff(getKeys(otherSets)));
    }

    /**
     * 返回本集合与给定所有集合的差集并存储在 destination集合 中
     * @param destination 差异存储集合
     * @param otherSets 其他对比集合
     * @return
     */
    public Long sdiifstore(RSet<Pojo> destination, RSet<Pojo>...otherSets){
        return jedis.sdiffstore(destination.getKey(),getKeys(otherSets));
    }

    /**
     * 返回本集合与给定所有集合的交集
     * @param otherSets 其他对比集合
     * @return
     */
    public Set<Pojo> sinter(RSet<Pojo>...otherSets){
        return getSet(jedis.sinter(getKeys(otherSets)));
    }

    /**
     * 返回本集合与给定所有集合的交集并存储在 destination集合中
     * @param destination 差异存储集合
     * @param otherSets 其他对比集合
     * @return
     */
    public Long sinterstore(RSet<Pojo> destination, RSet<Pojo>...otherSets){
        return jedis.sinterstore(destination.getKey(),getKeys(otherSets));
    }

    /**
     * 判断pojo元素是否是该集合的成员
     * @param pojo 待判断的元素
     * @return
     */
    public boolean sismember(Pojo pojo){
        return jedis.sismember(key,serialization(pojo));
    }

    /**
     * 返回集合中的所有成员
     * @return
     */
    public Set<Pojo> smembers(){
        return getSet(jedis.smembers(key));
    }

    /**
     * 将pojo元素从本合移动到 destination集合
     * @param destination 存储集合
     * @param pojo 待移动的元素
     * @return
     */
    public Long smove(RSet<Pojo> destination,Pojo pojo){
        if(sismember(pojo)){
            return jedis.smove(key,destination.getKey(),serialization(pojo));
        }
        throw new RuntimeException("集合\""+key+"\"中不存在该元素:["+pojo+"]，无法移动！");
    }

    /**
     * 移除并返回集合中的一个随机元素
     * @return
     */
    public Pojo spop(){
        return (Pojo) deserialization(type,jedis.spop(key));
    }

    /**
     * 移除并返回集合中的一组随机元素
     * @param count 元素个数
     * @return
     */
    public Set<Pojo> spop(long count){
        return getSet(jedis.spop(key,count));
    }

    /**
     * 返回集合中的一个随机元素
     * @return
     */
    public Pojo srandmember(){
        return (Pojo) deserialization(type,jedis.srandmember(key));
    }

    /**
     * 返回集合中的一组随机元素
     * @return
     */
    public List<Pojo> srandmember(int count){
        List<Pojo> pojoList=new ArrayList<>();
        List<String> jsonList = jedis.srandmember(key, count);
        return jsonList.stream().map((j)->{
           Pojo pojo= (Pojo) deserialization(type,j);
           return pojo;
        }).collect(Collectors.toList());
    }

    /**
     * 移除集合中一个或多个成员
     * @param pojos
     * @return
     */
    public Long srem(Pojo...pojos){
        String[] jsonPojo=new String[pojos.length];
        for (int i = 0,j=pojos.length; i <j ; i++) {
            jsonPojo[i]=serialization(pojos[i]);
        }
        return jedis.srem(key,jsonPojo);
    }

    /**
     * 返回本集合与所有给定集合的并集
     * @param otherSets
     * @return
     */
    public Set<Pojo> sunion( RSet<Pojo>...otherSets){
        return getSet(jedis.sunion(getKeys(otherSets)));
    }

    /**
     * 返回本集合与所有给定集合的并集,并将所有元素放入destination集合中
     * @param destination 存储集合
     * @param otherSets  其他集合
     * @return
     */
    public Long sunionstore(RSet<Pojo> destination, RSet<Pojo>...otherSets){
        return jedis.sunionstore(destination.getKey(),getKeys(otherSets));
    }

    /**
     * 迭代集合中的元素
     * @param cursor 游标
     * @return
     */
    public ScanResult<Pojo> sscan(String cursor){
        return sscan(cursor,new ScanParams());
    }

    /**
     * 迭代集合中的元素
     * @param cursor 游标
     * @param scanParams
     * @return
     */
    public ScanResult<Pojo> sscan(String cursor, ScanParams scanParams){
        ScanResult<String> sscan = jedis.sscan(key, cursor,scanParams);
        List<Pojo> pojoList=sscan.getResult().stream().map((j)->{
            Pojo pojo= (Pojo) deserialization(type,j);
            return pojo;
        }).collect(Collectors.toList());
        return new ScanResult<Pojo>(sscan.getCursor(),pojoList);
    }

    /**
     * 关闭Redis连接
     */
    public void close(){
        jedis.close();
    }

}

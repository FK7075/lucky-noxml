package com.lucky.jacklamb.redis.pojo;

import com.lucky.jacklamb.redis.JedisFactory;
import com.lucky.jacklamb.rest.LSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ListPosition;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.lucky.jacklamb.redis.pojo.Constant.DEL;

/**
 * Redis-List
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/29 3:05 下午
 */
public class RList<Pojo> implements RedisKey{

    private String key;

    private Type type;

    private Jedis jedis;

    private static LSON lson=new LSON();

    public RList(String key) {
        jedis= JedisFactory.getJedis();
        type= ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.key = "RList<"+type.getTypeName()+">-["+key+"]";
    }

    /**
     * 获取RedisKey
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * 返回一个Iterator，用于遍历这个RList
     * @return
     */
    public Iterator<Pojo> iterator(){
        Iterator<Pojo> iterator=new Iterator<Pojo>() {
            int index=0;
            boolean isRead=false;
            boolean isRemove=false;
            @Override
            public boolean hasNext() {
                if(index<size()){
                    isRead=false;
                    isRemove=false;
                    return true;
                }
                return false;
            }

            @Override
            public Pojo next() {
                long size=isRead?size()+1:size();
                if(index<size){
                    if(isRemove){
                        throw new UnsupportedOperationException("Element has been deleted");
                    }
                    if(!isRead){
                        Pojo pojo= getByIndex(index);
                        index++;
                        isRead=true;
                        return pojo;
                    }else{
                        return getByIndex(index-1);
                    }
                }
                return null;
            }

            @Override
            public void remove() {
                long size=isRead?size()+1:size();
                if(index<size){
                    if(isRead){
                        if (isRemove){
                            throw new UnsupportedOperationException("Element has been deleted");
                        }
                       lremByIndex(index-1);
                       index=index-1;
                       isRemove=true;
                    }else{
                        if (isRemove){
                            throw new UnsupportedOperationException("Element has been deleted");
                        }
                        lremByIndex(index);
                        isRemove=true;
                    }
                }else{
                    throw new UnsupportedOperationException("remove");
                }
            }
        };
        return iterator;
    }

    /**
     * 对象集合转Json数组
     * @param pojoList 对象集合
     * @param isRev 是否逆序
     * @return
     */
    private String[] list2JsonArray(List<Pojo> pojoList,boolean isRev){
        String[] jsonpojo=new String[pojoList.size()];
        if(isRev){
            for (int j=pojoList.size()-1,i =j; i >-1 ; i--) {
                jsonpojo[j-i]=lson.toJsonByGson(pojoList.get(i));
            }
        }else{
            for (int i = 0,j=pojoList.size(); i <j ; i++) {
                jsonpojo[i]=lson.toJsonByGson(pojoList.get(i));
            }
        }
        return jsonpojo;
    }

    /**
     * 将一个或多个对象插入到列表头部
     * @param pojo
     */
    public Long lpush(Pojo...pojo){
        String[] jsonpojo=new String[pojo.length];
        for (int i = 0,j=pojo.length; i <j ; i++) {
            jsonpojo[i]=lson.toJsonByGson(pojo[i]);
        }
        return jedis.lpush(key,jsonpojo);
    }

    /**
     * 将一个对象集合插入到列表头部
     * @param pojoList
     */
    public Long lpushAll(List<Pojo> pojoList){
        return jedis.lpush(key,list2JsonArray(pojoList,false));
    }

    /**
     * 将一个对象集合逆向插入到列表头部
     * @param pojoList
     */
    public Long lpushAllRev(List<Pojo> pojoList){
        return jedis.lpush(key,list2JsonArray(pojoList,true));
    }

    /**
     * 将一个或多个对象插入到列表尾部
     * @param pojo
     */
    public Long rpush(Pojo...pojo){
        String[] jsonpojo=new String[pojo.length];
        for (int i = 0,j=pojo.length; i <j ; i++) {
            jsonpojo[i]=lson.toJsonByGson(pojo[i]);
        }
        return jedis.rpush(key,jsonpojo);
    }

    /**
     * 将一个对象集合插入到列表尾部
     * @param pojoList
     */
    public Long rpushAll(List<Pojo> pojoList){
        return jedis.rpush(key,list2JsonArray(pojoList,false));
    }

    /**
     * 将一个对象集合逆向插入到列表尾部
     * @param pojoList
     */
    public Long rpushAllRev(List<Pojo> pojoList){
        return jedis.rpush(key,list2JsonArray(pojoList,true));
    }

    /**
     * 移除index位置的元素
     * @param index 要移除元素的索引
     */
    public Long lremByIndex(int index){
        if(index<0||index>size()-1){
            throw new ArrayIndexOutOfBoundsException("index="+index);
        }
        jedis.lset(key,index,DEL);
        return jedis.lrem(key,1,DEL);
    }

    /**
     * 移除列表中的pojo元素，如果有多个相同的pojo元素，会将所有的都移除
     * @param pojo 要移除的对象
     */
    public Long lremAll(Pojo pojo){
        return jedis.lrem(key,0,lson.toJsonByGson(pojo));
    }

    /**
     * 移除列表中的pojo元素，如果有多个相同的pojo元素，会移除前count个
     * @param pojo 要移除的对象
     * @param count 存在多个相同元素时移除的个数
     */
    public Long lrem(Pojo pojo,int count){
        return jedis.lrem(key,count,lson.toJsonByGson(pojo));
    }

    /**
     * 通过索引值得到一个元素
     * @param index 索引
     * @return
     */
    public Pojo getByIndex(int index){
        return (Pojo) lson.fromJson(type,jedis.lindex(key,index));
    }

    /**
     * 得到列表中指定位置范围的所有元素
     * @param start 开始位置
     * @param stop 结束位置
     * @return
     */
    public List<Pojo> getByLimit(int start,int stop){
        List<String> strPojos = jedis.lrange(key, start, stop);
        return strPojos.stream().map((sp)->{
            Pojo pojo = (Pojo) lson.fromJson(type, sp);
            return pojo;
        }).collect(Collectors.toList());
    }

    /**
     * 得到列表中所有的元素
     * @return
     */
    public List<Pojo> getAll(){
        return getByLimit(0,-1);
    }

    /**
     * 获取列表的长度
     * @return
     */
    public Long size(){
        return jedis.llen(key);
    }

    /**
     * 删除该RedisKey和列表中的所有元素
     */
    public Long del(){
        return jedis.del(key);
    }

    /**
     * 移出并获取列表的第一个元素
     * @return
     */
    public Pojo lpop(){
        return (Pojo) lson.fromJson(type,jedis.lpop(key));
    }

    /**
     * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * @return
     */
    public Pojo blpop(){
        List<String> blpop = jedis.blpop(key);
        if(blpop!=null&&!blpop.isEmpty()){
            return (Pojo) lson.fromJson(type,blpop.get(0));
        }
        return null;
    }

    /**
     * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * @param timout 等待超时时间
     * @return
     */
    public Pojo blpop(int timout){
        List<String> blpop = jedis.blpop(timout,key);
        if(blpop!=null&&!blpop.isEmpty()){
            return (Pojo) lson.fromJson(type,blpop.get(0));
        }
        return null;
    }

    /**
     * 移除列表的最后一个元素，返回值为移除的元素。
     * @return
     */
    public Pojo rpop(){
        return (Pojo) lson.fromJson(type,jedis.rpop(key));
    }

    /**
     * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * @return
     */
    public Pojo brpop(){
        List<String> blpop = jedis.brpop(key);
        if(blpop!=null&&!blpop.isEmpty()){
            return (Pojo) lson.fromJson(type,blpop.get(0));
        }
        return null;
    }

    /**
     * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * @param timout 超时时间
     * @return
     */
    public Pojo brpop(int timout){
        List<String> blpop = jedis.brpop(timout,key);
        if(blpop!=null&&!blpop.isEmpty()){
            return (Pojo) lson.fromJson(type,blpop.get(0));
        }
        return null;
    }

    /**
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除
     * @param start 开始位置
     * @param stop 结束位置
     */
    public String ltrim(int start,int stop){
        return jedis.ltrim(key, start, stop);
    }

    /**
     * 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它;
     * 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     * @param destinationPojo 待插入的列表
     * @param timout 等待超时时间
     * @return
     */
    public Pojo brpoplpush(RList<Pojo> destinationPojo,int timout){
        return (Pojo) lson.fromJson(type,jedis.brpoplpush(key,destinationPojo.getKey(),timout));
    }

    /**
     * 移除列表的最后一个元素，并将该元素添加到另一个列表并返回
     * @param destinationPojo 待插入的列表
     * @return
     */
    public Pojo rpoplpush(RList<Pojo> destinationPojo){
        return (Pojo) lson.fromJson(type,jedis.rpoplpush(key,destinationPojo.getKey()));
    }

    public Long linsert(ListPosition listPosition,Pojo pivot,Pojo value){
        return jedis.linsert(key,listPosition,lson.toJsonByGson(pivot),lson.toJsonByGson(value));
    }

    /**
     * 关闭Redis连接
     */
    public void close(){
        jedis.close();
    }


//    public static void main(String[] args) {
//        RList foo = new RList<Book>("sds"){};
//        // 在类的外部这样获取
//        Type type = ((ParameterizedType)foo.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//        System.out.println(type.getTypeName());
//    }

}

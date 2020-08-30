package com.lucky.jacklamb.redis.pojo;

import com.lucky.jacklamb.redis.JedisFactory;
import com.lucky.jacklamb.rest.LSON;
import redis.clients.jedis.Jedis;

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
public class RList<Pojo> {

    private String key;

    private Type type;

    private Jedis jedis;

    private static LSON lson=new LSON();

    public RList(String key) {
        jedis= JedisFactory.getJedis();
        type= ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.key = "RList<"+type.getTypeName()+">-["+key+"]";
    }

    public String getKey() {
        return key;
    }

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


    public void lpush(Pojo pojo){
        jedis.lpush(key,lson.toJsonByGson(pojo));
    }

    public void lpushAll(List<Pojo> pojoList){
        for (Pojo pojo : pojoList) {
            lpush(pojo);
        }
    }

    public void lpushAllRev(List<Pojo> pojoList){
        for(int i=pojoList.size()-1;i>=-1;i--){
            lpush(pojoList.get(i));
        }
    }

    public void rpush(Pojo pojo){
        jedis.rpush(key,lson.toJsonByGson(pojo));
    }

    public void rpushAll(List<Pojo> pojoList){
        for (Pojo pojo : pojoList) {
            rpush(pojo);
        }
    }

    public void rpushAllRev(List<Pojo> pojoList){
        for(int i=pojoList.size()-1;i>=-1;i--){
            rpush(pojoList.get(i));
        }
    }

    public void lremByIndex(int index){
        if(index<0||index>size()-1){
            throw new ArrayIndexOutOfBoundsException("index="+index);
        }
        jedis.lset(key,index,DEL);
        jedis.lrem(key,1,DEL);
    }

    public void lremAll(Pojo pojo){
        jedis.lrem(key,0,lson.toJsonByGson(pojo));
    }

    public void lrem(Pojo pojo,int count){
        jedis.lrem(key,count,lson.toJsonByGson(pojo));
    }

    public Pojo getByIndex(int index){
        return (Pojo) lson.fromJson(type,jedis.lindex(key,index));
    }

    public List<Pojo> getByLimit(int start,int stop){
        List<String> strPojos = jedis.lrange(key, start, stop);
        return strPojos.stream().map((sp)->{
            Pojo pojo = (Pojo) lson.fromJson(type, sp);
            return pojo;
        }).collect(Collectors.toList());
    }

    public List<Pojo> getAll(){
        return getByLimit(0,-1);
    }

    public Long size(){
        return jedis.llen(key);
    }

    public void del(){
        jedis.del(key);
    }

    public Pojo lpop(){
        return (Pojo) lson.fromJson(type,jedis.lpop(key));
    }

    public Pojo blpop(){
        List<String> blpop = jedis.blpop(key);
        if(blpop!=null&&!blpop.isEmpty()){
            return (Pojo) lson.fromJson(type,blpop.get(0));
        }
        return null;
    }

    public Pojo blpop(int timout){
        List<String> blpop = jedis.blpop(timout,key);
        if(blpop!=null&&!blpop.isEmpty()){
            return (Pojo) lson.fromJson(type,blpop.get(0));
        }
        return null;
    }

    public Pojo rpop(){
        return (Pojo) lson.fromJson(type,jedis.rpop(key));
    }

    public Pojo brpop(){
        List<String> blpop = jedis.brpop(key);
        if(blpop!=null&&!blpop.isEmpty()){
            return (Pojo) lson.fromJson(type,blpop.get(0));
        }
        return null;
    }

    public Pojo brpop(int timout){
        List<String> blpop = jedis.brpop(timout,key);
        if(blpop!=null&&!blpop.isEmpty()){
            return (Pojo) lson.fromJson(type,blpop.get(0));
        }
        return null;
    }

    public void ltrim(int start,int stop){
        jedis.ltrim(key, start, stop);
    }


//    public static void main(String[] args) {
//        RList foo = new RList<Book>("sds"){};
//        // 在类的外部这样获取
//        Type type = ((ParameterizedType)foo.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//        System.out.println(type.getTypeName());
//    }

}

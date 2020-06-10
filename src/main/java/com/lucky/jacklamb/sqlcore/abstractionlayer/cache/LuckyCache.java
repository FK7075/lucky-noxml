package com.lucky.jacklamb.sqlcore.abstractionlayer.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Lucky查询缓存处理类(DL)
 * 
 * @author fk-7075
 *
 */
public class LuckyCache {
	private static Map<String,Map<String, List<?>>> cacheMap;

	private LuckyCache() {
		if(cacheMap==null)
			cacheMap = new HashMap<String,Map<String, List<?>>>();
	}

	public static LuckyCache getLuckyCache() {
		return new LuckyCache();
	}
	
	public boolean containsDbname(String dbname) {
		return cacheMap.containsKey(dbname);
	}
	
	public Map<String,List<?>> getMapByDbName(String dbname){
		if(containsDbname(dbname))
			return cacheMap.get(dbname);
		return null;
	}
	
	/**
	 * 判断缓存中是否存在该sql
	 * @param sql
	 * @return
	 */
	public boolean contains(String dbname,String sql) {
		if(getMapByDbName(dbname)==null)
			return false;
		return getMapByDbName(dbname).containsKey(sql);
	}

	/**
	 * 添加到缓存
	 * @param key
	 * @param value
	 */
	public void add(String dbname,String key, List<?> value) {
		if (containsDbname(dbname)) {
			if(!cacheMap.get(dbname).containsKey(key)) {
				cacheMap.get(dbname).put(key, value);
			}
		}else {
			Map<String,List<?>> dbMap=new HashMap<>();
			dbMap.put(key, value);
			cacheMap.put(dbname, dbMap);
		}
	}

	/**
	 * 从缓存拿值
	 * @param key
	 * @return
	 */
	public List<?> get(String dbname,String key) {
		if (contains(dbname,key)) {
			return cacheMap.get(dbname).get(key);
		} else {
			return null;
		}
	}
	
	/**
	 * 非查询操作时删除缓存中对应的内容 
	 * @param dbname
	 */
	public void evenChange(String dbname) {
		empty(dbname);
	}
	
	/**
	 * 清空缓存
	 */
	public void empty(String dbname) {
		if(containsDbname(dbname))
			cacheMap.get(dbname).clear();
	}
}

package com.lucky.jacklamb.file.ini;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.ArrayCast;
 
public class INIConfig {
	
	private  IniFilePars iniFilePars;
	
	private static Map<String,IniFilePars> iniMap;
	
	public INIConfig(){
		if(iniMap==null) {
			iniMap=new HashMap<>();
			iniFilePars=new IniFilePars();
			iniMap.put("appconfig.ini", iniFilePars);
		}else {
			iniFilePars=iniMap.get("appconfig.ini");
		}
	}
	
	public INIConfig(String path) {
		if(path.startsWith("/")) {
			path=path.substring(1);
		}
		if(iniMap==null) {
			iniMap=new HashMap<>();
			iniFilePars=new IniFilePars(path);
			iniMap.put(path, iniFilePars);
		}else if(iniMap.containsKey(path)){
			iniFilePars=iniMap.get(path);
		}else {
			iniFilePars=new IniFilePars(path);
			iniMap.put(path, iniFilePars);
		}
	}
	
	/**
	 * 得到配置文件中所有的配置信息
	 * @return
	 */
	public Map<String,Map<String,String>> getIniMap(){
		return iniFilePars.getIniMap();
	}
	/**
	 * 得到App节下的所有key-value值组成的Map
	 * @return
	 */
	public  Map<String,String> getAppParamMap() {
		return iniFilePars.getSectionMap("App");
	}
	
	/**
	 *  得到App节下的某一个key对应的value值
	 * @param key key名
	 * @return
	 */
	public  String getAppParam(String key) {
		return getAppParamMap().get(key);
	}
	
	/**
	 * 得到App节下的某一个key对应的value值(指定类型)
	 * @param key key名
	 * @param clazz 类型Class
	 * @return
	 */
	public  <T> T getAppParam(String key,Class<T> clazz) {
		return getValue("App",key,clazz);
	}
	
	/**
	 * 得到App节下的某一个key对应的value值(String[]类型)
	 * @param key key名
	 * @param separator 分隔符
	 * @return
	 */
	public  String[] getAppStringArray(String key,String separator) {
		return getArray("App",key,separator);
	}
	
	
	/**
	 * 得到App节下的某一个key对应的value值(String[]类型)
	 * @param key key名
	 * @param separator 分隔符
	 * @return
	 */
	public  String[] getAppStringArray(String key) {
		return getAppStringArray(key,",");
	}
	
	public  <T> T[] getAppArray(String key,Class<T> clazz) {
		return getArray("App",key,clazz);
	}
	
	public  <T> T[] getAppArray(String key,Class<T> clazz,String separator) {
		return getArray("App",key,clazz,separator);
	}
	
	/**
	 * 得到某个人指定节下的所有的key-value值组成的Map
	 * @param section 节的名称
	 * @return
	 */
	public  Map<String,String> getSectionMap(String section) {
		return iniFilePars.getSectionMap(section);
	}
	
	/**
	 * 得到某个指定节下指定key的value值
	 * @param section
	 * @param key
	 * @return
	 */
	public  String getValue(String section,String key) {
		return iniFilePars.getSectionMap(section).get(key);
	}
	
	/**
	 * 得到一个具体类型的Value
	 * @param section 节名称
	 * @param key key名
	 * @param clazz 指定类型的Class
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public  <T> T getValue(String section,String key,Class<T> clazz) {
		return (T) JavaConversion.strToBasic(getValue(section,key), clazz);
	}
	
	/**
	 * 得到一个String[]形式的value
	 * @param section 节名称
	 * @param key key名称
	 * @param separator 分隔符
	 * @return
	 */
	public  String[] getArray(String section,String key,String separator) {
		if(iniFilePars.isHasKey(section, key)) {
			return iniFilePars.getValue(section, key).split(separator);
		}
		return null;
	}
	
	/**
	 * 得到一个String[]形式的value
	 * @param section 节名称
	 * @param key key名称
	 * @return
	 */
	public  String[] getArray(String section,String key) {
		return getArray(section,key,",");
	}
	
	/**
	 * 得到一个指定类型数组形式的value
	 * @param section 节名称
	 * @param key key名称
	 * @param changTypeClass 数组类型Class
	 * @param separator 分隔符
	 * @return
	 */
	public  <T> T[] getArray(String section,String key,Class<T> changTypeClass,String separator) {
		return ArrayCast.strArrayChange(getArray(section,key,separator), changTypeClass);
	}
	
	/**
	 * 得到一个指定类型集合形式的value
	 * @param section 节名
	 * @param key key名
	 * @param collectionClass 集合类型
	 * @param genericClass 泛型类型
	 * @param separator 分隔符
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public  <T extends Collection<M>,M> T getCollection(String section,String key,Class<T> collectionClass,Class<M> genericClass,String separator) {
		M[] objArr=getArray(section,key,genericClass,separator);
		if(collectionClass.isAssignableFrom(List.class)) {
			return (T) Arrays.asList(objArr);
		}else if(collectionClass.isAssignableFrom(Set.class)){
			Set<M> set=new HashSet<>(Arrays.asList(objArr));
			return (T) set;
		}else {
			return null;
		}
	}
	
	/**
	 * 得到App节下指定类型集合形式的value
	 * @param key key名
	 * @param collectionClass 集合类型
	 * @param genericClass 泛型类型
	 * @param separator 分隔符
	 * @return
	 */
	public  <T extends Collection<M>,M> T getAppCollection(String key,Class<T> collectionClass,Class<M> genericClass,String separator) {
		return getCollection("App",key,collectionClass,genericClass,separator);
	}
	
	/**
	 * 得到App节下指定类型集合形式的value
	 * @param key key名
	 * @param collectionClass 集合类型
	 * @param genericClass 泛型类型
	 * @return
	 */
	public  <T extends Collection<M>,M> T getAppCollection(String key,Class<T> collectionClass,Class<M> genericClass) {
		return getCollection("App",key,collectionClass,genericClass);
	}
	
	/**
	 * 得到App节下String类型集合形式的value
	 * @param key key名
	 * @param collectionClass 集合类型
	 * @return
	 */
	public  <T extends Collection<String>> T getAppCollection(String key,Class<T> collectionClass) {
		return getCollection("App",key,collectionClass,String.class);
	}
	
	/**
	 * 得到指定节下指定类型集合形式的value
	 * @param section 节名
	 * @param key key名
	 * @param collectionClass 集合类型
	 * @param genericClass 泛型类型
	 * @return
	 */
	public  <T extends Collection<M>,M> T getCollection(String section,String key,Class<T> collectionClass,Class<M> genericClass) {
		return getCollection(section,key,collectionClass,genericClass,",");
	}
	
	/**
	 * 得到指定节下String类型集合形式的value
	 * @param section 节名
	 * @param key key名
	 * @param collectionClass 集合类型
	 * @return
	 */
	public  <T extends Collection<String>> T getCollection(String section,String key,Class<T> collectionClass) {
		return getCollection(section,key,collectionClass,String.class,",");
	}
	
	/**
	 * 得到一个指定类型数组形式的value
	 * @param section 节名称
	 * @param key key名称
	 * @param changTypeClass 数组类型Class
	 * @return
	 */
	public  <T> T[] getArray(String section,String key,Class<T> changTypeClass) {
		return getArray(section,key,changTypeClass,",");
	}
	
	/**
	 * 将某个节下的配置信息封装为一个特定的对象
	 * @param clazz 对象的Class
	 * @return
	 */
	public  <T> T getObject(Class<T> clzz) {
		return getObject(clzz,clzz.getSimpleName());
	}
	
	
	/**
	 * 将某个节下的配置信息封装为一个特定的对象
	 * @param clazz 对象的Class
	 * @param section 节名称
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public  <T> T getObject(Class<T> clazz,String section) {
		if(!iniFilePars.isHasSection(section))
			return null;
		Object object=null;
		try {
			Map<String, String> sectionMap = iniFilePars.getSectionMap(section);
			Constructor<T> constructor = clazz.getConstructor();
			constructor.setAccessible(true);
			object=constructor.newInstance(); 
			Field[] fields=clazz.getDeclaredFields();
			String fieldName;
			for(Field field:fields) {
				fieldName=field.getName();
				if(sectionMap.containsKey(fieldName)) {
					field.setAccessible(true);
					String sectionValue = sectionMap.get(fieldName);
					if(field.getType().isArray()) {
						field.set(object, getArray(section,sectionValue,field.getType()));
					}else if(field.getType().isAssignableFrom(List.class)) {
						field.set(object, getCollection(section,fieldName,List.class,ArrayCast.getClassFieldGenericType(field)[0]));
					}else if(field.getType().isAssignableFrom(Set.class)) {
						field.set(object, getCollection(section,fieldName,Set.class,ArrayCast.getClassFieldGenericType(field)[0]));
					}else if(field.getType().getClassLoader()==null) {
						field.set(object, JavaConversion.strToBasic(sectionValue, field.getType()));
					}else if(sectionValue.startsWith("@S:")) {
						field.set(object,getObject(field.getType(),sectionValue.substring(3)));
					}else {
						field.set(object, Class.forName(sectionValue).newInstance());
					}
				}
			}
			return (T) object;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 打印配置文件中的所有配置信息
	 */
	public void printIniMap() {
		Map<String, Map<String, String>> iniMap = getIniMap();
		for(Entry<String,Map<String,String>> entry: iniMap.entrySet()) {
			System.out.println("["+entry.getKey()+"]");
			for(Entry<String,String> kv:entry.getValue().entrySet()) {
				System.out.println("\t"+kv.getKey()+"="+kv.getValue());
			}
		}
	}
	

}

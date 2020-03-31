package com.lucky.jacklamb.rest;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.lucky.jacklamb.exception.JsonFormatException;
import com.lucky.jacklamb.ioc.config.ScanConfig;
import com.lucky.jacklamb.utils.LuckyUtils;

/**
 * pojo对象转json字符串
 * 
 * @author fk7075
 *
 */
public class LSON {
	
	private static Logger log=Logger.getLogger(LSON.class);

	private String jsonStr;
	
	/**
	 * 传入一个对象，返回该对象的Json字符串
	 * @param jsonObject
	 * @return
	 */
	public String toJson(Object jsonObject) {
		toJsonString(jsonObject);
		return jsonStr;
	}
	

	/**
	 * 传入一个Json字符串,返回一个指定类型的对象
	 * @param objectClass 返回对象的类型
	 * @param jsonStr Json字符串
	 * @return
	 */
	public <T> T toObject(Class<T> objectClass,String jsonStr) {
		if(!check(jsonStr)) {
			log.error("错误的Json字符串"+jsonStr,new JsonFormatException("错误的Json字符串"+jsonStr));
			throw new JsonFormatException("错误的Json字符串"+jsonStr);
		}
		return null;
	}
	
	public boolean check(String jsonStr) {
		return false;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void toJsonString(Object jsonObject) {
		if(jsonObject==null)
			jsonStr="null";
		else {
			Class<?> clzz = jsonObject.getClass();
			if (Collection.class.isAssignableFrom(clzz)) {
				try {
					jsonStr = collectionToJsonStr((Collection<?>) jsonObject);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}else if(clzz.isEnum()) {
				jsonStr="\""+jsonObject.toString()+"\"";
			} else if (clzz.isArray()) {
				try {
					jsonStr = arrayToJsonStr((Object[])jsonObject);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}else if(Map.class.isAssignableFrom(clzz)){
				jsonStr=mapToJsonStr((Map<Object,Object>)jsonObject);
			} else if (clzz.getClassLoader() != null) {
				try {
					jsonStr = objectToJsonStr(jsonObject);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}else {
				if(String.class.isAssignableFrom(clzz)||Character.class.isAssignableFrom(clzz)
				   ||java.sql.Date.class.isAssignableFrom(clzz)||Time.class.isAssignableFrom(clzz)
				   ||Timestamp.class.isAssignableFrom(clzz))
					jsonStr="\""+jsonObject.toString()+"\"";
				else if(java.util.Date.class.isAssignableFrom(clzz))
					jsonStr="\""+LuckyUtils.getDate((Date)jsonObject)+"\"";
				else
					jsonStr=jsonObject.toString();
			}
		}

	}
	
	private String mapToJsonStr(Map<Object,Object> map) {
		LSON lson=new LSON();
		if(map==null||map.isEmpty()) {
			return "{}";
		}
		StringBuilder str=new StringBuilder("{");
		for(Entry<Object,Object> entry:map.entrySet()) {
			str.append(lson.toJson(entry.getKey())).append(":").append(lson.toJson(entry.getValue())).append(",");
		}
		if(str.toString().endsWith(","))
			return str.substring(0, str.length()-1)+"}";
		return "{}";
	}

	private String arrayToJsonStr(Object[] objects) throws IllegalArgumentException, IllegalAccessException {
		if (objects == null || objects.length == 0) {
			return "[]";
		}
		StringBuilder arrayJsonStr = new StringBuilder("[");
		List<String> field_json_copy = new ArrayList<>();
		List<String> field_json = new ArrayList<>();
		for (Object objStr : objects) {
			field_json_copy.add(objectToJsonStr(objStr));
		}
		field_json_copy.stream().filter(a->a!=null).forEach(field_json::add);
		for (int i = 0; i < field_json.size(); i++) {
			arrayJsonStr.append(field_json.get(i)).append(",");
		}
		if(arrayJsonStr.toString().endsWith(","))
			return arrayJsonStr.substring(0, arrayJsonStr.length()-1)+"]";
		return "[]";
	}

	/**
	 * 将List集合形式的Pojo转化为Json格式
	 * @param list pojo格式的数据
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private <T> String collectionToJsonStr(Collection<T> list) throws IllegalArgumentException, IllegalAccessException {
		if (list.isEmpty() || list == null) {
			return "[]";
		}
		StringBuilder listJsonStr =new StringBuilder("[");
		List<String> field_json_copy = new ArrayList<>();
		List<String> field_json = new ArrayList<>();
		for (T objStr : list) {
			field_json_copy.add(objectToJsonStr(objStr));
		}
		field_json_copy.stream().filter(a->a!=null).forEach(field_json::add);
		for (int i = 0; i < field_json.size(); i++) {
			listJsonStr.append(field_json.get(i)).append(",");
		}
		if(listJsonStr.toString().endsWith(","))
			return listJsonStr.substring(0, listJsonStr.length()-1)+"]";
		return "[]";
	}

	/**
	 * 将pojo对象转化为JSON格式的数据
	 * @param object  pojo对象
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private String objectToJsonStr(Object object) throws IllegalArgumentException, IllegalAccessException {
		if (object == null) {
			return "{}";
		}
		if (object.getClass().getClassLoader() != null) {
			StringBuilder objJsonStr =new StringBuilder("{");
			List<String> field_json_copy = new ArrayList<>();
			List<String> field_json = new ArrayList<>();
			Field[] fields = object.getClass().getDeclaredFields();
			for (Field field : fields) {
				if(!field.getType().getName().equals(object.getClass().getName()))
					field_json_copy.add(fieldToJsonStr(object, field));
			}
			field_json_copy.stream().filter(a->a!=null).forEach(field_json::add);
			for (int i = 0; i < field_json.size(); i++) {
				objJsonStr.append(field_json.get(i)).append(",");
			}
			if(objJsonStr.toString().endsWith(","))
				return objJsonStr.substring(0, objJsonStr.length()-1)+"}";
			return "{}";
		} else {
			if(object instanceof String||object instanceof Character
					   ||object instanceof java.sql.Date||object instanceof Time
					   ||object instanceof Timestamp)
						return "\""+object.toString()+"\"";
					else if(object instanceof java.util.Date)
						return "\""+LuckyUtils.getDate((Date)object)+"\"";
					else
						return object.toString();
		}

	}

	/**
	 * 将不为null的属性转变为json格式
	 * @param field_Obj  目标对象
	 * @param field 目标属性
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private String fieldToJsonStr(Object field_Obj, Field field)
			throws IllegalArgumentException, IllegalAccessException {
		LSON lson=new LSON();
		StringBuilder fieldJsonStr = new StringBuilder();
		field.setAccessible(true);
		Object obj = field.get(field_Obj);
		StringBuilder fieldValueJson;
		StringBuilder fieldNameJson;
		if (obj != null) {
			fieldValueJson = new StringBuilder(lson.toJson(obj));
			fieldNameJson=new StringBuilder(lson.toJson(AttrUtil.getField(field)));
			if(!"{}".equals(fieldValueJson.toString())&&!"[]".equals(fieldValueJson.toString())) {
				fieldJsonStr.append(fieldNameJson).append(":").append(fieldValueJson);
				return fieldJsonStr.toString();
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		TT object=new TT();
		object.setStr("String属性");
		List<Double> list=new ArrayList<>();
		list.add(12.5);list.add(55.7);list.add(99.999);
		object.setList(list);
		Map<String,Integer> map=new HashMap<>();
		map.put("key1", 111);
		map.put("key2", 222);
		object.setMap(map);
		BB bb=new BB();
		bb.setBname("BNAME");
		String[] arr= {"OK","YES","HELLO"};
		bb.setArray(arr);
		object.setBb(bb);
		List<BB> list_bb=new ArrayList<>();
		list_bb.add(bb);list_bb.add(new BB("BB2"));
		object.setList_BB(list_bb);
		Map<String,BB> map_bb=new HashMap<>();
		map_bb.put("map1", bb);
		map_bb.put("map2", bb);
		map_bb.put("map3", new BB("MAPBB"));
		object.setMap_BB(map_bb);
		LSON l=new LSON();
		System.out.println(l.toJson(object));
		System.out.println(LuckyUtils.getDate(new Date()));
		ScanConfig s=ScanConfig.defaultScanConfig();
		System.out.println(l.toJson(s));
	}
	
	public String formatJson(Object jsonObject) {
		return FormatUtil.formatJson(toJson(jsonObject));
	}
	
}


class  FormatUtil {

    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr))
            return "";
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
            case '"':
                                if (last != '\\'){
                    isInQuotationMarks = !isInQuotationMarks;
                                }
                sb.append(current);
                break;
            case '{':
            case '[':
                sb.append(current);
                if (!isInQuotationMarks) {
                    sb.append('\n');
                    indent++;
                    addIndentBlank(sb, indent);
                }
                break;
            case '}':
            case ']':
                if (!isInQuotationMarks) {
                    sb.append('\n');
                    indent--;
                    addIndentBlank(sb, indent);
                }
                sb.append(current);
                break;
            case ',':
                sb.append(current);
                if (last != '\\' && !isInQuotationMarks) {
                    sb.append('\n');
                    addIndentBlank(sb, indent);
                }
                break;
            default:
                sb.append(current);
            }
        }

        return sb.toString();
    }

    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }
}

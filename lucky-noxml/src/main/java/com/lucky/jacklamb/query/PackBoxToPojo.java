package com.lucky.jacklamb.query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PackBoxToPojo {
	
	public static Object[] transFormPojos(Object packbox) {
		List<Object> list=new ArrayList<>();
		Class<?> clzz=packbox.getClass();
		Field[] fields=clzz.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			Object fiobj;
			try {
				fiobj = field.get(packbox);
				if(fiobj!=null)
					list.add(fiobj);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list.toArray();
	}

}

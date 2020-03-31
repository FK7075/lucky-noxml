package com.lucky.jacklamb.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;

public class ArrayCast {
	
	/**
	 * 将String[]转为其它类型的数组
	 * @param strArr
	 * @param changTypeClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] strArrayChange(String[] strArr, Class<T> changTypeClass) {
		return (T[]) JavaConversion.strArrToBasicArr(strArr, changTypeClass);
	}

	/**
	 * 返回集合属性的泛型,如果不是java自带的类型，会在泛型类型后加上$ref<br>
	 * List[String]  ->String<br>
	 * Map[String,Ingeger]  ->[String,Integer]<br>
	 * Map[String,MyPojo]   ->[String,MyPojo$ref]<br>
	 *
	 * @param field
	 * @return
	 */
	public static String[] getFieldGenericType(Field field) {
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) genericType;
			Type[] actualTypeArguments = pt.getActualTypeArguments();
			String[] gener = new String[actualTypeArguments.length];
			for (int i = 0; i < gener.length; i++) {
				Class<?> gc=(Class<?>)actualTypeArguments[i];
				if(gc.getClassLoader()!=null)
					gener[i]=gc.getSimpleName()+"$ref";
				else
					gener[i]=gc.getSimpleName();
			}
			return gener;
		}
		return null;
	}
	
	public static Class<?>[] getClassFieldGenericType(Field field){
		Type genericType = field.getGenericType();
		Class<?>[] GenericType = null;
		if (genericType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) genericType;
			Type[] types = pt.getActualTypeArguments();
			GenericType=new Class<?>[types.length];
			for(int i=0;i<types.length;i++) {
				GenericType[i]=(Class<?>) types[i];
			}
		}
		return GenericType;
	}
}

package com.lucky.jacklamb.sqlcore.util;

import com.lucky.jacklamb.annotation.orm.NoColumn;
import com.lucky.jacklamb.utils.reflect.ClassUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FieldAndValue {

	private String idField;

	private Object idValue;

	private Map<String, Object> fieldNameAndValue;
	
	private Object pojo;

	public String getIdField() {
		return idField;
	}

	public void setIdField(String idField) {
		this.idField = idField;
	}

	public Object getIdValue() {
		return idValue;
	}

	public void setIdValue(Object idValue) {
		this.idValue = idValue;
	}

	public Map<String, Object> getFieldNameAndValue() {
		return fieldNameAndValue;
	}

	public void setFieldNameAndValue(Map<String, Object> fieldNameAndValue) {
		this.fieldNameAndValue = fieldNameAndValue;
	}

	public FieldAndValue(Object pojo) {
		try {
			this.pojo=pojo;
			setIDField(pojo);
			setNotNullFields(pojo);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("不合法参数异常！",e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("非法访问异常",e);
		}
		
	}
	
	public boolean containsField(String field) {
		return fieldNameAndValue.containsKey(field);
	}
	
	public boolean containsFields(String...fields) {
		for(String str:fields) {
			if(!containsField(str))
				throw new RuntimeException("传入的"+pojo.getClass().getName()+"对象"+pojo.toString()+"的非空属性映射中不包含\""+str+"\",无法完成更新操作");
		}
		return true;
	}

	public void setIDField(Object pojo) throws IllegalArgumentException, IllegalAccessException {
		Class<?> pojoClass = pojo.getClass();
		Field id = PojoManage.getIdField(pojoClass);
		id.setAccessible(true);
		this.idField = PojoManage.getTableField(id);
		this.idValue = id.get(pojo);
	}

	public void setNotNullFields(Object pojo) throws IllegalArgumentException, IllegalAccessException {
		fieldNameAndValue = new HashMap<>();
		Class<?> pojoClass = pojo.getClass();
		Field[] fields = ClassUtils.getAllFields(pojoClass);
		Object fieldValue;
		for (Field field : fields) {
			if(field.isAnnotationPresent(NoColumn.class))
				continue;
			field.setAccessible(true);
			fieldValue = field.get(pojo);
			if (fieldValue != null)
				fieldNameAndValue.put(PojoManage.getTableField(field), fieldValue);
		}
	}
}

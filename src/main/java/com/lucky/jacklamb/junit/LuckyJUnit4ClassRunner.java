package com.lucky.jacklamb.junit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.lucky.jacklamb.annotation.ioc.Autowired;
import com.lucky.jacklamb.annotation.ioc.Value;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.ArrayCast;

public class LuckyJUnit4ClassRunner extends BlockJUnit4ClassRunner{
	
	
	public LuckyJUnit4ClassRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected Object createTest() throws Exception {
		Object createTest = super.createTest();
		ApplicationBeans applicationBeans=ApplicationBeans.createApplicationBeans();
		return createTestObject(applicationBeans,createTest);
	}
	
	private Object createTestObject(ApplicationBeans applicationBeans,Object testObject) throws InstantiationException, IllegalAccessException {
		Field[] allFields = testObject.getClass().getDeclaredFields();
		Autowired auto;
		Value value;
		Class<?> fieldClass;
		for(int i=0;i<allFields.length;i++) {
			fieldClass=allFields[i].getType();
			if(allFields[i].isAnnotationPresent(Autowired.class)) {
				auto=allFields[i].getAnnotation(Autowired.class);
				allFields[i].setAccessible(true);
				String id=auto.value();
				if("".equals(id)) {
					allFields[i].set(testObject, applicationBeans.getBean(fieldClass));
				}else {
					allFields[i].set(testObject,applicationBeans.getBean(id));
				}
			}else if(allFields[i].isAnnotationPresent(Value.class)) {
				value=allFields[i].getAnnotation(Value.class);
				String[] val = value.value();
				allFields[i].setAccessible(true);
				if(val.length==0) {//类型扫描
					allFields[i].set(testObject, applicationBeans.getBean(fieldClass));
				}else {
					if(fieldClass.isArray()) {//基本类型的数组类型
						allFields[i].set(testObject,ArrayCast.strArrayChange(val, fieldClass));
					}else if(List.class.isAssignableFrom(fieldClass)) {//List类型
						List<Object> list=new ArrayList<>();
						String fx=ArrayCast.getFieldGenericType(allFields[i])[0];
						if(fx.endsWith("$ref")) {
							for(String z:val) {
								list.add(applicationBeans.getBean(z));
							}
						}else {
							for(String z:val) {
								list.add(JavaConversion.strToBasic(z, fx));
							}
						}
						allFields[i].set(testObject, list);
					}else if(Set.class.isAssignableFrom(fieldClass)) {//Set类型
						Set<Object> set=new HashSet<>();
						String fx=ArrayCast.getFieldGenericType(allFields[i])[0];
						if(fx.endsWith("$ref")) {
							for(String z:val) {
								set.add(applicationBeans.getBean(z));
							}
						}else {
							for(String z:val) {
								set.add(JavaConversion.strToBasic(z, fx));
							}
						}
						allFields[i].set(testObject, set);
					}else if(Map.class.isAssignableFrom(fieldClass)) {//Map类型
						Map<Object,Object> map=new HashMap<>();
						String[] fx=ArrayCast.getFieldGenericType(allFields[i]);
						boolean one=fx[0].endsWith("$ref");
						boolean two=fx[1].endsWith("$ref");
						if(one&&two) {//K-V都不是基本类型
							for(String z:val) {
								String[] kv=z.split(":");
								map.put(applicationBeans.getBean(kv[0]), applicationBeans.getBean(kv[1]));
							}
						}else if(one&&!two) {//V是基本类型
							for(String z:val) {
								String[] kv=z.split(":");
								map.put(applicationBeans.getBean(kv[0]), JavaConversion.strToBasic(kv[1], fx[1]));
							}
						}else if(!one&&two) {//K是基本类型
							for(String z:val) {
								String[] kv=z.split(":");
								map.put(JavaConversion.strToBasic(kv[0], fx[0]),applicationBeans.getBean(kv[1]));
							}
						}else {//K-V都是基本类型
							for(String z:val) {
								String[] kv=z.split(":");
								map.put(JavaConversion.strToBasic(kv[0], fx[0]), JavaConversion.strToBasic(kv[1], fx[1]));
							}
						}
						allFields[i].set(testObject, map);
					}else {//自定义的基本类型
						allFields[i].set(testObject, JavaConversion.strToBasic(val[0], fieldClass.getSimpleName()));
					}
				}
			}
		}
		return testObject;
	}

}

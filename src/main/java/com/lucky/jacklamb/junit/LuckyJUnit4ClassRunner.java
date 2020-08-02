package com.lucky.jacklamb.junit;

import com.lucky.jacklamb.annotation.ioc.Autowired;
import com.lucky.jacklamb.annotation.ioc.Value;
import com.lucky.jacklamb.aop.core.PointRunFactory;
import com.lucky.jacklamb.aop.core.AopProxyFactory;
import com.lucky.jacklamb.expression.$Expression;
import com.lucky.jacklamb.file.ini.INIConfig;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Field;
import java.util.*;

public class LuckyJUnit4ClassRunner extends BlockJUnit4ClassRunner{
	
	
	public LuckyJUnit4ClassRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected Object createTest() throws Exception {
		Object createTest = super.createTest();
		Class<?> aClass = createTest.getClass();

		//当前测试类如果存在事务注解@Transaction则执行事务代理
		if(AopProxyFactory.isTransaction(aClass)||AopProxyFactory.isCacheable(aClass)){
			createTest= PointRunFactory.createProxyFactory().getProxy(createTest.getClass());
		}
		ApplicationBeans applicationBeans=ApplicationBeans.createApplicationBeans();
		return createTestObject(applicationBeans,createTest);
	}
	
	private Object createTestObject(ApplicationBeans applicationBeans,Object testObject) throws InstantiationException, IllegalAccessException {
		Field[] allFields = ClassUtils.getAllFields(testObject.getClass());
		Autowired auto;
		Value value;
		Class<?> fieldClass;
		for(int i=0;i<allFields.length;i++) {
			allFields[i].setAccessible(true);
			fieldClass=allFields[i].getType();
			if(allFields[i].isAnnotationPresent(Autowired.class)) {
				auto=allFields[i].getAnnotation(Autowired.class);
				String auval = auto.value();
				if("".equals(auval)) {
					allFields[i].set(testObject, applicationBeans.getBean(fieldClass));//类型扫描
				}else if(auval.contains("${")&&auval.contains("}")){
					String key=auval.substring(2,auval.length()-1);
					if(key.startsWith("S:")){
						allFields[i].set(testObject,new INIConfig().getObject(fieldClass,key.substring(2)));
					}else{
						allFields[i].set(testObject, $Expression.translation(auval,fieldClass));
					}
				}else{
					allFields[i].set(testObject, applicationBeans.getBean(auto.value()));//id注入
				}
			}else if(allFields[i].isAnnotationPresent(Value.class)) {
				value=allFields[i].getAnnotation(Value.class);
				String[] val = value.value();
				if(val.length==0) {//类型扫描
					allFields[i].set(testObject, applicationBeans.getBean(fieldClass));
				}else {
					if(fieldClass.isArray()) {//基本类型的数组类型
						allFields[i].set(testObject,JavaConversion.strArrToBasicArr(val, fieldClass));
					}else if(List.class.isAssignableFrom(fieldClass)) {//List类型
						List<Object> list=new ArrayList<>();
						String fx= FieldUtils.getStrGenericType(allFields[i])[0];
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
						String fx=FieldUtils.getStrGenericType(allFields[i])[0];
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
						String[] fx=FieldUtils.getStrGenericType(allFields[i]);
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

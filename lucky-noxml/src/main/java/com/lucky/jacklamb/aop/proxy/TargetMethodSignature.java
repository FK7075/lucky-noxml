package com.lucky.jacklamb.aop.proxy;

import com.lucky.jacklamb.cglib.ASMUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 当前执行方法的所有相关项
 * @author fk-7075
 *
 */
public class TargetMethodSignature {
	
	/**
	 * 当前真实类的代理对象
	 */
	private Object aspectObject;
	
	/**
	 * 当前真实类对应的Class
	 */
	private Class<?> targetClass;
	
	/**
	 * 当前真实方法的Method
	 */
	private Method currMethod;
	
	/**
	 * 当前真实方法的参数列表
	 */
	private Object[] params;
	
	/**
	 * 由当前真实方法的参数列表值和其对应的位置组成的Map<index,value>
	 */
	private Map<Integer,Object> indexMap;
	
	/**
	 * 由当前真实方法的参数列表值和其对应参数名组成的Map<paramName,value>
	 */
	private Map<String,Object> nameMap;
	
	/**
	 * 参数列表的 Parameter[]
	 */
	private Parameter[] parameters;
	
	/**
	 * 当前真实方法的参数名列表
	 */
	private String[] paramNames;
	
	
	
	/**
	 * 得到当前真实类对应的Class
	 * @return
	 */
	public Class<?> getTargetClass() {
		return targetClass;
	}

	/**
	 * 得到当前真实类的代理对象
	 * @return
	 */
	public Object getAspectObject() {
		return aspectObject;
	}

	/**
	 *得到当前真实方法的Method
	 * @return
	 */
	public Method getCurrMethod() {
		return currMethod;
	}

	/**
	 * 得到当前真实方法的参数列表
	 * @return
	 */
	public Object[] getParams() {
		return params;
	}

	/**
	 * 得到由当前真实方法的参数列表值和其对应的位置组成的Map<index,value>
	 * @return
	 */
	public Map<Integer, Object> getIndexMap() {
		return indexMap;
	}

	/**
	 * 得到由当前真实方法的参数列表值和其对应参数名组成的Map<paramName,value>
	 * JDK8及以上版本可以使用，且必须开启-parameters才能得到正确的参数名，否则将得到（arg0，arg1...）
	 * @return
	 */
	public Map<String, Object> getNameMap() {
		return nameMap;
	}

	/**
	 * 得到参数列表的 Parameter[]
	 * @return
	 */
	public Parameter[] getParameters() {
		return parameters;
	}

	public TargetMethodSignature(Object aspectObject,Method currMethod,Object[] params) {
		this.aspectObject=aspectObject;
		this.targetClass=aspectObject.getClass().getSuperclass();
		this.currMethod=currMethod;
		this.params=params;
		this.paramNames= ASMUtil.getMethodParamNames(currMethod);
		indexMap=new HashMap<>();
		for(int i=0;i<params.length;i++) {
			indexMap.put(i+1, params[i]);
		}
		parameters = currMethod.getParameters();
		nameMap=new HashMap<>();
		for(int i=0;i<parameters.length;i++) {
			nameMap.put(paramNames[i], params[i]);
		}
	}
	

	public boolean containsIndex(int index) {
		return indexMap.containsKey(index);
	}
	
	/**
	 * 得到参数列表中index位置的参数
	 * @param index
	 * @return
	 */
	public Object getParamByIndex(int index){
		if(!containsIndex(index))
			return null;
		return indexMap.get(index);
	}
	
	public boolean containsParamName(String paramName) {
		return nameMap.containsKey(paramName);
	}
	
	public Object getParamByName(String paramName) {
		if(!containsParamName(paramName))
			return null;
		return nameMap.get(paramName);
	}
	


}

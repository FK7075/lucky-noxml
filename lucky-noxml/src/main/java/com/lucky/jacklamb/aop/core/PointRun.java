package com.lucky.jacklamb.aop.core;

import com.lucky.jacklamb.annotation.aop.After;
import com.lucky.jacklamb.annotation.aop.AopParam;
import com.lucky.jacklamb.annotation.aop.Around;
import com.lucky.jacklamb.annotation.aop.Before;
import com.lucky.jacklamb.aop.proxy.TargetMethodSignature;
import com.lucky.jacklamb.enums.Location;
import com.lucky.jacklamb.exception.IllegaAopparametersException;
import com.lucky.jacklamb.exception.PointRunException;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.IOCContainers;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;
import com.lucky.jacklamb.utils.reflect.MethodUtils;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;

public class PointRun {
	
	private AopPoint point;
	
	private String pointCutClass;
	
	private String pointCutMethod;
	
	public Method method;
	
	/**
	 * 使用一个Point对象构造PointRun
	 * @param point
	 */
	public PointRun(AopPoint point) {
		Method proceedMethod=MethodUtils.getDeclaredMethod(point.getClass(),"proceed",AopChain.class);
		Around exp = proceedMethod.getAnnotation(Around.class);
		this.point = point;
		this.point.setPriority(exp.priority());
		this.pointCutClass = exp.pointCutClass();
		this.pointCutMethod = exp.pointCutMethod();
	}
	
	/**
	 * 使用Point类型对象的Class来构造PointRun
	 * @param pointClass
	 */
	public PointRun(Class<?> pointClass) {
		Method proceedMethod=MethodUtils.getDeclaredMethod(pointClass,"proceed", AopChain.class);
		Around exp = proceedMethod.getAnnotation(Around.class);
		this.point = (AopPoint) ClassUtils.newObject(pointClass);
		this.point.setPriority(exp.priority());
		this.pointCutClass = exp.pointCutClass();
		this.pointCutMethod = exp.pointCutMethod();

	}

	/**
	 * 使用增强类的实例对象+增强方法Method来构造PointRun
	 * @param expand 增强类实例
	 * @param method 增强(方法)
	 */
	public PointRun(Object expand, Method method) {
		this.method=method;
		if(method.isAnnotationPresent(Before.class)) {
			Before before=method.getAnnotation(Before.class);
			this.point=conversion(expand,method,Location.BEFORE);
			this.point.setPriority(before.priority());
			this.pointCutClass = before.pointCutClass();
			this.pointCutMethod = before.pointCutMethod();
		}else if(method.isAnnotationPresent(After.class)) {
			After after=method.getAnnotation(After.class);
			this.point=conversion(expand,method,Location.AFTER);
			this.point.setPriority(after.priority());
			this.pointCutClass = after.pointCutClass();
			this.pointCutMethod = after.pointCutMethod();
		}else if(method.isAnnotationPresent(Around.class)){
			Around around=method.getAnnotation(Around.class);
			this.point=conversion(expand,method,Location.AROUND);
			this.point.setPriority(around.priority());
			this.pointCutClass = around.pointCutClass();
			this.pointCutMethod = around.pointCutMethod();
		}
	}

	public String getMateClass() {
		return pointCutClass;
	}

	public void setMateClass(String mateClass) {
		this.pointCutClass = mateClass;
	}

	public String getMateMethod() {
		return pointCutMethod;
	}

	public void setMateMethod(String mateMethod) {
		this.pointCutMethod = mateMethod;
	}

	public AopPoint getPoint() {
		return point;
	}

	public void setPoint(AopPoint point) {
		this.point = point;
	}
	
	/**
	 * 检验当前方法是否符合该Point的执行标准
	 * @param method
	 * @return
	 */
	public boolean standard(Method method) {
		try {
			return standardStart(method);
		}catch(StringIndexOutOfBoundsException e) {
			throw new RuntimeException("切入点配置错误，错误位置："+method+" ->@Before/@After/@Around(pointCutClass=>err)", e);
		}
	}
	
	/**
	 * 遍历mateMethod，逐个验证
	 * @param method 当前Method
	 * @return
	 */
	private boolean standardStart(Method method) {
		String methodName=method.getName();
		Parameter[] parameters = method.getParameters();
		String[] pointcutStr=pointCutMethod.split(",");
		if(Arrays.asList(pointcutStr).contains("public")) {
			//是否配置了public,如果配置了public，则所有非public都将不会执行该增强
			if(!Modifier.isPublic(method.getModifiers())) {
				return false;
			}
		}
		for(String str:pointcutStr) {
			if("*".equals(str)) {
				return true;
			}else if(str.contains("(")&&str.endsWith(")")){
				if(standardMethod(methodName,parameters,str)) {
					return true;
				}
			}else {
				if(standardName(methodName,str)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 方法名验证
	 * @param mothodName 当前方法的方法名
	 * @param pointcut 配置中配置的标准方法名
	 * @return
	 */
	private boolean standardName(String mothodName,String pointcut) {
		if(pointcut.startsWith("!")) {
			return !(mothodName.equals(pointcut.substring(1)));
		}else if(pointcut.startsWith("*")) {
			return mothodName.endsWith(pointcut.substring(1));
		}else if(pointcut.endsWith("*")) {
			return mothodName.startsWith(pointcut.substring(0, pointcut.length()-1));
		}else {
			return mothodName.equals(pointcut);
		}
	}
	
	/**
	 * 方法名+方法参数验证
	 * @param mothodName 当前方法的方法名
	 * @param parameters 当前方法的参数列表
	 * @param pointcut 配置中配置的标准方法名+参数
	 * @return
	 */
	private boolean standardMethod(String mothodName,Parameter[] parameters,String pointcut) {
		int indexOf = pointcut.indexOf("(");
		String methodNameStr;
		boolean pass=true;
		String[] methodParamStr=pointcut.substring(indexOf+1, pointcut.length()-1).split(" ");
		if(pointcut.startsWith("!")) {
			if(methodParamStr.length!=parameters.length) {
				return true;
			}
			methodNameStr=pointcut.substring(1, indexOf);
			for(int i=0;i<methodParamStr.length;i++) {
				if(!(methodParamStr[i].equals(parameters[i].getType().getSimpleName()))) {
					pass=false;
					break;
				}
			}
			return !(standardName(mothodName,methodNameStr)&&pass);
		}else {//没有  ！
			if(methodParamStr.length!=parameters.length) {
				return false;
			}
			methodNameStr=pointcut.substring(0, indexOf);
			for(int i=0;i<methodParamStr.length;i++) {
				if(!(methodParamStr[i].equals(parameters[i].getType().getSimpleName()))) {
					pass=false;
					break;
				}
			}
			return standardName(mothodName,methodNameStr)&&pass;
		}
	}
	
	/**
	 * 使用增强类的执行参数构造Point
	 * @param expand 增强类实例
	 * @param expandMethod 增强类方法
	 * @param location 增强位置
	 * @return
	 */
	private AopPoint conversion(Object expand, Method expandMethod, Location location) {
		AopPoint cpoint=new AopPoint() {
			
			@Override
			public Object proceed(AopChain chain) throws Throwable {
				IOCContainers.injection(expand);
				if(location==Location.BEFORE) {
					perform(expand,expandMethod,chain);
					return chain.proceed();
				}else if(location==Location.AFTER) {
					Object result=chain.proceed();
					perform(expand,expandMethod,chain);
					return result;
				}else if(location==Location.AROUND){
					return perform(expand,expandMethod,chain);
				}
				return null;
			}
			
			
			//执行增强方法
			private Object perform(Object expand, Method expandMethod,AopChain chain) {
				return MethodUtils.invoke(expand,expandMethod,setParams(expandMethod,chain));
			}
			
			//设置增强方法的执行参数-@AopParam配置
			private Object[] setParams(Method expandMethod,AopChain chain) {
				int index;
				String aopParamValue,indexStr;
				Parameter[] parameters = expandMethod.getParameters();
				Object[] expandParams=new Object[parameters.length];
				TargetMethodSignature targetMethodSignature = tlTargetMethodSignature.get();
				if(expandMethod.isAnnotationPresent(Around.class)){
					int cursor=0;
					for(int i=0;i<parameters.length;i++) {
						if(AopChain.class.isAssignableFrom(parameters[i].getType())){
							expandParams[i]=chain;
							cursor++;
						}
					}
					if(cursor==0){
						throw new AopParamsConfigurationException("环绕增强方法中必须要带有一个\"com.lucky.jacklamb.aop.core.AopChain\"类型的参数，并返回Object类型结果，该方法中没有AopChain参数，错误位置："+method);
					}
					if(cursor>1){
						throw new AopParamsConfigurationException("环绕增强方法中有且只能有一个\"com.lucky.jacklamb.aop.core.AopChain\"类型的参数，并返回Object类型结果，该方法中包含"+cursor+"个AopChain参数，错误位置："+method);
					}
				}
				for(int i=0;i<parameters.length;i++) {
					if(parameters[i].isAnnotationPresent(AopParam.class)){
						aopParamValue=parameters[i].getAnnotation(AopParam.class).value();
						if(aopParamValue.startsWith("ref:")) {//取IOC容器中的值
							if("ref:".equals(aopParamValue.trim())) {
								expandParams[i]=ApplicationBeans.createApplicationBeans().getBean(parameters[i].getType());
							} else {
								expandParams[i]=ApplicationBeans.createApplicationBeans().getBean(aopParamValue.substring(4));
							}

						}else if(aopParamValue.startsWith("ind:")) {//目标方法中的参数列表值中指定位置的参数值
							indexStr=aopParamValue.substring(4).trim();
							try {
								index=Integer.parseInt(indexStr);
							}catch(NumberFormatException e) {
								throw new AopParamsConfigurationException("错误的表达式，参数表达式中的索引不合法，索引只能为整数！错误位置："+expandMethod+"@AopParam("+aopParamValue+")=>err");
							}
							if(!targetMethodSignature.containsIndex(index)) {
								throw new AopParamsConfigurationException("错误的表达式，参数表达式中的索引超出参数列表索引范围！错误位置："+expandMethod+"@AopParam("+aopParamValue+")=>err");
							}
							expandParams[i]=targetMethodSignature.getParamByIndex(index);
						}else {//根据参数名得到具体参数
							if(!targetMethodSignature.containsParamName(aopParamValue)) {
								throw new AopParamsConfigurationException("错误的参数名配置，在目标方法中找不到参数名为\""+aopParamValue+"\"的参数，请检查配置信息!错误位置："+expandMethod+"@AopParam("+aopParamValue+")=>err");
							}
							expandParams[i]=targetMethodSignature.getParamByName(aopParamValue);
						}
					}else{
						Class<?> paramClass = parameters[i].getType();
						if(TargetMethodSignature.class.isAssignableFrom(paramClass)) {
							expandParams[i]=targetMethodSignature;
						}else if(Class.class.isAssignableFrom(paramClass)){
							expandParams[i]=targetMethodSignature.getTargetClass();
						}else if(Method.class.isAssignableFrom(paramClass)){
							expandParams[i]=targetMethodSignature.getCurrMethod();
						}else if(ApplicationBeans.createApplicationBeans().getBeans(paramClass).size()==1){
							expandParams[i]=ApplicationBeans.createApplicationBeans().getBean(paramClass);
						}else if(Object[].class==paramClass){
							expandParams[i]=targetMethodSignature.getParams();
						}else if(Parameter[].class==paramClass){
							expandParams[i]=targetMethodSignature.getParameters();
						}else if(Map.class.isAssignableFrom(paramClass)){
							Class<?>[] genericType = ClassUtils.getGenericType(parameters[i].getParameterizedType());
							if(genericType[0]==Integer.class&&genericType[1]==Object.class){
								expandParams[i]=targetMethodSignature.getIndexMap();
							}
							if(genericType[0]==String.class&&genericType[1]==Object.class){
								expandParams[i]=targetMethodSignature.getNameMap();
							}
						}
					}
				}
				return expandParams;
			}
		};
		return cpoint;
	}
	
}

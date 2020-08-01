package com.lucky.jacklamb.aop.proxy;

import com.lucky.jacklamb.annotation.aop.After;
import com.lucky.jacklamb.annotation.aop.AopParam;
import com.lucky.jacklamb.annotation.aop.Around;
import com.lucky.jacklamb.annotation.aop.Before;
import com.lucky.jacklamb.enums.Location;
import com.lucky.jacklamb.exception.IllegaAopparametersException;
import com.lucky.jacklamb.ioc.ApplicationBeans;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

public class PointRun {
	
	private Point point;
	
	private String pointCutClass;
	
	private String pointCutMethod;
	
	public Method method;
	
	/**
	 * 使用一个Point对象构造PointRun
	 * @param point
	 */
	public PointRun(Point point) {
		Method proceedMethod;
		try {
			proceedMethod = point.getClass().getDeclaredMethod("proceed", Chain.class);
			Around exp = proceedMethod.getAnnotation(Around.class);
			this.point = point;
			this.pointCutClass = exp.pointCutClass();
			this.pointCutMethod = exp.pointCutMethod();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * 使用Point类型对象的Class来构造PointRun
	 * @param pointClass
	 */
	public PointRun(Class<?> pointClass) {
		Method proceedMethod;
		try {
			proceedMethod =pointClass.getDeclaredMethod("proceed", Chain.class);
			Around exp = proceedMethod.getAnnotation(Around.class);
			Constructor<?> constructor = pointClass.getConstructor();
			constructor.setAccessible(true);
			this.point = (Point) constructor.newInstance();
			this.pointCutClass = exp.pointCutClass();
			this.pointCutMethod = exp.pointCutMethod();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			this.pointCutClass = before.pointCutClass();
			this.pointCutMethod = before.pointCutMethod();
		}else if(method.isAnnotationPresent(After.class)) {
			After after=method.getAnnotation(After.class);
			this.point=conversion(expand,method,Location.AFTER);
			this.pointCutClass = after.pointCutClass();
			this.pointCutMethod = after.pointCutMethod();
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

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
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
			throw new RuntimeException("切入点配置错误，错误位置："+method+" ->@Before/@After/@Around(pointcat=>err)", e);
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
			if(method.getModifiers()!=1)
				return false;
		}
		for(String str:pointcutStr) {
			if("*".equals(str)) {
				return true;
			}else if(str.contains("(")&&str.endsWith(")")){
				if(standardMethod(methodName,parameters,str))
					return true;
			}else {
				if(standardName(methodName,str))
					return true;
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
			if(methodParamStr.length!=parameters.length)
				return true;
			methodNameStr=pointcut.substring(1, indexOf);
			for(int i=0;i<methodParamStr.length;i++) {
				if(!(methodParamStr[i].equals(parameters[i].getType().getSimpleName()))) {
					pass=false;
					break;
				}
			}
			return !(standardName(mothodName,methodNameStr)&&pass);
		}else {//没有  ！
			if(methodParamStr.length!=parameters.length)
				return false;
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
	private Point conversion(Object expand, Method expandMethod,Location location) {
		Point cpoint=new Point() {
			
			@Override
			public Object proceed(Chain chain) throws Throwable {
				if(location==Location.BEFORE) {
					perform(expand,expandMethod);
					return chain.proceed();
				}else if(location==Location.AFTER) {
					Object result=chain.proceed();
					perform(expand,expandMethod);
					return result;
				}
				return null;
			}
			
			
			//执行增强方法
			private Object perform(Object expand, Method expandMethod) {
				try {
					expandMethod.setAccessible(true);
					return expandMethod.invoke(expand, setParams(expandMethod));
				} catch (IllegalAccessException e) {
					throw new RuntimeException("IllegalAccessException", e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("参数类型不匹配!在增强方法中配置了无法从目标方法参数列表获取的参数，错误位置："+expandMethod+ e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("InvocationTargetException", e);
				}
			}
			
			//设置增强方法的执行参数-@AopParam配置
			private Object[] setParams(Method expandMethod) {
				int index;
				String aopParamValue,indexStr;
				Parameter[] parameters = expandMethod.getParameters();
				Object[] expandParams=new Object[parameters.length];
				TargetMethodSignature targetMethodSignature = tlTargetMethodSignature.get();
				for(int i=0;i<parameters.length;i++) {
					if(TargetMethodSignature.class.isAssignableFrom(parameters[i].getType())) {
						expandParams[i]=targetMethodSignature;
						continue;
					}
					if(!parameters[i].isAnnotationPresent(AopParam.class))
						throw new IllegaAopparametersException("无法识别的AOP参数，前置增强或后置增强中存在无法识别的参数，错误原因：没有使用@AopParam注解标注参数！错误位置："+expandMethod);
					aopParamValue=parameters[i].getAnnotation(AopParam.class).value();
					if(aopParamValue.startsWith("ref:")) {//取IOC容器中的值
						if("ref:".equals(aopParamValue.trim())) 
							expandParams[i]=ApplicationBeans.createApplicationBeans().getBean(parameters[i].getType());
						else 
							expandParams[i]=ApplicationBeans.createApplicationBeans().getBean(aopParamValue.substring(4));
						
					}else if(aopParamValue.startsWith("ind:")) {//目标方法中的参数列表值中指定位置的参数值
						indexStr=aopParamValue.substring(4).trim();
						try {
							index=Integer.parseInt(indexStr);
						}catch(NumberFormatException e) {
							throw new RuntimeException("错误的表达式，参数表达式中的索引不合法，索引只能为整数！错误位置："+expandMethod+"@AopParam("+aopParamValue+")=>err");
						}
						if(!targetMethodSignature.containsIndex(index))
							throw new RuntimeException("错误的表达式，参数表达式中的索引超出参数列表索引范围！错误位置："+expandMethod+"@AopParam("+aopParamValue+")=>err");
						expandParams[i]=targetMethodSignature.getParamByIndex(index);	
					}else if(aopParamValue.equals("[params]")){//整个参数列表
						expandParams[i]=targetMethodSignature.getParams();
					}else if(aopParamValue.equals("[method]")) {//Method对象
						expandParams[i]=targetMethodSignature.getCurrMethod();
					}else if(aopParamValue.equals("[target]")) {//目标类的Class
						expandParams[i]=targetMethodSignature.getTargetClass();
					} else if(aopParamValue.equals("[targetMethodSignature]")){
						expandParams[i]=targetMethodSignature;
					}else {//根据参数名得到具体参数
						if(!targetMethodSignature.containsParamName(aopParamValue))
							throw new RuntimeException("错误的参数名配置，在目标方法中找不到参数名为\""+aopParamValue+"\"的参数，请检查配置信息!错误位置："+expandMethod+"@AopParam("+aopParamValue+")=>err");
						expandParams[i]=targetMethodSignature.getParamByName(aopParamValue);
					}
				}
				return expandParams;
			}
		};
		return cpoint;
	}
	
}

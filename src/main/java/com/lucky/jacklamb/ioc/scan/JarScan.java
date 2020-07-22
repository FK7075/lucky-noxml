package com.lucky.jacklamb.ioc.scan;

import com.lucky.jacklamb.annotation.aop.Aspect;
import com.lucky.jacklamb.annotation.mvc.*;
import com.lucky.jacklamb.conversion.annotation.Conversion;
import com.lucky.jacklamb.annotation.ioc.*;
import com.lucky.jacklamb.annotation.orm.Table;
import com.lucky.jacklamb.annotation.orm.mapper.Mapper;
import com.lucky.jacklamb.aop.proxy.Point;
import com.lucky.jacklamb.ioc.config.ApplicationConfig;
import com.lucky.jacklamb.ioc.config.LuckyConfig;
import com.lucky.jacklamb.ioc.enums.IocCode;
import com.lucky.jacklamb.servlet.exceptionhandler.JarScanException;
import com.lucky.jacklamb.rest.LSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class JarScan extends Scan {
	
	protected String jarpath;

	private static final Logger log= LogManager.getLogger(JarScan.class);
	
	private LSON lson;
	
	protected String prefix;


	public JarScan(Class<?> clzz) {
		lson=new LSON();
		String allname=clzz.getName();
		String simpleName=clzz.getSimpleName();
		prefix=allname.substring(0, allname.length()-simpleName.length()).replaceAll("\\.", "/");
		jarpath=clzz.getResource("").getPath();
		jarpath=jarpath.substring(5);
		if(jarpath.contains(".jar!")){
			if(jarpath.contains(":")){
				jarpath=jarpath.substring(1, jarpath.indexOf(".jar!")+4);
			}else{
				jarpath=jarpath.substring(0, jarpath.indexOf(".jar!")+4);
			}
		}

	}

	public List<Class<?>> loadComponent(List<String> suffixs) {
		List<Class<?>> className = new ArrayList<>();
		JarFile jarFile = null;

		try {
			jarFile = new JarFile(jarpath);
		} catch (IOException e) {
			throw new JarScanException("找不到jar文件：["+jarpath+"]",e);
		}
		Enumeration<JarEntry> entrys = jarFile.entries();
		String cpath;
		while (entrys.hasMoreElements()) {
			JarEntry entry = entrys.nextElement();
			String name = entry.getName();
			if (name.endsWith(".class") && name.startsWith(prefix)) {
				name = name.substring(0, name.length() - 6);
				cpath = name.substring(0, name.lastIndexOf("/"));
				for (String suf : suffixs) {
					if (cpath.endsWith(suf)) {
						String clzzName = name.replaceAll("/", "\\.");
						try {
							className.add(Class.forName(clzzName));
						} catch (ClassNotFoundException e) {
							throw new RuntimeException("类加载错误，错误path:" + clzzName, e);
						}
						break;
					}
				}
			}
		}
		return className;
	}

	@Override
	public void autoScan() {
		JarFile jarFile = null;

		try {
			jarFile = new JarFile(jarpath);
		} catch (IOException e) {
			throw new JarScanException("找不到jar文件：["+jarpath+"]",e);
		}
		Enumeration<JarEntry> entrys = jarFile.entries();
		try {
			while (entrys.hasMoreElements()) {
				JarEntry entry = entrys.nextElement();
				String name = entry.getName();
				if (name.endsWith(".class") && name.startsWith(prefix)) {
					name = name.substring(0, name.length() - 6);
					String clzzName = name.replaceAll("/", "\\.");
					Class<?> fileClass = Class.forName(clzzName);
					if (fileClass.isAnnotationPresent(Controller.class)||fileClass.isAnnotationPresent(CallController.class)||fileClass.isAnnotationPresent(LuckyClient.class))
						controllerClass.add(fileClass);
					else if (fileClass.isAnnotationPresent(Service.class))
						serviceClass.add(fileClass);
					else if (fileClass.isAnnotationPresent(Repository.class)
							|| fileClass.isAnnotationPresent(Mapper.class))
						repositoryClass.add(fileClass);
					else if (fileClass.isAnnotationPresent(Component.class)
							|| fileClass.isAnnotationPresent(Configuration.class)
							|| fileClass.isAnnotationPresent(ControllerExceptionHandler.class)
							|| fileClass.isAnnotationPresent(LuckyServlet.class)
							|| fileClass.isAnnotationPresent(LuckyFilter.class)
							|| fileClass.isAnnotationPresent(LuckyListener.class)
							|| fileClass.isAnnotationPresent(Conversion.class))
						componentClass.add(fileClass);
					else if(fileClass.isAnnotationPresent(Table.class))
						pojoClass.add(fileClass);
					else if (fileClass.isAnnotationPresent(Aspect.class) || Point.class.isAssignableFrom(fileClass))
						aspectClass.add(fileClass);
					else {
						try {
							if(fileClass.isAnnotationPresent(ServerEndpoint.class)||ServerApplicationConfig.class.isAssignableFrom(fileClass)||Endpoint.class.isAssignableFrom(fileClass)) {
								webSocketClass.add(fileClass);
							}
						}catch(Exception e) {
							continue;
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void findAppConfig() {
		JarFile jarFile = null;

		try {
			jarFile = new JarFile(jarpath);
		} catch (IOException e) {
			throw new JarScanException("找不到jar文件：["+jarpath+"]",e);
		}
		Enumeration<JarEntry> entrys = jarFile.entries();
		try {
		while (entrys.hasMoreElements()) {
			JarEntry entry = entrys.nextElement();
			String name = entry.getName();
			if (name.endsWith(".class") && name.startsWith(prefix)) {
				name = name.substring(0, name.length() - 6);
				String clzzName = name.replaceAll("/", "\\.");
				Class<?> fileClass = Class.forName(clzzName);
				if(fileClass.isAnnotationPresent(Configuration.class)) {
					Method[] beanMethods=fileClass.getDeclaredMethods();
					Object cfgObj = fileClass.newInstance();
					Class<?> returnType;
					Object config;
					StringBuilder info;
					for(Method method:beanMethods) {
						returnType=method.getReturnType();
						if(method.isAnnotationPresent(Bean.class)) {
							IocCode iocCode=method.getAnnotation(Bean.class).iocCode();
							method.setAccessible(true);
							if(LuckyConfig.class.isAssignableFrom(returnType)){
								config=method.invoke(cfgObj);
								info=new StringBuilder();
								info.append("@").append(config.getClass().getSimpleName()).append(" \"").append(lson.toJson1(config)).append("\"");
								log.info(info.toString());
							}else if(iocCode == IocCode.CONTROLLER){
								config=method.invoke(cfgObj);
								if(returnType==Class.class){
									Class ctrlClass= (Class) config;
									controllerClass.add(ctrlClass);
								}else if(returnType==Class[].class){
									Class[] ctrlClasses= (Class[]) config;
									Stream.of(ctrlClasses).forEach(controllerClass::add);
								}else{
									throw new RuntimeException("尝试创建一个@Controller组件失败！不合法的返回值类型"+returnType+",合法的返回值类型为Class和Class[]，错误位置："+method);
								}
							}else if(iocCode == IocCode.SERVICE){
								config=method.invoke(cfgObj);
								if(returnType==Class.class){
									Class ctrlClass= (Class) config;
									serviceClass.add(ctrlClass);
								}else if(returnType==Class[].class){
									Class[] ctrlClasses= (Class[]) config;
									Stream.of(ctrlClasses).forEach(serviceClass::add);
								}else{
									throw new RuntimeException("尝试创建一个@Service组件失败！不合法的返回值类型"+returnType+",合法的返回值类型为Class和Class[]，错误位置："+method);
								}
							}else if(iocCode == IocCode.REPOSITORY){
								config=method.invoke(cfgObj);
								if(returnType==Class.class){
									Class ctrlClass= (Class) config;
									repositoryClass.add(ctrlClass);
								}else if(returnType==Class[].class){
									Class[] ctrlClasses= (Class[]) config;
									Stream.of(ctrlClasses).forEach(repositoryClass::add);
								}else{
									throw new RuntimeException("尝试创建一个@Repository组件失败！不合法的返回值类型"+returnType+",合法的返回值类型为Class和Class[]，错误位置："+method);
								}
							}else if(iocCode==IocCode.TABLE){
								config=method.invoke(cfgObj);
								if(returnType==Class.class){
									Class ctrlClass= (Class) config;
									pojoClass.add(ctrlClass);
								}else if(returnType==Class[].class){
									Class[] ctrlClasses= (Class[]) config;
									Stream.of(ctrlClasses).forEach(pojoClass::add);
								}else{
									throw new RuntimeException("尝试创建一个@Table组件失败！不合法的返回值类型"+returnType+",合法的返回值类型为Class和Class[]，错误位置："+method);
								}
							}else if(iocCode==IocCode.ASPECT){
								config=method.invoke(cfgObj);
								if(returnType==Class.class){
									Class ctrlClass= (Class) config;
									aspectClass.add(ctrlClass);
								}else if(returnType==Class[].class){
									Class[] ctrlClasses= (Class[]) config;
									Stream.of(ctrlClasses).forEach(aspectClass::add);
								}else{
									throw new RuntimeException("尝试创建一个@Aspect组件失败！不合法的返回值类型"+returnType+",合法的返回值类型为Class和Class[]，错误位置："+method);
								}
							}else if(iocCode==IocCode.WEBSOCKET){
								config=method.invoke(cfgObj);
								if(returnType==Class.class){
									Class ctrlClass= (Class) config;
									webSocketClass.add(ctrlClass);
								}else if(returnType==Class[].class){
									Class[] ctrlClasses= (Class[]) config;
									Stream.of(ctrlClasses).forEach(webSocketClass::add);
								}else{
									throw new RuntimeException("尝试创建一个@ServerEndpoint组件失败！不合法的返回值类型"+returnType+",合法的返回值类型为Class和Class[]，错误位置："+method);
								}
							}
						}
					}
				}
				if(ApplicationConfig.class.isAssignableFrom(fileClass)&&fileClass.isAnnotationPresent(Configuration.class)) {
					appConfig=(ApplicationConfig) fileClass.newInstance();
				}
			}
		}
		} catch (ClassNotFoundException | InstantiationException |IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}

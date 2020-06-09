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
		if(jarpath.contains(".jar!"))
			jarpath=jarpath.substring(6, jarpath.indexOf(".jar!")+4);
	}

	public List<Class<?>> loadComponent(List<String> suffixs) {
		List<Class<?>> className = new ArrayList<>();
		JarFile jarFile = null;

		try {
			jarFile = new JarFile(jarpath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
							|| fileClass.isAnnotationPresent(ExceptionHander.class)
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
					Class<?> returnType;
					Object config;
					StringBuilder info;
					for(Method method:beanMethods) {
						returnType=method.getReturnType();
						if(method.isAnnotationPresent(Bean.class)&&LuckyConfig.class.isAssignableFrom(returnType)) {
							info=new StringBuilder();
							method.setAccessible(true);
							config=method.invoke(fileClass.newInstance());
							info.append("@").append(config.getClass().getSimpleName()).append(" \"").append(lson.toJson1(config)).append("\"");
							log.info(info.toString());
							
						}
					}
				}
				if(ApplicationConfig.class.isAssignableFrom(fileClass)&&fileClass.isAnnotationPresent(Configuration.class)) {
					appConfig=(ApplicationConfig) fileClass.newInstance();
				}
			}
		}
		} catch (ClassNotFoundException e) {
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
	
}

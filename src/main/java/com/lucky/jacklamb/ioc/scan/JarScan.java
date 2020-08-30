package com.lucky.jacklamb.ioc.scan;

import com.lucky.jacklamb.annotation.ioc.Configuration;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.config.ApplicationConfig;
import com.lucky.jacklamb.rest.LSON;
import com.lucky.jacklamb.servlet.exceptionhandler.JarScanException;
import com.lucky.jacklamb.sqlcore.mapper.xml.MapperXMLParsing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarScan extends Scan {
	
	protected String jarpath;

	private static final Logger log= LogManager.getLogger(JarScan.class);
	
	protected String prefix;


	public JarScan(Class<?> clzz) throws URISyntaxException {
		lson=new LSON();
		String allname=clzz.getName();
		String simpleName=clzz.getSimpleName();
		prefix=allname.substring(0, allname.length()-simpleName.length()).replaceAll("\\.", "/");
		jarpath=clzz.getResource("").getPath();
		System.out.println(jarpath);
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
					load(fileClass);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<MapperXMLParsing> getAllMapperXml(String path){
		JarFile jarFile = null;
		Set<MapperXMLParsing> xmls=new HashSet<>();
		try {
			jarFile = new JarFile(jarpath);
		} catch (IOException e) {
			throw new JarScanException("找不到jar文件：["+jarpath+"]",e);
		}
		Enumeration<JarEntry> entrys = jarFile.entries();
		while (entrys.hasMoreElements()) {
			JarEntry entry = entrys.nextElement();
			String name = entry.getName();
			if (name.endsWith(".xml") && name.startsWith(path)) {
				try{
					if(MapperXMLParsing.isMapperXml(ApplicationBeans.class.getResourceAsStream("/"+name))){
						xmls.add(new MapperXMLParsing(ApplicationBeans.class.getResourceAsStream("/"+name)));
					}
				}catch (Exception e){
					e.printStackTrace();
				}

			}
		}
		return xmls;
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
				registered(fileClass);
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

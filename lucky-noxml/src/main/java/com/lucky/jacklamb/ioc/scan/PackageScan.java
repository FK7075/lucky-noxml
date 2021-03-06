package com.lucky.jacklamb.ioc.scan;

import com.lucky.jacklamb.annotation.ioc.Configuration;
import com.lucky.jacklamb.ioc.config.ApplicationConfig;
import com.lucky.jacklamb.sqlcore.mapper.xml.MapperXMLParsing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * 不做配置时的默认包扫描
 * @author fk-7075
 *
 */
public class PackageScan extends Scan {

	//Wim: E:/project  Mac: /User/loca
	private String projectPath;

	//Win: E:/project/ Mac: /User/loca/
	private String fileProjectPath;

	private static final Logger log= LogManager.getLogger(PackageScan.class);
	
	private LuckyClassLoader luckyClassLoader;
	
	
	public PackageScan() throws URISyntaxException {
		super();
		projectPath=PackageScan.class.getClassLoader().getResource("").toURI().getPath();
		if(projectPath.endsWith("/classes/")) {
			projectPath=projectPath.substring(0,projectPath.length()-8);
		}else if(projectPath.endsWith("/test-classes/")) {
			projectPath=projectPath.substring(0,projectPath.length()-13);
		}
		if(projectPath.contains(":")){
			fileProjectPath=projectPath.substring(1);
			projectPath=projectPath.replaceAll("\\\\", "/").substring(1,projectPath.length()-1);
		}else{
			fileProjectPath=projectPath;
			projectPath=projectPath.replaceAll("\\\\", "/").substring(0,projectPath.length()-1);
		}
	}
	
	/**
	 * 找到所有Ioc组件所在文件夹的包路径，并存入到的集合中
	 * @param components
	 * @param suffix
	 */
	public void loadComponent(Set<Class<?>> components,String...suffix) {
		List<String> clist=new ArrayList<>();
		findDafaultFolder(clist,projectPath,suffix);
		addClassPath(components,clist);
	}
	
	/**
	 * 找到所有Ioc组件所在文件夹的包路径，并存入到的集合中
	 * @param suffixlist 自定义的包后缀名集合
	 * @return
	 */
	public Set<Class<?>> loadComponent(List<String> suffixlist) {
		String[] suffix=new String[suffixlist.size()];
		suffixlist.toArray(suffix);
		Set<Class<?>> components=new HashSet<>(16);
		List<String> clist=new ArrayList<>();
		findDafaultFolder(clist,projectPath,suffix);
		addClassPath(components,clist);
		return components;
	}
	
	
	/**
	 * 找到所有Mapper组件所在文件夹的绝对路径，并存入到的集合中
	 * @param mappers mapper接口组件
	 */
	public void loadMapper(Set<Class<?>> mappers,String...suffix) {
		List<String> mlist=new ArrayList<>();
		findDafaultFolder(mlist,projectPath,suffix);
		addClassPath(mappers,mlist);
	}
	
	/**
	 * 得到所有组件的包路径,放入容器中
	 * @param components 容器
	 * @param paths 组件所在文件夹的绝对路径
	 */
	private void addClassPath(Set<Class<?>> components,List<String> paths) {
		for(String path:paths) {
			File file=new File(path);
			File[] listFiles = file.listFiles();
			for(File f:listFiles) {
				if(f.getName().endsWith(".class")) {
					String clzzpath=f.getAbsolutePath().replaceAll("/","\\\\");
					clzzpath.substring(0, clzzpath.length()-6);
					String u=(projectPath+"/").replaceAll("/", "\\\\");
					clzzpath=clzzpath.replace(u, "");
					clzzpath=clzzpath.replaceAll("\\\\", "\\.");
					clzzpath=clzzpath.substring(0, clzzpath.length()-6);
					try {
						if(clzzpath.startsWith("classes.")) {
							clzzpath=clzzpath.substring(8);
							components.add(Class.forName(clzzpath));
						}else if(clzzpath.startsWith("test-classes.")) {
							luckyClassLoader=new LuckyClassLoader(fileProjectPath+File.separator+"test-classes");
							clzzpath=clzzpath.substring(13);
							components.add(luckyClassLoader.loadClass(clzzpath));
						}else {
							components.add(Class.forName(clzzpath));
						}
					} catch (ClassNotFoundException e) {
						throw new RuntimeException("类加载错误，错误path:"+clzzpath, e);
					}
				}
			}
				
		}
	}
	
	
	/**
	 * 找到文件名为suffix的文件夹
	 * @param defaultPaths 用于存储目标文件夹绝对路径的集合
	 * @param path 开始文件夹的绝对路径
	 * @param suffixs 需要得到的文件夹名
	 */
	private void findDafaultFolder(List<String> defaultPaths,String path,String...suffixs){
		List<String> suffixList=Arrays.asList(suffixs);
		File file=new File(path);
		File[] listFiles = file.listFiles();
		for(File f:listFiles) {
			if(f.isDirectory()) {
				if(isSuffix(suffixList,f.getName().toLowerCase()))
					defaultPaths.add(f.getAbsolutePath());
				else
					findDafaultFolder(defaultPaths,path+"/"+f.getName(),suffixs);
			}
		}
	}
	
	/**
	 * 判断str是否以suffixs中的某个元素结尾
	 * @param suffixs
	 * @param str
	 * @return
	 */
	private boolean isSuffix(List<String> suffixs,String str) {
		for(String suffix:suffixs) {
			if(str.endsWith(suffix))
				return true;
		}
		return false;
	}
	
	@Override
	public void autoScan() {
		try {
			fileScan(fileProjectPath);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<MapperXMLParsing> getAllMapperXml(String path) {
		Set<MapperXMLParsing> xmls=new HashSet<>();
		URL url = PackageScan.class.getClassLoader().getResource(path);
		if(url==null) {
			return xmls;
		}
		try {
			File mapperXml=new File(url.getFile());
			getMapperXml(mapperXml,xmls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmls;
	}

	private void getMapperXml(File file,Set<MapperXMLParsing> mapXmls) throws Exception {
		if(file.isFile()&&file.getName().endsWith(".xml")&&MapperXMLParsing.isMapperXml(new FileInputStream(file))){
			mapXmls.add(new MapperXMLParsing(file));
		}else if(file.isDirectory()){
			File[] sonFiles = file.listFiles();
			for (File sonf : sonFiles) {
				getMapperXml(sonf,mapXmls);
			}
		}
	}


	@Override
	public void findAppConfig() {
		try {
			findConfig(fileProjectPath);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	private void findConfig(String path) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		File bin=new File(path);
		File[] listFiles = bin.listFiles();
		for(File file:listFiles) {
			if(!file.isDirectory()&&file.getAbsolutePath().endsWith(".class")) {
				Class<?> fileClass=getFileClass(file);
				registered(fileClass);
				if(ApplicationConfig.class.isAssignableFrom(fileClass)&&fileClass.isAnnotationPresent(Configuration.class)) {
					appConfig=(ApplicationConfig) fileClass.newInstance();
				}
			}else if(file.isDirectory()){
				findConfig(path+"/"+file.getName());
			}else {
				continue;
			}
		}
	}

	private void fileScan(String path) throws ClassNotFoundException {
		File bin=new File(path);
		File[] listFiles = bin.listFiles();
		for(File file:listFiles) {
			if(file.isDirectory()) {
				fileScan(path+"/"+file.getName());
			}else if(file.getAbsolutePath().endsWith(".class")) {
				load(getFileClass(file));
			}
		}
	}

	private Class<?> getFileClass(File file) throws ClassNotFoundException {
		String className=file.getAbsolutePath().replaceAll("\\\\", "/").replaceAll(fileProjectPath, "").replaceAll("/", "\\.");
		className=className.substring(0,className.length()-6);
		Class<?> fileClass=null;
		if(className.startsWith("classes.")) {
			className=className.substring(8);
			fileClass=Class.forName(className);
		}else if(className.startsWith("test-classes.")) {
			luckyClassLoader=new LuckyClassLoader(fileProjectPath+File.separator+"test-classes");
			className=className.substring(13);
			fileClass=luckyClassLoader.loadClass(className);
		}else{
			fileClass=Class.forName(className);
		}
		return  fileClass;
	}

}



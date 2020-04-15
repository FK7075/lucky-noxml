package com.lucky.jacklamb.file.ini;

import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_FILTER;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_FILTER_MAPPING;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_HANDERPREFIXANDSUFFIX;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_LISTENER;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_SERVLET;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_SERVLET_MAPPING;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_SPECIFIRESOURCESIPRESTRICT;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_SQL_INI;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_STATICHANDER;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_SUFFIX_SCAN;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_TOMCAT;
import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.SECTION_WEB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import com.lucky.jacklamb.enums.Scan;
import com.lucky.jacklamb.ioc.config.ScanConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;

public class IniFilePars {
	
	private static IniFilePars iniFilePars;
	
	private Map<String,Map<String,String>> iniMap;
	
	//当前节
	private String currSection;
	
	//当前行
	private String currLine;
	
	//文件路径
	private String iniName;
	
	//是否存在换行符"\"
	private boolean isNewline=false;
	
	//多行配置中的key
	private String newLineKey;
	
	//多行配置中的value
	private StringBuilder newLineValue;
	
	//文件输入流
	private InputStream iniInputStream;
	
	public Map<String, Map<String, String>> getIniMap() {
		return iniMap;
	}

	public IniFilePars() {
		iniMap=new HashMap<>();
		iniInputStream=IniFilePars.class.getClassLoader().getResourceAsStream("appconfig.ini");
		iniName="appconfig.ini";
		try {
			if(iniMap.isEmpty())
				pars();
		}catch(ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException(iniName+"配置文件内容格式不正确",e);
		}
	}
	
	public IniFilePars(String iniFilePath) {
		iniMap=new HashMap<>();
		iniInputStream=IniFilePars.class.getClassLoader().getResourceAsStream(iniFilePath);
		iniName=iniFilePath;
		try {
			if(iniMap.isEmpty())
				pars();
		}catch(ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException(iniName+"配置文件内容格式不正确",e);
		}
	}
	
	public static IniFilePars getIniFilePars() {
		if(iniFilePars==null)
			iniFilePars=new IniFilePars();
		return iniFilePars;
	}
	
	public boolean iniExist() {
		return iniInputStream!=null;
	}
	
	private void pars() {
		if(iniInputStream!=null) {
			InputStreamReader isr = new InputStreamReader(iniInputStream);
			BufferedReader read = new BufferedReader(isr);
			Map<String,String> kvMap=new HashMap<>();
			try {
				while((currLine = read.readLine()) != null) {
					if(currLine.contains(";")) {
						currLine=currLine.substring(0,currLine.indexOf(";"));
					}
					if(currLine.contains("#")) {
						currLine=currLine.substring(0,currLine.indexOf("#"));
					}
					if(currLine.startsWith(";")||currLine.startsWith("#")) {
						continue;
					}else if(currLine.startsWith("[")&&currLine.endsWith("]")) {
						currSection=currLine.substring(1,currLine.length()-1);
						if(iniMap.containsKey(currSection))
							throw new RuntimeException(iniName+"配置文件内容格式不正确,存在两个相同的Section:["+currSection+"]");
						iniMap.put(currSection,new HashMap<>());
						continue;
					}else if(!currLine.endsWith("\\")&&!isNewline) {//不是以"\"结尾，而且之前也不存在以"\"结尾的行
						if(currLine.contains("=")) {
							currLine=currLine.replaceFirst("=", "%Lucky%FK@7075&XFL");
							String[] KV = currLine.split("%Lucky%FK@7075&XFL");
							if(iniMap.containsKey(currSection)) {
								kvMap=iniMap.get(currSection);
								kvMap.put(KV[0], KV[1]);
							}else {
								kvMap.put(KV[0], KV[1]);
								iniMap.put(currSection, kvMap);
							}
						}
					}else if(currLine.endsWith("\\")&&!isNewline&&currLine.contains("=")) {//是以"\"结尾，而且之前不存在以"\"结尾的行
						currLine=currLine.replaceFirst("=", "%Lucky%FK@7075&XFL");
						String[] KV = currLine.split("%Lucky%FK@7075&XFL");
						isNewline=true;
						newLineKey=KV[0];
						newLineValue=new StringBuilder(KV[1].subSequence(0, KV[1].length()-1));
					}else if(currLine.endsWith("\\")&&isNewline) {//是以"\"结尾，而且存在以"\"结尾的行
						currLine=currLine.replaceAll("\\t", " ");
						int index=firstNoSpaceIndex(currLine);
						if(index==0||index==1)
							newLineValue.append(currLine.substring(0, currLine.length()-1));
						else
							newLineValue.append(currLine.substring(index-1, currLine.length()-1));
					}else if(!currLine.endsWith("\\")&&isNewline) {//不是以"\"结尾，而且存在以"\"结尾的行
						kvMap=iniMap.get(currSection);
						currLine=currLine.replaceAll("\\t", " ");
						int index=firstNoSpaceIndex(currLine);
						if(index==0||index==1)
							newLineValue.append(currLine);
						else
							newLineValue.append(currLine.substring(index-1, currLine.length()));
						kvMap.put(newLineKey, newLineValue.toString());
						iniMap.put(currSection, kvMap);
						isNewline=false;
					}
					else {
						continue;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				read.close();
				isr.close();
				iniInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean isHasSection(String section) {
		return iniMap.containsKey(section);
	}
	
	public boolean isHasKey(String section,String key) {
		if(isHasSection(section)) {
			return iniMap.get(section).containsKey(key);
		}else {
			return false;
		}
	}
	
	public Map<String,String> getSectionMap(String section){
		if(iniMap.containsKey(section))
			return iniMap.get(section);
		return null;
	}
	
	public String getValue(String section,String key) {
		Map<String, String> sectionMap = getSectionMap(section);
		if(sectionMap!=null) {
			if(sectionMap.containsKey(key))
				return sectionMap.get(key);
			return null;
		}
		return null;
	}
	
	public void modifyAllocation(ScanConfig scan,WebConfig web,ServerConfig server) {
		if(iniMap.isEmpty())
			return;
		Map<String, String> sectionMap;
		if(this.isHasSection(SECTION_SUFFIX_SCAN)) {
			sectionMap = this.getSectionMap(SECTION_SUFFIX_SCAN);
			scan.setScanMode(Scan.SUFFIX_SCAN);
			setScanConfig(scan,sectionMap);
		}
		if(this.isHasSection(SECTION_SQL_INI)) {
			scan.setSqlIniPath(this.getSectionMap(SECTION_SQL_INI).get("path"));
		}
		if(this.isHasSection(SECTION_TOMCAT)) {
			sectionMap = this.getSectionMap(SECTION_TOMCAT);
			setTomcat(server,sectionMap);
		}
		if(this.isHasSection(SECTION_SERVLET)) {
			sectionMap = this.getSectionMap(SECTION_SERVLET);
			addServlet(server,sectionMap);
		}
		
		if(this.isHasSection(SECTION_LISTENER)) {
			sectionMap = this.getSectionMap(SECTION_LISTENER);
			addListener(server, sectionMap);
		}
		if(this.isHasSection(SECTION_FILTER)) {
			sectionMap = this.getSectionMap(SECTION_FILTER);
			addFilter(server,sectionMap);
		}
		if(this.isHasSection(SECTION_WEB)) {
			sectionMap = this.getSectionMap(SECTION_WEB);
			webSetting(web,sectionMap);
		}
		if(this.isHasSection(SECTION_HANDERPREFIXANDSUFFIX)) {
			sectionMap = this.getSectionMap(SECTION_HANDERPREFIXANDSUFFIX);
			addPrefixAndSuffix(web,sectionMap);
		}
		if(this.isHasSection(SECTION_STATICHANDER)) {
			sectionMap = this.getSectionMap(SECTION_STATICHANDER);
			addStaticHander(web,sectionMap);
		}
		if(this.isHasSection(SECTION_SPECIFIRESOURCESIPRESTRICT)) {
			sectionMap = this.getSectionMap(SECTION_SPECIFIRESOURCESIPRESTRICT);
			addspecifiResourcesIpRestrict(web,sectionMap);
		}
		
	}
	
	private void addspecifiResourcesIpRestrict(WebConfig web,Map<String, String> sectionMap) {
		Map<String,List<String>> resAndIpsMap=new HashMap<>();
		for(Entry<String,String> e:sectionMap.entrySet()) {
			resAndIpsMap.put(e.getKey(), Arrays.asList(e.getValue().trim().split(",")));
		}
		web.setSpecifiResourcesIpRestrict(resAndIpsMap);
	}
	
	private void addStaticHander(WebConfig web,Map<String, String> sectionMap) {
		for(Entry<String,String> e:sectionMap.entrySet()) {
			web.addStaticHander(e.getKey(), e.getValue());
		}
	}
	
	private void addPrefixAndSuffix(WebConfig web,Map<String, String> sectionMap) {
		String p="",s="";
		if(sectionMap.containsKey("prefix"))
			p=sectionMap.get("prefix");
		if(sectionMap.containsKey("suffix"))
			s=sectionMap.get("suffix");
		web.setHanderPrefixAndSuffix(p, s);
	}
	
	private void webSetting(WebConfig web,Map<String, String> sectionMap) {
		if(sectionMap.containsKey("httpclient-connectTimeout")){
			web.setConnectTimeout(Integer.parseInt(sectionMap.get("httpclient-connectTimeout")));
		}
		if(sectionMap.containsKey("httpclient-connectionRequestTimeout")){
			web.setConnectionRequestTimeout(Integer.parseInt(sectionMap.get("httpclient-connectionRequestTimeout")));
		}
		if(sectionMap.containsKey("httpclient-socketTimeout")){
			web.setSocketTimeout(Integer.parseInt(sectionMap.get("httpclient-socketTimeout")));
		}
		if(sectionMap.containsKey("encoding")) {
			web.setEncoding(sectionMap.get("encoding"));
		}
		if(sectionMap.containsKey("openStaticResourceManage")) {
			web.openStaticResourceManage(Boolean.parseBoolean(sectionMap.get("openStaticResourceManage")));
		}
		if(sectionMap.containsKey("postChangeMethod")) {
			web.postChangeMethod(Boolean.parseBoolean(sectionMap.get("postChangeMethod")));
		}
		if(sectionMap.containsKey("globalResourcesIpRestrict")) {
			web.addGlobalResourcesIpRestrict(sectionMap.get("globalResourcesIpRestrict").trim().split(","));
		}
		if(sectionMap.containsKey("staticResourcesIpRestrict")) {
			web.addStaticResourcesIpRestrict(sectionMap.get("staticResourcesIpRestrict").trim().split(","));
		}
	}
	
	private void addListener(ServerConfig server,Map<String, String> sectionMap) {
		Collection<String> values = sectionMap.values();
		for(String listener:values) {
			try {
				server.addListener((EventListener)Class.forName(listener).newInstance());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void addFilter(ServerConfig server,Map<String, String> sectionMap) {
		Set<String> filterNames = sectionMap.keySet();
		for(String filterName:filterNames) {
			if(!this.isHasKey(SECTION_FILTER_MAPPING, filterName))
				throw new RuntimeException("appconfig.ini配置文件中有Filter没有配置请求映射：[Filter]->"+filterName+"="+sectionMap.get(filterName));
			try {
				server.addFilter((Filter)Class.forName(sectionMap.get(filterName)).newInstance(), this.getValue(SECTION_FILTER_MAPPING, filterName).split(","));
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("appconfig.ini配置文件有Filter配置错误，无法创建实例！[Filter]->"+filterName+"="+sectionMap.get(filterName),e);
			}
		}
	}
	
	private void addServlet(ServerConfig server,Map<String, String> sectionMap) {
		Set<String> servletNames = sectionMap.keySet();
		for(String servletName:servletNames) {
			if(!this.isHasKey(SECTION_SERVLET_MAPPING, servletName))
				throw new RuntimeException("appconfig.ini配置文件中有Servlet没有配置请求映射：[Servlet]->"+servletName+"="+sectionMap.get(servletName));
			try {
				server.addServlet((HttpServlet)Class.forName(sectionMap.get(servletName)).newInstance(), this.getValue(SECTION_SERVLET_MAPPING, servletName).split(","));
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("appconfig.ini配置文件有Servlet配置错误，无法创建实例！[Servlet]->"+servletName+"="+sectionMap.get(servletName),e);
			}
		}
	}
	
	private void setTomcat(ServerConfig server,Map<String, String> sectionMap) {
		if(sectionMap.containsKey("port")) {
			server.setPort(Integer.parseInt(sectionMap.get("port")));
		}
		if(sectionMap.containsKey("sessionTimeout")) {
			server.setSessionTimeout(Integer.parseInt(sectionMap.get("sessionTimeout")));
		}
		if(sectionMap.containsKey("closePort")) {
			server.setClosePort(Integer.parseInt(sectionMap.get("closePort")));
		}
		if(sectionMap.containsKey("shutdown")) {
			server.setShutdown(sectionMap.get("shutdown"));
		}
		if(sectionMap.containsKey("docBase")) {
			server.setDocBase(sectionMap.get("docBase"));
		}
		if(sectionMap.containsKey("ap-docBase")) {
			server.setApDocBase(sectionMap.get("ap-docBase"));
		}
		if(sectionMap.containsKey("baseDir")) {
			server.setBaseDir(sectionMap.get("baseDir"));
		}
		if(sectionMap.containsKey("ap-baseDir")) {
			server.setApBaseDir(sectionMap.get("ap-baseDir"));
		}
		if(sectionMap.containsKey("contextPath")) {
			server.setContextPath(sectionMap.get("contextPath"));
		}
		if(sectionMap.containsKey("webapp")) {
			server.setWebapp(sectionMap.get("webapp"));
		}
		if(sectionMap.containsKey("url-encoding")) {
			server.setURIEncoding(sectionMap.get("url-encoding"));
		}
		if(sectionMap.containsKey("autoDeploy")) {
			server.setAutoDeploy(Boolean.parseBoolean(sectionMap.get("autoDeploy")));
		}
		if(sectionMap.containsKey("reloadable")) {
			server.setReloadable(Boolean.parseBoolean(sectionMap.get("reloadable")));
		}
		if(sectionMap.containsKey("autoCreateWebapp")){
			server.autoCreateWebapp(Boolean.parseBoolean(sectionMap.get("autoCreateWebapp")));
		}
	}
	
	private void setScanConfig(ScanConfig scan,Map<String, String> sectionMap) {
		String suffixStr;
		if(sectionMap.containsKey("controller")) {
			suffixStr=sectionMap.get("controller");
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddControllerPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addControllerPackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("service")) {
			suffixStr=sectionMap.get("service");
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddServicePackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addServicePackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("repository")) {
			suffixStr=sectionMap.get("repository");
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddRepositoryPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addRepositoryPackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("aspect")) {
			suffixStr=sectionMap.get("aspect");
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddAspectPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addAspectPackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("component")) {
			suffixStr=sectionMap.get("component");
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddComponentPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addComponentPackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("pojo")) {
			suffixStr=sectionMap.get("pojo");
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddPojoPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addPojoPackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("websocket")) {
			suffixStr=sectionMap.get("websocket");
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddWebSocketPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addWebSocketPackSuffix(suffixStr.trim().split(","));
			}
		}
	}
	
	private static int firstNoSpaceIndex(String str) {
		char[] charArr = str.toCharArray();
		int i=0;
		boolean isHave=false;
		for(char ch:charArr) {
			if(ch!=' ') {
				isHave=true;
				break;
			}
			i++;
		}
		return isHave?i:-1;
	}
}


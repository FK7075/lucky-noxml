package com.lucky.jacklamb.file.ini;

import java.io.*;
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
import com.lucky.jacklamb.expression.$Expression;
import com.lucky.jacklamb.expression.ExpressionEngine;
import com.lucky.jacklamb.ioc.config.ScanConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;
import com.lucky.jacklamb.ioc.config.ServiceConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;
import com.lucky.jacklamb.redis.JedisFactory;

import static com.lucky.jacklamb.start.RunParam.*;

import static com.lucky.jacklamb.sqlcore.datasource.SectionKey.*;

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
		try {
			iniMap=new HashMap<>();
			String runIni = System.getProperty(LUCKY_CONFIG_LOCATION);
			if(runIni!=null){
				iniInputStream=new FileInputStream(runIni);
			}else {
				File dirFile=new File(System.getProperty("user.dir")+File.separator+"appconfig.ini");
				if(dirFile.exists()){
					iniInputStream=new FileInputStream(dirFile);
				}else{
					iniInputStream=IniFilePars.class.getClassLoader().getResourceAsStream("appconfig.ini");
				}
			}
			iniName="appconfig.ini";
			if(iniMap.isEmpty())
				pars();
		}catch(ArrayIndexOutOfBoundsException | UnsupportedEncodingException e) {
			throw new RuntimeException(iniName+"配置文件内容格式不正确",e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(iniName+"配置文件找不到！",e);
		}
	}
	
	public IniFilePars(String iniFilePath) {
		iniMap=new HashMap<>();
		iniInputStream=IniFilePars.class.getClassLoader().getResourceAsStream(iniFilePath);
		iniName=iniFilePath;
		try {
			if(iniMap.isEmpty())
				pars();
		}catch(ArrayIndexOutOfBoundsException | UnsupportedEncodingException e) {
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
	
	private void pars() throws UnsupportedEncodingException {
		if(iniInputStream!=null) {
			InputStreamReader isr = new InputStreamReader(iniInputStream,"UTF-8");
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
	
	public void modifyAllocation(ScanConfig scan, WebConfig web, ServerConfig server, ServiceConfig service) {
		if(iniMap.isEmpty())
			return;
		if(iniMap.containsKey("Redis")){
			JedisFactory.initJedisPool();
		}
		Map<String, String> sectionMap;
		if(this.iniMap.containsKey(SECTION_SERVICE)){
			sectionMap=this.getSectionMap(SECTION_SERVICE);
			if(sectionMap.containsKey("serviceName")){
				service.setServiceName($Expression.translation(sectionMap.get("serviceName")));
			}
			if(sectionMap.containsKey("isRegistrycenter")){
				service.setRegistrycenter($Expression.translation(sectionMap.get("isRegistrycenter"),boolean.class));
			}
			if(sectionMap.containsKey("hostName")){
				service.setHostName($Expression.translation(sectionMap.get("hostName")));
			}
			if(sectionMap.containsKey("serviceUrl")){
				service.setServiceUrl($Expression.translation(sectionMap.get("serviceUrl")));
			}
		}
		if(this.isHasSection(SECTION_APP)){
			sectionMap=this.getSectionMap(SECTION_APP);
			scan.setApp(sectionMap);
		}
		if(this.isHasSection(SECTION_SUFFIX_SCAN)) {
			sectionMap = this.getSectionMap(SECTION_SUFFIX_SCAN);
			scan.setScanMode(Scan.SUFFIX_SCAN);
			setScanConfig(scan,sectionMap);
		}
		if(this.isHasSection(SECTION_MAPPER_XML)) {
			scan.setMapperXmlPath($Expression.translation(this.getSectionMap(SECTION_MAPPER_XML).get("path")));
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
		if(this.isHasSection(SECTION_HANDLER_PREFIX_AND_SUFFIX)) {
			sectionMap = this.getSectionMap(SECTION_HANDLER_PREFIX_AND_SUFFIX);
			addPrefixAndSuffix(web,sectionMap);
		}
		if(this.isHasSection(SECTION_STATIC_HANDLER)) {
			sectionMap = this.getSectionMap(SECTION_STATIC_HANDLER);
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
			resAndIpsMap.put(e.getKey(), Arrays.asList($Expression.translation(e.getValue()).trim().split(",")));
		}
		web.setSpecifiResourcesIpRestrict(resAndIpsMap);
	}
	
	private void addStaticHander(WebConfig web,Map<String, String> sectionMap) {
		for(Entry<String,String> e:sectionMap.entrySet()) {
			web.addStaticHander(e.getKey(), $Expression.translation(e.getValue()));
		}
	}
	
	private void addPrefixAndSuffix(WebConfig web,Map<String, String> sectionMap) {
		String p="",s="";
		if(sectionMap.containsKey("prefix"))
			p=$Expression.translation(sectionMap.get("prefix"));
		if(sectionMap.containsKey("suffix"))
			s=$Expression.translation(sectionMap.get("suffix"));
		web.setHanderPrefixAndSuffix(p, s);
	}
	
	private void webSetting(WebConfig web,Map<String, String> sectionMap) {
		if(sectionMap.containsKey("multipartMaxFileSize")){
			web.setMultipartMaxFileSize(Long.parseLong(ExpressionEngine.calculate(sectionMap.get("multipartMaxFileSize"))));
		}
		if(sectionMap.containsKey("multipartMaxRequestSize")){
			web.setMultipartMaxRequestSize(Long.parseLong(ExpressionEngine.calculate(sectionMap.get("multipartMaxRequestSize"))));
		}
		if(sectionMap.containsKey("webRoot")){
			web.setWebRoot(sectionMap.get("webRoot"));
		}
		if(sectionMap.containsKey("httpClient-connectTimeout")){
			web.setConnectTimeout($Expression.translation(sectionMap.get("httpClient-connectTimeout"),int.class));
		}
		if(sectionMap.containsKey("httpClient-connectionRequestTimeout")){
			web.setConnectionRequestTimeout($Expression.translation(sectionMap.get("httpClient-connectionRequestTimeout"),int.class));
		}
		if(sectionMap.containsKey("httpClient-socketTimeout")){
			web.setSocketTimeout($Expression.translation(sectionMap.get("httpClient-socketTimeout"),int.class));
		}
		if(sectionMap.containsKey("encoding")) {
			web.setEncoding($Expression.translation(sectionMap.get("encoding")));
		}
		if(sectionMap.containsKey("openStaticResourceManage")) {
			web.openStaticResourceManage($Expression.translation(sectionMap.get("openStaticResourceManage"),boolean.class));
		}
		if(sectionMap.containsKey("postChangeMethod")) {
			web.postChangeMethod($Expression.translation(sectionMap.get("postChangeMethod"),boolean.class));
		}
		if(sectionMap.containsKey("globalResourcesIpRestrict")) {
			web.addGlobalResourcesIpRestrict($Expression.translation(sectionMap.get("globalResourcesIpRestrict")).trim().split(","));
		}
		if(sectionMap.containsKey("staticResourcesIpRestrict")) {
			web.addStaticResourcesIpRestrict($Expression.translation(sectionMap.get("staticResourcesIpRestrict")).trim().split(","));
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
		Map<String, String> loadOnStartUpMap = this.getSectionMap(SECTION_SERVLET_LOADONSTARTUP);
		for(String servletName:servletNames) {
			if(!this.isHasKey(SECTION_SERVLET_MAPPING, servletName))
				throw new RuntimeException("appconfig.ini配置文件中有Servlet没有配置请求映射：[Servlet]->"+servletName+"="+sectionMap.get(servletName));
			try {
				if(loadOnStartUpMap!=null){
					Integer loadOnStartUp=loadOnStartUpMap.containsKey(servletName)?Integer.parseInt(loadOnStartUpMap.get(servletName)):-1;
					server.addServlet((HttpServlet)Class.forName(sectionMap.get(servletName)).newInstance(), loadOnStartUp,this.getValue(SECTION_SERVLET_MAPPING, servletName).split(","));
				}else{
					server.addServlet((HttpServlet)Class.forName(sectionMap.get(servletName)).newInstance(), this.getValue(SECTION_SERVLET_MAPPING, servletName).split(","));
				}
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
			server.setPort($Expression.translation(sectionMap.get("port"),int.class));
		}
		if(sectionMap.containsKey("sessionTimeout")) {
			server.setSessionTimeout($Expression.translation(sectionMap.get("sessionTimeout"),int.class));
		}
		if(sectionMap.containsKey("closePort")) {
			server.setClosePort($Expression.translation(sectionMap.get("closePort"),int.class));
		}
		if(sectionMap.containsKey("shutdown")) {
			server.setShutdown($Expression.translation(sectionMap.get("shutdown")));
		}
		if(sectionMap.containsKey("docBase")) {
			server.setDocBase(sectionMap.get("docBase"));
		}
		if(sectionMap.containsKey("baseDir")) {
			server.setBaseDir(sectionMap.get("baseDir"));
		}
		if(sectionMap.containsKey("contextPath")) {
			server.setContextPath($Expression.translation(sectionMap.get("contextPath")));
		}
		if(sectionMap.containsKey("webapp")) {
			server.setWebapp($Expression.translation(sectionMap.get("webapp")));
		}
		if(sectionMap.containsKey("url-encoding")) {
			server.setURIEncoding($Expression.translation(sectionMap.get("url-encoding")));
		}
		if(sectionMap.containsKey("autoDeploy")) {
			server.setAutoDeploy($Expression.translation(sectionMap.get("autoDeploy"),boolean.class));
		}
		if(sectionMap.containsKey("reloadable")) {
			server.setReloadable($Expression.translation(sectionMap.get("reloadable"),boolean.class));
		}
		if(sectionMap.containsKey("requestTargetAllow")){
			server.setRequestTargetAllow($Expression.translation(sectionMap.get("requestTargetAllow")));
		}
	}
	
	private void setScanConfig(ScanConfig scan,Map<String, String> sectionMap) {
		String suffixStr;
		if(sectionMap.containsKey("controller")) {
			suffixStr= $Expression.translation(sectionMap.get("controller"));
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddControllerPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addControllerPackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("service")) {
			suffixStr=$Expression.translation(sectionMap.get("service"));
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddServicePackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addServicePackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("repository")) {
			suffixStr=$Expression.translation(sectionMap.get("repository"));
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddRepositoryPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addRepositoryPackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("aspect")) {
			suffixStr=$Expression.translation(sectionMap.get("aspect"));
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddAspectPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addAspectPackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("component")) {
			suffixStr=$Expression.translation(sectionMap.get("component"));
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddComponentPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addComponentPackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("pojo")) {
			suffixStr=$Expression.translation(sectionMap.get("pojo"));
			if(suffixStr.startsWith("reset:")) {
				scan.emptyAddPojoPackSuffix(suffixStr.substring(6).trim().split(","));
			}else {
				scan.addPojoPackSuffix(suffixStr.trim().split(","));
			}
		}
		if(sectionMap.containsKey("websocket")) {
			suffixStr=$Expression.translation(sectionMap.get("websocket"));
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


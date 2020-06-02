package com.lucky.jacklamb.ioc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.servlet.Model;
import com.lucky.jacklamb.utils.Jacklabm;

public class URLAndRequestMethod {
	
	private String url;
	
	private Set<RequestMethod> methods;
	
	public URLAndRequestMethod() {
		methods=new HashSet<>();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public Set<RequestMethod> getMethods() {
		return methods;
	}

	public void addMethods(RequestMethod[] methods) {
		Stream.of(methods).forEach(this.methods::add);
	}
	
	public void addMethod(RequestMethod method) {
		this.methods.add(method);
	}

	
	public boolean myEquals(URLAndRequestMethod uRLAndRequestMethod) {
		if(uRLAndRequestMethod==null)
			return false;
		if(!uRLAndRequestMethod.getUrl().equals(url))
			return false;
		for(RequestMethod urmRM:uRLAndRequestMethod.getMethods()) {
			if(methods.contains(urmRM))
				return true;
		}
		return false;
	}
	
	public URLAndRequestMethod findUrl(Model model,List<URLAndRequestMethod> urlList) throws IOException {
		boolean isPass=false;
		URLAndRequestMethod tempURLAndRequestMethod = null;
		for(URLAndRequestMethod temp:urlList) {
			if(isConform(temp.getUrl(),url)) {
				isPass=true;
				tempURLAndRequestMethod=temp;
				break;
			}
		}
		if(!isPass) {
			model.error(Code.NOTFOUND,"不正确的url："+url, "找不与请求相匹配的映射资,请检查您的URL是否正确！");
			return null;
		}
		for(RequestMethod currmethod : methods)
			if(!tempURLAndRequestMethod.getMethods().contains(currmethod)) {
				model.error(Code.REFUSED,"不合法的请求类型"+this.methods,"您的请求类型"+this.methods+" , 当前方法并不支持！");
				return null;
			}
		return tempURLAndRequestMethod;
	}
	
	/**
	 * 判断当前传入的url是否符合容器中的某一个映射
	 * @param mapstr
	 * @param currurl
	 * @return
	 */
	public boolean isConform(String mapstr,String currurl) {
		String[] mapArray=participle(mapstr);
		String[] urlArray=participle(currurl);
		if(mapstr.endsWith("}*")&&urlArray.length>=mapArray.length){
			if("lucyxfl".equals(urlArray[0]))
				return  false;
			for(int i=0;i<mapArray.length-1;i++){
				if(!mapArray[i].endsWith("}")&&!mapArray[i].startsWith("#{")){
					if(!mapArray[i].equals(urlArray[i]))
						return false;
				}
			}
			return true;
		}
		if(mapArray.length!=urlArray.length)
			return false;
		boolean rest=true;
		for(int i=0;i<mapArray.length;i++)
			rest=rest&&wordVerification(mapArray[i],urlArray[i]);
		return rest;
	}

	/**
	 * 单词汇校验(判断单词是否符合模板)
	 * @param template 模板
	 * @param word 单词
	 * @return
	 */
	public boolean wordVerification(String template,String word) {
		if(template.startsWith("[")&&template.endsWith("]")) {//[candidate1,candidate2,candidate3]//候选词匹配，匹配其中一个即可
			String[] split = template.substring(1, template.length()-1).trim().split(",");
			return Arrays.asList(split).contains(word);
		}
		if(template.startsWith("![")&&template.endsWith("]")) {//![candidate1,candidate2,candidate3]//候选词反向匹配，匹配其中任意一个都不行
			String[] split = template.substring(2, template.length()-1).trim().split(",");
			return !Arrays.asList(split).contains(word);
		}
		if(template.startsWith("*"))//word必须以template结尾
			return word.endsWith(template.substring(1));
		if(template.endsWith("*"))//word必须以template开始
			return word.startsWith(template.substring(0,template.length()-1));
		if("?".equals(template)||(template.startsWith("#{")&&template.endsWith("}")))//参数项，任意word都匹配
			return true;
		return template.equals(word);//没有特殊符号，表示word必须为template
	}
	
	private String[] participle(String url) {
		String[] split = url.split("/");
		List<String> list=new ArrayList<>();
		Stream.of(split).filter(a->!"".equals(a)).forEach(list::add);
		String[] rest=new String[list.size()];
		list.toArray(rest);
		return rest;
	}

}

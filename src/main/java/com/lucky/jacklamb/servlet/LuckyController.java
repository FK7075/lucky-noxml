package com.lucky.jacklamb.servlet;

import com.google.gson.reflect.TypeToken;
import com.lucky.jacklamb.annotation.ioc.Autowired;
import com.lucky.jacklamb.file.utils.FileCopyUtils;
import com.lucky.jacklamb.ioc.ComponentIOC;
import com.lucky.jacklamb.ioc.RepositoryIOC;
import com.lucky.jacklamb.ioc.ServiceIOC;
import com.lucky.jacklamb.md5.MD5Utils;
import com.lucky.jacklamb.rest.LSON;
import com.lucky.jacklamb.servlet.core.Model;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public abstract class LuckyController {

	@Autowired
	protected ServiceIOC serviceIOC;

	@Autowired
	protected RepositoryIOC repositoryIOC;

	@Autowired
	protected ComponentIOC componentIOC;
	
	protected Model model;
	
	protected HttpServletRequest request;
	
	protected HttpServletResponse response;
	
	protected HttpSession session;

	protected ServletContext application;

	protected static LSON lson=new LSON();

	/**
	 * 文件下载
	 * @param byteArray byte[]
	 * @param setDownloadName 客户端显示的文件名
	 * @throws IOException
	 */
	protected void download(byte[] byteArray,String setDownloadName) throws IOException {
		FileCopyUtils.download(response,byteArray,setDownloadName);
	}

	/**
	 * 文件下载
	 * @param in File对象(文件)
	 * @throws IOException
	 */
	protected void download(File in) throws IOException {
		FileCopyUtils.download(response,in);
	}

	/**
	 * 文件下载
	 * @param in InputStream
	 * @param setDownloadName 客户端得显示的文件名
	 * @throws IOException
	 */
	protected void download(InputStream in,String setDownloadName) throws IOException {
		FileCopyUtils.download(response,in,setDownloadName);
	}

	/**
	 * 文件预览
	 * @param byteArray byte[]
	 * @throws IOException
	 */
	protected void preview(byte[] byteArray,String fileName) throws IOException {
		FileCopyUtils.preview(model,byteArray,fileName);
	}

	/**
	 * 文件预览
	 * @param in File对象(文件)
	 * @throws IOException
	 */
	protected void preview(File in) throws IOException {
		FileCopyUtils.preview(model,in);
	}

	/**
	 * 文件预览
	 * @param in InputStream
	 * @throws IOException
	 */
	protected void preview(InputStream in,String fileName) throws IOException {
		FileCopyUtils.preview(model,in,fileName);
	}

	/**
	 * 将对象转化为JSON字符串
	 * @param pojo
	 * @return
	 */
	protected String toJson(Object pojo){
		return lson.toJsonByGson(pojo);
	}

	/**
	 * 将JSON字符串转化为Java对象
	 * @param targetClass
	 * @param jsonString
	 * @param <T>
	 * @return
	 */
	protected <T> T fromJson(Class<T> targetClass,String jsonString){
		return lson.toObject(targetClass,jsonString);
	}

	/**
	 * 将JSON字符串转化为Java对象
	 * @param token
	 * @param jsonString
	 * @return
	 */
	protected Object fromJson(TypeToken token,String jsonString){
		return lson.toObject(token,jsonString);
	}

	/**
	 * 将JSON字符串转化为Java对象
	 * @param type
	 * @param jsonString
	 * @return
	 */
	protected Object fromJson(Type type, String jsonString){
		return lson.toObject(type,jsonString);
	}

	/**
	 * MD5加密
	 * @param clear 明文
	 * @return 密文
	 */
	protected String md5(String clear){
		return MD5Utils.md5(clear);
	}

	/**
	 * MD5加密
	 * @param clear 明文
	 * @param salt 盐
	 * @param cycle 循环加密的次数
	 * @return
	 */
	protected String md5(String clear,String salt,int cycle){
		return MD5Utils.md5(clear,salt,cycle);
	}
}

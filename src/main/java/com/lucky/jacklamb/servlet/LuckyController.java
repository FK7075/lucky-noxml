package com.lucky.jacklamb.servlet;

import com.lucky.jacklamb.annotation.ioc.Autowired;
import com.lucky.jacklamb.file.utils.FileCopyUtils;
import com.lucky.jacklamb.ioc.ComponentIOC;
import com.lucky.jacklamb.ioc.RepositoryIOC;
import com.lucky.jacklamb.ioc.ServiceIOC;
import com.lucky.jacklamb.rest.LSON;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

	protected LSON lson=new LSON();

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

}

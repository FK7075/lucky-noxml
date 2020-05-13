package com.lucky.jacklamb.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lucky.jacklamb.conversion.util.FieldUtils;
import com.lucky.jacklamb.file.utils.FileCopyUtils;
import com.lucky.jacklamb.ioc.config.WebConfig;

public class StaticResourceManage {
	
	public static boolean isLegalIp(WebConfig webCfg,String currIp) {
		if(!webCfg.getStaticResourcesIpRestrict().isEmpty()&&!webCfg.getStaticResourcesIpRestrict().contains(currIp))
			return false;
		return true;
	}
	
	public static boolean isLegalRequest(WebConfig webCfg,String currIp,HttpServletResponse resp,String uri) {
		return isLegalIp(webCfg,currIp)&&isStaticResource(resp,uri);
	}
	
	public static boolean isStaticResource(HttpServletResponse resp,String uri) {
		String lowercaseUri=uri.toLowerCase();
		if(lowercaseUri.endsWith(".jpg")||lowercaseUri.endsWith(".jfif")||lowercaseUri.endsWith(".jpe")||lowercaseUri.endsWith(".jpeg")) {
			resp.setContentType("image/jpeg");
			return true;
		}else if(lowercaseUri.endsWith(".png")) {
			resp.setContentType("image/png");
			return true;
		}else if(lowercaseUri.endsWith(".ico")) {
			resp.setContentType("image/x-icon");
			return true;
		}else if(lowercaseUri.endsWith(".gif")) {
			resp.setContentType("image/gif");
			return true;
		}else if(lowercaseUri.endsWith(".tif")||lowercaseUri.endsWith(".tiff")) {
			resp.setContentType("image/tiff");
			return true;
		}else if(lowercaseUri.endsWith(".pdf")) {
			resp.setContentType("application/pdf");
			return true;
		}else if(lowercaseUri.endsWith(".js")) {
			resp.setContentType("application/javascript");
			return true;
		}else if(lowercaseUri.endsWith(".css")||lowercaseUri.endsWith(".css.map")) {
			resp.setContentType("text/css");
			return true;
		}else if(lowercaseUri.endsWith(".xml")||lowercaseUri.endsWith(".xsl")||lowercaseUri.endsWith(".xquery")||lowercaseUri.endsWith(".svg")) {
			resp.setContentType("text/xml");
			return true;
		}else if(lowercaseUri.endsWith(".html")||lowercaseUri.endsWith(".htm")) {
			resp.setContentType("text/html");
			return true;
		}else if(lowercaseUri.endsWith(".txt")||lowercaseUri.endsWith(".woff2")||lowercaseUri.endsWith(".woff")||lowercaseUri.endsWith(".ttf")) {
			resp.setContentType("text/plain");
			return true;
		}else if(lowercaseUri.endsWith(".doc")||lowercaseUri.endsWith(".docx")) {
			resp.setContentType("application/msword");
			return true;
		}else if(lowercaseUri.endsWith(".xls")||lowercaseUri.endsWith(".xlsx")) {
			resp.setContentType("application/x-xls");
			return true;
		}else if(lowercaseUri.endsWith(".avi")) {
			resp.setContentType("video/avi");
			return true;
		}else if(lowercaseUri.endsWith(".rmvb")) {
			resp.setContentType("application/vnd.rn-realmedia-vbr");
			return true;
		}else if(lowercaseUri.endsWith(".rm")) {
			resp.setContentType("application/vnd.rn-realmedia");
			return true;
		}else if(lowercaseUri.endsWith(".mp4")) {
			resp.setContentType("audio/mp4");
			return true;
		}else if(lowercaseUri.endsWith(".aiff")) {
			resp.setContentType("audio/aiff");
			return true;
		}else if(lowercaseUri.endsWith(".mp3")) {
			resp.setContentType("audio/mp3");
			return true;
		}else if(lowercaseUri.endsWith(".wma")) {
			resp.setContentType("audio/x-ms-wma");
			return true;
		}else {
			return false;
		}
	}

	public static void response(HttpServletRequest request,HttpServletResponse response,String uri) throws IOException {
		String realPath = request.getServletContext().getRealPath(uri);
		if(realPath!=null) {
			File targetFile=new File(realPath);
			FileCopyUtils.copyToServletOutputStream(response,targetFile);
		}
	}
}

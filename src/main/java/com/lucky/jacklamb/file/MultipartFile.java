package com.lucky.jacklamb.file;

import com.lucky.jacklamb.file.utils.FileCopyUtils;
import com.lucky.jacklamb.utils.base.LuckyUtils;

import java.io.*;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

public class MultipartFile {
	
	private InputStream originalFileInpueStream;//用户上传的文件对应的输入流
	private String uploadFileName;//文件上传到服务器后的文件名
	private String fileType;//文件类型
	private String projectPath;//项目的路径
	private String originalFileName;//原始的文件名
	
	/**
	 * 
	 * @param originalFileInpueStream
	 * @param projectPath
	 * @param filename
	 */
	public MultipartFile(InputStream originalFileInpueStream,String projectPath,String filename) {
		this.originalFileInpueStream=originalFileInpueStream;
		this.originalFileName=filename;
		this.fileType=filename.substring(filename.lastIndexOf("."));;
		this.uploadFileName=originalFileName.replaceAll(fileType,"")+"_"+new Date().getTime()+"_"+ LuckyUtils.getRandomNumber() +getFileType();
		this.projectPath=projectPath;
	}

	/**
	 * 获得上传文件的类型
	 * @return
	 */
	public String getFileType() {
		return this.fileType;
	}
	
	/**
	 * 获得上传到服务器后的文件名
	 * @return
	 */
	public String getFileName() {
		return uploadFileName;
	}

	/**
	 *获得文件的原始文件名
	 * @return
	 */
	public String getOriginalFileName() {
		return originalFileName;
	}

	/**
	 * 设置上传后的文件名
	 * @param noSuffixFileName
	 */
	public void setFileName(String noSuffixFileName){
		uploadFileName=noSuffixFileName+getFileType();
	}
	
	/**
	 * 将文件复制到服务器上的DocBase中的文件夹中
	 * @param docRelativePath 填写以服务器文件目录为根目录的相对路径
	 * @throws IOException
	 */
	public void copyToDocBaseFolder(String docRelativePath) throws IOException {
		File file=new File(projectPath+"/"+docRelativePath);
		copyToFolder(file);
	}
	
	/**
	 * 将文件复制到系统的任意位置上文件夹中
	 * @param filepath 绝对路径
	 * @throws IOException
	 */
	public void copyToFolder(String filepath) throws IOException {
		File file=new File(filepath);
		copyToFolder(file);
	}
	
	private void copyToFolder(File folder) throws IOException {
		if(folder.isFile()){
			throw new RuntimeException("文件上传错误！ Message : 错误的文件夹："+folder.getAbsolutePath());
		}
		if(!folder.exists())
			folder.mkdirs();
		FileOutputStream outfile=new FileOutputStream(folder.getAbsoluteFile()+File.separator+uploadFileName);//projectPath+"/"+docRelativePath+"/"+uploadFileName);
		FileCopyUtils.copy(originalFileInpueStream,outfile);
	}

	
	/**
	 * 文件下载
	 * @param response Response对象
	 * @param file 要下载的文件
	 * @throws IOException
	 */
	public static void downloadFile(HttpServletResponse response,File file) throws IOException {
		if(!file.exists())
			throw new RuntimeException("找不到文件,无法完成下载操作！"+file.getAbsolutePath());
		InputStream bis = new BufferedInputStream(new FileInputStream(file));
        //转码，免得文件名中文乱码  
        String filename = URLEncoder.encode(file.getName(),"UTF-8");  
        //设置文件下载头  
        response.addHeader("Content-Disposition", "attachment;filename=" + filename);    
        //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型    
        response.setContentType("multipart/form-data");   
        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        FileCopyUtils.copy(bis,out);
	}
	
	/**
	 * 获得上传文件的大小
	 * @return
	 * @throws IOException 
	 */
	public int getFileSize() throws IOException {
		return originalFileInpueStream.available();
	}
	
	/**
	 * 获得文件对应的InputStream
	 * @return
	 */
	public InputStream getInputStream() {
		return originalFileInpueStream;
	}
	
	/**
	 * 关闭文件输入流
	 * @throws IOException
	 */
	public void close() throws IOException {
		if(originalFileInpueStream!=null)
			originalFileInpueStream.close();
	}
	
	 

}

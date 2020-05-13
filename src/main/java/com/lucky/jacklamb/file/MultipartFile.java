package com.lucky.jacklamb.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

public class MultipartFile {
	
	private InputStream originalFileInpueStream;//用户上传的文件对应的输入流
	private String uploadFileName;//文件上传到服务器后的文件名
	private String fileType;//文件类型
	private String projectPath;//项目的路径
	
	/**
	 * 
	 * @param part
	 * @param projectPath
	 */
	public MultipartFile(Part part,String projectPath){
		try {
			this.originalFileInpueStream=(FileInputStream) part.getInputStream();
			String disposition=part.getHeader("Content-Disposition");
			this.fileType=disposition.substring(disposition.lastIndexOf("."));
			this.uploadFileName=UUID.randomUUID().toString()+getFileType();
			this.projectPath=projectPath;
		} catch (ClassCastException | IOException e) {
			throw new RuntimeException("参数信息不正确，无法构造MultipartFile类实例！");
		}
	}
	
	/**
	 * 
	 * @param originalFileInpueStream
	 * @param projectPath
	 * @param fileType
	 */
	public MultipartFile(InputStream originalFileInpueStream,String projectPath,String fileType) {
		this.originalFileInpueStream=originalFileInpueStream;
		this.fileType=fileType;
		this.uploadFileName=UUID.randomUUID().toString()+getFileType();
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
	 * 将文件复制到服务器上的path下
	 * @param docRelativePath 填写以服务器文件目录为根目录的相对路径
	 * @throws IOException
	 */
	public void copyToDocRelativePath(String docRelativePath) throws IOException {
		File file=new File(projectPath+"/"+docRelativePath);
		copy(file);
	}
	
	/**
	 * 将文件复制到系统的任意位置上
	 * @param filepath 绝对路径
	 * @throws IOException
	 */
	public void copyToFilePath(String filepath) throws IOException {
		File file=new File(filepath);
		copy(file);
	}
	
	private void copy(File file) throws IOException {
		if(!file.isDirectory())
			file.mkdirs();
		FileOutputStream outfile=new FileOutputStream(file.getAbsoluteFile()+"/"+uploadFileName);//projectPath+"/"+docRelativePath+"/"+uploadFileName);
		byte[] buffer=new byte[1024*6];
		int length=0;
		while((length=originalFileInpueStream.read(buffer))!=-1) {
			outfile.write(buffer, 0, length);
			outfile.flush();
		}
		outfile.close();
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
		@SuppressWarnings("resource")
		InputStream bis = new BufferedInputStream(new FileInputStream(file));
        //转码，免得文件名中文乱码  
        String filename = URLEncoder.encode(file.getName(),"UTF-8");  
        //设置文件下载头  
        response.addHeader("Content-Disposition", "attachment;filename=" + filename);    
        //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型    
        response.setContentType("multipart/form-data");   
        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        int len = 0;
        byte[] bt = new byte[1024*6];
        while((len = bis.read(bt)) != -1){
            out.write(bt,0,len);
            out.flush();
        }
        out.close();
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

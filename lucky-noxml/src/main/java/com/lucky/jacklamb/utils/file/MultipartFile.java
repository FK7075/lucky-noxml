package com.lucky.jacklamb.utils.file;

import com.lucky.jacklamb.servlet.core.Model;
import com.lucky.jacklamb.utils.base.LuckyUtils;

import java.io.*;
import java.util.Date;

public class MultipartFile {
	
	private InputStream originalFileInputStream;//用户上传的文件对应的输入流
	private String uploadFileName;//文件上传到服务器后的文件名
	private String fileType;//文件类型
	private String originalFileName;//原始的文件名

	/**
	 *
	 * @param originalFileInputStream
	 * @param filename
	 */
	public MultipartFile(InputStream originalFileInputStream,String filename) {
		this.originalFileInputStream=originalFileInputStream;
		this.originalFileName=filename;
		this.fileType=filename.substring(filename.lastIndexOf("."));;
		this.uploadFileName=originalFileName.replaceAll(fileType,"")+"_"+new Date().getTime()+"_"+ LuckyUtils.getRandomNumber() +getFileType();
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
		File file=new File(new Model().getRealPath("/")+"/"+docRelativePath);
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
		FileUtils.copy(originalFileInputStream,outfile);
	}

	/**
	 * 获得上传文件的大小
	 * @return
	 * @throws IOException 
	 */
	public int getFileSize() throws IOException {
		return originalFileInputStream.available();
	}
	
	/**
	 * 获得文件对应的InputStream
	 * @return
	 */
	public InputStream getInputStream() {
		return originalFileInputStream;
	}

	/** 获得文件对应的byte数组 */
	public byte[] getByte() throws IOException {
		return FileUtils.copyToByteArray(originalFileInputStream);
	}

}

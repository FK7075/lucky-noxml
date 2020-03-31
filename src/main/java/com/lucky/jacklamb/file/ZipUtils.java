package com.lucky.jacklamb.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
 
/**
 * 压缩类型枚举
 * 
 * @author Log
 *
 */
enum CompressType {
//	GZIP是用于UNIX系统的文件压缩，在Linux中经常会使用到*.gz的文件，就是GZIP格式
	ZIP, JAR, GZIP
}
 
public class ZipUtils {
 
	public static void main(String[] args) {
//		compress(CompressType.ZIP);
		unZip("D:/solrJ-search.zip", "D:/solrJ-search-fk");
//		unZip("压缩.jar", "压缩后jar");
	}
 
	public static void compress(CompressType type) {
		if (type == CompressType.ZIP) {
			zip("压缩", "压缩.zip", CompressType.ZIP);
		} else if (type == CompressType.JAR) {
			zip("压缩", "压缩.jar", CompressType.JAR);
		}
	}
 
	public static void zip(String inputFile, String outputFile, CompressType type) {
		zip(new File(inputFile), new File(outputFile), type);
	}
 
	/**
	 * 初始化压缩包信息并开始进行压缩
	 * 
	 * @param inputFile  需要压缩的文件或文件夹
	 * @param outputFile 压缩后的文件
	 * @param type       压缩类型
	 */
	public static void zip(File inputFile, File outputFile, CompressType type) {
		ZipOutputStream zos = null;
		try {
			if (type == CompressType.ZIP) {
				zos = new ZipOutputStream(new FileOutputStream(outputFile));
			} else if (type == CompressType.JAR) {
				zos = new JarOutputStream(new FileOutputStream(outputFile));
			} else {
				zos = new ZipOutputStream(new FileOutputStream(outputFile));
			}
			// 设置压缩包注释
			zos.setComment("From Log");
			zipFile(zos, inputFile, null);
			System.err.println("压缩完成!");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("压缩失败!");
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
 
	/**
	 * 如果是单个文件，那么就直接进行压缩。如果是文件夹，那么递归压缩所有文件夹里的文件
	 * 
	 * @param zos       压缩输出流
	 * @param inputFile 需要压缩的文件
	 * @param path      需要压缩的文件在压缩包里的路径
	 */
	public static void zipFile(ZipOutputStream zos, File inputFile, String path) {
		if (inputFile.isDirectory()) {
			// 记录压缩包中文件的全路径
			String p = null;
			File[] fileList = inputFile.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				File file = fileList[i];
				// 如果路径为空，说明是根目录
				if (path == null || path.isEmpty()) {
					p = file.getName();
				} else {
					p = path + File.separator + file.getName();
				}
				// 打印路径
				System.out.println(p);
				// 如果是目录递归调用，直到遇到文件为止
				zipFile(zos, file, p);
			}
		} else {
			zipSingleFile(zos, inputFile, path);
		}
	}
 
	/**
	 * 压缩单个文件到指定压缩流里
	 * 
	 * @param zos       压缩输出流
	 * @param inputFile 需要压缩的文件
	 * @param path      需要压缩的文件在压缩包里的路径
	 * @throws FileNotFoundException
	 */
	public static void zipSingleFile(ZipOutputStream zos, File inputFile, String path) {
		try {
			InputStream in = new FileInputStream(inputFile);
			zos.putNextEntry(new ZipEntry(path));
			write(in, zos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
	/**
	 * 解压压缩包到指定目录
	 * 
	 * @param inputFile
	 * @param outputFile
	 */
	public static void unZip(String inputFile, String outputFile) {
		unZip(new File(inputFile), new File(outputFile));
	}
 
	/**
	 * 解压压缩包到指定目录
	 * 
	 * @param inputFile
	 * @param outputFile
	 */
	public static void unZip(File inputFile, File outputFile) {
		if (!outputFile.exists()) {
			outputFile.mkdirs();
		}
 
		ZipFile zipFile = null;
		ZipInputStream zipInput = null;
		ZipEntry entry = null;
		OutputStream output = null;
		InputStream input = null;
		File file = null;
		try {
			zipFile = new ZipFile(inputFile);
			zipInput = new ZipInputStream(new FileInputStream(inputFile));
			String path = outputFile.getAbsolutePath() + File.separator;
			while ((entry = zipInput.getNextEntry()) != null) {
				// 从压缩文件里获取指定已压缩文件的输入流
				input = zipFile.getInputStream(entry);
 
				// 拼装压缩后真实文件路径
				String fileName = path + entry.getName();
				System.out.println(fileName);
 
				// 创建文件缺失的目录（不然会报异常：找不到指定文件）
				file = new File(fileName.substring(0, fileName.lastIndexOf(File.separator)));
				file.mkdirs();
 
				// 创建文件输出流
				output = new FileOutputStream(fileName);
 
				// 写出解压后文件数据
				write(input, output);
				output.close();
			}
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.close();
				}
 
				if (zipInput != null) {
					zipInput.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	/**
	 * 从输入流写入到输出流的方便方法 【注意】这个函数只会关闭输入流，且读写完成后会调用输出流的flush()函数，但不会关闭输出流！
	 * 
	 * @param input
	 * @param output
	 */
	private static void write(InputStream input, OutputStream output) {
		int len = -1;
		byte[] buff = new byte[1024];
		try {
			while ((len = input.read(buff)) != -1) {
				output.write(buff, 0, len);
			}
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
}
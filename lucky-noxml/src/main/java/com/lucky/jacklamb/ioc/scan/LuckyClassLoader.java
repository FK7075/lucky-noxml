package com.lucky.jacklamb.ioc.scan;

import com.lucky.jacklamb.utils.file.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 类加载器
 * @author fk-7075
 *
 */
public class LuckyClassLoader extends ClassLoader {

	private String filePath;

	public LuckyClassLoader(String path) {
		this.filePath = path;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] b = loadClassData(name);
		return defineClass(name, b, 0, b.length);
	}

	private byte[] loadClassData(String name) {
		name=name.replaceAll("\\.", "/")+".class";
		if(filePath.endsWith("/")||filePath.endsWith("\\"))
			name=filePath+name;
		else
			name=filePath+File.separator+name;
		InputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = new FileInputStream(new File(name));
			out = new ByteArrayOutputStream();
			FileUtils.copy(in,out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();

	}

}

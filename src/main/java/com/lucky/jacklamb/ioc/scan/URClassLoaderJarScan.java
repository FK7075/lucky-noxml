package com.lucky.jacklamb.ioc.scan;

import java.io.File;

import com.lucky.jacklamb.file.ini.INIConfig;

/**
 * 当ClassLoader为URLClassLoader时的包扫描
 * @author fk-7075
 *
 */
public class URClassLoaderJarScan extends JarScan {

	public URClassLoaderJarScan(Class<?> clzz) {
		super(clzz);
		INIConfig ini=new INIConfig();
		jarpath=System.getProperty("user.dir")+File.separator+ini.getValue("Jar", "name");
	}
	


}

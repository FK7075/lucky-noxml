package com.lucky.jacklamb.ioc.scan;

import com.lucky.jacklamb.ioc.config.AppConfig;

import java.net.URISyntaxException;

public class ScanFactory {
	
	private static PackageScan pack;
	
	private static JarScan jar;


	public static Scan createScan(){
		if(PackageScan.class.getClassLoader().getResource("")==null) {
			if(jar==null) {
				try {
					jar= new JarScan(AppConfig.applicationClass);
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
				jar.init();
			}
			return jar;	
		}else {
			if("java.net.URLClassLoader".equals(PackageScan.class.getClassLoader().getClass().getName())) {
				if(jar==null) {
					try {
						jar= new URClassLoaderJarScan(AppConfig.applicationClass);
					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
					jar.init();
					return jar;
				}
				return jar;
			}
			if(pack==null) {
				try {
					pack= new PackageScan();
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
				pack.init();
			}
			return pack;
		}
	}

}

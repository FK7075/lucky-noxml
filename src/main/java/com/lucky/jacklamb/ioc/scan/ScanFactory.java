package com.lucky.jacklamb.ioc.scan;

import com.lucky.jacklamb.ioc.config.AppConfig;

public class ScanFactory {
	
	private static PackageScan pack;
	
	private static JarScan jar;
	
	public static Scan createScan() {
		if(PackageScan.class.getClassLoader().getResource("")==null) {
			if(jar==null) {
				jar= new JarScan(AppConfig.applicationClass);
				jar.init();
			}
			return jar;	
		}else {
			if("java.net.URLClassLoader".equals(PackageScan.class.getClassLoader().getClass().getName())) {
				if(jar==null) {
					jar= new URClassLoaderJarScan(AppConfig.applicationClass);
					jar.init();
					return jar;
				}
				return jar;
			}
			if(pack==null) {
				pack= new PackageScan();
				pack.init();
			}
			return pack;
		}
	}

}

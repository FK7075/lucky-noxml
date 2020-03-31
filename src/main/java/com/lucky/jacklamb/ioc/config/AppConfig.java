package com.lucky.jacklamb.ioc.config;

import com.lucky.jacklamb.file.ini.IniFilePars;
import com.lucky.jacklamb.ioc.scan.ScanFactory;
import com.lucky.jacklamb.utils.LuckyUtils;

public class AppConfig {

	private static AppConfig appConfig;

	private ScanConfig scancfg;

	private WebConfig webcfg;

	private ServerConfig servercfg;
	
	public static Class<?> applicationClass;

	private static boolean isFirst = true;

	private AppConfig() {
		scancfg = ScanConfig.defaultScanConfig();
		webcfg = WebConfig.defauleWebConfig();
		servercfg = ServerConfig.defaultServerConfig();
		ApplicationConfig appconfig = ScanFactory.createScan().getApplicationConfig();
		if(isFirst) {
			if (appconfig != null) {
				System.err.println(LuckyUtils.showtime() + "[HELPFUL HINTS]  发现配置类" + appconfig.getClass().getName()
						+ "，将使用类中的配置初始化LUCKY...");
				appconfig.init(scancfg, webcfg, servercfg);
			}
			new IniFilePars().modifyAllocation(scancfg, webcfg, servercfg);
			isFirst = false;
		}
	}

	public static AppConfig getAppConfig() {
		if (appConfig == null)
			appConfig = new AppConfig();
		return appConfig;
	}

	public ScanConfig getScanConfig() {
		return scancfg;
	}

	public WebConfig getWebConfig() {
		return webcfg;
	}

	public ServerConfig getServerConfig() {
		return servercfg;
	}
	
	/**
	 * 得到默认状态下的WebConfig
	 * @return
	 */
	public static WebConfig getDefaultWebConfig() {
		return WebConfig.defauleWebConfig();
	}
	
	/**
	 * 得到默认状态下的ScanConfig
	 * @return
	 */
	public static ScanConfig getDefaultScanConfig() {
		return ScanConfig.defaultScanConfig();
	}
	
	/**
	 * 得到默认状态下的ServerConfig
	 * @return
	 */
	public static ServerConfig getDefaultServerConfig() {
		return ServerConfig.defaultServerConfig();
	}

}

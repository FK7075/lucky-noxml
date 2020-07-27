package com.lucky.jacklamb.ioc.config;

import com.lucky.jacklamb.file.ini.IniFilePars;
import com.lucky.jacklamb.ioc.scan.ScanFactory;
import com.lucky.jacklamb.utils.base.LuckyUtils;

public class AppConfig {

	private static AppConfig appConfig;

	private ScanConfig scancfg;

	private WebConfig webcfg;

	private ServerConfig servercfg;

	private ServiceConfig servicecfg;
	
	public static Class<?> applicationClass;

	private static boolean isFirst = true;

	private AppConfig() {
		scancfg = ScanConfig.defaultScanConfig();
		webcfg = WebConfig.defauleWebConfig();
		servercfg = ServerConfig.defaultServerConfig();
		servicecfg=ServiceConfig.defaultServiceConfig();
		ApplicationConfig appconfig = ScanFactory.createScan().getApplicationConfig();
		if(isFirst) {
			if (appconfig != null) {
				System.err.println(LuckyUtils.showtime() + "[HELPFUL HINTS]  发现配置类" + appconfig.getClass().getName()
						+ "，将使用类中的配置初始化LUCKY...");
				appconfig.init(scancfg, webcfg, servercfg,servicecfg);
			}
			new IniFilePars().modifyAllocation(scancfg, webcfg, servercfg,servicecfg);
			isFirst = false;
		}
	}

	public static AppConfig getAppConfig() {
		if (appConfig == null)
			appConfig = new AppConfig();
		return appConfig;
	}

	/**
	 * 得到当前状态下的ScanConfig
	 * @return
	 */
	public ScanConfig getScanConfig() {
		return scancfg;
	}

	/**
	 * 得到当前状态下的WebConfig
	 * @return
	 */
	public WebConfig getWebConfig() {
		return webcfg;
	}

	/**
	 * 得到当前状态下的ServerConfig
	 * @return
	 */
	public ServerConfig getServerConfig() {
		return servercfg;
	}

	/**
	 * 得到当前状态下的ServiceConfig
	 * @return
	 */
	public ServiceConfig getServiceConfig() {
		return servicecfg;
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

	/**
	 * 得到默认状态下的ServiceConfig
	 * @return
	 */
	public static ServiceConfig getDefaultServiceConfig() {
		return ServiceConfig.defaultServiceConfig();
	}

}

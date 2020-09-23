package com.lucky.jacklamb.ioc.config;

public abstract class ApplicationConfig {
	
	protected final void init(ScanConfig scan,WebConfig web,ServerConfig server,ServiceConfig service) {
		scanPackConfig(scan);
		webConfig(web);
		serverConfig(server);
		serviceConfig(service);
	}
	
	
	/**
	 * 包扫描相关配置
	 * @param scan
	 */
	protected void scanPackConfig(ScanConfig scan) {
		
	}
	
	/**
	 * Web层相关配置
	 * @param web
	 */
	protected void webConfig(WebConfig web) {
		
	}
	
	/**
	 * 内嵌tomcat服务器的配置
	 * @param server
	 */
	protected void serverConfig(ServerConfig server) {
		
	}

	/**
	 * 服务配置
	 * @param service
	 */
	protected void serviceConfig(ServiceConfig service){

	}

}

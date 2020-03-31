package com.lucky.jacklamb.exception;

public class NoDataSourceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoDataSourceException() {
		super("没有可用数据源，无法创建mapper接口的代理对象....");
	}
	
	public NoDataSourceException(String message) {
		super(message);
	}

}

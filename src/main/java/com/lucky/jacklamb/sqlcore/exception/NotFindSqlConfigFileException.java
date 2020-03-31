package com.lucky.jacklamb.sqlcore.exception;

/**
 * 数据库类型无法识别异常
 * @author fk-7075
 *
 */
public class NotFindSqlConfigFileException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NotFindSqlConfigFileException(String message) {
		super(message);
	}
	
	public NotFindSqlConfigFileException(String message, Throwable cause) {
		super(message,cause);
	}

}

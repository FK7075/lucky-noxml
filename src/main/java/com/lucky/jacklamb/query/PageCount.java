package com.lucky.jacklamb.query;

@FunctionalInterface
public interface PageCount {
	
	/**
	 * 返回数据的总记录条数
	 * @return
	 */
	int getCount();

}

package com.lucky.jacklamb.utils;

import com.lucky.jacklamb.sqlcore.c3p0.SqlOperation;

public class LuckyManager {
	
	/**
	 * 获得SqlOperation对象
	 * @return
	 */
	public static SqlOperation getSqlOperation(String name) {
		return new SqlOperation(name);
	}
	
}

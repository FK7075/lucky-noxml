package com.lucky.jacklamb.query;

import java.util.List;

public interface PageLimit<T> {
	
	/**
	 * 得到当前页的所有数据
	 * @param objs 执行方法所需要的参数列表（使用当前页码currPage代替limit的index参数）
	 * @return
	 */
	List<T> limit(int currPage,int size);

}

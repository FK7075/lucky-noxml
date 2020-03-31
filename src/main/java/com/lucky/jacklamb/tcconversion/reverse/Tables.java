package com.lucky.jacklamb.tcconversion.reverse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.lucky.jacklamb.utils.LuckyUtils;
/**
 * 得到数据库中所有的表的名字
 * @author fk-7075
 *
 */
public class Tables {
	private List<String> tablenames=new ArrayList<String>();//数据库中表的名字

	public List<String> getTablenames() {
		return tablenames;
	}

	public void setTablenames(List<String> tablenames) {
		this.tablenames = tablenames;
	}
	/**
	 * 得到数据库中所有表的名字
	 */
	public Tables(String dbname) {
		ResultSet rs=LuckyUtils.getResultSet(dbname,"show tables;");
			try {
					while(rs.next()) {
						this.tablenames.add(LuckyUtils.TableToClass(rs.getString(1)));
				}
			} catch (SQLException e) {
				System.err.println("xflfk:表名获取失败！");
				e.printStackTrace();
		}
	}
}

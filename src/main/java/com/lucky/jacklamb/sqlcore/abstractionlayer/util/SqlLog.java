package com.lucky.jacklamb.sqlcore.abstractionlayer.util;

import com.lucky.jacklamb.sqlcore.c3p0.DataSource;
import com.lucky.jacklamb.sqlcore.c3p0.ReadIni;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;

/**
 * 日志管理类
 * 更具配置信息打印或不打印sql信息
 * @author fk-7075
 *
 */
public class SqlLog {
	
	private boolean log;
	private DataSource dataSource;
	private String jdbcUrl;
	private SqlFormatUtil sqlFormatUtil;
	private static final PrintStream loger= System.out;
	
	public SqlLog(String dbname) {
		dataSource=ReadIni.getDataSource(dbname);
		log=dataSource.isLog();
		sqlFormatUtil=new SqlFormatUtil();
		jdbcUrl=dataSource.getJdbcUrl();
		if(jdbcUrl.contains("?")) {
			jdbcUrl=jdbcUrl.substring(0,jdbcUrl.indexOf("?"));
		}
	}
	
	
	public void isShowLog(String sql, Object[] obj) {
		if(log)
			log(sql,obj);
	}
	
	public void isShowLog(String sql,Object obj[][]) {
		if(log)
			logBatch(sql,obj);
	}
	
	private void log(String sql, Object[] obj) {
		StringBuilder sb=new StringBuilder("\nDatabase   : ");
		sb.append(jdbcUrl).append("\n").append("SQL        : ").append(formatSql(sql));
		if (obj == null)
			sb.append("\nParameters : { }");
		else {
			sb.append("\nParameters : { ");
			for (Object o : obj) {
				sb.append("(").append((o!=null?o.getClass().getSimpleName():"NULL")).append(")").append(o).append("   ");
			}
			sb.append("}");
		}
		loger.println(sb.toString());
	}
	/**
	 * 
	 * @param sql
	 * @param obj
	 */
	private void logBatch(String sql,Object obj[][]) {
		StringBuilder sb=new StringBuilder("\nDatabase   : ");
		sb.append(jdbcUrl).append("\n").append("SQL        : ").append(formatSql(sql));
		if(obj==null||obj.length==0)
			sb.append("\nParameters : { }");
		else {
			for(int i=0;i<obj.length;i++) {
				sb.append("\nParameters : { ");
				for(Object o:obj[i]) {
					sb.append("(").append((o!=null?o.getClass().getSimpleName():"NULL")).append(")").append(o).append("   ");
				}
				sb.append("}");
			}
		}
		loger.println(sb.toString());
	}
	
	private String formatSql(String sql) {
		if(dataSource.isFormatSqlLog())
			return "\n"+sqlFormatUtil.format(sql);
		return sql;
		
	}
}

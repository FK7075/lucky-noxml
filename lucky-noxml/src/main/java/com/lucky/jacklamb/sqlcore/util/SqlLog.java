package com.lucky.jacklamb.sqlcore.util;

import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
import com.lucky.jacklamb.sqlcore.datasource.ReaderInI;
import com.lucky.jacklamb.utils.base.LuckyUtils;

import java.io.PrintStream;

/**
 * 日志管理类
 * 更具配置信息打印或不打印sql信息
 * @author fk-7075
 *
 */
public class SqlLog {
	
	private boolean log;
	private boolean showCompleteSQL;
	private LuckyDataSource dataSource;
	private String jdbcUrl;
	private String dbname;
	private SqlFormatUtil sqlFormatUtil;
	private static final PrintStream loger= System.out;
	
	public SqlLog(String dbname) {
		this.dbname=dbname;
		dataSource= ReaderInI.getDataSource(dbname);
		log=dataSource.getLog();
		showCompleteSQL=dataSource.getShowCompleteSQL();
		sqlFormatUtil=new SqlFormatUtil();
		jdbcUrl=dataSource.getJdbcUrl();
		if(jdbcUrl.contains("?")) {
			jdbcUrl=jdbcUrl.substring(0,jdbcUrl.indexOf("?"));
		}
	}
	
	
	public void isShowLog(String sql, Object[] obj) {
		if(log) {
			log(sql,obj);
		}
	}
	
	public void isShowLog(String sql,Object obj[][]) {
		if(log) {
			logBatch(sql,obj);
		}
	}

	public void isShowLog(String[] sqls){
		if(log) {
			logBatch(sqls);
		}
	}
	
	private void log(String sql, Object[] obj) {
		StringBuilder sb=new StringBuilder("\nTime        : ").append(LuckyUtils.time()).append("\nDatabase    : ").append("【"+dbname+"】");
		sb.append(jdbcUrl).append("\n").append("SQL         : ").append(formatSql(sql));
		if (obj == null||obj.length==0) {
			sb.append("\nParameters  : { }");
		} else {
			sb.append("\nParameters  : { ");
			for (Object o : obj) {
				sb.append("(").append((o!=null?o.getClass().getSimpleName():"NULL")).append(")").append(o).append("   ");
			}
			sb.append("}");
			if(showCompleteSQL){
				sb.append("\nCompleteSQL : ").append(CreateSql.getCompleteSql(sql,obj));
			}
		}
		loger.println(sb.toString());
	}
	/**
	 * 
	 * @param sql
	 * @param obj
	 */
	private void logBatch(String sql,Object obj[][]) {
		StringBuilder sb=new StringBuilder(  "\nTime       : ").append(LuckyUtils.time()).append("\nDatabase   : ").append("【"+dbname+"】");
		sb.append(jdbcUrl).append("\n").append("SQL        : ").append(formatSql(sql));
		if(obj==null||obj.length==0) {
			sb.append("\nParameters : { }");
		} else {
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

	private void logBatch(String[] sqls){
		StringBuilder sqlB=new StringBuilder();
		for (String sql : sqls) {
			sqlB.append(sql).append("\n");
		}
		StringBuilder sb=new StringBuilder(  "\nTime       : ").append(LuckyUtils.time()).append("\nDatabase   : ").append("【"+dbname+"】").append(jdbcUrl).append("\n");
		sb.append("SQL        : ").append("\n");
		sb.append(sqlB.toString());
		loger.println(sb.toString());
	}
	
	private String formatSql(String sql) {
		if(dataSource.getFormatSqlLog()) {
			return "\n"+sqlFormatUtil.format(sql);
		}
		return sql;
		
	}
}

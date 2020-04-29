package com.lucky.jacklamb.utils;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.lucky.jacklamb.sqlcore.c3p0.SqlOperation;

/**
 * Lucky的工具类
 * @author fk-7075
 *
 */
public class LuckyUtils {
	/**
	 * 单词的首字母大写
	 * @param tableName 原始单词
	 * @return 首字母变大写后的单词
	 */
	public static String TableToClass(String tableName) {
		return tableName.toUpperCase().substring(0, 1)+tableName.substring(1, tableName.length());
	}
	
	/**
	 * 单词的首字母小写
	 * @param tableName 原始单词
	 * @return 首字母变小写后的单词
	 */
	public static String TableToClass1(String tableName) {
		return tableName.toLowerCase().substring(0, 1)+tableName.substring(1, tableName.length());
	}
	/**
	 * 快速得到结果集
	 * @param sql 预编译的SQL
	 * @param obj 填充占位符的对象数组
	 * @return 查询结果集
	 */
	public static ResultSet getResultSet(String dbname,String sql,Object...obj) {
		SqlOperation sqlop=new SqlOperation();
		return sqlop.getResultSet(dbname,sql, obj);
	}
	/**
	 * 获得属性的类型去掉长度
	 * @param type 带长度的属性类型
	 * @return 不带长度的属性
	 */
	public static String getMySqlType(String type) {
		if(type.indexOf("(")>=0)
			return type.substring(0, type.indexOf("("));
		else
			return type;
	}
	
	
	/**
	 * 将用','分隔的字符串截取为集合
	 * @param str 用','分隔的字符串
	 * @return list集合
	 */
	public static List<String> strToArray(String str) {
		List<String> list=new ArrayList<>();
		String[] strArray=str.split(",");
		for (String s : strArray) {
			list.add(s);
		}
		return list;
	}
	
	
	public static String getSqlStatem(String nosql) {
		if(nosql.contains("#{")) {
			int start=nosql.indexOf("#");
			int end=nosql.indexOf("}")+1;
			String sub=nosql.substring(start, end);
			nosql=nosql.replace(sub, "?");
			nosql=getSqlStatem(nosql);
		}
		return nosql;
	}
	
	public static List<String> getSqlField(String nosql){
		List<String> list=new ArrayList<>();
		while(nosql.contains("#{")) {
			int start=nosql.indexOf("#{")+2;
			int end=nosql.indexOf("}");
			String field=nosql.substring(start, end);
			list.add(field);
			nosql=nosql.replaceFirst("#\\{"+field+"\\}", "");
		}
		return list;
	}
	
	
	public static String showtime() {
	     String id=null;
	     id="["+time()+"]  ";
	     return id;
	}
	
	/**
	 * 按照指定的格式获取当前时间的字符串
	 * @param format 格式（YYYY-MM-DD HH:MM:SS）
	 * @return
	 */
	public static String time(String format) {
	     Date date=new Date();
	     SimpleDateFormat sf=
	    	 new SimpleDateFormat(format);
	     return sf.format(date);
	}
	
	/**
	 * 获取当前时间
	 * @return
	 */
	public static String time() {
	     Date date=new Date();
	     SimpleDateFormat sf=
	    	 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     return sf.format(date);
	}
	
	/**
	 * 将Date按照格式转化为String
	 * @param date Data对象
	 * @param format (eg:yyyy-MM-dd HH:mm:ss)
	 * @return
	 */
	public static String time(Date date,String format) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}
	
	/**
	 * 按照指定格式将字符串转化为Date对象
	 * @param dateStr
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date getDate(String dateStr,String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getDate(Date date,String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	public static String getDate(Date date) {
		return getDate(date,"yyyy-MM-dd HH:mm:ss");
	}
	
	/**
	 * 时间运算
	 * @param dateStr
	 * @param format
	 * @param calendarField
	 * @param amount
	 * @return
	 */
	public static Date addDate(String dateStr,String format,int calendarField,int amount) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(getDate(dateStr,format));
		instance.add(calendarField, amount);
		return instance.getTime();
	}
	
	/**
	 * 时间运算
	 * @param dateStr
	 * @param calendarField
	 * @param amount
	 * @return
	 */
	public static Date addDate(String dateStr,int calendarField,int amount) {
		return addDate(dateStr,"yyyy-MM-dd",calendarField,amount);
	}
	
	/**
	 * 基于当前时间的基础上的时间运算
	 * @param calendarField
	 * @param amount
	 * @return
	 */
	public static Date currAddDate(int calendarField,int amount) {
		Calendar instance = Calendar.getInstance();
		instance.add(calendarField, amount);
		return instance.getTime();
	}
	
	
	/**
	 * 年月日转Date 
	 * @param dateStr (eg:2020-06-31)
	 * @return
	 */
	public static Date getDate(String dateStr) {
		return getDate(dateStr,"yyyy-MM-dd");
	}
	
	/**
	 * 年月日时分秒转Date
	 * @param dateTimeStr (eg:2020-06-31 12:23:06)
	 * @return
	 */
	public static Date getDateTime(String dateTimeStr) {
		return getDate(dateTimeStr,"yyyy-MM-dd HH:mm:ss");
	}
	
	/**
	 * 将String转化为java.sql.Date
	 * @param dateStr
	 * @return
	 */
	public static java.sql.Date getSqlDate(String dateStr){
		return new java.sql.Date(getDate(dateStr,"yyyy-MM-dd").getTime());
	}
	
	/**
	 * java.sql.Date的运算
	 * @param dateStr
	 * @param calendarField
	 * @param amount
	 * @return
	 */
	public static java.sql.Date addSqlDate(String dateStr,int calendarField,int amount){
		return new java.sql.Date(addDate(dateStr,calendarField,amount).getTime());
	}
	
	/**
	 * java.sql.Date的运算
	 * @param dateStr
	 * @param format
	 * @param calendarField
	 * @param amount
	 * @return
	 */
	public static java.sql.Date addSqlDate(String dateStr,String format,int calendarField,int amount){
		return new java.sql.Date(addDate(dateStr,format,calendarField,amount).getTime());
	}
	
	/**
	 * 基于当前时间java.sql.Date的运算
	 * @param calendarField
	 * @param amount
	 * @return
	 */
	public static java.sql.Date currAddSqlDate(int calendarField,int amount){
		return new java.sql.Date(currAddDate(calendarField, amount).getTime());
	}
	
	/**
	 * 获取当前时间的java.sql.Date
	 * @return
	 */
	public static java.sql.Date getSqlDate(){
		return new java.sql.Date(new Date().getTime());
	}
	
	/**
	 * 将String转化为java.sql.Time
	 * @param timeStr
	 * @return
	 */
	public static java.sql.Time getSqlTime(String timeStr){
		return new java.sql.Time(getDate(timeStr,"HH:mm:ss").getTime());
	}
	
	/**
	 * 获取当前时间的java.sql.Time
	 * @return
	 */
	public static java.sql.Time getSqlTime(){
		return new java.sql.Time(new Date().getTime());
	}
	
	/**
	 * 将String转化为java.sql.Timestamp
	 * @param timestampStr
	 * @return
	 */
	public static Timestamp getTimestamp(String timestampStr) {
		return new Timestamp(getDate(timestampStr,"yyyy-MM-dd HH:mm:ss").getTime());
	}
	
	/**
	 * 获取当前时间的java.sql.Timestamp
	 * @return
	 */
	public static Timestamp getTimestamp() {
		return new Timestamp(new Date().getTime());
	}
	
	/**
	 * 判断该类型是否为java类型
	 * @param clzz
	 * @return
	 */
	public static boolean isJavaClass(Class<?> clzz) {
		return clzz!=null&&clzz.getClassLoader()==null;
	}
	
}

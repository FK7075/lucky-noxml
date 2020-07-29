package com.lucky.jacklamb.query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.lucky.jacklamb.annotation.orm.mapper.Page;
import com.lucky.jacklamb.enums.JOIN;
import com.lucky.jacklamb.sqlcore.jdbc.core.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.jdbc.SqlCoreFactory;
import com.lucky.jacklamb.utils.base.LuckyUtils;

public class Paging <T>{
	
	private List<?> list=null;
	private int recordnum;//总记录数
	private int pagenum;//总页数
	private int currentpagenum;//当前页码
	private int pagesize;//分页数
	private int index;//当前页的第一条记录的标号
	private SqlCore sqlCore;
	private Object pagObect;//分页的策略对象
	private Method pagenumMethod;//分页策略对象中得到总页数的方法
	private Method pagingMethod;//分页策略对象中得到当前页数据的方法
	private String countSql;//查询总页数的sql语句
	private String limitSql;//分页查询的语句
	private PageCount count;
	private PageLimit<T> limit;
	
	


	public List<?> getList() {
		return list;
	}

	/**
	 * 总记录数
	 * @return
	 */
	public int getRecordnum() {
		return recordnum;
	}

	/**
	 * 总页面数
	 * @return
	 */
	public int getPagenum() {
		return pagenum;
	}

	/**
	 * 当前页的页码
	 * @return
	 */
	public int getCurrentpagenum() {
		return currentpagenum;
	}

	/**
	 * 分页数
	 * @return
	 */
	public int getPagesize() {
		return pagesize;
	}

	/**
	 * 当前页第一条数据在整张表中的位置
	 * @return
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * 初始化分页处理器,使用Lambda表达式确定总记录数和当前页(真分页)
	 * @param count 获取记录条数的方法
	 * @param limit 获取当前页数据的方法
	 */
	public Paging(PageCount count,PageLimit<T> limit) {
		this.count=count;
		this.limit=limit;
	}
	
	/**
	 * 初始化分页处理器,使用Lambda表达式确定策略方法(伪分页)
	 * @param pagelist
	 */
	public Paging(PageList<T> pagelist) {
		list=pagelist.getPageList();
		recordnum=list.size();
	}
	
	/**
	 * 初始化分页处理器，数据量大时建议使用使用，使用此构造方法
	 * 必须使用getLimitList(Class<T> clzz,Object...params)方法获得数据
	 * @param sqlStr SQl语句(COUNT语句或LIMIT语句)
	 */ 
	public Paging(String sqlStr) {
		if(sqlStr.toUpperCase().contains("COUNT")) {
			countSql=sqlStr;
			limitSql=countSqlToLimitSql(sqlStr);
		}else if(sqlStr.toUpperCase().contains("LIMIT")) {
			limitSql=sqlStr;
			countSql=limitSqlToCountSql(sqlStr);
		}
	}

	/**
	 * 初始化分页处理器，数据量大时建议使用使用（结合@Page注解一起使用）,使用此构造方法
	 * 必须使用getLimitList(Object...params)方法获得数据
	 * @param pageObject 分页策略对象
	 * @param pagenumFunctionID 得到总页数的方法的ID
	 * @param pagingFunctionID 执行分页的方法的ID
	 */
	public Paging(Object pageObject,String pagenumFunctionID,String pagingFunctionID) {
		this.pagObect=pageObject;
		Class<?> clzz=pagObect.getClass();
		Method[] methods=clzz.getDeclaredMethods();
		for(Method method:methods) {
			if(method.isAnnotationPresent(Page.class)) {
				Page page=method.getAnnotation(Page.class);
				if(pagenumFunctionID.equals(page.value())) {
					pagenumMethod=method;
				}else if(pagingFunctionID.equals(page.value())) {
					pagingMethod=method;
				}
			}
		}
	}
	
	/**
	 * Paging初始化(数据量小时建议使用)
	 * 
	 * --分页策略对象是一个具体的任意的Java类，分页策略对象中满足以下条件的方法即为分页策略方法。
	 * ----1.方法的返回值类型必须为List<?>
	 * ----2.方法必须为无参方法
	 * ----4.方法体的内容是为了得到#要分页展示的所有#数据所组成的List集合
	 * 
	 * @param pageObject 分页策略对象
	 * @param pagemethod 所采用的的分页策略（分页对象中的方法名）
	 */
	@SuppressWarnings("unchecked")
	public Paging(Object pageObject,String pagemethod) {
		List<T> pagelist=new ArrayList<>();
		Class<?> clzz=pageObject.getClass();
		try {
			Method merhod=clzz.getDeclaredMethod(pagemethod);
			pagelist=(List<T>) merhod.invoke(pageObject);
		} catch (Exception e) {
			System.err.println("xflfk__:分页策略对象的分页策略方法不符合规范！");
			e.printStackTrace();
		}
		list=pagelist;
		recordnum=list.size();
	}

	/**
	 * 初始化分页对象
	 * @param dbname 数据源名称
	 * @param initializePojo 初始化信息对象
	 */
	public Paging(String dbname,InitializePoJo initializePojo) {
		sqlCore=SqlCoreFactory.createSqlCore(dbname);
		if(initializePojo.getPojos().isEmpty()) {
			this.list=sqlCore.getList(initializePojo.getClzz(), initializePojo.getSql(), initializePojo.getSqlobj().toArray());
			this.recordnum=list.size();
		}else {
			QueryBuilder query=new QueryBuilder();
			for(Object po:initializePojo.getPojos().toArray())
				query.addObject(po);
			query.setJoin(JOIN.INNER_JOIN);
			ObjectToJoinSql join=new ObjectToJoinSql(query);
			String sql=join.getJoinSql();
			Object[] object=join.getJoinObject();
			this.list=sqlCore.getList(initializePojo.getClzz(), sql,object);
			this.recordnum=list.size();
		}
	}
	
	/**
	 * 初始化分页对象
	 * @param packBokClass 接受分页结果的包装器
	 * @param sqlStr 查询的预编译SQL
	 * @param sqlObjs 填充占位符的对象
	 */
	public Paging(Class<?> packBokClass,String sqlStr,Object...sqlObjs) {
		this.list=sqlCore.getList(packBokClass, sqlStr, sqlObjs);
		this.recordnum=list.size();
	}
	
	/**
	 * 初始化分页对象
	 * @param dbname 数据源名称
	 * @param packBokClass 接受分页结果的包装器
	 * @param pojos 包含查询信息的pojo数组
	 */
	public Paging(String dbname,Class<T> packBokClass,Object... pojos) {
		sqlCore=SqlCoreFactory.createSqlCore(dbname);
		QueryBuilder query=new QueryBuilder();
		for(Object po:pojos)
			query.addObject(po);
		query.setJoin(JOIN.INNER_JOIN);
		ObjectToJoinSql join=new ObjectToJoinSql(query);
		String sql=join.getJoinSql();
		Object[] object=join.getJoinObject();
		this.list=sqlCore.getList(packBokClass, sql,object);
		recordnum=list.size();
	}
	
	
	
	/**
	 * 以List集合的形式返回当前页的所有内容
	 * @param currentpagenum 当前页码
	 * @param pagesize 分页数
	 * @return 以List集合的形式返回当前页的所有内容
	 */
	public  List<T> getPageList(int currentpagenum,int pagesize){
		List<T> page=new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<T> list_t=(List<T>) list;
		this.pagesize=pagesize;
		if(recordnum%pagesize==0) {
			pagenum=recordnum/pagesize;
		}else {
			pagenum=recordnum/pagesize+1;
		}
		if(currentpagenum>=pagenum) {
			this.currentpagenum=pagenum;
		}else if(currentpagenum<=1) {
			this.currentpagenum=1;
		}else {
			this.currentpagenum=currentpagenum;
		}
		index=(this.currentpagenum-1)*this.pagesize;
		int end=index+this.pagesize;
		if(end>recordnum) {
			end=recordnum;
		}
		for(int j=index;j<end;j++) {
			page.add(list_t.get(j));
		}
		return page;
	}
	

	/**
	 * 得到当前页的所有内容的List<T>集合(配合@Page注解使用，数据量大时建议使用)
	 * @param params 执行分页逻辑的方法的参数列表，顺序必须遵循分页方法的参数顺序，
	 *  而且要使用@Page("currnum")标记代表当前页的参数，@Page("size")标记代表分页数的参数，此方法必须配合
	 *  构造器Paging(Object pageObject,String pagenumFunctionID,String pagingFunctionID)使用
	 * @return
	 */
	@SuppressWarnings("unchecked") 
	public List<T> getLimitList(Object...params){
		List<T> list=new ArrayList<>();
		try {
			pagenumMethod.setAccessible(true);
			recordnum=(int) pagenumMethod.invoke(pagObect);
			init(recordnum,params);
			pagingMethod.setAccessible(true);
			list= (List<T>) pagingMethod.invoke(pagObect, params);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 获取当前页的所有数据
	 * @param currentpagenum 当前页的页码
	 * @param pagesize 分页数
	 * @return
	 */
	public List<T> getLimitList(int currentpagenum,int pagesize){
		recordnum=count.getCount();
		this.pagesize=pagesize;
		if(recordnum%pagesize==0) {
			pagenum=recordnum/pagesize;
		}else {
			pagenum=recordnum/pagesize+1;
		}
		if(currentpagenum>=pagenum) {
			this.currentpagenum=pagenum;
		}else if(currentpagenum<=1) {
			this.currentpagenum=1;
		}else {
			this.currentpagenum=currentpagenum;
		}
		index=(this.currentpagenum-1)*this.pagesize;
		return limit.limit(index,this.pagesize);
	}
	
	/**
	 * 得到当前页的所有内容的List<T>集合，使用SQL语句实现
	 * @param dbname 数据源名称
	 * @param clzz 包装结果集的对象的Class
	 * @param params 执行分页语句的所需的所有参数，使用当前页码代替index，此方法需要配合
	 * 构造器Paging(String sqlStr,boolean isCountSql)使用
	 * @return
	 */
	public List<T> getLimitList(String dbname,Class<T> clzz,Object...params){
		int len=params.length;
		SqlCore sql=SqlCoreFactory.createSqlCore(dbname);
		Object[] countParams=new Object[len-2];
		for(int i=0;i<countParams.length;i++) {
			countParams[i]=params[i];
		}
		recordnum=sql.getObject(int.class, countSql, countParams);
		init(recordnum,params);
		return sql.getList(clzz, limitSql, params);
	}
	
	//使用总记录数量初始化各项属性的值
	private void init(int recordnum,Object...params) {
		int len=params.length;
		this.pagesize=(int) params[len-1];
		int currentPage=(int) params[len-2];
		if(recordnum%pagesize==0)
			this.pagenum=recordnum/pagesize;
		else
			this.pagenum=recordnum/pagesize+1;
		if(currentPage<=1)
			currentpagenum=1;
		else if(currentPage>=pagenum)
			currentpagenum=pagenum;
		else
			currentpagenum=currentPage;
		index=(currentpagenum-1)*this.pagesize;
		params[len-2]=index;
	}
	
	//将LIMIT语句转化为COUNT语句
	private String limitSqlToCountSql(String limitStr) {
		int start=limitStr.toUpperCase().indexOf("LIMIT");
		int end =limitStr.length();
		int st=limitStr.toUpperCase().indexOf("SELECT")+6;
		int ed=limitStr.toUpperCase().indexOf("FROM");
		limitStr=limitStr.replace(limitStr.substring(start, end),"");
		limitStr=limitStr.replace(limitStr.substring(st, ed)," COUNT(*) ");
		return limitStr;
	}

	//将COUNT语句转化为LIMIT语句
	private String countSqlToLimitSql(String countStr) {
		String limitSql=null;
		if(countStr.replaceAll(" ", "").toUpperCase().contains("COUNT(*)")) {
			countStr+=" LIMIT ?,?";
			int start=countStr.toUpperCase().indexOf("SELECT")+6;
			int end=countStr.toUpperCase().indexOf("FROM");
			String str=countStr.substring(start,end);
			limitSql=countStr.replace(str, " * ");
		}else {
			String sqlof=countStr.replaceAll(" ", "");
			int st=sqlof.indexOf("(")+1;
			int ed=sqlof.indexOf(")");
			String param=sqlof.substring(st, ed);
			List<String> paramlist=LuckyUtils.strToArray(param);
			if(countStr.toUpperCase().contains("WHERE")) {
				for (String string : paramlist) {
					countStr+=" AND "+string+" IS NOT NULL";
				}
			}else {
				for (int i=0;i<paramlist.size();i++) {
					if(i==0) {
						countStr+=" WHERE "+paramlist.get(i)+" IS NOT NULL";
					}else {
						countStr+=" AND "+paramlist.get(i)+" IS NOT NULL";
					}
				}
			}
			countStr+=" LIMIT ?,?";
			int start=countStr.toUpperCase().indexOf("SELECT")+6;
			int end=countStr.toUpperCase().indexOf("FROM");
			String str=countStr.substring(start,end);
			limitSql=countStr.replace(str, " * ");
		}
		return limitSql;
	}
	

}

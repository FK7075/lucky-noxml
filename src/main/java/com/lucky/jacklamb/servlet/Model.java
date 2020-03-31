package com.lucky.jacklamb.servlet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.rest.LSON;
import com.lucky.jacklamb.rest.LXML;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.ArrayCast;

/**
 * mvc的核心中转类
 * 
 * @author fk-7075
 *
 */
public class Model {
	
	private static Logger log=Logger.getLogger(Model.class);

	private final String HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	/**
	 * 解码方式
	 */
	private String encod = "ISO-8859-1";

	/**
	 * Request对象
	 */
	private HttpServletRequest req;

	/**
	 * Response对象
	 */
	private HttpServletResponse resp;
	
	/**
	 * ServletConfig对象
	 */
	private ServletConfig servletConfig;

	/**
	 * url请求的方法
	 */
	private RequestMethod requestMethod;

	/**
	 * 页面参数集合Map<String,String[]>
	 */
	private Map<String, String[]> parameterMap;

	/**
	 * Rest风格的参数集合Map<String,String>
	 */
	private Map<String, String> restMap;

	private ServletOutputStream outputStream;

	/**
	 * Model构造器
	 * 
	 * @param request
	 *            Request对象
	 * @param response
	 *            Response对象
	 * @param requestMethod
	 *            url请求的方法
	 * @param encod
	 *            解码方式
	 * @throws IOException
	 */
	public Model(HttpServletRequest request, HttpServletResponse response,ServletConfig servletConfig , RequestMethod requestMethod, String encod)
			throws IOException {
		this.servletConfig=servletConfig;
		this.encod = encod;
		req = request;
		resp = response;
		this.requestMethod = requestMethod;
		this.parameterMap = getRequestParameterMap();
		restMap = new HashMap<>();
	}

	public Model(HttpServletRequest request, HttpServletResponse response) throws IOException {
		req = request;
		resp = response;
		this.parameterMap = getRequestParameterMap();
		restMap = new HashMap<>();
	}

	/**
	 * 得到所有的Rest风格的参数集合RestParamMap
	 * 
	 * @return
	 */
	public Map<String, String> getRestMap() {
		return restMap;
	}

	/**
	 * 设置RestParamMap
	 * 
	 * @param restMap
	 */
	protected void setRestMap(Map<String, String> restMap) {
		this.restMap = restMap;
	}

	public void addParameter(String key, String[] values) {
		parameterMap.put(key, values);
	}

	/**
	 * 得到所有页面参数集合RequestParameterMap
	 * 
	 * @return parameterMap--><Map<String,String[]>>
	 */
	public Map<String, String[]> getParameterMap() {
		return parameterMap;
	}

	/**
	 * 得到当前请求的请求类型
	 * 
	 * @return
	 */
	public RequestMethod getRequestMethod() {
		return requestMethod;
	}

	/**
	 * 设置当前请求的请求类型
	 * 
	 * @param requestMethod
	 */
	protected void setRequestMethod(RequestMethod requestMethod) {
		this.requestMethod = requestMethod;
	}

	/**
	 * 将文本信息写入Cookie
	 * 
	 * @param name
	 *            "K"
	 * @param value
	 *            "V"
	 * @param maxAge
	 *            内容的最长保存时间
	 */
	public void setCookieContent(String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		resp.addCookie(cookie);
	}

	/**
	 * 根据"name"获取Cookit中的文本信息,并转化为指定的编码格式
	 * 
	 * @param name
	 *            NAME
	 * @param encoding
	 *            编码方式
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getCookieContent(String name, String encoding) throws UnsupportedEncodingException {
		String info = null;
		Cookie[] cookies = req.getCookies();
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				info = cookie.getValue();
				info = URLDecoder.decode(info, encoding);
			}
		}
		return info;
	}

	/**
	 * 根据"name"获取Cookit中的文本信息(UTF-8)
	 * 
	 * @param name
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getCookieContent(String name) throws UnsupportedEncodingException {
		String info = null;
		Cookie[] cookies = req.getCookies();
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				info = cookie.getValue();
				info = URLDecoder.decode(info, "UTF-8");
			}
		}
		return info;
	}

	/**
	 * 向request域对象中存值
	 * 
	 * @param name
	 * @param value
	 */
	public void setRequestAttribute(String name, Object value) {
		req.setAttribute(name, value);
	}

	/**
	 * 向request域中取Object类型值
	 * 
	 * @param name
	 * @return
	 */
	public Object getRequestAttribute(String name) {
		return req.getAttribute(name);
	}

	/**
	 * 得到String类型的页面参数
	 * 
	 * @param name
	 * @return
	 */
	public String getRequestPrarmeter(String name) {
		return req.getParameter(name);
	}

	/**
	 * 向session域中存值
	 * 
	 * @param name
	 * @param object
	 */
	public void setSessionAttribute(String name, Object object) {
		req.getSession().setAttribute(name, object);
	}

	/**
	 * 向session域中取值
	 * 
	 * @param name
	 * @return
	 */
	public Object getSessionAttribute(String name) {
		return req.getSession().getAttribute(name);
	}
	
	/**
	 * 向application域中存值
	 * @param name
	 * @param object
	 */
	public void setServletContext(String name,Object object) {
		getServletContext().setAttribute(name, object);
	}
	
	/**
	 * 向application域中取值
	 * 
	 * @param name
	 * @return
	 */
	public Object getServletContextAttribute(String name) {
		return getServletContext().getAttribute(name);
	}

	/**
	 * 使用response对象的Writer方法将对象模型写出为JSON格式数据
	 * 
	 * @param pojo(数组，对象，Collection,Map)
	 * @throws IOException
	 */
	public void witerJson(Object pojo){
		LSON lson = new LSON();
		log.debug(lson.formatJson(pojo));
		writer(lson.toJson(pojo));
	}

	/**
	 * 使用response对象的Writer方法将对象模型写出为XML格式数据
	 * 
	 * @param pojo(数组，对象，Collection,Map)
	 * @throws IOException
	 */
	public void witerXml(Object pojo){
		LXML lson = new LXML(pojo);
		log.debug(HEAD + lson.getXmlStr());
		writer(HEAD + lson.getXmlStr());
	}

	/**
	 * 使用response对象的Writer方法写出数据
	 * 
	 * @param info
	 * @throws IOException
	 */
	public void writer(Object info) {
		try {
			if (outputStream == null)
				outputStream = resp.getOutputStream();
			outputStream.write(info.toString().getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 返回项目发布后file文件(夹)的绝对路径
	 * 
	 * @param file
	 * @return
	 */
	public String getRealPath(String file) {
		return req.getServletContext().getRealPath(file);
	}

	/**
	 * 返回项目发布后file文件(夹)的File对象
	 * 
	 * @param file
	 * @return
	 */
	public File getRealFile(String file) {
		String path = getRealPath(file);
		if (path != null) {
			File fileF = new File(path);
			return fileF;
		}
		return null;
	}

	/**
	 * 删除DocBase文件中的某个文件
	 * 
	 * @param file
	 * @return
	 */
	public boolean delRealFile(String file) {
		if (file == null || file == "" || file == "/")
			return false;
		File delFile = getRealFile(file);
		if (delFile != null && delFile.exists()) {
			delFile.delete();
			return true;
		}
		return false;
	}

	/**
	 * 得到request对象
	 * 
	 * @return
	 */
	public HttpServletRequest getRequest() {
		return req;
	}
	
	/**
	 * 得到Appliction对象
	 * @return
	 */
	public ServletContext getServletContext() {
		return req.getServletContext();
	}
	
	/**
	 * 得到ServletConfig对象
	 * @return
	 */
	public ServletConfig getServletConfig() {
		return this.servletConfig;
	}

	/**
	 * 得到response对象
	 * 
	 * @return
	 */
	public HttpServletResponse getResponse() {
		return resp;
	}

	/**
	 * 得到session对象
	 * 
	 * @return
	 */
	public HttpSession getSession() {
		return req.getSession();
	}

	/**
	 * 得到RequestParameterMap
	 * 
	 * @return parameterMap--><Map<String,String[]>>
	 */
	private Map<String, String[]> getRequestParameterMap() {
		HttpServletRequest request = getRequest();
		Map<String, String[]> res = new HashMap<>();
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (Entry<String, String[]> entry : parameterMap.entrySet()) {
			String[] mapStr = entry.getValue();
			String[] mapStr_cpoy = new String[mapStr.length];
			for (int i = 0; i < mapStr.length; i++) {
				try {
					String characterEncoding = request.getCharacterEncoding();
					mapStr_cpoy[i] = new String(mapStr[i].getBytes(encod), characterEncoding);

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			res.put(entry.getKey(), mapStr_cpoy);
		}
		return res;
	}

	/**
	 * 判断parameterMap中是否存在值为paramName的Key
	 * @param paramName
	 * @return
	 */
	public boolean parameterMapContainsKey(String paramName) {
		return parameterMap.containsKey(paramName);
	}

	public boolean restMapContainsKey(String paramName) {
		return restMap.containsKey(paramName);
	}

	/**
	 * 将String类型的数组转为其他类型的数组String[]->{Integer[],Double[]....}
	 * 
	 * @param strArr
	 * @param changTypeClass
	 * @return T[]
	 */
	public <T> T[] strArrayChange(String[] strArr, Class<T> changTypeClass) {
		return ArrayCast.strArrayChange(strArr, changTypeClass);
	}

	/**
	 * 得到parameterMap中key对应String[]
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public String[] getArray(String key) {
		return parameterMap.get(key);
	}

	/**
	 * 得到parameterMap中key对应String[]转型后的T[]
	 * 
	 * @param key
	 *            键
	 * @param clzz
	 *            目标类型T的Class
	 * @return
	 */
	public <T> T[] getArray(String key, Class<T> clzz) {
		return (T[]) ArrayCast.strArrayChange(parameterMap.get(key), clzz);
	}

	/**
	 * 得到RestParamMap中key对应Value
	 * 
	 * @param key
	 * @return
	 */
	public String getRestParam(String key) {
		return restMap.get(key);
	}

	/**
	 * 得到RestParamMap中key对应的String转型后的T
	 * 
	 * @param key
	 *            键
	 * @param clzz
	 *            目标类型T的Class
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRestParam(String key, Class<T> clzz) {
		return (T) JavaConversion.strToBasic(restMap.get(key), clzz);
	}

	/**
	 * 转发
	 * 
	 * @param address
	 * @throws ServletException
	 * @throws IOException
	 */
	public void forward(String address) {
		try {
			req.getRequestDispatcher(address).forward(req, resp);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 重定向
	 * 
	 * @param address
	 * @throws IOException
	 */
	public void redirect(String address) {
		try {
			resp.sendRedirect(address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

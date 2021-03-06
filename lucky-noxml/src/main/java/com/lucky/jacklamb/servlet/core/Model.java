package com.lucky.jacklamb.servlet.core;

import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.exception.NotFindDocBaseFolderException;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.rest.LSON;
import com.lucky.jacklamb.rest.LXML;
import com.lucky.jacklamb.servlet.LuckyWebContext;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.base.ErrorPage;
import com.lucky.jacklamb.utils.file.MultipartFile;
import com.lucky.jacklamb.utils.serializable.*;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * mvc的核心中转类[数据中心]
 *
 * @author fk-7075
 */
public class Model {

    private static final Logger log = LogManager.getLogger(Model.class);
    private static JSONSerializationScheme jsonSerializationScheme=new GsonSerializationScheme();
    private static XMLSerializationScheme xmlSerializationScheme=new XtreamSerializationScheme();
    private final Integer port=AppConfig.getAppConfig().getServerConfig().getPort();
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
     * MultipartFile类型文件参数集合
     */
    private Map<String, MultipartFile[]> multipartFileMap;
    /**
     * File类型的文件参数集合
     */
    private Map<String, File[]> uploadFileMap;
    /**
     * Rest风格的参数集合Map<String,String>
     */
    private Map<String, String> restMap;
    private ServletOutputStream outputStream;
    private String baseDir;

    public static void setJsonSerializationScheme(JSONSerializationScheme jsonSerializationScheme) {
        Model.jsonSerializationScheme = jsonSerializationScheme;
    }
    public static void setXmlSerializationScheme(XMLSerializationScheme xmlSerializationScheme) {
        Model.xmlSerializationScheme = xmlSerializationScheme;
    }
    public static JSONSerializationScheme getJsonSerializationScheme() {
        return jsonSerializationScheme;
    }
    public static XMLSerializationScheme getXmlSerializationScheme() {
        return xmlSerializationScheme;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        if(outputStream==null){
            outputStream=resp.getOutputStream();
        }
        return outputStream;
    }

    /**
     * Model构造器
     *
     * @param request       Request对象
     * @param response      Response对象
     * @param requestMethod url请求的方法
     * @param encod         解码方式
     * @throws IOException
     */
    public Model(HttpServletRequest request, HttpServletResponse response, ServletConfig servletConfig, RequestMethod requestMethod, String encod) {
        init(request, response);
        this.servletConfig = servletConfig;
        this.encod = encod;
        this.requestMethod = requestMethod;
    }

    public Model(){
        LuckyWebContext currentContext = LuckyWebContext.getCurrentContext();
        init(currentContext.getRequest(),currentContext.getResponse());
        servletConfig=currentContext.getServletConfig();
        requestMethod=currentContext.getRequestMethod();
    }

    public void init(HttpServletRequest request, HttpServletResponse response){
        req = request;
        resp = response;
        this.parameterMap = getRequestParameterMap();
        this.multipartFileMap = new HashMap<>();
        this.restMap = new HashMap<>();
        this.uploadFileMap = new HashMap<>();
        baseDir = AppConfig.getAppConfig().getServerConfig().getBaseDir();
    }

    /**
     * 得到所有的Rest风格的参数集合RestParamMap
     *
     * @return
     */
    public Map<String, String> getRestMap() {
        return restMap;
    }

    public boolean uploadFileMapContainsKey(String key) {
        return uploadFileMap.containsKey(key);
    }

    public File[] getUploadFileArray(String key) {
        return uploadFileMap.get(key);
    }

    public boolean multipartFileMapContainsKey(String key) {
        return multipartFileMap.containsKey(key);
    }

    public Map<String, File[]> getUploadFileMap() {
        return uploadFileMap;
    }

    public void addUploadFile(String key, File[] uploadFiles) {
        uploadFileMap.put(key, uploadFiles);
    }

    public void setUploadFileMap(Map<String, File[]> uploadFileMap) {
        this.uploadFileMap = uploadFileMap;
    }

    public MultipartFile[] getMultipartFileArray(String key) {
        return multipartFileMap.get(key);
    }

    public Map<String, MultipartFile[]> getMultipartFileMap() {
        return multipartFileMap;
    }

    public void addMultipartFile(String key, MultipartFile[] multipartFiles) {
        this.multipartFileMap.put(key, multipartFiles);
    }

    /**
     * 添加一个文件参数
     *
     * @param multipartFileMap
     */
    public void setMultipartFileMap(Map<String, MultipartFile[]> multipartFileMap) {
        this.multipartFileMap = multipartFileMap;
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

    public int getParameterSize() {
        return parameterMap.size();
    }

    public String getDefaultParameterValue(){
        String[] values = new ArrayList<>(parameterMap.values()).get(0);
        return values[values.length-1];
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
     * @param name   "K"
     * @param value  "V"
     * @param maxAge 内容的最长保存时间
     */
    public void setCookieContent(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        resp.addCookie(cookie);
    }

    /**
     * 根据"name"获取Cookit中的文本信息,并转化为指定的编码格式
     *
     * @param name     NAME
     * @param encoding 编码方式
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getCookieContent(String name, String encoding) throws UnsupportedEncodingException {
        String info = null;
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }
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
        if (cookies == null) {
            return null;
        }
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
    public String getRequestParameter(String name) {
        String[] array = getArray(name);
        return array[array.length-1];
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
     *
     * @param name
     * @param object
     */
    public void setServletContext(String name, Object object) {
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
     * @param pojo (数组，对象，Collection,Map)
     */
    public void writerJson(Object pojo) throws IOException {
        resp.setContentType("application/json");
        writer(jsonSerializationScheme.serialization(pojo));
    }

    /**
     * 使用response对象的Writer方法将对象模型写出为XML格式数据
     *
     * @param pojo (数组，对象，Collection,Map)
     */
    public void writerXml(Object pojo) throws IOException {
        resp.setContentType("application/xml");
        writer(xmlSerializationScheme.serialization(pojo));
    }

    /**
     * 使用response对象的Writer方法写出数据
     *
     * @param info
     */
    public void writer(Object info) {
        try {
            getOutputStream().write(info.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            error(e,Code.ERROR);
        }
    }

    /**
     * 使用response对象的Writer方法将Reader中的数据返回
     * @param in
     * @throws IOException
     */
    public void writerReader(Reader in) throws IOException {
        StringWriter sw=new StringWriter();
        IOUtils.copy(in,sw);
        writer(sw.toString());
    }

    /**
     * 返回项目发布后file文件(夹)的绝对路径
     *
     * @param file
     * @return
     */
    public String getRealPath(String file) {
        if(docBaseIsExist()){
            return req.getServletContext().getRealPath(file);
        }
        throw new NotFindDocBaseFolderException("您没有配置「docBase」，无法获取其中的文件！");
    }

    public boolean docBaseIsExist(){
        return AppConfig.getAppConfig().getServerConfig().getDocBase()!=null;
    }


    /**
     * 返回项目发布后file文件(夹)的File对象
     *
     * @param file
     * @return
     */
    public File getRealFile(String file) {
        String path = getRealPath(file);
        return new File(path);
    }

    /**
     * 删除DocBase文件中的某个文件
     *
     * @param file
     * @return
     */
    public boolean delRealFile(String file) {
        if (file == null || file == "" || file == "/") {
            return false;
        }
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
     *
     * @return
     */
    public ServletContext getServletContext() {
        return req.getServletContext();
    }

    /**
     * 得到ServletConfig对象
     *
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
     *
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
        return (T[]) JavaConversion.strArrToBasicArr(strArr, changTypeClass);
    }

    /**
     * 得到parameterMap中key对应String[]
     *
     * @param key 键
     * @return
     */
    public String[] getArray(String key) {
        return parameterMap.get(key);
    }

    /**
     * 得到parameterMap中key对应String[]转型后的T[]
     *
     * @param key  键
     * @param clzz 目标类型T的Class
     * @return
     */
    public <T> T[] getArray(String key, Class<T> clzz) {
        return (T[]) JavaConversion.strArrToBasicArr(parameterMap.get(key), clzz);
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
     * @param key  键
     * @param clzz 目标类型T的Class
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
        } catch (ServletException |IOException e) {
            throw new RuntimeException("转发失败，请检查转发地址！["+address+"]...",e);
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
            throw new RuntimeException("重定向失败，请检查重定向地址！["+address+"]...",e);
        }
    }



    /**
     * 获取访问者IP地址
     *
     * @return
     */
    public String getIpAddr() {
        String ip = null;
        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = req.getHeader("X-Forwarded-For");
        String unknown = "unknown";
        if (ipAddresses == null || ipAddresses.length() == 0 || unknown.equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = req.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || unknown.equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = req.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || unknown.equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = req.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || unknown.equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = req.getHeader("X-Real-IP");
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ipAddresses)) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 得到baseDir中的一个File对象
     *
     * @param path
     * @return
     */
    public File getBaseDir(String path) {
        return new File(baseDir + path);
    }

    /**
     * 向浏览器返回详细错误日志
     * @param e
     * @param code
     */
    public void error(Throwable e,Code code) {
        StringWriter buffer=new StringWriter();
        e.printStackTrace(new PrintWriter(buffer));
        e.printStackTrace();
        log.error(buffer.toString());
        String stackMsg = buffer.toString().replaceAll("\\r\\n", "<br/>").replaceAll("\\t", "&emsp;&emsp;");
        stackMsg=stackMsg.replaceAll("\\n","<br/>");
        error(code,stackMsg,e.toString());
    }

    public void error(Code code,String Message,String Description) {
        try {
            //"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36"
            String userAgent = req.getHeader("User-Agent");
            if(userAgent.startsWith("Mozilla/")){
                resp.setContentType("text/html");
                writer(ErrorPage.exception(code, Message, Description));
            }else{
                writerJson(new ExceptionMessage(code.code,code.errTitle,Description));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ExceptionMessage{

    private Date errTime;

    private int errCode;

    private String errType;

    private String message;

    public ExceptionMessage(int errCode, String errType, String message) {
        this.errTime = new Date();
        this.errCode = errCode;
        this.errType = errType;
        this.message = message;
    }

    public Date getErrTime() {
        return errTime;
    }

    public void setErrTime(Date errTime) {
        this.errTime = errTime;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrType() {
        return errType;
    }

    public void setErrType(String errType) {
        this.errType = errType;
    }

}

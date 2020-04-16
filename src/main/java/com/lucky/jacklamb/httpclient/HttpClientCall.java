package com.lucky.jacklamb.httpclient;

import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HttpClientCall {

    private static Logger log= Logger.getLogger(HttpClientCall.class);

    private static WebConfig webConfig= AppConfig.getAppConfig().getWebConfig();

    /**
     * <pre>
     * 方法体说明：向远程接口发起请求，返回字符串类型结果
     * @param url 接口地址
     * @param requestMethod 请求类型
     * @param params 传递参数
     * @return String 返回结果
     * </pre>
     */
    public static String call(String url, RequestMethod requestMethod, Map<String, String> params, String ...auth) throws IOException {
        //接口返回结果
        String methodResult = null;
        String parameters = "";
        String chineseParam="";
        boolean hasParams = false;
        //将参数集合拼接成特定格式，如name=zhangsan&age=24
        for(String key : params.keySet()){
            String value = URLEncoder.encode(params.get(key), "UTF-8");
            parameters += key +"="+ value +"&";
            chineseParam+= key+"="+params.get(key)+"&";
            hasParams = true;
        }
        if(hasParams){
            parameters = parameters.substring(0, parameters.length()-1);
            chineseParam=chineseParam.substring(0,chineseParam.length()-1);
        }

        //请求判断
        boolean isGet = requestMethod==RequestMethod.GET;
        boolean isPost = requestMethod==RequestMethod.POST;
        boolean isPut = requestMethod==RequestMethod.PUT;
        boolean isDelete = requestMethod==RequestMethod.DELETE;

        //创建HttpClient连接对象
        CloseableHttpClient client = HttpClients.createDefault();
        HttpRequestBase method = null;

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(webConfig.getConnectTimeout()).setConnectionRequestTimeout(webConfig.getConnectionRequestTimeout())
                .setSocketTimeout(webConfig.getSocketTimeout()).build();
        if(!url.contains("?")){
            chineseParam="".equals(chineseParam)?"":"?"+chineseParam;
        }else{
            chineseParam="".equals(chineseParam)?"":"&"+chineseParam;
        }
        if(isGet){
            if(!url.contains("?")){
                parameters="".equals(parameters)?"":"?"+parameters;
            }else{
                parameters="".equals(parameters)?"":"&"+parameters;
            }
            log.info("调用远程接口 ==> [-GET-] "+url+chineseParam);
            url += parameters;
            method = new HttpGet(url);
        }else if(isPost){
            method = new HttpPost(url);
            log.info("调用远程接口 ==> [-POST-] "+url);
            log.info("接口参数 ==>"+params);
            HttpPost postMethod = (HttpPost) method;
            StringEntity entity = new StringEntity(parameters);
            postMethod.setEntity(entity);
        }else if(isPut){
            method = new HttpPut(url);
            log.info("调用远程接口 ==> [-PUT-] "+url);
            log.info("接口参数 ==>"+params);
            HttpPut putMethod = (HttpPut) method;
            StringEntity entity = new StringEntity(parameters);
            putMethod.setEntity(entity);
        }else if(isDelete){
            if(!url.contains("?")){
                parameters="".equals(parameters)?"":"?"+parameters;
            }else{
                parameters="".equals(parameters)?"":"&"+parameters;
            }
            log.info("调用远程接口 ==> [-GET-] "+url+chineseParam);
            url += parameters;
            method = new HttpDelete(url);
        }
        method.setConfig(requestConfig);
        //设置参数内容类型
        method.addHeader("Content-Type","application/x-www-form-urlencoded");
        //httpClient本地上下文
        HttpClientContext context = null;
        if(!(auth==null || auth.length==0)){
            String username = auth[0];
            String password = auth[1];
            UsernamePasswordCredentials credt = new UsernamePasswordCredentials(username,password);
            //凭据提供器
            CredentialsProvider provider = new BasicCredentialsProvider();
            //凭据的匹配范围
            provider.setCredentials(AuthScope.ANY, credt);
            context = HttpClientContext.create();
            context.setCredentialsProvider(provider);
        }
        //访问接口，返回状态码
        HttpResponse response = client.execute(method, context);
        //返回状态码200，则访问接口成功
        if(response.getStatusLine().getStatusCode()==200){
            methodResult = EntityUtils.toString(response.getEntity());
        }
        client.close();
        return methodResult;
    }


    /**
     * <pre>
     * 方法体说明：向远程接口发起GET请求，返回字符串类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @return String 返回结果
     * </pre>
     */
    public static String getCall(String url,Map<String, String> params, String ...auth) throws IOException {
        return call(url,RequestMethod.GET,params,auth);
    }

    /**
     * <pre>
     * 方法体说明：向远程接口发起GET请求，返回字符串类型结果
     * @param url 接口地址
     * @return String 返回结果
     * </pre>
     */
    public static String getCall(String url, String ...auth) throws IOException {
        return call(url,RequestMethod.GET,new HashMap<>(),auth);
    }

    /**
     * <pre>
     * 方法体说明：向远程接口发起POST请求，返回字符串类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @return String 返回结果
     * </pre>
     */
    public static String postCall(String url,Map<String, String> params, String ...auth) throws IOException {
        return call(url,RequestMethod.POST,params,auth);
    }

    /**
     * <pre>
     * 方法体说明：向远程接口发起PUT请求，返回字符串类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @return String 返回结果
     * </pre>
     */
    public static String putCall(String url,Map<String, String> params, String ...auth) throws IOException {
        return call(url,RequestMethod.PUT,params,auth);
    }

    /**
     * <pre>
     * 方法体说明：向远程接口发起DELETE请求，返回字符串类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @return String 返回结果
     * </pre>
     */
    public static String deleteCall(String url,Map<String, String> params, String ...auth) throws IOException {
        return call(url,RequestMethod.DELETE,params,auth);
    }

    /**
     * <pre>
     * 方法体说明：向远程接口发起DELETE请求，返回字符串类型结果
     * @param url 接口地址
     * @return String 返回结果
     * </pre>
     */
    public static String deleteCall(String url,String ...auth) throws IOException {
        return call(url,RequestMethod.DELETE,new HashMap<>(),auth);
    }

}

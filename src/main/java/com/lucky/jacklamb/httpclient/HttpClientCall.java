package com.lucky.jacklamb.httpclient;

import com.google.gson.Gson;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.exception.NotFindRequestException;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientCall {

    private static Logger log= Logger.getLogger(HttpClientCall.class);

    private static WebConfig webConfig= AppConfig.getAppConfig().getWebConfig();

    /**
     * 方法体说明：向远程接口发起请求，返回字符串类型结果
     * @param url 接口地址
     * @param requestMethod 请求类型
     * @param params 传递参数
     * @param auth 访问凭证(username,password)
     * @return String 返回结果
     */
    public static String call(String url, RequestMethod requestMethod, Map<String, String> params, String ...auth) throws IOException, URISyntaxException {
        //创建HttpClient连接对象
        CloseableHttpClient client = HttpClients.createDefault();
        HttpRequestBase method = getHttpRequestObject(url,params,requestMethod);
        method.setConfig(getRequestConfig());
        method.addHeader("Content-Type","application/x-www-form-urlencoded");
        HttpResponse response= client.execute(method, getHttpClientContext(auth));
        String methodResult =responseToString(response);
        client.close();
        return methodResult;
    }

    /**
     * 方法体说明：向远程接口发起GET请求，返回字符串类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @param auth 访问凭证(username,password)
     * @return String 返回结果
     */
    public static String getCall(String url,Map<String, String> params, String ...auth) throws IOException, URISyntaxException {
        return call(url,RequestMethod.GET,params,auth);
    }

    /**
     * 方法体说明：向远程接口发起GET请求，返回字符串类型结果
     * @param url 接口地址
     * @param auth 访问凭证(username,password)
     * @return String 返回结果
     */
    public static String getCall(String url, String ...auth) throws IOException, URISyntaxException {
        return call(url,RequestMethod.GET,new HashMap<>(),auth);
    }

    /**
     * 方法体说明：向远程接口发起POST请求，返回字符串类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @param auth 访问凭证(username,password)
     * @return String 返回结果
     */
    public static String postCall(String url,Map<String, String> params, String ...auth) throws IOException, URISyntaxException {
        return call(url,RequestMethod.POST,params,auth);
    }

    /**
     * 方法体说明：向远程接口发起PUT请求，返回字符串类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @param auth 访问凭证(username,password)
     * @return String 返回结果
     */
    public static String putCall(String url,Map<String, String> params, String ...auth) throws IOException, URISyntaxException {
        return call(url,RequestMethod.PUT,params,auth);
    }

    /**
     * 方法体说明：向远程接口发起DELETE请求，返回字符串类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @param auth 访问凭证(username,password)
     * @return String 返回结果
     */
    public static String deleteCall(String url,Map<String, String> params, String ...auth) throws IOException, URISyntaxException {
        return call(url,RequestMethod.DELETE,params,auth);
    }

    /**
     * 方法体说明：向远程接口发起DELETE请求，返回字符串类型结果
     * @param url 接口地址
     * @param auth 访问凭证(username,password)
     * @return String 返回结果
     */
    public static String deleteCall(String url,String ...auth) throws IOException, URISyntaxException {
        return call(url,RequestMethod.DELETE,new HashMap<>(),auth);
    }


    /**
     * 注：只有返回数据为JSON格式时才有效
     * 向远程接口发起GET请求，返回Object类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @param type 转换的目的类型
     * @param auth 访问凭证(username,password)
     * @return 返回对象类型的结果
     * @throws IOException
     */
    public static Object getCall(String url, Map<String, String> params,Type type, String ...auth) throws IOException, URISyntaxException {
        String result=call(url,RequestMethod.GET,params,auth);
        return new Gson().fromJson(result,type);
    }


    /**
     * 注：只有返回数据为JSON格式时才有效
     * 向远程接口发起GET请求，返回Object类型结果
     * @param url 接口地址
     * @param type 转换的目的类型
     * @param auth 访问凭证(username,password)
     * @return 返回对象类型的结果
     * @throws IOException
     */
    public static Object getCall(String url,Type type, String ...auth) throws IOException, URISyntaxException {
        String result= call(url,RequestMethod.GET,new HashMap<>(),auth);
        return new Gson().fromJson(result,type);
    }


    /**
     * 注：只有返回数据为JSON格式时才有效
     * 向远程接口发起POST请求，返回Object类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @param type 转换的目的类型
     * @param auth 访问凭证(username,password)
     * @return 返回对象类型的结果
     * @throws IOException
     */
    public static Object postCall(String url,Map<String, String> params,Type type, String ...auth) throws IOException, URISyntaxException {
        String result = call(url,RequestMethod.POST,params,auth);
        return new Gson().fromJson(result,type);
    }


    /**
     * 注：只有返回数据为JSON格式时才有效
     * 向远程接口发起PUT请求，返回Object类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @param type 转换的目的类型
     * @param auth 访问凭证(username,password)
     * @return 返回对象类型的结果
     * @throws IOException
     */
    public static Object putCall(String url,Map<String, String> params,Type type, String ...auth) throws IOException, URISyntaxException {
        String result= call(url,RequestMethod.PUT,params,auth);
        return new Gson().fromJson(result,type);
    }


    /**
     * 注：只有返回数据为JSON格式时才有效
     * 向远程接口发起DELETE请求，返回Object类型结果
     * @param url 接口地址
     * @param params 传递参数
     * @param type 转换的目的类型
     * @param auth 访问凭证(username,password)
     * @return 返回对象类型的结果
     * @throws IOException
     */
    public static Object deleteCall(String url,Map<String, String> params,Type type, String ...auth) throws IOException, URISyntaxException {
        String result= call(url,RequestMethod.DELETE,params,auth);
        return new Gson().fromJson(result,type);
    }


    /**
     * 注：只有返回数据为JSON格式时才有效
     * 向远程接口发起DELETE请求，返回Object类型结果
     * @param url 接口地址
     * @param type 转换的目的类型
     * @param auth 访问凭证(username,password)
     * @return 返回对象类型的结果
     * @throws IOException
     */
    public static Object deleteCall(String url,Type type,String ...auth) throws IOException, URISyntaxException {
        String result= call(url,RequestMethod.DELETE,new HashMap<>(),auth);
        return new Gson().fromJson(result,type);
    }

    /**
     * 获取httpClient本地上下文
     * @param auth
     * @return HttpClientContext对象
     */
    private static HttpClientContext getHttpClientContext(String...auth){
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
        return context;
    }

    /**
     * 处理响应结果，将响应结果转化为String类型
     * @param response HttpResponse对象
     * @return String结果
     * @throws IOException
     */
    private static String responseToString(HttpResponse response) throws IOException {
        int code=response.getStatusLine().getStatusCode();
        log.info("Response Status ==> "+code);
        if(code==200){
            return EntityUtils.toString(response.getEntity(),"UTF-8");
        }else{
            return null;
        }
    }

    /**
     * 得到Request的配置对对象
     * @return
     */
    private static RequestConfig getRequestConfig(){
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(webConfig.getConnectTimeout()).setConnectionRequestTimeout(webConfig.getConnectionRequestTimeout())
                .setSocketTimeout(webConfig.getSocketTimeout()).build();
        return requestConfig;
    }

    /**
     * 得到HttpGet/HttpPost/HttpDelete/HttpPut对象
     * @param url url地址
     * @param params 参数列表
     * @param requestMethod 请求类型
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private static HttpRequestBase getHttpRequestObject(String url,Map<String,String> params,RequestMethod requestMethod) throws IOException, URISyntaxException {
        if(requestMethod==RequestMethod.GET){
            log.debug("HttpClient Request => [-GET-] "+url);
            if(isNullParam(params)){
                return new HttpGet(url);
            }else{
                URIBuilder builder=new URIBuilder(url);
                for(String key:params.keySet())
                    builder.addParameter(key,params.get(key));
                return new HttpGet(builder.build());
            }
        }else if(requestMethod==RequestMethod.DELETE){
            log.debug("HttpClient Request => [-DELETE-] "+url);
            if(isNullParam(params)){
                return new HttpDelete(url);
            }else{
                URIBuilder builder=new URIBuilder(url);
                for(String key:params.keySet())
                    builder.addParameter(key,params.get(key));
                return new HttpDelete(builder.build());
            }
        }else if(requestMethod==RequestMethod.POST){
            log.debug("HttpClient Request => [-POST-] "+url);
            if(isNullParam(params)){
                return new HttpPost(url);
            }else{
                HttpPost post=new HttpPost(url);
                post.setEntity(getUrlEncodedFormEntity(params));
                return post;
            }
        }else if(requestMethod==RequestMethod.PUT){
            log.debug("HttpClient Request => [-PUT-] "+url);
            if(isNullParam(params)){
                return new HttpPut(url);
            }else{
                HttpPut put=new HttpPut(url);
                put.setEntity(getUrlEncodedFormEntity(params));
                return put;
            }
        }else{
            log.error("Lucky目前不支持该请求 [-"+requestMethod+"-]");
            throw new NotFindRequestException("Lucky目前不支持该请求 [-"+requestMethod+"-]");
        }
    }

    /**
     * 判断是否有参数
     * @param params map
     * @return
     */
    private static boolean isNullParam(Map<String,String> params){
        if(params==null||params.isEmpty())
            return true;
        return false;
    }

    /**
     * 得到POST、PUT请求的参数
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    private static UrlEncodedFormEntity getUrlEncodedFormEntity(Map<String,String> params) throws UnsupportedEncodingException {
        List<BasicNameValuePair> list = new ArrayList<>();
        for(String key:params.keySet())
            list.add(new BasicNameValuePair(key,params.get(key)));
        return new UrlEncodedFormEntity(list);
    }

}

package com.lucky.jacklamb.httpclient.service;

import com.lucky.jacklamb.annotation.mvc.LuckyClient;
import com.lucky.jacklamb.annotation.mvc.FileDownload;
import com.lucky.jacklamb.annotation.mvc.FileUpload;
import com.lucky.jacklamb.annotation.mvc.RequestMapping;
import com.lucky.jacklamb.aop.util.ASMUtil;
import com.lucky.jacklamb.conversion.util.ClassUtils;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.exception.NotMappingMethodException;
import com.lucky.jacklamb.file.MultipartFile;
import com.lucky.jacklamb.httpclient.HttpClientCall;
import com.lucky.jacklamb.httpclient.exception.JsonConversionException;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.mapping.Mapping;
import com.lucky.jacklamb.mapping.MappingDetails;
import com.lucky.jacklamb.rest.LSON;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeignClientControllerProxy {

    /**
     * 获得FeignClientController接口的代理对象
     *
     * @param feignClientControllerClass
     * @param <T>
     * @return
     */
    public static <T> T getFeignClientControllerProxyObject(Class<T> feignClientControllerClass) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(feignClientControllerClass);
        MethodInterceptor interceptor = (object, method, params, methodProxy) -> {
            LuckyClient fc = feignClientControllerClass.getAnnotation(LuckyClient.class);
            String regUrl = AppConfig.getAppConfig().getServiceConfig().getServiceUrl();
            regUrl = regUrl.endsWith("/") ? regUrl : regUrl + "/";
            //注册中心的地址 eg: http://127.0.0.1:8761/
            regUrl = regUrl.substring(0, regUrl.length() - 8);
            if (Mapping.isMappingMethod(method)) {
                Map<String, Object> callapiMap = getParamMap(method, params);
                callapiMap.put("agreement", fc.agreement());
                //获取远程接口的地址
                MappingDetails md;
                String apiUrl = feignClientControllerClass.isAnnotationPresent(RequestMapping.class) ? feignClientControllerClass.getAnnotation(RequestMapping.class).value() : "/";
                apiUrl = apiUrl.endsWith("/") ? apiUrl : apiUrl + "/";
                apiUrl = apiUrl.startsWith("/") ? apiUrl : "/" + apiUrl;
                String serviceName = fc.value();
                md = Mapping.getMappingDetails(method);
                String methodUrl = md.value.startsWith("/") ? md.value.substring(1) : md.value;
                String regApiUrl = regUrl + serviceName + apiUrl + methodUrl;


                //文件下载的请求，服务将返回byte[]类型的结果
                if (method.isAnnotationPresent(FileDownload.class)) {
                    return fileDownload(regUrl, serviceName,apiUrl + methodUrl,md.method[0], callapiMap);
                }

                //调用远程接口
                String callResult = call(method, regUrl, serviceName, apiUrl + methodUrl, regApiUrl, md.method[0], callapiMap);

                //封装返回结果
                Class<?> returnClass = method.getReturnType();
                try {
                    return new LSON().toObject(returnClass, callResult);
                } catch (Exception e) {
                    throw new JsonConversionException(serviceName,returnClass, callResult, e);
                }

            }
            throw new NotMappingMethodException("该方法不是Mapping方法，无法执行代理！错误位置：" + method);
        };
        enhancer.setCallback(interceptor);
        return (T) enhancer.create();
    }


    /**
     * @param method        Method对象
     * @param regIpPort     注册中心地址 eg: http://127.0.0.1:8761
     * @param serviceName   要请求的服务的服务名 eg: Four
     * @param methodApi     资源路径 eg: may/upload
     * @param url           请求地址 eg: http://127.0.0.1:8761/Three/may/upload
     * @param requestMethod 请求方式 eg: POST
     * @param paramMap
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private static String call(Method method, String regIpPort, String serviceName, String methodApi, String url, RequestMethod requestMethod, Map<String, Object> paramMap) throws IOException, URISyntaxException {
        if (method.isAnnotationPresent(FileUpload.class)) {
            /*
                带文件的请求，首先访问注册中心的服务解析接口，得到所有文件服务的地址，
                得到地址后在将文件请求发送到相应的文件服务器上
             */
            String RegUrl = regIpPort + "lucyxfl/getUrl";
            Map<String, Object> serviceNameMap = new HashMap<>();
            serviceNameMap.put("serviceName", serviceName);
            String[] fileServerUrls = (String[]) HttpClientCall.getCall(RegUrl, serviceNameMap, String[].class);
            String result = "";
            for (String fileUrl : fileServerUrls) {
                String fileApiUrl = paramMap.get("agreement").toString().toLowerCase() + "://" + fileUrl + methodApi;
                result = HttpClientCall.uploadFile(fileApiUrl, paramMap);
            }
            return result;
        }
        return HttpClientCall.call(url, requestMethod, paramMap);
    }

    private static byte[] fileDownload(String regIpPort, String serviceName, String methodApi, RequestMethod requestMethod, Map<String, Object> paramMap) throws IOException, URISyntaxException {
                    /*
                带文件的请求，首先访问注册中心的服务解析接口，得到所有文件服务的地址，
                得到地址后在将文件请求发送到相应的文件服务器上
             */
        String RegUrl = regIpPort + "lucyxfl/getUrl";
        Map<String, Object> serviceNameMap = new HashMap<>();
        serviceNameMap.put("serviceName", serviceName);
        String[] fileServerUrls = (String[]) HttpClientCall.getCall(RegUrl, serviceNameMap, String[].class);
        for (String fileUrl : fileServerUrls) {
            try{
                String fileApiUrl = paramMap.get("agreement").toString().toLowerCase() + "://" + fileUrl + methodApi;
                return  HttpClientCall.callByte(fileApiUrl, requestMethod,paramMap);
            }catch (Exception e){

            }
        }
        throw new RuntimeException("服务器异常，文件下载失败......");
    }

    /**
     * 获取并封装请求远程接口的参数
     *
     * @param method
     * @param params
     * @return
     * @throws IOException
     * @throws IllegalAccessException
     */
    private static Map<String, Object> getParamMap(Method method, Object[] params) throws IOException, IllegalAccessException {
        Map<String, Object> callapiMap = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        List<String> paramName = ASMUtil.getInterfaceMethodParamNames(method);
        String key = null;
        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramClass = params[i].getClass();
            //可以直接put的类型：JDK自带的类型、MultipartFile、MultipartFile[]
            if (paramClass.getClassLoader() == null || paramClass == MultipartFile.class || paramClass == MultipartFile[].class) {
                key = Mapping.getParamName(parameters[i], paramName.get(i));
                callapiMap.put(key, params[i]);
            } else {
                Field[] fields = ClassUtils.getAllFields(paramClass);
                Object fieldValue;
                for (Field field : fields) {
                    field.setAccessible(true);
                    fieldValue = field.get(params[i]);
                    if (fieldValue != null)
                        callapiMap.put(field.getName(), fieldValue.toString());
                }
            }
        }
        return callapiMap;
    }
}

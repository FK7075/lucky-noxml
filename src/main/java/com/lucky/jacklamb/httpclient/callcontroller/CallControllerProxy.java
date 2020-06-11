package com.lucky.jacklamb.httpclient.callcontroller;

import com.lucky.jacklamb.annotation.ioc.CallController;
import com.lucky.jacklamb.annotation.mvc.FileDownload;
import com.lucky.jacklamb.annotation.mvc.FileUpload;
import com.lucky.jacklamb.aop.util.ASMUtil;
import com.lucky.jacklamb.cglib.CglibProxy;
import com.lucky.jacklamb.conversion.util.ClassUtils;
import com.lucky.jacklamb.enums.RequestMethod;
import com.lucky.jacklamb.exception.NotMappingMethodException;
import com.lucky.jacklamb.file.MultipartFile;
import com.lucky.jacklamb.httpclient.HttpClientCall;
import com.lucky.jacklamb.httpclient.exception.JsonConversionException;
import com.lucky.jacklamb.servlet.mapping.Mapping;
import com.lucky.jacklamb.servlet.mapping.MappingDetails;
import com.lucky.jacklamb.rest.LSON;
import net.sf.cglib.proxy.MethodInterceptor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallControllerProxy {

    /**
     * 获得CallController接口的代理对象
     * @param callControllerClass
     * @param <T>
     * @return
     */
    public static <T> T getCallControllerProxyObject(Class<T> callControllerClass){
        return CglibProxy.getCglibProxyObject(callControllerClass,new CallControllerMethodInterceptor(callControllerClass));
    }
}

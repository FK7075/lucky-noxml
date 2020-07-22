package com.lucky.jacklamb.servlet.exceptionhandler;

import com.lucky.jacklamb.annotation.mvc.ControllerExceptionHandler;
import com.lucky.jacklamb.annotation.mvc.ExceptionHandler;
import com.lucky.jacklamb.annotation.mvc.RestBody;
import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.servlet.ResponseControl;
import com.lucky.jacklamb.servlet.core.Model;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LuckyExceptionHandler {

    private ResponseControl responseControl;

    /**
     * Model对象
     */
    protected Model model;

    /**
     * 当前请求响应的Controller对象
     */
    protected Object controllerObj;

    /**
     * 当前请求响应的Controller对象的Class对象
     */
    protected Class<?> currClass;

    /**
     * 当前请求响应的Controller方法
     */
    protected Method currMethod;

    /**
     * 当前请求响应的Controller方法参数
     */
    protected Object[] params;


    /**
     * 初始化
     *
     * @param model
     * @param controllerObj
     * @param currClass
     * @param currMethod
     * @param params
     */
    public void init(Model model, Object controllerObj, Class<?> currClass,
                     Method currMethod, Object[] params) {
        this.model = model;
        this.controllerObj = controllerObj;
        this.currClass = currClass;
        this.currMethod = currMethod;
        this.params = params;
        responseControl = new ResponseControl();
    }

    /**
     * 异常处理
     */
    public boolean dispose(Throwable e) {
        Method method = getExceptionMethod(e);
        if (method == null) {
            return false;
        }
        ControllerExceptionHandler ceh=this.getClass().getAnnotation(ControllerExceptionHandler.class);
        Rest rest = ceh.rest();
        Rest methodRest = null;
        if (method.isAnnotationPresent(RestBody.class)) {
            methodRest = method.getAnnotation(RestBody.class).value();
        }
        rest = methodRest == null ? rest : methodRest;
        Parameter[] parameters = method.getParameters();
        Object[] params = new Object[parameters.length];
        int i = 0;
        for (Parameter parameter : parameters) {
            Class<?> type = parameter.getType();
            if (Throwable.class.isAssignableFrom(type)) {
                params[i] = e;
            } else if (Model.class.isAssignableFrom(type)) {
                params[i] = model;
            } else if (Method.class.isAssignableFrom(type)) {
                params[i] = currMethod;
            } else if (Class.class.isAssignableFrom(type)) {
                params[i] = currClass;
            } else if (HttpRequest.class.isAssignableFrom(type)) {
                params[i] = model.getRequest();
            } else if (HttpResponse.class.isAssignableFrom(type)) {
                params[i] = model.getResponse();
            } else if (HttpSession.class.isAssignableFrom(type)) {
                params[i] = model.getSession();
            } else if (ServletContext.class.isAssignableFrom(type)) {
                params[i] = model.getServletContext();
            } else if (Object[].class.isAssignableFrom(type)) {
                params[i] = this.params;
            } else if (Object.class.isAssignableFrom(type)) {
                params[i] = controllerObj;
            }
            i++;
        }
        method.setAccessible(true);
        final Object result;
        try {
            result = method.invoke(this, params);
        } catch (IllegalAccessException ef) {
            return false;
        } catch (InvocationTargetException ef) {
            return false;
        }
        List<String> globalprefixAndSuffix= AppConfig.getAppConfig().getWebConfig().getHanderPrefixAndSuffix();
        if(!"".equals(ceh.prefix()))
            globalprefixAndSuffix.set(0,ceh.prefix());
        if(!"".equals(ceh.suffix()))
            globalprefixAndSuffix.set(1,ceh.suffix());
        responseControl.jump(model,rest,method,result,globalprefixAndSuffix);
        return true;
    }

    //找出处理该异常的方法
    private Method getExceptionMethod(Throwable e) {
        Map<Class<? extends Throwable>, Method> classMethodMap = methodToMap();
        List<Class<? extends Throwable>> exceptionFamily = getExceptionFamily(e.getClass());
        for (Class<? extends Throwable> aClass : exceptionFamily) {
            if (classMethodMap.containsKey(aClass))
                return classMethodMap.get(aClass);
        }
        return null;
    }

    private Map<Class<? extends Throwable>, Method> methodToMap() {
        Map<Class<? extends Throwable>, Method> exceptionMap = new HashMap<>();
        Method[] allMethod = ClassUtils.getAllMethod(this.getClass());
        for (Method method : allMethod) {
            if (method.isAnnotationPresent(ExceptionHandler.class)) {
                Class<? extends Throwable>[] exMapping = method.getAnnotation(ExceptionHandler.class).value();
                for (Class<? extends Throwable> eClass : exMapping) {
                    if (exceptionMap.containsKey(eClass) && exceptionMap.get(eClass) != method)
                        throw new RuntimeException("您的全局异常处理器" + this.getClass() + "中的异常配置存在歧义！对于 " + eClass + " 异常的处理存在多个Method:[" + method + " , " + exceptionMap.get(eClass) + "]");
                    exceptionMap.put(eClass, method);
                }
            }
        }
        return exceptionMap;
    }

    private List<Class<? extends Throwable>> getExceptionFamily(Class<? extends Throwable> ec) {
        List<Class<? extends Throwable>> family = new ArrayList<>();
        family.add(ec);
        if (ec.getSuperclass() == Object.class)
            return family;
        getExceptionFamily((Class<? extends Throwable>) ec.getSuperclass()).stream().forEach(family::add);
        return family;
    }
}
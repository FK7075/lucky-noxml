package com.lucky.jacklamb.servlet.exceptionhandler;

import com.lucky.jacklamb.annotation.mvc.ControllerExceptionHandler;
import com.lucky.jacklamb.annotation.mvc.ExceptionHandler;
import com.lucky.jacklamb.annotation.mvc.ResponseBody;
import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.servlet.ResponseControl;
import com.lucky.jacklamb.servlet.core.Model;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.MethodUtils;
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

/**
 * Lucky全局Controller异常处理的基类
 * 所有异常处理器必须继承该类，并且使用@ControllerExceptionHandler注解标注
 * 以给定该异常处理器的作用范围
 */
public abstract class LuckyExceptionHandler {

    /**
     * 响应处理器
     */
    private ResponseControl responseControl;

    /**
     * Model对象
     */
    protected Model model;

    /**
     * 当响应前请求的Controller对象
     */
    protected Object controllerObj;

    /**
     * 响应当前请求的Controller对象的Class对象
     */
    protected Class<?> currClass;

    /**
     * 响应当前请求的Controller方法
     */
    protected Method currMethod;

    /**
     * 响应当前请求的Controller方法参数
     */
    protected Object[] params;


    /**
     * 初始化<br/>
     * 1.属性初始化(这些属性将以参数的提供给异常处理器的某个具体的方法)<br/>
     * @param model         Model对称
     * @param controllerObj 当前Controller对象
     * @param currClass     响应当前请求的Controller对象的Class对象
     * @param currMethod    响应当前请求的Controller方法
     * @param params        响应当前请求的Controller方法参数
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
     * Controller全局异常处理<br/>
     * 1.使用getExceptionMethod方法找到当前异常所对应的处理逻辑(Method)<br/>
     * 2.如果找不到，返回false，说明本异常处理器无法处理当前异常<br/>
     * 3.如果可以找到，便执行该方法，执行结束后返回true，表示该异常已经处理<br/>
     *
     * @param e 当前异常
     * @return
     */
    public boolean dispose(Throwable e) {
        Method method = getExceptionMethod(e);
        if (method == null) {
            return false;
        }
        ControllerExceptionHandler ceh = this.getClass().getAnnotation(ControllerExceptionHandler.class);
        Rest rest = ceh.rest();
        Rest methodRest = null;
        if (method.isAnnotationPresent(ResponseBody.class)) {
            methodRest = method.getAnnotation(ResponseBody.class).value();
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
        final Object result = MethodUtils.invoke(this, method, params);
        List<String> globalprefixAndSuffix = AppConfig.getAppConfig().getWebConfig().getHanderPrefixAndSuffix();
        if (!"".equals(ceh.prefix())) {
            globalprefixAndSuffix.set(0, ceh.prefix());
        }
        if (!"".equals(ceh.suffix())) {
            globalprefixAndSuffix.set(1, ceh.suffix());
        }
        responseControl.jump(model, rest, method, result, globalprefixAndSuffix);
        return true;
    }

    /**
     * 找出某个异常对应的处理方法<br/>
     * 1.得到当前异常的继承体系，得到当前异常处理器的异常处理映射Map[? ex Throwable,Method]<br/>
     * 2.遍历这个异常体系（子类到父类的顺序）<br/>
     * 3.判断该异常是否有相对应的处理映射，如果有则返回该映射(Method),并结束当前方法<br/>
     * 4.如果不存在该异常的映射，则返回NULL<br/>
     * @param e 当前异常
     * @return
     */
    private Method getExceptionMethod(Throwable e) {
        //得到当前异常处理器的异常处理映射
        Map<Class<? extends Throwable>, Method> classMethodMap = methodToMap();
        //得到当前异常的继承体系
        List<Class<? extends Throwable>> exceptionFamily = getExceptionFamily(e.getClass());
        for (Class<? extends Throwable> aClass : exceptionFamily) {
            if (classMethodMap.containsKey(aClass)) {
                return classMethodMap.get(aClass);
            }
        }
        return null;
    }

    /**
     * 根据@ExceptionHandler配置得到异常与异常处理的映射
     * 即将某个异常映射到其相应的异常处理方法
     * ArithmeticException => this.method1();
     *
     * @return
     */
    private Map<Class<? extends Throwable>, Method> methodToMap() {
        Map<Class<? extends Throwable>, Method> exceptionMap = new HashMap<>();
        Method[] allMethod = ClassUtils.getAllMethod(this.getClass());
        for (Method method : allMethod) {
            if (method.isAnnotationPresent(ExceptionHandler.class)) {
                Class<? extends Throwable>[] exMapping = method.getAnnotation(ExceptionHandler.class).value();
                for (Class<? extends Throwable> eClass : exMapping) {
                    if (exceptionMap.containsKey(eClass) && exceptionMap.get(eClass) != method) {
                        throw new RuntimeException("您的全局异常处理器" + this.getClass() + "中的异常配置存在歧义！对于 " + eClass + " 异常的处理存在多个Method:[" + method + " , " + exceptionMap.get(eClass) + "]");
                    }
                    exceptionMap.put(eClass, method);
                }
            }
        }
        return exceptionMap;
    }

    /**
     * 得到一个异常类的继承体系
     * ArithmeticException
     * =>
     * ArithmeticException ex RuntimeException ex Exception ex Throwable
     *
     * @param ec 当前异常的Class
     * @return
     */
    private List<Class<? extends Throwable>> getExceptionFamily(Class<? extends Throwable> ec) {
        List<Class<? extends Throwable>> family = new ArrayList<>();
        family.add(ec);
        if (ec.getSuperclass() == Object.class) {
            return family;
        }
        getExceptionFamily((Class<? extends Throwable>) ec.getSuperclass()).stream().forEach(family::add);
        return family;
    }
}
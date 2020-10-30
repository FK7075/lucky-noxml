package com.lucky.jacklamb.servlet.exceptionhandler;

import com.lucky.jacklamb.annotation.ioc.Controller;
import com.lucky.jacklamb.annotation.mvc.ControllerExceptionHandler;
import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.exception.NotFindBeanException;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.servlet.core.Model;
import com.lucky.jacklamb.utils.base.LuckyUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *  DispatchServlet异常拦截器<br/>
 *  DispatchServlet中产生的所有异常都会被此拦截器拦截，做统一的异常处理
 * @author fk-7075
 */
public class DispatchServletExceptionInterceptor {

    private static Logger log = Logger.getLogger(DispatchServletExceptionInterceptor.class);

    /**
     * 响应当前请求的Controller对象
     */
    protected Object controllerObj;

    /**
     * 当响应前请求的Controller对象的Class对象
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
     * Model对象
     */
    protected Model model;

    /**
     * ExceptionMapping的注册中心
     */
    private static List<ExceptionMapping> registry= new ArrayList<>();

    /**IOC容器中的所有LuckyExceptionHandler*/
    private static List<Object> beans;

    static {
        try {
            beans = ApplicationBeans.createApplicationBeans().getBeans(LuckyExceptionHandler.class);
        } catch (NotFindBeanException e) {
            beans = new ArrayList<>();
            log.debug("用户没有注册ExceptionHandler组件，Controller中产生异常需要在Controller中进行处理，如果需要解耦异常处理请使用@ControllerExceptionHandler注解定义一组ExceptionHandler");
        }
        exceptionHandlerRegistered();
    }

    /**
     * 初始化
     * 初始化<br/>
     * 1.属性初始化(这些属性后期将提供给有需要的异常处理器)<br/>
     * 2.异常处理器的收集,收集IOC容器中的所有的ExceptionMapping,以便在执行统一异常处理时被选着调用<br/>
     * @param model         Model对象
     * @param controllerObj 响应当前请求的Controller对象
     * @param currMethod    响应当前请求的Controller方法
     * @param params        响应当前请求的Controller方法参数
     */
    public void initialize(Model model, Object controllerObj, Method currMethod, Object[] params) {
        this.controllerObj = controllerObj;
        this.currClass = controllerObj.getClass();
        this.currMethod = currMethod;
        this.params = params;
        this.model = model;
    }

    /**
     * 全局异常处理<br/>
     * 1.使用getExceptionMethod方法找到当前异常所对应的处理逻辑(Method)<br/>
     * 2.如果找不到，返回false，说明本异常处理器无法处理当前异常<br/>
     * 3.如果可以找到，便执行该方法，执行结束后返回true，表示该异常已经处理<br/>
     *
     * @param e 当前异常
     */
    protected void globalExceptionHandler(Throwable e) {
        LuckyExceptionHandler exceobj;
        ControllerExceptionHandler eh;
        for (Object obj : beans) {
            exceobj = (LuckyExceptionHandler) obj;
            eh = exceobj.getClass().getAnnotation(ControllerExceptionHandler.class);
            if (eh.value().length == 0) {
                exceobj.init(model, controllerObj, currClass, currMethod, params);
                if (exceobj.dispose(e)) {
                    return;
                }
            }
        }
        model.error(e, Code.ERROR);
    }

    /**
     * 异常处理器映射的收集<br/>
     * 收集IOC容器中的所有的ExceptionMapping,并将这些映射器放入全局的集合中
     */
    public static void exceptionHandlerRegistered() {
        LuckyExceptionHandler exceobj;
        ExceptionMapping edh;
        ControllerExceptionHandler eh;
        String[] scope;
        for (Object obj : beans) {
            exceobj = (LuckyExceptionHandler) obj;
            eh = exceobj.getClass().getAnnotation(ControllerExceptionHandler.class);
            scope = eh.value();
            edh = new ExceptionMapping(scope, exceobj);
            registry.add(edh);
        }
    }


    /**
     * 统一异常处理<br/>
     * 1.如果异常处理器映射集合中没有一个异常处理器则执行全局异常处理<br/>
     * 2.否则以方法优先于类的方式执行该异常的异常处理器逻辑<br/>
     * 3.如果在集合中找不到处理该异常的异常处理器，则执行全局异常处理<br/>
     *
     * @param e 当前异常
     */
    public void unifiedExceptionHandler(Throwable e) {
        if (registry.isEmpty()) {
            globalExceptionHandler(e);
            return;
        }
        String ctrlName = getControllerID();
        String cmethodName = currMethod.getName();
        for (ExceptionMapping methodED : registry) {
            //方法优先
            if (methodED.root(ctrlName, cmethodName)) {
                LuckyExceptionHandler dispose = methodED.getDispose();
                dispose.init(model, controllerObj, currClass, currMethod, params);
                if(dispose.dispose(e)){
                    return;
                }else{
                    globalExceptionHandler(e);
                    return;
                }

            }
            //类其次
            if (methodED.root(ctrlName)) {
                LuckyExceptionHandler dispose = methodED.getDispose();
                dispose.init(model, controllerObj, currClass, currMethod, params);
                if(dispose.dispose(e)){
                    return;
                }else{
                    globalExceptionHandler(e);
                    return;
                }
            }

        }
        globalExceptionHandler(e);
    }

    private String getControllerID() {
        if (currClass.isAnnotationPresent(Controller.class)) {
            Controller annotation = currClass.getAnnotation(Controller.class);
            if (!"".equals(annotation.value()))
                return currClass.getAnnotation(Controller.class).value();
            return LuckyUtils.TableToClass1(currClass.getSimpleName());
        } else {
            String controllerClassName = LuckyUtils.TableToClass1(currClass.getSimpleName());
            if(controllerClassName.contains("$$EnhancerByCGLIB$$")){
                return LuckyUtils.TableToClass1(currClass.getSuperclass().getSimpleName());
            }
            return LuckyUtils.TableToClass1(currClass.getSimpleName());
        }
    }
}


package com.lucky.jacklamb.servlet;

import com.lucky.jacklamb.annotation.mvc.InitRun;
import com.lucky.jacklamb.conversion.util.MethodUtils;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.ComponentIOC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 服务器启动时执行执行项
 */
public class ServerStartRun {

    private String componentName;

    private Object controllerObject;

    private Method controllerMethod;

    private String[] params;

    public String getComponentName() {
        return componentName;
    }

    private ComponentIOC componentIOC= ApplicationBeans.iocContainers.getAppIOC();

    private ApplicationBeans beans=ApplicationBeans.createApplicationBeans();

    public ServerStartRun() {
    }

    public ServerStartRun(String componentName, Object controllerObject, Method controllerMethod, String[] params) {
        this.componentName = componentName;
        this.controllerObject = controllerObject;
        this.controllerMethod = controllerMethod;
        this.params = params;
    }

    public void runAdd(){
        try{
            Object[] runParams = MethodUtils.getRunParam(controllerMethod, params,beans);
            Object runResult = controllerMethod.invoke(controllerObject, runParams);
            if (controllerMethod.getReturnType() != void.class) {
                componentIOC.addAppMap(componentName,runResult);
            }
        }catch (InvocationTargetException e){
            throw new RuntimeException(e);
        }catch (IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

}

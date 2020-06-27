package com.lucky.jacklamb.servlet;

import com.lucky.jacklamb.utils.reflect.MethodUtils;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.ComponentIOC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 服务器启动时执行执行项
 */
public class ServerStartRun {

    private int priority;

    public int getPriority() {
        return priority;
    }

    private String componentName;

    private Object controllerObject;

    private Method controllerMethod;

    public Method getControllerMethod() {
        return controllerMethod;
    }

    private String[] params;

    public String getComponentName() {
        return componentName;
    }

    private ComponentIOC componentIOC= ApplicationBeans.iocContainers.getAppIOC();

    private ApplicationBeans beans=ApplicationBeans.createApplicationBeans();

    public ServerStartRun() {
    }

    public ServerStartRun(int priority,String componentName, Object controllerObject, Method controllerMethod, String[] params) {
        this.priority=priority;
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

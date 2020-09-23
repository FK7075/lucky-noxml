package com.lucky.jacklamb.servlet.mapping.param;

import com.lucky.jacklamb.servlet.core.Model;

import java.lang.reflect.Method;
import java.util.Map;

public interface ParamPars {

    Map<String,Object> getParamNameAndValue(Model model, Class<?> controllerClass, Method method);
}

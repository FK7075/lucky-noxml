package com.lucky.jacklamb.mapping.param;

import com.lucky.jacklamb.servlet.Model;

import java.lang.reflect.Method;
import java.util.Map;

public interface ParamPars {

    Map<String,Object> getParamNameAndValue(Model model, Class<?> controllerClass, Method method);
}

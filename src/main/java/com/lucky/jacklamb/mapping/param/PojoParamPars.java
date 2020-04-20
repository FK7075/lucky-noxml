package com.lucky.jacklamb.mapping.param;

import com.lucky.jacklamb.servlet.Model;

import java.lang.reflect.Method;
import java.util.Map;

public class PojoParamPars implements ParamPars {

    @Override
    public Map<String, Object> getParamNameAndValue(Model model, Class<?> controllerClass, Method method) {
        return null;
    }
}

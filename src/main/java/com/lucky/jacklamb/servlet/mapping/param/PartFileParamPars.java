package com.lucky.jacklamb.servlet.mapping.param;

import com.lucky.jacklamb.servlet.core.Model;

import java.lang.reflect.Method;
import java.util.Map;

public class PartFileParamPars implements ParamPars {
    @Override
    public Map<String, Object> getParamNameAndValue(Model model, Class<?> controllerClass, Method method) {
        return null;
    }
}

package com.lucky.jacklamb.mapping;

import com.lucky.jacklamb.annotation.mvc.*;
import com.lucky.jacklamb.enums.RequestMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Mapping {

    /**
     * 判断一个方法是否为映射方法
     * @param method
     * @return
     */
    public static boolean isMappingMethod(Method method){
        if(method.isAnnotationPresent(RequestMapping.class)||
        method.isAnnotationPresent(GetMapping.class)||
        method.isAnnotationPresent(PostMapping.class)||
        method.isAnnotationPresent(PutMapping.class)||
        method.isAnnotationPresent(DeleteMapping.class)){
            return true;
        }
        return false;
    }

    /**
     * 得到Mapping注解的详细配置
     * @param controllerMethod Controller方法对象
     * @return
     */
    public static MappingDetails getMappingDetails(Method controllerMethod){
        MappingDetails md=new MappingDetails();
        if(controllerMethod.isAnnotationPresent(RequestMapping.class)){
            RequestMapping mapping=controllerMethod.getAnnotation(RequestMapping.class);
            md.value=mapping.value();
            md.ip=mapping.ip();
            md.ipSection=mapping.ipSection();
            md.method=mapping.method();
            return md;
        }

        if(controllerMethod.isAnnotationPresent(GetMapping.class)){
            GetMapping mapping=controllerMethod.getAnnotation(GetMapping.class);
            md.value=mapping.value();
            md.ip=mapping.ip();
            md.ipSection=mapping.ipSection();
            md.method=new RequestMethod[]{RequestMethod.GET};
            return md;
        }

        if(controllerMethod.isAnnotationPresent(PostMapping.class)){
            PostMapping mapping=controllerMethod.getAnnotation(PostMapping.class);
            md.value=mapping.value();
            md.ip=mapping.ip();
            md.ipSection=mapping.ipSection();
            md.method=new RequestMethod[]{RequestMethod.POST};
            return md;
        }

        if(controllerMethod.isAnnotationPresent(PutMapping.class)){
            PutMapping mapping=controllerMethod.getAnnotation(PutMapping.class);
            md.value=mapping.value();
            md.ip=mapping.ip();
            md.ipSection=mapping.ipSection();
            md.method=new RequestMethod[]{RequestMethod.PUT};
            return md;
        }

        if(controllerMethod.isAnnotationPresent(DeleteMapping.class)){
            DeleteMapping mapping=controllerMethod.getAnnotation(DeleteMapping.class);
            md.value=mapping.value();
            md.ip=mapping.ip();
            md.ipSection=mapping.ipSection();
            md.method=new RequestMethod[]{RequestMethod.DELETE};
            return md;
        }

        return null;
    }

    /**
     * 得到一个参数的标记参数名
     * @param param Parameter对象
     * @param paramName 该参数的参数名
     * @return
     */
    public static String getParamName(Parameter param, String paramName) {
        if (param.isAnnotationPresent(RequestParam.class)) {
            RequestParam rp = param.getAnnotation(RequestParam.class);
            return rp.value();
        } else {
            return paramName;
        }
    }

}

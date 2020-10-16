package com.lucky.jacklamb.ioc.scan;

import com.google.gson.Gson;
import com.lucky.jacklamb.utils.file.Resources;
import com.lucky.jacklamb.utils.reflect.ClassUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/10/3 12:32 上午
 */
public class InternalComponents {

    private static InternalComponents internalComponents;
    private static String path="/lucky-config/config/Internal-components.json";

    private List<String> direct;

    private List<String> judgment;

    public List<String> getDirect() {
        return direct;
    }

    public void setDirect(List<String> direct) {
        this.direct = direct;
    }

    public List<String> getJudgment() {
        return judgment;
    }

    public void setJudgment(List<String> judgment) {
        this.judgment = judgment;
    }

    public static InternalComponents getInternalComponents(){
        if(internalComponents==null){
            internalComponents=new Gson().fromJson(Resources.getReader(path),InternalComponents.class);
        }
        return internalComponents;
    }

    public List<Class<?>> getDirectClass(){
        return direct.stream().map(ClassUtils::getClass).collect(Collectors.toList());
    }

    public List<Class<?>> getJudgmentClass(){
        return judgment.stream().map(ClassUtils::getClass).collect(Collectors.toList());
    }
}

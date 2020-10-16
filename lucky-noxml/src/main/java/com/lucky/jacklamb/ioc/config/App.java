package com.lucky.jacklamb.ioc.config;

import com.lucky.jacklamb.expression.$Expression;
import com.lucky.jacklamb.utils.file.ini.INIConfig;

import java.util.HashMap;
import java.util.Map;

public class App {

    private Map<String,String> appMap;


    public App(){
        appMap=new HashMap<>();
    }

    public Map<String, String> getAppMap() {
        return appMap;
    }

    public void setAppMap(Map<String, String> appMap) {
        for(Map.Entry<String,String> entry:appMap.entrySet()){
            this.appMap.put(entry.getKey(), $Expression.translation(entry.getValue()));
        }
    }

    public void addAppMap(String key,String value){
        appMap.put(key,$Expression.translation(value));
    }


    public static <T> T getApp(Class<T> clzz) {
        return new INIConfig().getObject(clzz,"App");
    }

}

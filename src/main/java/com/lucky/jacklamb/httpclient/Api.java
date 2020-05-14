package com.lucky.jacklamb.httpclient;

import com.lucky.jacklamb.expression.$Expression;
import com.lucky.jacklamb.file.ini.INIConfig;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ScanConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;


public class Api {

    /**
     * 将注解中配置的CallApi转化为实际的地址
     * @param annApiStr
     * @return
     */
    public static String getApi(String annApiStr){
        if(annApiStr.startsWith("${")&&annApiStr.contains("}")){
            INIConfig ini=new INIConfig();
            if(ini.getAppParamMap()!=null){
                return $Expression.translation(annApiStr);
            }
            ScanConfig scan=AppConfig.getAppConfig().getScanConfig();
            return $Expression.translation(annApiStr,scan.getApp().getAppMap());
        }
        return annApiStr;
    }



}

package com.lucky.jacklamb.httpclient;

import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;


public class Api {

    /**
     * 将注解中配置的CallApi转化为实际的地址
     * @param annApiStr
     * @return
     */
    public static String getApi(String annApiStr){
        if(annApiStr.startsWith("${")&&annApiStr.contains("}")){
            WebConfig webConfig=AppConfig.getAppConfig().getWebConfig();
            int bound=annApiStr.indexOf("}");
            String name=annApiStr.substring(2,bound);
            return webConfig.getApi(name)+annApiStr.substring(bound+1);
        }
        return annApiStr;
    }



}

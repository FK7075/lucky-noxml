package com.lucky.jacklamb.httpclient.service;

import com.lucky.jacklamb.annotation.ioc.Controller;
import com.lucky.jacklamb.annotation.mvc.RequestMapping;
import com.lucky.jacklamb.annotation.mvc.RestBody;
import com.lucky.jacklamb.annotation.mvc.RestParam;
import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.ioc.config.AppConfig;

@Controller("lucky-feignClient-xfl")
public class FeignClientController {

    public final String SERVICE_NAME= AppConfig.getAppConfig().getServiceConfig().getServiceName();

    @RestBody(Rest.TXT)
    @RequestMapping("@rqe-lucy-xfl-0721/#{serviceName}")
    public int request(@RestParam("serviceName") String serviceName){
        return SERVICE_NAME.equals(serviceName)?1:-1;
    }
}

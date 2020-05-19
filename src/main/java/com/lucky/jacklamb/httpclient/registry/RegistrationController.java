package com.lucky.jacklamb.httpclient.registry;

import com.lucky.jacklamb.annotation.ioc.Controller;
import com.lucky.jacklamb.annotation.mvc.RequestMapping;
import com.lucky.jacklamb.annotation.mvc.RequestParam;
import com.lucky.jacklamb.annotation.mvc.RestBody;
import com.lucky.jacklamb.annotation.mvc.RestParam;
import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.httpclient.HttpClientCall;
import com.lucky.jacklamb.ioc.config.LuckyConfig;
import com.lucky.jacklamb.servlet.LuckyController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

@Controller("registrationCenter")
public class RegistrationController extends LuckyController {

    private static final Logger log= LogManager.getLogger(RegistrationController.class);

    private static ServiceCenter serviceCenter=new ServiceCenter();

    @RequestMapping("lucyxfl/register")
    public void register(@RequestParam("serviceName") String serviceName, @RequestParam("port")Integer port){
        serviceCenter.registry(serviceName,model.getIpAddr(),port);
    }

    @RequestMapping("lucyxfl/logout")
    public void logOut(@RequestParam("serviceName") String serviceName, @RequestParam("port")Integer port){
        serviceCenter.logOut(serviceName,model.getIpAddr(),port);
    }

    @RestBody
    @RequestMapping("/")
    public Map<String, Map<String, ServiceInfo>> services(){
        return serviceCenter.getClientMap();
    }

    @RestBody(Rest.TXT)
    @RequestMapping("#{serviceName}/#{api}")
    public String request(@RestParam("serviceName") String serviceName,
                          @RestParam("api")String api,
                          @RequestParam(value="agreement",def = "HTTP")String agreement)
            throws IOException, URISyntaxException {
        if(!serviceCenter.isHaveClientName(serviceName))
            return "ON SERVICE";
        return serviceCenter.request(agreement,serviceName,api,model);
    }

    @RestBody(Rest.TXT)
    @RequestMapping("#{serviceName}/#{api1}/#{api2}")
    public String request(@RestParam("serviceName") String serviceName,
                          @RestParam("api1")String api1,
                          @RestParam("api2")String api2,
                          @RequestParam(value="agreement",def = "HTTP")String agreement)
            throws IOException, URISyntaxException {
        if(!serviceCenter.isHaveClientName(serviceName))
            return "ON SERVICE";
        String api=api1+"/"+api2;
        return serviceCenter.request(agreement,serviceName,api,model);
    }

    @RestBody(Rest.TXT)
    @RequestMapping("#{serviceName}/#{api1}/#{api2}/#{api3}")
    public String request(@RestParam("serviceName") String serviceName,
                          @RestParam("api1")String api1,
                          @RestParam("api2")String api2,
                          @RestParam("api3")String api3,
                          @RequestParam(value="agreement",def = "HTTP")String agreement)
            throws IOException, URISyntaxException {
        if(!serviceCenter.isHaveClientName(serviceName))
            return "ON SERVICE";
        String api=api1+"/"+api2+"/"+api3;
        return serviceCenter.request(agreement,serviceName,api,model);
    }

    @RestBody(Rest.TXT)
    @RequestMapping("#{serviceName}/#{api1}/#{api2}/#{api3}/#{api4}")
    public String request(@RestParam("serviceName") String serviceName,
                          @RestParam("api1")String api1,
                          @RestParam("api2")String api2,
                          @RestParam("api3")String api3,
                          @RestParam("api4")String api4,
                          @RequestParam(value="agreement",def = "HTTP")String agreement)
            throws IOException, URISyntaxException {
        if(!serviceCenter.isHaveClientName(serviceName))
            return "ON SERVICE";
        String api=api1+"/"+api2+"/"+api3+"/"+api4;
        return serviceCenter.request(agreement,serviceName,api,model);
    }

    @RestBody(Rest.TXT)
    @RequestMapping("#{serviceName}/#{api1}/#{api2}/#{api3}/#{api4}/#{api5}")
    public String request(@RestParam("serviceName") String serviceName,
                          @RestParam("api1")String api1,
                          @RestParam("api2")String api2,
                          @RestParam("api3")String api3,
                          @RestParam("api4")String api4,
                          @RestParam("api5")String api5,
                          @RequestParam(value="agreement",def = "HTTP")String agreement)
            throws IOException, URISyntaxException {
        if(!serviceCenter.isHaveClientName(serviceName))
            return "ON SERVICE";
        String api=api1+"/"+api2+"/"+api3+"/"+api4+"/"+api5;
        return serviceCenter.request(agreement,serviceName,api,model);
    }



}

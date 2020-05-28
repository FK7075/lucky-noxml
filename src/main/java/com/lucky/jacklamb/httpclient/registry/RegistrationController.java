package com.lucky.jacklamb.httpclient.registry;

import com.lucky.jacklamb.annotation.ioc.Controller;
import com.lucky.jacklamb.annotation.mvc.RequestMapping;
import com.lucky.jacklamb.annotation.mvc.RequestParam;
import com.lucky.jacklamb.annotation.mvc.RestBody;
import com.lucky.jacklamb.annotation.mvc.RestParam;
import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.file.utils.FileCopyUtils;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.servlet.LuckyController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
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

    @RequestMapping("lucyxfl/file/#{name}")
    public void file(@RestParam("name")String name) throws IOException {
        InputStream resourceAsStream = ApplicationBeans.class.getResourceAsStream("/static/"+name);
        FileCopyUtils.copyToServletOutputStream(response,resourceAsStream);
    }

    @RequestMapping("/")
    public void services() throws IOException {
        InputStream resourceAsStream = ApplicationBeans.class.getResourceAsStream("/static/service.html");
        FileCopyUtils.copyToServletOutputStream(response,resourceAsStream);
    }

    @RestBody
    @RequestMapping("allService")
    public Map<String, Map<String, ServiceInfo>> allService(){
        return serviceCenter.getClientMap();
    }

    @RestBody(Rest.TXT)
    @RequestMapping("#{serviceName}/#{api}*")
    public String request(@RestParam("serviceName") String serviceName,
                          @RestParam("api")String api,
                          @RequestParam(value="agreement",def = "HTTP")String agreement)
            throws IOException, URISyntaxException {
        if(!serviceCenter.isHaveClientName(serviceName))
            return "ERROR: [ON SERVICE] No service found: \""+serviceName+"\"";
        return serviceCenter.request(agreement,serviceName,api,model);
    }
}

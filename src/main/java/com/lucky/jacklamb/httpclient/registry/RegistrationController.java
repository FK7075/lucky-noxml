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
public final class RegistrationController extends LuckyController {

    private static final Logger log= LogManager.getLogger(RegistrationController.class);

    private static ServiceCenter serviceCenter=new ServiceCenter();

    //注册接口
    @RequestMapping("lucyxfl/register")
    public void register(@RequestParam("serviceName") String serviceName, @RequestParam("port")Integer port){
        serviceCenter.registry(serviceName,model.getIpAddr(),port);
    }

    //注销接口
    @RequestMapping("lucyxfl/logout")
    public void logOut(@RequestParam("serviceName") String serviceName, @RequestParam("port")Integer port){
        serviceCenter.logOut(serviceName,model.getIpAddr(),port);
    }

    //classpath:static/ 下的文件访问接口
    @RequestMapping("lucyxfl/file/#{name}")
    public void file(@RestParam("name")String name) throws IOException {
        InputStream resourceAsStream = ApplicationBeans.class.getResourceAsStream("/static/"+name);
        FileCopyUtils.preview(response,resourceAsStream);
    }

    //注册中心页面
    @RequestMapping("/")
    public void services() throws IOException {
        InputStream resourceAsStream = ApplicationBeans.class.getResourceAsStream("/static/service.html");
        FileCopyUtils.preview(response,resourceAsStream);
    }

    //得到所有已经注册的服务的地址
    @RestBody
    @RequestMapping("allService")
    public Map<String, Map<String, ServiceInfo>> allService(){
        return serviceCenter.getClientMap();
    }


    //将项目名解析为地址
    @RestBody
    @RequestMapping("lucyxfl/getUrl")
    public String[] getUrlByServiceName(String serviceName){
        return serviceCenter.getUrlByServiceName(serviceName);
    }

    //请求转发接口，支持文件上传
    @RestBody(Rest.TXT)
    @RequestMapping("#{SERVIC_ENAME}/#{API}*")
    public String request(@RestParam("SERVIC_ENAME") String serviceName,
                          @RestParam("API")String api,
                          @RequestParam(value="agreement",def = "HTTP")String agreement)
            throws IOException, URISyntaxException {
        if(!serviceCenter.isHaveClientName(serviceName))
            return "ERROR: [ON SERVICE] No service found: \""+serviceName+"\"";
        return serviceCenter.request(agreement,serviceName,api,model);
    }
}

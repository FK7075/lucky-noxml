package com.lucky.jacklamb.httpclient.service;

import com.lucky.jacklamb.annotation.ioc.Controller;
import com.lucky.jacklamb.annotation.mvc.*;
import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.httpclient.HttpClientCall;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;
import com.lucky.jacklamb.ioc.config.ServiceConfig;
import com.lucky.jacklamb.servlet.LuckyController;
import com.lucky.jacklamb.start.LuckyApplication;
import com.lucky.jacklamb.start.LuckyShutdown;
import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Controller("lucky-feignClient-xfl")
public class FeignClientController extends LuckyController {

    public final String SERVICE_NAME = AppConfig.getAppConfig().getServiceConfig().getServiceName();

    private static final Logger log = LogManager.getLogger(FeignClientController.class);

    //服务状态验证
    @RestBody(Rest.TXT)
    @RequestMapping("@rqe-lucy-xfl-0721/#{serviceName}")
    public int request(@RestParam("serviceName") String serviceName) {
        return SERVICE_NAME.equals(serviceName) ? 1 : -1;
    }

    //服务注册
    @InitRun(id = "LUCKY-SERVICE-REGISTERED", priority = 1)
    public void registered() {
        ServiceConfig service = AppConfig.getAppConfig().getServiceConfig();
        ServerConfig serverCfg = AppConfig.getAppConfig().getServerConfig();
        //存在[Service]配置，配置类型为服务时，将此服务注册到注册中心
        try {
            String url = service.getServiceUrl().endsWith("/") ? service.getServiceUrl() + "register" : service.getServiceUrl() + "/register";
            Map<String, Object> param = new HashMap<>();
            param.put("serviceName", service.getServiceName());
            param.put("port", AppConfig.getAppConfig().getServerConfig().getPort() + "");
            if (serverCfg.getClosePort() != null&&serverCfg.getShutdown() != null)
                param.put("off", "true");
            HttpClientCall.postCall(url, param);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    //服务注销与关机
    @RestBody(Rest.TXT)
    @RequestMapping("@LUCKY-APP-OFF/CL")
    public int off(@RequestParam(value = "isClose", def = "false") boolean isClose,
                       @RequestParam(value = "closePort", def = "null") Integer closePort,
                       @RequestParam(value = "shutdown", def = "null") String shutdown) throws IOException {
        if (!isClose) {
            log.info("注册中心已经注销了本服务.....");
            return 1;
        } else {
            Integer cp = AppConfig.getAppConfig().getServerConfig().getClosePort();
            String cs = AppConfig.getAppConfig().getServerConfig().getShutdown();
            if (cp != null && cs != null) {
                if ((cp.equals(closePort)) && (cs.equals(shutdown))) {
                    new LuckyShutdown().shutdown();
                    return 1;
                }
                return -1;
            }
            return -1;
        }
    }

    //注销
    @CloseRun(priority = 10)
    public void closeRun() {
        try {
            ServiceConfig service = AppConfig.getAppConfig().getServiceConfig();
            String url = service.getServiceUrl().endsWith("/") ? service.getServiceUrl() + "logout" : service.getServiceUrl() + "/logout";
            Map<String, Object> param = new HashMap<>();
            param.put("serviceName", service.getServiceName());
            param.put("port", AppConfig.getAppConfig().getServerConfig().getPort() + "");
            HttpClientCall.postCall(url, param);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}

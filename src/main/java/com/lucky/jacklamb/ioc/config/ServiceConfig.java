package com.lucky.jacklamb.ioc.config;

/**
 *服务配置器
 */
public class ServiceConfig implements LuckyConfig {

    private static ServiceConfig serviceConfig;

    private String serviceName;

    private boolean isRegistrycenter;

    private String hostName;

    private String serviceUrl;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isRegistrycenter() {
        return isRegistrycenter;
    }

    public void setRegistrycenter(boolean registrycenter) {
        isRegistrycenter = registrycenter;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    private ServiceConfig(){
        this.isRegistrycenter=false;
    }

    public static ServiceConfig defaultServiceConfig(){
        if(serviceConfig==null)
            serviceConfig=new ServiceConfig();
        return serviceConfig;
    }
}

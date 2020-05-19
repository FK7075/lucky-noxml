package com.lucky.jacklamb.httpclient.registry;

public class ServiceInfo {

    private String ip;

    private Integer port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public ServiceInfo(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public ServiceInfo() {
    }

    public String getApiUrl(String agreement){
        if("HTTP".equalsIgnoreCase(agreement))
            return "http://"+ip+":"+port;
        if("HTTPS".equalsIgnoreCase(agreement))
            return "https://"+ip+":"+port;
        throw new RuntimeException("错误的协议名："+agreement);
    }
}

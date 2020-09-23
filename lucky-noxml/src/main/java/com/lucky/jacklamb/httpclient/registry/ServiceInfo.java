package com.lucky.jacklamb.httpclient.registry;

import java.util.Date;

public class ServiceInfo {

    private String ip;

    private Integer port;

    private boolean off;

    private Date ctime;

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public boolean isOff() {
        return off;
    }

    public void setOff(boolean off) {
        this.off = off;
    }

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

    public ServiceInfo(String ip, Integer port, boolean off) {
        this.ip = ip;
        this.port = port;
        this.off = off;
        this.ctime=new Date();
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

    public String getCheckUrl(){
        return getApiUrl("HTTP")+"/@rqe-lucy-xfl-0721/";
    }
}

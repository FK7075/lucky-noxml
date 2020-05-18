package com.lucky.jacklamb.httpclient.registry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegistrationServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }


}

class Client{

    private String ip;

    private Integer port;

    public Client(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    private Map<String,Map<String,Client>> clientMap=new HashMap<>();

    //注册
    public void registry(String clientName,String ip,Integer port){
        if(clientMap.containsKey(clientName)){
            Map<String, Client> domainMap = clientMap.get(clientName);
            String domain=ip+":"+port;
            if(!domainMap.containsKey(domain)){
                domainMap.put(domain,new Client(ip,port));
            }
        }else{
            Map<String,Client> portMap=new HashMap<>();
            portMap.put(ip+":"+port,new Client(ip,port));
            clientMap.put(clientName,portMap);
        }
    }

    //注销
    public void logOut(String clientName,String ip,Integer port){
        if(clientMap.containsKey(clientName)){
            Map<String, Client> domainMap = clientMap.get(clientName);
            String domain=ip+":"+port;
            if(domainMap.containsKey(domain)){
                domainMap.remove(domain);
                if(domainMap.isEmpty())
                    clientMap.remove(clientName);
            }
        }
    }

    //响应请求
    public String request(String clientName,String api,String jsonParam){

        return null;
    }
}
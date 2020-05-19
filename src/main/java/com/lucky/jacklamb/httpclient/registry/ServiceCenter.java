package com.lucky.jacklamb.httpclient.registry;

import com.lucky.jacklamb.expression.$Expression;
import com.lucky.jacklamb.httpclient.HttpClientCall;
import com.lucky.jacklamb.servlet.Model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceCenter {

    private Map<String, Map<String,ServiceInfo>> clientMap=new HashMap<>();

    public Map<String, Map<String, ServiceInfo>> getClientMap() {
        return clientMap;
    }

    //注册
    public void registry(String clientName,String ip,Integer port){
        if(clientMap.containsKey(clientName)){
            Map<String, ServiceInfo> domainMap = clientMap.get(clientName);
            String domain=ip+":"+port;
            if(!domainMap.containsKey(domain)){
                domainMap.put(domain,new ServiceInfo(ip,port));
            }
        }else{
            Map<String,ServiceInfo> portMap=new HashMap<>();
            portMap.put(ip+":"+port,new ServiceInfo(ip,port));
            clientMap.put(clientName,portMap);
        }
    }

    //注销
    public void logOut(String clientName,String ip,Integer port){
        if(clientMap.containsKey(clientName)){
            Map<String, ServiceInfo> domainMap = clientMap.get(clientName);
            String domain=ip+":"+port;
            if(domainMap.containsKey(domain)){
                domainMap.remove(domain);
                if(domainMap.isEmpty())
                    clientMap.remove(clientName);
            }
        }
    }

    //响应请求
    public String request(String agreement,String clientName, String api, Model model) throws IOException, URISyntaxException {
        ServiceInfo serviceInfo=getServiceInfoByRandom(clientName);
        api=api.startsWith("/")?api:"/"+api;
        String apiUrl= $Expression.translationSharp(serviceInfo.getApiUrl(agreement)+api,model.getRestMap());
        Map<String,String> params=new HashMap<>();
        Map<String, String[]> parameterMap = model.getParameterMap();
        Set<String> paramKeys = parameterMap.keySet();
        String[] paramValue;
        for(String key:paramKeys){
            paramValue=parameterMap.get(key);
            if(paramValue.length==1){
                params.put(key,paramValue[0]);
            }else{
                params.put(key, Arrays.toString(paramValue));
            }
        }
        String result=HttpClientCall.call(apiUrl,model.getRequestMethod(),params);
        return result;
    }

    //判断clientName是否已经注册
    public boolean isHaveClientName(String clientName){
        if(!clientMap.containsKey(clientName))
            return false;
        return !clientMap.get(clientName).isEmpty();
    }

    //请求随机转发
    private ServiceInfo getServiceInfoByRandom(String clientName){
        Map<String, ServiceInfo> domainMap = clientMap.get(clientName);
        List<String> domainList = domainMap.keySet().stream().collect(Collectors.toList());
        final double d = Math.random();
        final int index=(int)(d*domainList.size());
        return domainMap.get(domainList.get(index));
    }
}

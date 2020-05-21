package com.lucky.jacklamb.httpclient.registry;

import com.lucky.jacklamb.expression.$Expression;
import com.lucky.jacklamb.httpclient.HttpClientCall;
import com.lucky.jacklamb.servlet.Model;
import org.apache.http.conn.HttpHostConnectException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceCenter {

    private Map<String, Map<String, ServiceInfo>> clientMap = new HashMap<>();

    public Map<String, Map<String, ServiceInfo>> getClientMap() {
        return clientMap;
    }

    //注册
    public void registry(String clientName, String ip, Integer port) {
        if (clientMap.containsKey(clientName)) {
            Map<String, ServiceInfo> domainMap = clientMap.get(clientName);
            String domain = ip + ":" + port;
            if (!domainMap.containsKey(domain)) {
                domainMap.put(domain, new ServiceInfo(ip, port));
            }
        } else {
            Map<String, ServiceInfo> portMap = new HashMap<>();
            portMap.put(ip + ":" + port, new ServiceInfo(ip, port));
            clientMap.put(clientName, portMap);
        }
    }

    //注销
    public void logOut(String clientName, String ip, Integer port) {
        if (clientMap.containsKey(clientName)) {
            Map<String, ServiceInfo> domainMap = clientMap.get(clientName);
            String domain = ip + ":" + port;
            if (domainMap.containsKey(domain)) {
                domainMap.remove(domain);
                if (domainMap.isEmpty())
                    clientMap.remove(clientName);
            }
        }
    }

    /**
     * 请求的转发与响应
     * 一个服务名下有多个服务时，会随机选取其中的服务进行转发，当有服务挂掉后
     * 会先注销掉那个服务，而后继续随机选取服务并发起请求，直到找到可以正常响应的为止
     * 如果均无法访问则会返回错误信息
     *
     * @param agreement  使用的协议(http/https)
     * @param clientName 请求的服务
     * @param api        请求的资源
     * @param model      Model对象
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public String request(String agreement, String clientName, String api, Model model) throws IOException, URISyntaxException {
        if (isHaveClientName(clientName)) {
            ServiceInfo serviceInfo = getServiceInfoByRandom(clientName);
            api = api.startsWith("/") ? api : "/" + api;
            String apiUrl = $Expression.translationSharp(serviceInfo.getApiUrl(agreement) + api, model.getRestMap());
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> parameterMap = model.getParameterMap();
            Set<String> paramKeys = parameterMap.keySet();
            String[] paramValue;
            for (String key : paramKeys) {
                paramValue = parameterMap.get(key);
                if (paramValue.length == 1) {
                    params.put(key, paramValue[0]);
                } else {
                    params.put(key, Arrays.toString(paramValue));
                }
            }
            try {
                String result = HttpClientCall.call(apiUrl, model.getRequestMethod(), params);
                return result;
            } catch (HttpHostConnectException e) {
                logOut(clientName, serviceInfo.getIp(), serviceInfo.getPort());
                return request(agreement, clientName, api, model);
            }

        } else {
            return "ERROR: [ON SERVICE] No service found: \"" + clientName + "\"";
        }

    }

    /**
     * 判断服务名clientName是否已经被注册
     *
     * @param clientName 服务名
     * @return
     */
    public boolean isHaveClientName(String clientName) {
        if (!clientMap.containsKey(clientName))
            return false;
        return !clientMap.get(clientName).isEmpty();
    }

    /**
     * 随机的挑选访问的服务
     * @param clientName 服务名
     * @return
     */
    private ServiceInfo getServiceInfoByRandom(String clientName) {
        Map<String, ServiceInfo> domainMap = clientMap.get(clientName);
        List<String> domainList = domainMap.keySet().stream().collect(Collectors.toList());
        final double d = Math.random();
        final int index = (int) (d * domainList.size());
        return domainMap.get(domainList.get(index));
    }
}

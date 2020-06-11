package com.lucky.jacklamb.servlet.staticsource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lucky.jacklamb.file.utils.FileCopyUtils;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.config.WebConfig;
import com.lucky.jacklamb.servlet.core.Model;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticResourceManage {

    private static Map<String,String> contentTypeMap;

    static{
        try {
            contentTypeMap=new HashMap<>();
            BufferedReader br = new BufferedReader( new InputStreamReader(ApplicationBeans.class.getResourceAsStream("/config/ContentType.json"), "UTF-8"));
            Type type=new TypeToken<List<String[]>>(){}.getType();
            List<String[]> arrContentType=new Gson().fromJson(br,type);
            for (String[] kv : arrContentType) {
                contentTypeMap.put(kv[0],kv[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isLegalIp(WebConfig webCfg, String currIp) {
        if (!webCfg.getStaticResourcesIpRestrict().isEmpty() && !webCfg.getStaticResourcesIpRestrict().contains(currIp))
            return false;
        return true;
    }

    public static boolean isLegalRequest(WebConfig webCfg, String currIp, HttpServletResponse resp, String uri) {
        return isLegalIp(webCfg, currIp) && isStaticResource(resp, uri);
    }

    public static boolean isStaticResource(HttpServletResponse resp, String uri) {
        if(!uri.contains("."))
            return false;
        String lowercaseUri = uri.toLowerCase();
        lowercaseUri=lowercaseUri.substring(lowercaseUri.lastIndexOf("."));
        if(contentTypeMap.containsKey(lowercaseUri)){
            resp.setContentType(contentTypeMap.get(lowercaseUri));
            return true;
        }
        return false;
    }

    public static boolean resources(Model model, String uri){
        String realPath = model.getRequest().getServletContext().getRealPath(uri);
        File file=new File(realPath);
        return file.exists();
    }

    public static void response(Model model, String uri) throws IOException {
        String realPath = model.getRequest().getServletContext().getRealPath(uri);
        File targetFile = new File(realPath);
        FileCopyUtils.preview(model, targetFile);
    }

}

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
            BufferedReader br = new BufferedReader( new InputStreamReader(ApplicationBeans.class.getResourceAsStream("/lucky-config/config/ContentType.json"), "UTF-8"));
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

    public static boolean resources(Model model,String webRoot, String uri){
        String path;
        if(webRoot.startsWith("${classpath}")){
            webRoot=webRoot.substring(12);
            InputStream staticStream=ApplicationBeans.class.getResourceAsStream(webRoot+uri);
            return staticStream!=null;
        }else if(webRoot.startsWith("${user.dir}")){
            webRoot=webRoot.substring(11);
            return new File(System.getProperty("user.dir")+uri).exists();
        }else if(webRoot.startsWith("${docBase}")){
            webRoot=webRoot.substring(10);
        }else{
            return new File(webRoot+uri).exists();
        }
        String realPath = model.getRealPath(uri);
        InputStream staticStream=ApplicationBeans.class.getResourceAsStream("/webapp"+uri);
        if(realPath==null&&staticStream==null)
          return false;
        if(realPath!=null&&new File(realPath).exists())
            return true;
        return staticStream!=null;
    }

    public static void response(Model model,String webRoot, String uri) throws IOException {
        if(webRoot.startsWith("${classpath}")){

        }else if(webRoot.startsWith("${user.dir}")){

        }else if(webRoot.startsWith("${docBase}")){

        }else{

        }
        File staticFile=model.getRealFile(uri);
        if(staticFile!=null&&staticFile.exists()){
            FileCopyUtils.preview(model, staticFile);
            return;
        }
        InputStream staticStream=ApplicationBeans.class.getResourceAsStream("/webapp"+uri);
        FileCopyUtils.preview(model,staticStream,uri.substring(uri.lastIndexOf("/")));

    }

}

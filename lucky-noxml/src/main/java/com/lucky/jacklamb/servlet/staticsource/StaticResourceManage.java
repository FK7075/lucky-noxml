package com.lucky.jacklamb.servlet.staticsource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lucky.jacklamb.utils.file.FileUtils;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.WebConfig;
import com.lucky.jacklamb.servlet.core.Model;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 静态资源管理器
 * @author fk-7075
 */
public class StaticResourceManage {

    private static Map<String,String> contentTypeMap;

    private static final String WebRoot=AppConfig.getAppConfig().getWebConfig().getWebRoot();

    private static String WEB_ROOT_PREFIX;

    private static String TARGET_WEB_ROOT;

    static{
        try {
            contentTypeMap=new HashMap<>();
            BufferedReader br = new BufferedReader( new InputStreamReader(ApplicationBeans.class.getResourceAsStream("/lucky-config/config/ContentType.json"), "UTF-8"));
            Type type=new TypeToken<List<String[]>>(){}.getType();
            List<String[]> arrContentType=new Gson().fromJson(br,type);
            for (String[] kv : arrContentType) {
                contentTypeMap.put(kv[0],kv[1]);
            }
            String webRoot=WebRoot;
            if(webRoot.startsWith("${classpath}")){
                webRoot=webRoot.substring(12);
                webRoot=webRoot.startsWith("/")?webRoot:"/"+webRoot;
                TARGET_WEB_ROOT=webRoot.endsWith("/")?webRoot.substring(0,webRoot.length()-1):webRoot;
                WEB_ROOT_PREFIX="CP";
            }else if(webRoot.startsWith("${user.dir}")){
                webRoot=webRoot.substring(11);
                webRoot=webRoot.startsWith("/")?webRoot:"/"+webRoot;
                webRoot=webRoot.endsWith("/")?webRoot.substring(0,webRoot.length()-1):webRoot;
                TARGET_WEB_ROOT=System.getProperty("user.dir") + webRoot;
                WEB_ROOT_PREFIX="UD";
            }else if(webRoot.startsWith("${docBase}")){
                TARGET_WEB_ROOT=webRoot.substring(10);
                WEB_ROOT_PREFIX="DB";
            }else{
                TARGET_WEB_ROOT=webRoot.endsWith("/")?webRoot.substring(0,webRoot.length()-1):webRoot;
                WEB_ROOT_PREFIX="ABS";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isLegalIp(WebConfig webCfg, String currIp) {
        if (!webCfg.getStaticResourcesIpRestrict().isEmpty() && !webCfg.getStaticResourcesIpRestrict().contains(currIp)) {
            return false;
        }
        return true;
    }

    public static boolean isLegalRequest(WebConfig webCfg, String currIp, HttpServletResponse resp, String uri) {
        return isLegalIp(webCfg, currIp) && isStaticResource(resp, uri);
    }

    public static boolean isStaticResource(HttpServletResponse resp, String uri) {
        if(!uri.contains(".")) {
            return false;
        }
        String lowercaseUri = uri.toLowerCase();
        lowercaseUri=lowercaseUri.substring(lowercaseUri.lastIndexOf("."));
        if(contentTypeMap.containsKey(lowercaseUri)){
            resp.setContentType(contentTypeMap.get(lowercaseUri));
            return true;
        }
        return false;
    }

    public static boolean resources(Model model, String uri){
        //uri /xxx/xxx
        if(docBaseFileIsExist(model,uri)) {
            return true;
        }
        switch (WEB_ROOT_PREFIX){
            case "CP" :return ApplicationBeans.class.getResourceAsStream(TARGET_WEB_ROOT+uri)!=null;
            case "DB" :return model.getRealFile(TARGET_WEB_ROOT+uri)!=null;
            default   :return new File(TARGET_WEB_ROOT+uri).exists();
        }
    }

    private static boolean docBaseFileIsExist(Model model, String uri){
        if(model.docBaseIsExist()){
            return model.getRealFile(uri).exists();
        }
        return false;
    }

    public static void response(Model model, String uri) throws IOException {
        if(docBaseFileIsExist(model,uri)){
            FileUtils.preview(model, model.getRealFile(uri));
            return;
        }
        switch (WEB_ROOT_PREFIX){
            case "CP" :{
                InputStream staticStream=ApplicationBeans.class.getResourceAsStream(TARGET_WEB_ROOT+uri);
                FileUtils.preview(model,staticStream,uri.substring(uri.lastIndexOf("/")));
                break;
            }
            case "DB" :{
                File staticFile=model.getRealFile(TARGET_WEB_ROOT+uri);
                FileUtils.preview(model, staticFile);
                break;
            }
            default: {
                File staticFile=new File(TARGET_WEB_ROOT+uri);
                FileUtils.preview(model, staticFile);
                break;
            }
        }
    }

}

package com.lucky.jacklamb.servlet.staticsource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lucky.jacklamb.file.utils.FileCopyUtils;
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

public class StaticResourceManage {

    private static Map<String,String> contentTypeMap;

    private static final String WebRoot=AppConfig.getAppConfig().getWebConfig().getWebRoot();

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

    public static boolean resources(Model model, String uri){
        //uri /xxx/xxx
        if(docBaseFileIsExist(model,uri))
            return true;
        String webRoot=WebRoot;
        if(webRoot.startsWith("${classpath}")){
            webRoot=webRoot.substring(12);
            // /xxx
            webRoot=webRoot.startsWith("/")?webRoot:"/"+webRoot;
            webRoot=webRoot.endsWith("/")?webRoot.substring(0,webRoot.length()-1):webRoot;
            return ApplicationBeans.class.getResourceAsStream(webRoot+uri)!=null;
        }else if(webRoot.startsWith("${user.dir}")){
            webRoot=webRoot.substring(11);
            webRoot=webRoot.startsWith("/")?webRoot:"/"+webRoot;
            webRoot=webRoot.endsWith("/")?webRoot.substring(0,webRoot.length()-1):webRoot;
            return new File(System.getProperty("user.dir")+webRoot+uri).exists();
        }else if(webRoot.startsWith("${docBase}")){
            webRoot=webRoot.substring(10);
            return model.getRealFile(webRoot+uri)!=null;
        }else{
            return new File(webRoot+uri).exists();
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
            FileCopyUtils.preview(model, model.getRealFile(uri));
            return;
        }
        String webRoot=WebRoot;
        if(webRoot.startsWith("${classpath}")){
            webRoot=webRoot.substring(12);
            webRoot=webRoot.startsWith("/")?webRoot:"/"+webRoot;
            webRoot=webRoot.endsWith("/")?webRoot.substring(0,webRoot.length()-1):webRoot;
            InputStream staticStream=ApplicationBeans.class.getResourceAsStream(webRoot+uri);
            FileCopyUtils.preview(model,staticStream,uri.substring(uri.lastIndexOf("/")));
        }else if(webRoot.startsWith("${user.dir}")){
            webRoot=webRoot.substring(11);
            webRoot=webRoot.startsWith("/")?webRoot:"/"+webRoot;
            webRoot=webRoot.endsWith("/")?webRoot.substring(0,webRoot.length()-1):webRoot;
            File staticFile = new File(System.getProperty("user.dir") + webRoot + uri);
            FileCopyUtils.preview(model, staticFile);
        }else if(webRoot.startsWith("${docBase}")){
            webRoot=webRoot.substring(10);
            File staticFile=model.getRealFile(webRoot+uri);
            FileCopyUtils.preview(model, staticFile);
        }else{
            File staticFile=new File(webRoot+uri);
            FileCopyUtils.preview(model, staticFile);
        }
    }

}

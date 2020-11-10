package com.lucky.jacklamb.thymeleaf.utils;

import com.lucky.jacklamb.utils.file.ini.IniFilePars;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Map;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/1 3:04 下午
 */
public class ThymeleafConfig {

    public static final String SECTION="Thymeleaf";

    private static ThymeleafConfig conf;
    private boolean enabled=false;
    private String encoding="UTF-8";
    private String prefix="classpath:/templates/";
    private String suffix=".html";
    private boolean cache=false;
    private String model="HTML";

    private ThymeleafConfig(){
        init();
    }

    private void init(){
        IniFilePars iniFilePars = IniFilePars.getIniFilePars();
        if(iniFilePars.isHasSection(SECTION)){
            enabled=true;
            Map<String, String> sectionMap = iniFilePars.getSectionMap(SECTION);
            if(sectionMap.containsKey("encoding")){
                encoding=sectionMap.get("encoding").trim();
            }
            if(sectionMap.containsKey("prefix")){
                prefix=sectionMap.get("prefix").trim();
            }
            if(sectionMap.containsKey("suffix")){
                suffix=sectionMap.get("suffix").trim();
            }
            if(sectionMap.containsKey("cache")){
                cache=Boolean.parseBoolean(sectionMap.get("cache").trim());
            }
            if(sectionMap.containsKey("model")){
                model=sectionMap.get("model");
            }
        }
    }

    public static ThymeleafConfig getConf() {
        if(conf==null){
            conf=new ThymeleafConfig();
        }
        return conf;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getPrefix() {
        if(prefix.startsWith("classpath:")){
            prefix=prefix.substring(10);
        }
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public boolean isCache() {
        return cache;
    }

    public String getModel() {
        return model;
    }
}

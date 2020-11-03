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
    private TemplateMode model=TemplateMode.HTML;

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
                String[] models=sectionMap.get("model").trim().split(",");
                for (String mod : models) {
                    switch (mod.toUpperCase()){
                        case "HTML"       : {model=TemplateMode.HTML;break;}
                        case "XML"        : {model=TemplateMode.XML;break;}
                        case "TEXT"       : {model=TemplateMode.TEXT;break; }
                        case "JAVASCRIPT" : {model=TemplateMode.JAVASCRIPT;break;}
                        case "CSS"        : {model=TemplateMode.CSS;break; }
                        case "RAW"        : {model=TemplateMode.RAW;break;}
                    }
                }
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

    public TemplateMode getModel() {
        return model;
    }
}

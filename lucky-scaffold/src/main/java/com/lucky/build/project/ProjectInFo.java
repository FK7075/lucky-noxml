package com.lucky.build.project;

import lombok.Data;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/23 12:28
 */
@Data
public class ProjectInFo {

    private static final String dir = System.getProperty("user.dir");

    private String groupId;

    private String mavenRepository;

    private String projectPath=dir;

    private String artifactId;

    private String version="1.0-SNAPSHOT";

    private String projectName;

    private String mainClassName;

    private String mainClass;

    private ProjectInFo(){}

    private static Properties readCfg() throws IOException {
        String cfgPath=dir.endsWith(File.separator)?dir+"build.properties":dir+File.separator+"build.properties";
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(cfgPath),"UTF-8"));
        Properties properties=new Properties();
        properties.load(br);
        return properties;
    }

    public static ProjectInFo getProjectInFo(){
        ProjectInFo pif=new ProjectInFo();
        Properties properties=null;
        try {
            properties=readCfg();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Field[] fields = ProjectInFo.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldName=field.getName();
            if(properties.containsKey(fieldName)){
                fieldSet(pif,field,properties.get(fieldName));
            }
        }
        pif.setArtifactId(pif.getArtifactId()
                .replaceAll(" +"," ")
                .replaceAll("_+","_")
                .replaceAll("-+","-"));
        pif.setGroupId(pif.getGroupId()
                .replaceAll(" +"," ")
                .replaceAll("_+","_")
                .replaceAll("-+","-"));
        pif.setProjectName(pif.getArtifactId());
        pif.setMainClassName(getMainClassName(pif.getArtifactId())+"Application");
        pif.setMainClass(pif.getGroupId()+"."+pif.getMainClassName());
        return pif;
    }

    private static void fieldSet(Object source, Field field, Object value){
        field.setAccessible(true);
        try {
            field.set(source,value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static String getMainClassName(String sourceStr){
        String[] A1 = sourceStr.split(" ");
        StringBuilder sb=new StringBuilder();
        for (String s : A1) {
            sb.append(capitalizeFirstLetter(s));
        }
        String[] A2 = sb.toString().split("_");
        sb=new StringBuilder();
        for (String s : A2) {
            sb.append(capitalizeFirstLetter(s));
        }
        String[] A3 = sb.toString().split("-");
        sb=new StringBuilder();
        for (String s : A3) {
            sb.append(capitalizeFirstLetter(s));
        }
        return sb.toString();
    }

    /**
     * 单词的首字母大写
     * @param tableName 原始单词
     * @return 首字母变大写后的单词
     */
    public static String capitalizeFirstLetter(String tableName) {
        return tableName.toUpperCase().substring(0, 1)+tableName.substring(1, tableName.length());
    }

    public static void main(String[] args) {
        String test="gr ege_fwe-fw";
        System.out.println(getMainClassName(test));
    }


}

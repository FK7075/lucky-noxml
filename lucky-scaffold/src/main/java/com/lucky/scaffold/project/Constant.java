package com.lucky.scaffold.project;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lucky.scaffold.file.FileCopy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/23 12:29
 */
public class Constant {

    /** 项目的组织ID */
    public static final String GROUP_ID="@:GroupId";
    /** 项目ID */
    public static final String ARTIFACT_ID="@:ArtifactId";
    /** 版本信息 */
    public static final String VERSION="@:Version";
    /** 项目名 */
    public static final String PROJECT_NAME="@:Name";
    /** 启动类全限定名 */
    public static final String MAIN_CLASS="@:MainClass";
    /** 替他的Maven依赖*/
    public static final String MAVEN_DEPENDENCY="@:Dependency";
    /** 启动类类名 */
    public static final String MAIN_NAME="@:MainName";
    public static final String $="@L@";
    public static final String PACKAGE="@:Package";
    public static final String JAVA="/src/main/java/";
    public static final String TOOL="/src/main/tool/scaffold/";
    public static final String RESOURCES="/src/main/resources/";
    public static final String TEST_JAVA="/src/test/java/";
    public static final String TEST_RESOURCES="/src/test/resources/";

    public static Map<String,String> getMavenDependency() throws IOException {
        Gson gson=new Gson();
        BufferedReader br=new BufferedReader(new InputStreamReader(FileCopy.class.getResourceAsStream("/temp/dependency-template.json"),"UTF-8"));
        Map<String, String> map = gson.fromJson(br, new TypeToken<Map<String, String>>() {
        }.getType());
        return map;
    }

}

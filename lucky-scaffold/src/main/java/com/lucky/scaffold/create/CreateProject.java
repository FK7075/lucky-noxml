package com.lucky.scaffold.create;

import com.lucky.scaffold.file.FileCopy;
import com.lucky.scaffold.project.ProjectInFo;
import org.apache.commons.io.IOUtils;
import static com.lucky.scaffold.project.Constant.*;

import java.io.*;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/23 15:00
 */
public class CreateProject {

    private static final ProjectInFo project=ProjectInFo.getFinalProjectInFo(ProjectInFo.inputProjectInfo());
    private static final InputStream POM_TEMP=CreateProject.class.getResourceAsStream("/temp/pom-template.xml");
    private static final InputStream IML_TEMP=CreateProject.class.getResourceAsStream("/temp/idea-template.iml");
    private static final InputStream MAIN_TEMP=CreateProject.class.getResourceAsStream("/temp/main-template.java");
    private static final InputStream TEST_TEMP=CreateProject.class.getResourceAsStream("/temp/test-template.java");
    private static final InputStream INI_HELP=CreateProject.class.getResourceAsStream("/temp/appconfig-help.ini");

    private static String projectPath;
    private static String groupId;

    static {
        projectPath = project.getProjectPath();
        projectPath=projectPath.endsWith("/")?projectPath:projectPath+"/";
        projectPath=projectPath+project.getArtifactId();
        groupId = project.getGroupId();
        groupId=groupId.replaceAll("\\.","/");
    }

    public static void main(String[] args) throws IOException {
        if(project.getMavenRepository()!=null){
            luckyImportToMavenLibrary();
        }
        System.out.printf("开始创建Lucky项目==>[%s]...\n",project.getArtifactId());
        createFolder();
        createFile();
        System.out.println("----------------------------------------------------------------------------------------------------");
        System.out.println("BUILD SUCCESS");
        System.out.println("----------------------------------------------------------------------------------------------------");
        System.out.printf("项目创建完成！\n项目所在位置：%s\n项  目  名  ：%s\n",project.getProjectPath(),project.getArtifactId());
    }

    /**
     * 将Lucky项目导入本地Maven创库
     */
    private static void luckyImportToMavenLibrary() throws IOException {
        if(project.getMavenRepository()!=null){
            System.out.printf("开始将Lucky导入本地Maven创库[%s]\n",project.getMavenRepository());
            String dir=System.getProperty("user.dir");
            dir=dir.endsWith(File.separator)?dir+"lucky/com":dir+File.separator+"lucky/com";
            FileCopy.copyFolder(new File(dir),new File(project.getMavenRepository()));
            System.out.printf("Lucky运行环境导入成功，位置：%s\n",project.getMavenRepository());
            System.out.println("----------------------------------------------------------------------------------------------------");
            System.out.println("Lucky依赖");
            System.out.println("----------------------------------------------------------------------------------------------------");
            System.out.printf("       <!-- Lucky运行环境 -->\n" +
                    "        <dependency>\n" +
                    "            <groupId>com.lucky.jacklamb</groupId>\n" +
                    "            <artifactId>lucky</artifactId>\n" +
                    "            <version>1.0.0</version>\n" +
                    "        </dependency>\n");
            System.out.println("----------------------------------------------------------------------------------------------------");
        }
    }

    /**
     * 创建必要的文件
     * @throws IOException
     */
    private static void createFile() throws IOException {
        String pom = IOUtils.toString(POM_TEMP,"UTF-8");
        pom=pom.replaceAll(GROUP_ID,project.getGroupId())
                .replaceAll(ARTIFACT_ID,project.getArtifactId())
                .replaceAll(VERSION,project.getVersion())
                .replaceAll(PROJECT_NAME,project.getProjectName())
                .replaceAll($,"\\$")
                .replaceAll(MAVEN_DEPENDENCY,ProjectInFo.addMavenDependencyString.toString())
                .replaceAll(MAIN_CLASS,project.getMainClass());
        FileOutputStream pomFile=new FileOutputStream(projectPath+"/pom.xml");
        IOUtils.write(pom,pomFile,"UTF-8");
        System.out.println("写入文件   [POM]      : pom.xml");

        String iml=IOUtils.toString(IML_TEMP,"UTF-8");
        FileOutputStream imlFile=new FileOutputStream(projectPath+"/"+project.getArtifactId()+".iml");
        IOUtils.write(iml,imlFile,"UTF-8");
        System.out.println("写入文件   [IDEA]     : "+project.getArtifactId()+".iml");

        String main=IOUtils.toString(MAIN_TEMP,"UTF-8");
        main=main.replaceAll(PACKAGE,project.getGroupId())
                .replaceAll(MAIN_NAME,project.getMainClassName());
        FileOutputStream mainFile=new FileOutputStream(projectPath+JAVA+groupId+"/"+project.getMainClassName()+".java");
        IOUtils.write(main,mainFile,"UTF-8");
        System.out.println("写入文件   [启动类]   : "+project.getMainClassName()+".java");

        String test=IOUtils.toString(TEST_TEMP,"UTF-8");
        test=test.replaceAll(PACKAGE,project.getGroupId())
                .replaceAll(MAIN_NAME,project.getMainClassName());
        FileOutputStream testFile=new FileOutputStream(projectPath+TEST_JAVA+groupId+"/"+project.getMainClassName()+"Test.java");
        IOUtils.write(test,testFile,"UTF-8");
        System.out.println("写入文件   [测试类]   : "+project.getMainClassName()+"Test.java");

        String help=IOUtils.toString(INI_HELP,"UTF-8");
        FileOutputStream helpFile=new FileOutputStream(projectPath+RESOURCES+"appconfig-help.ini");
        IOUtils.write(help,helpFile,"UTF-8");
        System.out.println("写入文件   [帮助文档] : appconfig-help.ini");

        new File(projectPath+RESOURCES+"appconfig.ini").createNewFile();
        System.out.println("写入文件   [配置文件] : appconfig.ini");
    }

    /**
     * 创建Maven项目的项目结构
     */
    private static void createFolder() {
        new File(projectPath+RESOURCES).mkdirs();
        System.out.println("创建文件夹 [PACKAGE]  : "+RESOURCES);
        new File(projectPath+TEST_RESOURCES).mkdirs();
        System.out.println("创建文件夹 [PACKAGE]  : "+TEST_RESOURCES);
        new File(projectPath+JAVA+groupId).mkdirs();
        System.out.println("创建文件夹 [PACKAGE]  : "+JAVA);
        new File(projectPath+TEST_JAVA+groupId).mkdirs();
        System.out.println("创建文件夹 [PACKAGE]  : "+TEST_JAVA);
    }
}

package com.lucky.scaffold.transfer;

import com.lucky.scaffold.file.FileCopy;
import com.lucky.scaffold.project.ProjectInFo;

import java.io.File;
import java.io.IOException;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/28 9:07
 */
public class Transfer {

    private static final ProjectInFo project=ProjectInFo.getFinalProjectInFo(ProjectInFo.getProjectInFo());
    private static final File locationLuckyMaven=new File(project.getMavenRepository()+"com/github/fk7075/");
    private static final File scaffoldLucky=new File(project.getScaffold()+"src/main/tool/scaffold/lucky/com/github/");
    private static final File scaffoldJar=new File(project.getScaffold()+"target/lucky-scaffold-jar-with-dependencies.jar");
    private static final File toolJar=new File(project.getScaffold()+"src/main/tool/scaffold/");

    public static void main(String[] args) throws IOException {
        FileCopy.copyFolder(locationLuckyMaven,scaffoldLucky);
        FileCopy.copyFiles(scaffoldJar,toolJar);
        System.out.println("------------------------------------------------------------------------");
        System.out.println("BUILD SUCCESS");
        System.out.println("------------------------------------------------------------------------");
    }
}

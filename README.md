<img src="static/image/Logo.png" alt="Logo" style="zoom:140%;" />

[TOC]

# Lucky使用文档

## 一.Lucky的简介

​		简而言之，言而简之，反正我不喜欢写简介......

## 二.下载安装

**1.源码地址：**

https://github.com/FK7075/lucky-noxml

**2.安装**

不急不急，有空再写........

## 三.编写第一个Lucky项目



**1.添加Lucky的依赖**

```xml
<!-- Lucky运行环境 -->
<dependency>
    <groupId>com.lucky.jacklamb</groupId>
    <artifactId>lucky</artifactId>
    <version>1.0.0</version>
</dependency>
```

**2.添加插件**

​	2.1编译插件

```xml
<!-- 编译插件 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <encoding>UTF-8</encoding>
        <compilerArgs>
            <!-- 添加编译参数-parameters -->
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

​	2.2打包插件

```xml
<!-- 打包插件1 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.0.1</version>
    <executions>
        <execution>
            <id>copy-dependencies</id>
            <phase>package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <outputDirectory>
                    ${project.build.directory}/lib
                </outputDirectory>
                <overWriteReleases>false</overWriteReleases>
                <overWriteSnapshots>false</overWriteSnapshots>
                <overWriteIfNewer>true</overWriteIfNewer>
            </configuration>
        </execution>
    </executions>
</plugin>

<!--打包插件2，用于将应用打包为可运行的Jar包-->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
        <archive>
            <manifest>
                <!-- 主函数所在类 -->
                <mainClass>
                    com.hzczx.nomal.NormlLitiagentApplication
                </mainClass>
            </manifest>
        </archive>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
    </configuration>
    <executions>
        <execution>
            <id>make-assembly</id> <!-- this is used for inheritance merges -->
            <phase>package</phase> <!-- 指定在打包节点执行jar包合并操作 -->
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```


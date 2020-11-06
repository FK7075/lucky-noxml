<img src="static/image/Logo.png" alt="Logo" style="zoom:140%;" />





## 一.简介

### 项目地址

- [Github](https://github.com/FK7075/lucky-noxml)
- [Gitee](https://gitee.com/Fk7075/lucky-noxml)

## 二.安装

###  Maven

在项目的pom.xml的dependencies中加入以下内容:

```xml
<dependency>
  <groupId>com.github.fk7075</groupId>
  <artifactId>lucky</artifactId>
  <version>1.1.0</version>
</dependency>
```



### Gradle

```json
implementation 'com.github.fk7075:lucky:1.1.0'
```



### 非Maven项目

点击下面链接，下载对应版本的`lucky-X.X.X-jar-with-dependencies.jar`即可：

- [Maven中央仓库](https://search.maven.org/artifact/com.github.fk7075/lucky)



> 注意：Lucky只支持JDK8+，再使用前请将您的JDK升级到JDK8+



### 脚手架安装

在[Github](https://github.com/FK7075/lucky-noxml)或者[Gitee](https://gitee.com/Fk7075/lucky-noxml)中下载整个项目的源代码，然后进入`/lucky-scaffold/src/main/tool/scaffold/`启动脚手架程序，根据指引输入相关的信息便可安装成功，完成安装后就可以使用Maven引入了。

- Windows         ：双击启动 `interactive.bat`
- Linux/MacOS  ：命令启动 `./interactive.sh`

> 注意：Lucky脚手架本质是一个快速生成（<u>基于IDEA和Maven</u>）Lucky项目结构的应用，但也包含了安装Lucky环境的功能，如需使用脚手架来**安装Lucky**请使用 `@m` 命令调出安装配置项并正确输入本地Maven仓库路径，输入完成后便会自动安装。具体操作如下图所示：

![luckyScaffold](static/image/luckyScaffold.png)



## 三.Hello World

1.创建一个简单的Maven项目（例如  `groupId`：**com.lucky.demo**，`artifactId`：**demo01**）

2在pom.xml中引入Lucky依赖

3.在<u>最外层包</u>下创建一个带Main方法的启动类并编写如下代码(如：**com.lucky.demo.DemoApplication**)

```java
/**
 * Lucky启动类
 * 用于启动内嵌的Tomcat服务器
 */
public class DemoApplication {

    public static void main( String[] args ) {
        LuckyApplication.run(DemoApplication.class,args);
    }
}
```

4.启动Main方法，打开浏览器访问[localhost:8080](http://localhost:8080),看到如下欢迎页面即表示Lucky已经启动成功

![luckyhelloworld](static/image/luckyhelloworld.png)
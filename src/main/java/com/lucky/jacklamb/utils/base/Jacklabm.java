package com.lucky.jacklamb.utils.base;

import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ScanConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;

public abstract class Jacklabm {

    public static final Logger log= LogManager.getLogger(ApplicationBeans.class);;

    private static ScanConfig sc;

    private static String FOUR;

    private static String FIVE;

    public static boolean first = true;

    static {
        try {
            FOUR=getHtmlString(Code.NOTFOUND);
            FIVE=getHtmlString(Code.ERROR);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 打印logo
     */
    public static void welcome() {
        if (!first)
            return;
        InputStream logoStream = ApplicationBeans.class.getResourceAsStream("/logo/logo.txt");
        if(logoStream!=null){
            first = false;
            try {
                log.info("找到自定义的启动logo文件：classpath: /logo/logo.txt");
                System.out.println(IOUtils.toString(logoStream));
                System.out.println(versionInfo());
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        sc = AppConfig.getAppConfig().getScanConfig();
        if (sc.getCustomLogo() != null) {
            first = false;
            System.out.println(sc.getCustomLogo());
            System.out.println(versionInfo());
            return;
        }
        first = false;
        System.out.println(sc.getLogo().getLogo());
        System.out.println(versionInfo());
    }


    /**
     * 根据错误信息填充404.html或者500.html
     * @param code
     * @param Message
     * @param Description
     * @return
     * @throws IOException
     */
    public static String exception(Code code, String Message, String Description) throws IOException {
        boolean isFive=code==Code.ERROR;
        String input = isFive?FIVE:FOUR;
        input = input.replaceAll("@:errTitle", code.errTitle.replaceAll("\\$", "LUCKY_RDS_CHAR_DOLLAR_0721"));
        input = input.replaceAll("@:errType", code.code + "");
        input = input.replaceAll("@:time", LuckyUtils.time());
        input = input.replaceAll("@:Message", Message.replaceAll("\\$", "LUCKY_RDS_CHAR_DOLLAR_0721"));
        input = input.replaceAll("@:Description", Description.replaceAll("\\$", "LUCKY_RDS_CHAR_DOLLAR_0721"));
        return input.replaceAll("LUCKY_RDS_CHAR_DOLLAR_0721", "\\$");
    }

    /**
     * 获取404.html或者500.html的代码
     * @param code
     * @return
     * @throws IOException
     */
    private static String getHtmlString(Code code) throws IOException {
        String htmlString;
        boolean isFive=code==Code.ERROR;
        InputStream html=isFive?ApplicationBeans.class.getResourceAsStream("/err/500.html"):
                    ApplicationBeans.class.getResourceAsStream("/err/404.html");
        if (html==null) {
            StringWriter sw = new StringWriter();
            BufferedReader br = isFive?new BufferedReader(new InputStreamReader(ApplicationBeans.class.getResourceAsStream("/config/500.html"), "UTF-8")):
                                       new BufferedReader(new InputStreamReader(ApplicationBeans.class.getResourceAsStream("/config/404.html"), "UTF-8"));
            IOUtils.copy(br, sw);
            htmlString = sw.toString();
        } else {
            htmlString = IOUtils.toString(html, "UTF-8");
        }
        return htmlString;
    }

    private static int getMaxLength(String os, String java, String lucky) {
        int os_l = os.length();
        int java_l = java.length();
        int lucky_l = lucky.length();
        int temp = os_l > java_l ? os_l : java_l;
        return temp > lucky_l ? temp : lucky_l;
    }

    private static String getSameStr(String str, int maxLength) {
        if (str.length() == maxLength) {
            return str;
        }
        int poor = maxLength - str.length();
        for (int i = 0; i < poor; i++)
            str += " ";
        return str;
    }

    /**
     * 获取版本信息(OS,Java,Lucky)
     * @return
     */
    public static String versionInfo(){
        String os = ":: " + System.getProperty("os.name");
        String osvsersion = "           :: (v" + System.getProperty("os.version") + ")";
        String java = ":: Java";
        String javaversioin = "           :: (v" + System.getProperty("java.version") + ")";
        String lucky = ":: Lucky";
        String luckyversion = "           :: (v1.0.0.RELEASE)";
        int maxLength = getMaxLength(os, java, lucky);
        String d="";
        d += "\n\t\t" + getSameStr(os, maxLength) + osvsersion;
        d += "\n\t\t" + getSameStr(java, maxLength) + javaversioin;
        d += "\n\t\t" + getSameStr(lucky, maxLength) + luckyversion+"\n\n";
        return d;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(exception(Code.ERROR, "$M", "D"));
    }

}

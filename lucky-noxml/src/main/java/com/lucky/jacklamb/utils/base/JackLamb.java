package com.lucky.jacklamb.utils.base;

import com.lucky.jacklamb.enums.Logo;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ScanConfig;
import com.lucky.jacklamb.servlet.core.Model;
import com.lucky.jacklamb.utils.file.FileUtils;
import com.lucky.jacklamb.utils.file.Resources;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public abstract class JackLamb {

    public static final Logger log = LogManager.getLogger(JackLamb.class);
    private static ScanConfig sc;
    public static boolean first = true;


    /**
     * 打印logo
     */
    public static void welcome() {
        if (!first)
            return;
        InputStream logoStream = Resources.getInputStream(StaticFile.USER_LOGO_FILE);
        if (logoStream != null) {
            first = false;
            try {
                Console.white(IOUtils.toString(logoStream));
                versionInfo();
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        sc = AppConfig.getAppConfig().getScanConfig();
        if (sc.getCustomLogo() != null) {
            first = false;
            Console.white(sc.getCustomLogo());
            versionInfo();
            return;
        }
        first = false;
        Console.white(sc.getLogo().getLogo());
        versionInfo();
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
    public static void versionInfo() {
        String os = ":: " + System.getProperty("os.name");
        String osvsersion = "           :: (v" + System.getProperty("os.version") + ")";
        String java = ":: Java";
        String javaversioin = "           :: (v" + System.getProperty("java.version") + ")";
        String lucky = ":: Lucky";
        String luckyversion = "           :: ("+Version.version()+")";
        int maxLength = getMaxLength(os, java, lucky);
        String d = "";
        Console.print("\n\n    ");Console.white( getSameStr(java, maxLength));Console.white(javaversioin);
        Console.print("\n    ");Console.white( getSameStr(lucky, maxLength));Console.white(luckyversion);
        Console.green("\n    ");Console.white( getSameStr(os, maxLength));Console.white(osvsersion+"\n\n");
    }

    public static void welcome(Model model) throws IOException {
        FileUtils.preview(model,Resources.getInputStream(StaticFile.LUCKY),"LUCKY.html");
    }

    public static void main(String[] args) {
        System.out.println(Logo.LUCKY.getLogo());
        versionInfo();
    }

}

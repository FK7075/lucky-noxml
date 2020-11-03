package com.lucky.jacklamb.utils.base;

import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ScanConfig;
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
    public static String versionInfo() {
        String os = ":: " + System.getProperty("os.name");
        String osvsersion = "           :: (v" + System.getProperty("os.version") + ")";
        String java = ":: Java";
        String javaversioin = "           :: (v" + System.getProperty("java.version") + ")";
        String lucky = ":: Lucky";
        String luckyversion = "           :: (v1.1.1.RELEASE)";
        int maxLength = getMaxLength(os, java, lucky);
        String d = "";
        d += "\n\t\t" + getSameStr(os, maxLength) + osvsersion;
        d += "\n\t\t" + getSameStr(java, maxLength) + javaversioin;
        d += "\n\t\t" + getSameStr(lucky, maxLength) + luckyversion + "\n\n";
        return d;
    }

}

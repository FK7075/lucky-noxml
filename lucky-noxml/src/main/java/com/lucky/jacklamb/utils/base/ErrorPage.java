package com.lucky.jacklamb.utils.base;

import com.lucky.jacklamb.enums.Code;
import com.lucky.jacklamb.utils.file.Resources;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/10/30 8:46 下午
 */
public abstract   class ErrorPage {

    private final static String LUCKY_STR="LUCKY_RDS_CHAR_DOLLAR_0721";
    private static String FOUR;
    private static String FIVE;

    static {
        try {
            FOUR = getHtmlString(Code.NOTFOUND);
            FIVE = getHtmlString(Code.ERROR);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据错误信息填充404.html或者500.html
     *
     * @param code
     * @param Message
     * @param Description
     * @return
     * @throws IOException
     */
    public static String exception(Code code, String Message, String Description) throws IOException {
        boolean isFive = code == Code.ERROR;
        String input = isFive ? FIVE : FOUR;
        input = input.replaceAll("@:errTitle", code.errTitle.replaceAll("\\$", LUCKY_STR));
        input = input.replaceAll("@:errType", code.code + "");
        input = input.replaceAll("@:time", LuckyUtils.time());
        input = input.replaceAll("@:Message", Message.replaceAll("\\$", LUCKY_STR));
        input = input.replaceAll("@:Description", Description.replaceAll("\\$", LUCKY_STR));
        return input.replaceAll(LUCKY_STR, "\\$");
    }

    /**
     * 获取404.html或者500.html的代码
     *
     * @param code
     * @return
     * @throws IOException
     */
    private static String getHtmlString(Code code) throws IOException {
        String htmlString;
        boolean isFive = code == Code.ERROR;
        InputStream html = isFive ? Resources.getInputStream(StaticFile.USER_ERR_500) :
                Resources.getInputStream(StaticFile.USER_ERR_404);
        if (html == null) {
            StringWriter sw = new StringWriter();
            Reader br = isFive ? Resources.getReader(StaticFile.ERR_500):Resources.getReader(StaticFile.ERR_404);
            IOUtils.copy(br, sw);
            htmlString = sw.toString();
        } else {
            htmlString = IOUtils.toString(html, "UTF-8");
        }
        return htmlString;
    }
}

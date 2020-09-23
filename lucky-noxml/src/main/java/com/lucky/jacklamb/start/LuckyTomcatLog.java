package com.lucky.jacklamb.start;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.CharArrayWriter;

public class LuckyTomcatLog extends AccessLogValve {

    /**
     * The descriptive information about
     * this implementation.
     */
    protected static final String info =
            "com.huawei.cloudsop.tomcat.api.log.AccessLogValveExt/1.0";

    private static final Logger accessLog =LogManager.getLogger(LuckyTomcatLog.class);

    // rewrite log func of
    public void log(CharArrayWriter message) {
        accessLog.info(message.toString());
    }

    @Override
    public void log(Request request, Response response, long time) {
        super.log(request, response, time);
    }

    public String getInfo() {
        return info;
    }

    @Override
    protected synchronized void open() {
        // do nothing
    }
}

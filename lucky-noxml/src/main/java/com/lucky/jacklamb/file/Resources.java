package com.lucky.jacklamb.file;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/10/3 12:34 上午
 */
public abstract class Resources {

    public static InputStream getInputStream(String filePath){
        return Resources.class.getResourceAsStream(filePath);
    }

    public static Reader getReader(String filePath){
        return new BufferedReader(new InputStreamReader(getInputStream(filePath)));
    }
}

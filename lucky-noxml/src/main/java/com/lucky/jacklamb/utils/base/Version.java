package com.lucky.jacklamb.utils.base;

import com.lucky.jacklamb.utils.file.Resources;

import java.io.IOException;

import static com.lucky.jacklamb.utils.base.StaticFile.LUCKY_VERSION;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/8 11:50 上午
 */
public abstract class Version {

    public static String version(){
        try {
            return Resources.getString(LUCKY_VERSION);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

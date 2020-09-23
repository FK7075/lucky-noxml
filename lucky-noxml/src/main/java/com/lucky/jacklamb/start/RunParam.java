package com.lucky.jacklamb.start;

import com.lucky.jacklamb.utils.reflect.ClassUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class RunParam {

    public static final String SERVER_PORT="server.port";

    public static final String LUCKY_CONFIG_LOCATION="lucky.config.location";

    public static List<String> params;

    static{
        String[] p={SERVER_PORT,LUCKY_CONFIG_LOCATION};
        params=Arrays.asList(p);
    }

    public static boolean isRunParam(String paramName){
       return params.contains(paramName);
    }


}

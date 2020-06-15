package com.lucky.jacklamb.sqlcore.datasource;

import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;

import java.lang.reflect.Field;
import java.util.Map;

public interface LuckyDataSource {

    default public LuckyDataSource getDataSource(Class<? extends LuckyDataSource> dataClass,Map<String,String> dataSectionMap) throws IllegalAccessException, InstantiationException {
        Field[] fields = dataClass.getFields();
        String fieldName;
        LuckyDataSource ldata=dataClass.newInstance();
        for (Field field : fields) {
            fieldName=field.getName();
            if(dataSectionMap.containsKey(fieldName)){
                field.setAccessible(true);
                field.set(ldata,dataSectionMap.get(fieldName));
            }
        }
        return ldata;
    }
}

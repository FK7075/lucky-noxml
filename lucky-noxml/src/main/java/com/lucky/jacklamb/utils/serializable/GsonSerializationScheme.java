package com.lucky.jacklamb.utils.serializable;

import com.lucky.jacklamb.rest.LSON;
import com.lucky.jacklamb.utils.reflect.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/11/12 10:58
 */
public class GsonSerializationScheme implements JSONSerializationScheme {

    private static LSON lson=new LSON();


    @Override
    public String serialization(Object object) throws IOException {
        if(object instanceof String){
            return object.toString();
        }
        return lson.toJsonByGson(object);
    }

    @Override
    public Object deserialization(Type objectType, String objectStr) throws Exception {
        return lson.fromJson(objectType,objectStr);
    }
}

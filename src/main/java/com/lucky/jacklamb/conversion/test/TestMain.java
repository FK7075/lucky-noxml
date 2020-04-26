package com.lucky.jacklamb.conversion.test;

import com.lucky.jacklamb.conversion.proxy.ConversionProxy;


import java.util.*;

public class TestMain {

    public static void main(String[] args) throws IllegalAccessException {
        ConversionProxy c=new ConversionProxy();
        User u=new User();
        String[] array={"w","d","r","fd"};
        Map<String,Double> map=new HashMap<>();
        map.put("ss",12.5);
        map.put("cc",33.3);
       TypeO t=new TypeO();
        t.setTypeID(34);
        t.setMap(map);
        t.setTypeName("高效");
        List<TypeO> list=new ArrayList<>();
        list.add(t);
        u.setId(1);
        u.setStringList(Arrays.asList(array));
        u.setArray(array);
        u.setName("Jack");
        u.setMath(22.5);
        u.setType(t);
        u.setType0list(list);
        Map<String, Object> user = c.getSourceNameValueMap(u, "");
        System.out.println(u.getClass()== User.class);
        System.out.println(user);
        System.out.println(user.get("Collection<"+ TypeO.class.getName()+">"));
    }

}

package com.lucky.jacklamb.expression;

import com.lucky.jacklamb.file.ini.INIConfig;
import com.lucky.jacklamb.mapping.Regular;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class $Expression {

    private static INIConfig ini=new INIConfig();

    public static String translation(String original){
        if(!original.contains("${")||!original.contains("}"))
            return original;
        List<String> $_key= Regular.getArrayByExpression(original.trim(),Regular.$_$);
        List<String> key=$_key.stream().map(a->a.substring(2,a.length()-1)).collect(Collectors.toList());
        for(int i=0;i<$_key.size();i++){
            original=original.replace($_key.get(i),tranWord(key.get(i)));
        }
        return original;
    }

    public static String translation(String original, Map<String,String> source){
        if(!original.contains("${")||!original.contains("}"))
            return original;
        List<String> $_key= Regular.getArrayByExpression(original.trim(),Regular.$_$);
        List<String> key=$_key.stream().map(a->a.substring(2,a.length()-1)).collect(Collectors.toList());
        for(int i=0;i<$_key.size();i++){
            original=original.replace($_key.get(i),source.get(key.get(i)));
        }
        return original;
    }

    public static String translationSharp(String original, Map<String,? extends Object> source){
        if(!original.contains("#{")||!original.contains("}"))
            return original;
        List<String> $_key= Regular.getArrayByExpression(original.trim(),Regular.Sharp);
        List<String> key=$_key.stream().map(a->a.substring(2,a.length()-1)).collect(Collectors.toList());
        for(int i=0;i<$_key.size();i++){
            original=original.replace($_key.get(i),source.get(key.get(i)).toString());
        }
        return original;
    }



    public static <T> T translation(String original,Class<T> clzz){
        return (T) JavaConversion.strToBasic(translation(original),clzz);
    }

    private static String tranWord(String word){
        if(word.startsWith("[")){
            String[] _arr=word.split(":");
            return ini.getValue(_arr[0].substring(1,_arr[0].length()-1),_arr[1]);
        }else{
            return ini.getAppParam(word);
        }

    }
}

package com.lucky.jacklamb.expression;

import com.lucky.jacklamb.annotation.mvc.ExceptionHander;
import com.lucky.jacklamb.file.ini.INIConfig;
import com.lucky.jacklamb.mapping.Regular;

import java.util.List;
import java.util.stream.Collectors;

public abstract class $Expression {

    private static INIConfig ini=new INIConfig();

    public static String translation(String original){
        List<String> key= Regular.getArrayByExpression(original.trim(),Regular.$_$).stream()
                .map(a->a.substring(2,a.length()-1))
                .collect(Collectors.toList());
        Object[] value=new Object[key.size()];
        for(int i=0;i<value.length;i++){
            value[i]=tranWord(key.get(i));
        }
        return ExpressionEngine.removeSymbol(original,value,"${","}");
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

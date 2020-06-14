package com.lucky.jacklamb.servlet.mapping.regula;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Regular {

    /**
     * ${}
     */
    public static final String $_$="\\$\\{[\\w|:|\\[|\\]|.|-]+\\}";

    public static final String Sharp="\\#\\{[\\w|:|\\[|\\]|.|-]+\\}";

    /**
     * 带数字标识的预编译SQL
     */
    public static final  String NUMSQL="\\?\\d+";

    public static final String $SQL="\\@:[_a-zA-Z][_a-zA-Z0-9]*";

    /**
     * 邮箱
     */
    public static final String Email="^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";

    /**
     * 手机号码
     */
    public static final String PhoneNumber="^([1][3,4,5,6,7,8,9])\\d{9}$";

    /**
     * 身份证号码
     */
    public static final String IdCard="^d{15}|d{}18$";

    /**
     * 域名
     */
    public static final String DomainName="[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(/.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+/.?";

    /**
     * URL
     */
    public static final String URL="[a-zA-z]+://[^\\s]*";

    /**
     *  帐号是否合法(字母开头，允许5-16字节，允许字母数字下划线)
     */
    public static final String Account="^[a-zA-Z][a-zA-Z0-9_]{4,15}$";

    /**
     * 短身份证号码(数字、字母x结尾)
     */
    public static final String ShortIdCard="^([0-9]){7,18}(x|X)?$";

    /**
     * 腾讯QQ
     */
    public static final String QQ="[1-9][0-9]{4,}";

    /**
     * 邮政编码
     */
    public final static String ZipCode="[1-9]\\d{5}(?!\\d)";

    /**
     * Ip地址
     */
    public static final String IP="\\d+\\.\\d+\\.\\d+\\.\\d+";

    /**
     * 强密码(必须包含大小写字母和数字的组合，不能使用特殊字符，长度在8-10之间)
     */
    public static final String StrongPassword="^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,10}$";

    public static boolean check(String tarStr,String regular){
        Pattern pattern=Pattern.compile(regular);
        return pattern.matcher(tarStr).matches();
    }

    public static boolean check(String tarStr,String[] regulars){
        Pattern pattern;
        for(String regex:regulars){
            pattern=Pattern.compile(regex);
            if(pattern.matcher(tarStr).matches())
                return true;
        }
        return false;
    }

    public static List<String> getArrayByExpression(String original, String reg){
        List<String> expressions=new ArrayList<>();
        Pattern patten = Pattern.compile(reg);//编译正则表达式
        Matcher matcher = patten.matcher(original);// 指定要匹配的字符串

        while (matcher.find()) { //此处find（）每次被调用后，会偏移到下一个匹配
            expressions.add(matcher.group());//获取当前匹配的值
        }
        return expressions;
    }

    public static void main(String[] args) {
        String sql="SELECT * FROM user WHERE a=@:name AND b=@:sex AND c=@:id ";
        System.out.println(getArrayByExpression(sql, $SQL));
        System.out.println(sql.replaceAll($SQL, "?"));
        System.out.println(check(sql, $SQL));
    }
}

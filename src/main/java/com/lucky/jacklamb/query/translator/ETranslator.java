package com.lucky.jacklamb.query.translator;

import com.lucky.jacklamb.sqlcore.util.PojoManage;
import com.lucky.jacklamb.utils.reflect.ClassUtils;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * 实体翻译器，可以使用此对象完成对象化的SQL语句的书写
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/16 10:50 上午
 */
public class ETranslator extends Translator{

    public ETranslator(Class<?> pojoClass) {
        super(pojoClass);
    }

    public static void main(String[] args) {
        ETranslator et=new ETranslator(Book.class);
        Class<?>[] genericType = ClassUtils.getGenericType(et.getClass().getGenericSuperclass());
        Book b=new Book();
        b.setId("book-id");
        b.setName("NAME");
        b.setInventory(123);
        et.where().allEq(b).in("inventory","SELECT inventory FROM table",new Object[]{});
        System.out.println(et.getSql());
        System.out.println(et.getParams());

    }
}

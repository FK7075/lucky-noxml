package com.lucky.jacklamb.query.translator;

import java.util.Map;

/**
 * 实体翻译器，可以使用此对象完成对象化的SQL语句的书写
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/16 10:50 上午
 */
public class ETranslator<E> extends Translator<E>{


    @Override
    public Translator setSqlSelect(String... columns) {
        return null;
    }
    @Override
    public Translator having(String havingSQl, Object... params) {
        return null;
    }
    @Override
    public Translator exists(String value) {
        return null;
    }

    @Override
    public Translator notExists(String value) {
        return null;
    }

    public static void main(String[] args) {
        ETranslator<Book> et=new ETranslator();
        Book b=new Book();
        b.setId("book-id");
        b.setName("NAME");
        b.setInventory(123);
        et.orS().allEq(b).end();
        System.out.println(et.getSql());
        System.out.println(et.getParams());

    }
}

package com.lucky.jacklamb.query.translator;

import com.lucky.jacklamb.sqlcore.util.PojoManage;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 翻译器，将对象化的查询语句转化为SQL语句
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/16 10:51 上午
 */
public abstract class Translator<E> {

    private StringBuilder sql;

    private List<Object> params;

    public StringBuilder getSql() {
        return sql;
    }

    public List<Object> getParams() {
        return params;
    }

    public Translator(){
        sql=new StringBuilder();
        params=new ArrayList<>();
    }

    public abstract Translator setSqlSelect(String...columns);

    public Translator add(){
        sql.append(" AND ");
        return this;
    }

    public Translator add(String sqlAnd,Object...params){
        sql.append(" AND ").append(sqlAnd).append(" ");
        Stream.of(params).forEach(this.params::add);
        return this;
    }

    public Translator or(){
        sql.append(" OR ");
        return this;
    }

    public Translator or(String sqlOr,Object...params){
        sql.append(" OR ").append(sqlOr).append(" ");
        Stream.of(params).forEach(this.params::add);
        return this;
    }

    public Translator orS(){
        sql.append(" OR (");
        return this;
    }

    public Translator andS(){
        sql.append(" AND (");
        return this;
    }

    public Translator end(){
        sql.append(")");
        return this;
    }

    public Translator eq(String columns,Object param){
        if(isEndBrackets())
            add();
        sql.append(columns).append(" =?");
        params.add(param);
        return this;
    }

    private Field[] allField;
    public Translator allEq(E pojo){
        if(allField==null){
            allField= ClassUtils.getAllFields(pojo.getClass());
        }
        for (Field field : allField) {
            Object value = FieldUtils.getValue(pojo, field);
            if(value!=null)
                eq(PojoManage.getTableField(field),value);
        }
        return this;
    }

    public Translator allEq(Map<String,Object> map){
        for(Map.Entry<String,Object> en:map.entrySet()){
            eq(en.getKey(),en.getValue());
        }
        return this;
    }

    /** 不等于*/
    public Translator ne(String columns,Object param){
        if(isEndBrackets())
            add();
        sql.append(columns).append( "<>? ");
        params.add(param);
        return this;
    }

    /** 大于 >*/
    public Translator gt(String columns,Object param){
        if(isEndBrackets())
            add();
        sql.append(columns).append(" >? ");
        params.add(param);
        return this;
    }

    /** 大于等于 >=*/
    public Translator ge(String columns,Object param){
        sql.append(" ").append(columns).append(" >=? ");
        params.add(param);
        return this;
    }

    /** 小于 <*/
    public Translator lt(String columns,Object param){
        if(isEndBrackets())
            add();
        sql.append(columns).append(" <? ");
        params.add(param);
        return this;
    }

    /** 小于等于 <=*/
    public Translator le(String columns,Object param){
        if(isEndBrackets())
            add();
        sql.append(columns).append(" <=?");
        params.add(param);
        return this;
    }

    public Translator like(String columns,Object param){
        if(isEndBrackets())
            add();
        sql.append(columns).append(" LIKE ?c");
        params.add(param);
        return this;
    }

    public Translator notLike(String columns,Object param){
        sql.append(columns).append("NOT LIKE ?c");
        params.add(param);
        return this;
    }

    public Translator in(String columns, Collection<?> collection){
        if(isEndBrackets())
            add();
        sql.append(columns).append(" IN ?C");
        params.add(collection);
        return this;
    }

    public Translator notIn(String columns, Collection<?>collection){
        if(isEndBrackets())
            add();
        sql.append(columns).append(" NOT IN ?C");
        params.add(collection);
        return this;
    }

    public Translator isNull(String columns){
        if(isEndBrackets())
            add();
        sql.append(columns).append(" IS NULL");
        return this;
    }

    public Translator isNotNull(String columns){
        if(isEndBrackets())
            add();
        sql.append(columns).append(" IS NOT NULL");
        return this;
    }

    public Translator groupBy(String columns){
        if(isEndBrackets())
            add();
        sql.append(" GROUP BY ").append(columns).append(" ");
        return this;
    }

    public abstract Translator having(String havingSQl,Object...params);

    public Translator orderAsc(String columns){
        sql.append(" ORDER BY ").append(columns).append(" ASC ");
        return this;
    }

    public Translator orderDesc(String columns){
        sql.append(" ORDER BY ").append(columns).append(" DESC ");
        return this;
    }

    public abstract Translator exists(String value);

    public abstract Translator notExists(String value);

    public Translator between(String column,Object val1,Object val2){
        if(isEndBrackets())
            add();
        sql.append(column).append(" BETWEEN ? AND ?");
        params.add(val1);
        params.add(val2);
        return this;
    }

    public Translator notBetween(String column,Object val1,Object val2){
        if(isEndBrackets())
            add();
        sql.append(column).append(" NOT BETWEEN ? AND ?");
        params.add(val1);
        params.add(val2);
        return this;
    }

    public void last(String sql,Object...params){
        this.sql.append(" ").append(sql);
        Stream.of(params).forEach(this.params::add);
    }

    public boolean isEndBrackets(){
        String trim = sql.toString().trim().toUpperCase();
        return !trim.endsWith("AND")&&!trim.endsWith("OR")&&!trim.endsWith("(")&&!trim.endsWith("WHERE");
    }
}

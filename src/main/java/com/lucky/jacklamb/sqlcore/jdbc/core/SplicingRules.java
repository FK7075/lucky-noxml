package com.lucky.jacklamb.sqlcore.jdbc.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/8/19 10:24
 */
public class SplicingRules {

    private StringBuilder pSql;

    private List<Object> params;

    private Map<String,Object> data;

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    private String addPrefix;
    private String addSuffix;
    private String removePrefix;
    private String removeSuffix;

    /**
     * 添加固定前缀
     * @param prefix
     */
    public void addFixedPrefix(String prefix) {
        this.addPrefix = prefix;
    }

    /**
     * 添加固定后缀
     * @param suffix
     */
    public void addFixedSuffix(String suffix) {
        this.addSuffix = suffix;
    }

    /**
     * 去除SQL拼接过程产生的多余前缀
     * @param redundantPrefix
     */
    public void removeRedundantPrefix(String redundantPrefix) {
        this.removePrefix = redundantPrefix;
    }

    /**
     * 去除SQL拼接过程产生的多余后缀
     * @param redundantSuffix
     */
    public void removeRedundantSuffix(String redundantSuffix) {
        this.removeSuffix = redundantSuffix;
    }

    public SplicingRules() {
        pSql=new StringBuilder("");
        params=new ArrayList<>();
    }

    public SplicingRules(String pSql, List<Object> params) {
        this.pSql =new StringBuilder(pSql);
        this.params = params;
    }

    public SplicingRules addSqlAndParam(String sqlPassage, Object...params){
        pSql.append(sqlPassage);
        this.params.addAll(Arrays.asList(params));
        return this;
    }

    /**
     * 获取当前PSQL
     * @return
     */
    public String getCurrPSql(){
        return pSql.toString();
    }

    public String getpSql(){
        if(removePrefix!=null){
            if(pSql.toString().startsWith(removePrefix)){
                pSql=new StringBuilder(pSql.substring(removePrefix.length()));
            }
        }
        if(addPrefix!=null){
            String currsql = pSql.toString();
            if(!"".equals(currsql)&&!currsql.trim().startsWith(addPrefix.trim())){
                pSql=new StringBuilder(addPrefix).append(pSql);
            }
        }
        if(removeSuffix!=null){
            if(pSql.toString().endsWith(removeSuffix)){
                pSql=new StringBuilder(pSql.substring(0,pSql.length()-removeSuffix.length()));
            }
        }
        if(addSuffix!=null){
            if(!pSql.toString().trim().endsWith(addSuffix.trim()))
                pSql.append(addSuffix);
        }
        return pSql.toString();
    }

    public void setpSql(String sqlPassage){
        pSql.append(sqlPassage);
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public void addParams(Object...params) {
        Stream.of(params).forEach(this.params::add);
    }

    public Object $(String name){
        return data.get(name);
    }
}



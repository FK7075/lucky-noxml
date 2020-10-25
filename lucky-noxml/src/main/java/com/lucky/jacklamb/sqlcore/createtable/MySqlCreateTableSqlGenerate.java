package com.lucky.jacklamb.sqlcore.createtable;

import com.lucky.jacklamb.enums.PrimaryType;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
import com.lucky.jacklamb.sqlcore.util.PojoManage;
import com.lucky.jacklamb.tcconversion.typechange.JDBChangeFactory;
import com.lucky.jacklamb.tcconversion.typechange.TypeConversion;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.regula.Regular;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/10/23 13:10
 */
public class MySqlCreateTableSqlGenerate implements CreateTableSqlGenerate {
    
    private String ddlTemp="SHOW CREATE TABLE %s" ;

    @Override
    public String createTableSql(String dbname, Class<?> pojoClass) {
        TypeConversion jDChangeFactory = JDBChangeFactory.jDBChangeFactory(dbname);
        StringBuilder sql=new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append("`").append(PojoManage.getTable(pojoClass,dbname)).append("`").append(" (");
        Field[] fields = ClassUtils.getAllFields(pojoClass);
        for (int i = 0; i < fields.length; i++) {
            if(PojoManage.isNoColumn(fields[i],dbname)){
                continue;
            }
            String fieldType=fields[i].getType().getSimpleName();
            if (i < fields.length - 1) {
                if (PojoManage.getTableField(dbname,fields[i]).equals(PojoManage.getIdString(pojoClass,dbname))) {
                    sql.append("`").append(PojoManage.getIdString(pojoClass,dbname)).append("`").append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append("(").append(PojoManage.getLength(fields[i],dbname)).append(")")
                            .append(" NOT NULL ").append(isAutoInt(pojoClass,dbname)).append(" PRIMARY KEY,");
                } else if (!("double".equals(jDChangeFactory.javaTypeToDb(fieldType))
                        || "datetime".equals(jDChangeFactory.javaTypeToDb(fieldType))
                        || "date".equals(jDChangeFactory.javaTypeToDb(fieldType)))) {
                    sql.append("`").append(PojoManage.getTableField(dbname,fields[i])).append("`").append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append("(").append(PojoManage.getLength(fields[i],dbname)).append(") ")
                            .append(allownull(fields[i],dbname)).append(",");
                } else {
                    sql.append("`").append(PojoManage.getTableField(dbname,fields[i])).append("`").append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append(allownull(fields[i],dbname)).append(",");
                }
            } else {
                if (PojoManage.getTableField(dbname,fields[i]).equals(PojoManage.getIdString(pojoClass,dbname))) {
                    sql.append("`").append(PojoManage.getTableField(dbname,fields[i])).append("`").append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append("(").append(PojoManage.getLength(fields[i],dbname)).append(")")
                            .append(" NOT NULL AUTO_INCREMENT PRIMARY KEY");
                } else if (!("double".equals(jDChangeFactory.javaTypeToDb(fieldType))
                        || "datetime".equals(jDChangeFactory.javaTypeToDb(fieldType))
                        || "date".equals(jDChangeFactory.javaTypeToDb(fieldType)))) {
                    sql.append("`").append(PojoManage.getTableField(dbname,fields[i])).append("`").append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append("(").append(PojoManage.getLength(fields[i],dbname)).append(") ")
                            .append(allownull(fields[i],dbname));
                } else {
                    sql.append("`").append(PojoManage.getTableField(dbname,fields[i])).append("`").append(" ").append(jDChangeFactory.javaTypeToDb(fieldType)).append(allownull(fields[i],dbname));
                }
            }
        }
        if(sql.toString().trim().endsWith(",")){
            sql=new StringBuilder(sql.toString().substring(0,sql.lastIndexOf(",")));
        }
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=UTF8");
        return sql.toString();
    }

    @Override
    public List<String> deleteKeyAndIndexSQL(String queryCreateSQL, String tableName) {
        List<String> delete=new ArrayList<>();
        findTableKey(delete,queryCreateSQL,tableName);
        findTableIndex(delete,queryCreateSQL,tableName);
        return delete;
    }


    @Override
    public  List<String> addKeyAndIndexSQL(String dbname, Class<?> pojoClass) {
        String table_name=PojoManage.getTable(pojoClass,dbname);
        List<String> indexlist = new ArrayList<>();
        String primary = PojoManage.primary(pojoClass,dbname);
        String[] indexs = PojoManage.index(pojoClass,dbname);
        String[] fulltextes = PojoManage.fulltext(pojoClass,dbname);
        String[] uniques = PojoManage.unique(pojoClass,dbname);
        if(!"".equals(primary)){
            String p_key="ALTER TABLE `"+table_name+"` ADD PRIMARY KEY(`"+primary+"`)";
            indexlist.add(p_key);
        }
        addAll(indexlist,table_name,indexs,"INDEX");
        addAll(indexlist,table_name,fulltextes,"FULLTEXT");
        addAll(indexlist,table_name,uniques,"UNIQUE");
        return indexlist;
    }

    @Override
    public String getDDL(Connection conn, String tableName) {
        PreparedStatement ps=null;
        ResultSet rs=null;
        String ddl=null;
        try {
            ps = conn.prepareStatement(String.format(ddlTemp, tableName));
            rs = ps.executeQuery();
            while (rs.next()){
                return rs.getString(2);
            }
            throw new RuntimeException("获取\""+tableName+"\"表的DDL信息出错！SQL："+String.format(ddlTemp, tableName));
        }catch (SQLException e){
            throw new RuntimeException(e);
        }finally {
            LuckyDataSource.release(rs,ps,conn);
        }
    }

    private static void findTableKey(List<String> delete,String queryCreateSQL,String tableName){
        String delKeyTemp="ALTER TABLE %s DROP FOREIGN KEY %s";
        List<String> keys = Regular.getArrayByExpression(queryCreateSQL, "CONSTRAINT([\\s\\S]*?)FOREIGN KEY");
        keys.stream().forEach((key)->{
            key=key.replaceAll("CONSTRAINT","").replaceAll("FOREIGN KEY","");
            delete.add(String.format(delKeyTemp,tableName,key));
        });
    }

    private static void findTableIndex(List<String> delete,String queryCreateSQL,String tableName){
        String delIndexTemp="ALTER TABLE %s DROP INDEX %s";
        List<String> keys = Regular.getArrayByExpression(queryCreateSQL,"(KEY|FULLTEXT KEY|UNIQUE KEY)([\\s\\S]*?)\\(");
        keys.stream().forEach((key)->{
            key=key.substring(0,key.length()-1);
            if(key.startsWith("FULLTEXT KEY")){
                key=key.replaceFirst("FULLTEXT KEY","");
            }else if(key.startsWith("UNIQUE KEY")){
                key=key.replaceFirst("UNIQUE KEY","");
            }else{
                key=key.replaceFirst("KEY","");
            }
            if(!"".equals(key.trim())){
                delete.add(String.format(delIndexTemp,tableName,key));
            }
        });
    }

    /**
     * 设置主键类型
     * @param clzz
     * @return
     */
    private String isAutoInt(Class<?> clzz,String dbname) {
        PrimaryType idType = PojoManage.getIdType(clzz,dbname);
        if(idType==PrimaryType.AUTO_INT) {
            return "AUTO_INCREMENT";
        }
        return "";
    }

    /**
     * 是否允许为NULL
     * @param field
     * @return
     */
    private String allownull(Field field,String dbname) {
        if(PojoManage.allownull(field,dbname)) {
            return " DEFAULT NULL ";
        }
        return " NOT NULL ";
    }

    /**
     * 拼接该实体中需要配置的所有索引信息
     * @param indexlist
     * @param tablename
     * @param indexs
     * @param type
     */
    private void addAll(List<String> indexlist, String tablename, String[] indexs, String type) {
        String key="ALTER TABLE `"+tablename+"` ADD ";
        for(String index:indexs) {
            String indexkey;
            if("INDEX".equals(type)) {
                indexkey=key+type+" `"+getRandomStr()+"`(";
            } else {
                indexkey=key+type+"(";
            }
            indexkey+=index+")";
            indexlist.add(indexkey);
        }

    }

}

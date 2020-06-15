package com.lucky.jacklamb.tcconversion.createtable;

import com.lucky.jacklamb.sqlcore.datasource.AutoPackage;
import com.lucky.jacklamb.sqlcore.datasource.ReadIni;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateTable {
    private AutoPackage autoPackage;
    private List<Class<?>> classlist;
    private String dbname;


    public CreateTable(String dbname) {
        this.dbname = dbname;
        classlist = ReadIni.getDataSource(dbname).getCaeateTable();
        autoPackage = new AutoPackage(dbname);
    }

    /**
     * 根据实体创建单张表格以及索引
     *
     * @param tableClass
     */
    public void createTable(Class<?> tableClass) {
        List<String> createIndexSQL = CreateTableSql.getIndexKey(tableClass);
        String[] createSQLS = new String[createIndexSQL.size() + 1];
        createSQLS[0] = CreateTableSql.getCreateTable(dbname, tableClass);
        for (int i = 0; i < createIndexSQL.size(); i++) {
            createSQLS[i + 1] = createIndexSQL.get(i);
        }
        autoPackage.updateBatch(createSQLS);
    }

    /**
     * 实体类生成表、以及索引、外键
     */
    public void createTable() {
        List<String> SQLS = new ArrayList<>();
        DeleteKeySql dtlkeysql = new DeleteKeySql(dbname, classlist);
        List<String> createTableSQL = classlist.stream().map(a -> CreateTableSql.getCreateTable(dbname, a)).collect(Collectors.toList());
        String[] ctsqls=new String[createTableSQL.size()];
        createTableSQL.toArray(ctsqls);
        autoPackage.updateBatch(ctsqls);
        dtlkeysql.deleteKeySql().stream().forEach(SQLS::add);// 删除所有现有的外键的SQL
        for (Class<?> str : classlist) {
            CreateTableSql.getForeignKey(str).stream().forEach(SQLS::add);//添加外键的SQL
            CreateTableSql.getIndexKey(str).stream().forEach(SQLS::add);//添加索引的SQL
        }
        String[] sqls=new String[SQLS.size()];
        SQLS.toArray(sqls);
        autoPackage.updateBatch(sqls);
    }
}

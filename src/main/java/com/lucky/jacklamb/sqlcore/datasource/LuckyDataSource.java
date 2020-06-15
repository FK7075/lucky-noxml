package com.lucky.jacklamb.sqlcore.datasource;

import com.lucky.jacklamb.conversion.util.ClassUtils;
import com.lucky.jacklamb.exception.NoDataSourceException;
import com.lucky.jacklamb.ioc.scan.ScanFactory;
import com.lucky.jacklamb.sqlcore.datasource.enums.Pool;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class LuckyDataSource {

    private Pool poolType;
    private String dbname;

    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClass;

    private Boolean log;
    private Boolean cache;
    private Integer cacheCapacity;
    private Boolean formatSqlLog;
    private List<Class<?>> createTable;
    private String reversePack;
    private String srcPath;

    public LuckyDataSource(){
        createTable= ScanFactory.createScan().getPojoClass();
        poolType=Pool.C3P0;
        cache=true;
        log=false;
        cacheCapacity=50;
    }

    public Pool getPoolType() {
        return poolType;
    }

    public void setPoolType(Pool poolType) {
        this.poolType = poolType;
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public Boolean getLog() {
        return log;
    }

    public void setLog(Boolean log) {
        this.log = log;
    }

    public Boolean getCache() {
        return cache;
    }

    public void setCache(Boolean cache) {
        this.cache = cache;
    }

    public Integer getCacheCapacity() {
        return cacheCapacity;
    }

    public void setCacheCapacity(Integer cacheCapacity) {
        this.cacheCapacity = cacheCapacity;
    }

    public Boolean getFormatSqlLog() {
        return formatSqlLog;
    }

    public void setFormatSqlLog(Boolean formatSqlLog) {
        this.formatSqlLog = formatSqlLog;
        if(formatSqlLog){
            log=true;
        }
    }

    public List<Class<?>> getCreateTable() {
        return createTable;
    }

    public void setCreateTable(List<Class<?>> createTable) {
        this.createTable = createTable;
    }

    public void addCreateTable(Class<?>...createTable) {
        Stream.of(createTable).forEach(this.createTable::add);
    }

    public String getReversePack() {
        return reversePack;
    }

    public void setReversePack(String reversePack) {
        this.reversePack = reversePack;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    protected LuckyDataSource getDataSource(Map<String,String> dataSectionMap) {
        Field[] fields = ClassUtils.getAllFields(this.getClass());
        String fieldName;
        for (Field field : fields) {
            fieldName=field.getName();
            if(dataSectionMap.containsKey(fieldName)){
                field.setAccessible(true);
                String valueStr = dataSectionMap.get(fieldName);
                if("createTable".equals(fieldName)){
                    List<Class<?>> tables=new ArrayList<>();
                    String[] split = valueStr.split(",");
                    for (String tab : split) {
                        try {
                            tables.add(Class.forName(tab));
                        } catch (ClassNotFoundException e) {
                            throw new NoDataSourceException("不正确的自动建表配置信息，无法执行建表程序，请检查classpath下的appconfig.ini配置文件中["+dbname+"]节中的'createTable'属性的配置信息。err=>"+tab);
                        }
                    }
                    try {
                        field.set(this, tables);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }else if("poolType".equals(fieldName)){
                    continue;
                }else{
                    try {
                        field.set(this, JavaConversion.strToBasic(valueStr,field.getType()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("类型转换异常！ERROR -> String: ["+valueStr+"]==>"+field.getType());
                    }
                }
            }
        }
        return this;
    }
}

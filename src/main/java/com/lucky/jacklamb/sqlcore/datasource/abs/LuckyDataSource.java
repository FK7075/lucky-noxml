package com.lucky.jacklamb.sqlcore.datasource.abs;

import com.lucky.jacklamb.exception.NoDataSourceException;
import com.lucky.jacklamb.ioc.scan.ScanFactory;
import com.lucky.jacklamb.sqlcore.datasource.enums.Pool;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class LuckyDataSource implements DataSource{

    /**
     * 配置类和配置文件中的数据源
     */
    protected static List<? extends LuckyDataSource> datalist;

    /**
     * 转换后的各个数据源的JDBC数据源
     */
    public static Map<String, DataSource> dbMap;

    //数据库连接池的类型
    private Pool poolType;
    //数据源的标示
    private String dbname;

    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClass;

    private Boolean log;
    private Boolean cache;
    private String cacheType;
    private String cacheExpiredTime;
    private Integer cacheCapacity;
    private Boolean formatSqlLog;
    private List<Class<?>> createTable;
    private String reversePack;
    private String srcPath;

    public LuckyDataSource(){
        createTable= ScanFactory.createScan().getPojoClass();
        poolType=Pool.HIKARICP;
        dbname="defaultDB";
        cache=true;
        cacheType="Java";
        cacheExpiredTime="0";
        log=false;
        formatSqlLog=false;
        cacheCapacity=50;
    }

    public Pool getPoolType() {
        return poolType;
    }

    public void setPoolType(Pool poolType) {
        this.poolType = poolType;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public String getCacheExpiredTime() {
        return cacheExpiredTime;
    }

    public void setCacheExpiredTime(String cacheExpiredTime) {
        this.cacheExpiredTime = cacheExpiredTime;
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



    public Connection getConnection(){
        try {
            return getTripartiteDataSource().getConnection();
        } catch (SQLException throwables) {
            throw new NoDataSourceException(throwables);
        }
    }

    public void init(){
        if(dbMap==null) {
            LuckyDataSource2TripartiteDataSource();
        }
    }

    /**
     * 初始化数据源
     * 将所有的LuckyDataSource转换为标准的第三方DataSource
     * 所有LuckyDataSource在创建之后必须先初始化，才能使用
     */
    protected abstract void LuckyDataSource2TripartiteDataSource();

    /**
     * 获取第三方DataSource
     * @param <T>
     * @return
     */
    public <T extends DataSource> T getTripartiteDataSource() {
       return (T) dbMap.get(getDbname());
    }


    @Override
    public Connection getConnection(String username, String password){
        try {
            return getTripartiteDataSource().getConnection(username, password);
        } catch (SQLException throwables) {
            throw new NoDataSourceException(throwables);
        }
    }

    /**
     * 关闭数据库资源
     * @param rs ResultSet对象
     * @param ps PreparedStatement对象
     * @param conn Connection对象
     */
    public static void release(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("资源关闭错误！");
        }
    }


    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getTripartiteDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getTripartiteDataSource().isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getTripartiteDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getTripartiteDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getTripartiteDataSource().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getTripartiteDataSource().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getTripartiteDataSource().getParentLogger();
    }

    /**
     * 关闭连接池
     */
    public static void close(){
        for(Map.Entry<String,DataSource> entry: dbMap.entrySet()){
            DataSource dataSource = entry.getValue();
            if(dataSource instanceof HikariDataSource){
                HikariDataSource hds= (HikariDataSource) dataSource;
                hds.close();
                continue;
            }
            if(dataSource instanceof ComboPooledDataSource){
                ComboPooledDataSource cds=(ComboPooledDataSource)dataSource;
                cds.close();
                continue;
            }
        }
    }
}

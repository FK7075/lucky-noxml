package com.lucky.jacklamb.sqlcore.datasource.hikaricp;

import com.lucky.jacklamb.sqlcore.datasource.factory.LuckyDataSource;
import com.lucky.jacklamb.sqlcore.datasource.enums.Pool;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class HikariCPDataSource extends LuckyDataSource {

    private String dataSourceClassName;

    private Boolean autoCommit;
    private Integer connectionTimeout;
    private Integer idleTimeout;
    private Integer maxLifetime;
    private String connectionTestQuery;
    private Integer minimumIdle;
    private Integer maximumPoolSize;
    private String poolName;
    private Object metricRegistry;
    private Properties healthCheckRegistry;

    private Integer initializationFailTimeout;
    private Boolean isolateInternalQueries;
    private Boolean allowPoolSuspension;
    private Boolean readOnly;
    private Boolean registerMbeans;
    private String catalog;
    private String connectionInitSql;
    private String transactionIsolation;
    private Integer validationTimeout;
    private Integer leakDetectionThreshold;
    private DataSource dataSource;
    private String schema;
    private ThreadFactory threadFactory;
    private ScheduledExecutorService scheduledExecutorService;

    public String getDataSourceClassName() {
        return dataSourceClassName;
    }

    public void setDataSourceClassName(String dataSourceClassName) {
        this.dataSourceClassName = dataSourceClassName;
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Integer getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(Integer maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public String getConnectionTestQuery() {
        return connectionTestQuery;
    }

    public void setConnectionTestQuery(String connectionTestQuery) {
        this.connectionTestQuery = connectionTestQuery;
    }

    public Integer getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(Integer minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public Object getMetricRegistry() {
        return metricRegistry;
    }

    public void setMetricRegistry(Object metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public Properties getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    public void setHealthCheckRegistry(Properties healthCheckRegistry) {
        this.healthCheckRegistry = healthCheckRegistry;
    }

    public Integer getInitializationFailTimeout() {
        return initializationFailTimeout;
    }

    public void setInitializationFailTimeout(Integer initializationFailTimeout) {
        this.initializationFailTimeout = initializationFailTimeout;
    }

    public Boolean getIsolateInternalQueries() {
        return isolateInternalQueries;
    }

    public void setIsolateInternalQueries(Boolean isolateInternalQueries) {
        this.isolateInternalQueries = isolateInternalQueries;
    }

    public Boolean getAllowPoolSuspension() {
        return allowPoolSuspension;
    }

    public void setAllowPoolSuspension(Boolean allowPoolSuspension) {
        this.allowPoolSuspension = allowPoolSuspension;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getRegisterMbeans() {
        return registerMbeans;
    }

    public void setRegisterMbeans(Boolean registerMbeans) {
        this.registerMbeans = registerMbeans;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getConnectionInitSql() {
        return connectionInitSql;
    }

    public void setConnectionInitSql(String connectionInitSql) {
        this.connectionInitSql = connectionInitSql;
    }

    public String getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(String transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    public Integer getValidationTimeout() {
        return validationTimeout;
    }

    public void setValidationTimeout(Integer validationTimeout) {
        this.validationTimeout = validationTimeout;
    }

    public Integer getLeakDetectionThreshold() {
        return leakDetectionThreshold;
    }

    public void setLeakDetectionThreshold(Integer leakDetectionThreshold) {
        this.leakDetectionThreshold = leakDetectionThreshold;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public HikariCPDataSource(){
        super();
        setPoolType(Pool.HIKARICP);
        connectionTestQuery="SELECT 1";
        autoCommit=true;
        connectionTimeout=30000;
        idleTimeout=600000;
        maxLifetime=1800000;
        maximumPoolSize=10;
        minimumIdle=maximumPoolSize;
        initializationFailTimeout=1;
        isolateInternalQueries=false;
        allowPoolSuspension=false;
        readOnly=false;
        registerMbeans=false;
        validationTimeout=5000;
        leakDetectionThreshold=0;
    }
}


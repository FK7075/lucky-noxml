package com.lucky.jacklamb.sqlcore.datasource.hikaricp;

import com.lucky.jacklamb.sqlcore.datasource.LuckyDataSource;

import javax.sql.DataSource;
import java.util.concurrent.ThreadFactory;

public class HikariCPDataSource implements LuckyDataSource {

    public String dataSourceClassName;
    public String jdbcUrl;
    public String username;
    public String password;
    public String driverClass;

    public Boolean autoCommit;
    public Integer connectionTimeout;
    public Integer idleTimeout;
    public Integer maxLifetime;
    public String connectionTestQuery;
    public Integer minimumIdle;
    public Integer maximumPoolSize;
    public String poolName;
    public Object metricRegistry;
    public Object healthCheckRegistry;

    public Integer initializationFailTimeout;
    public Boolean isolateInternalQueries;
    public Boolean allowPoolSuspensio;
    public Boolean readOnly;
    public Boolean registerMbeans;
    public String catalog;
    public String connectionInitSql;
    public String transactionIsolation;
    public Integer validationTimeout;
    public Integer leakDetectionThreshold;
    public DataSource dataSource;
    public String schema;
    public ThreadFactory threadFactory;
    public String ScheduledExecutorService;


}


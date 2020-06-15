package com.lucky.jacklamb.sqlcore.datasource.c3p0;

import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.scan.ScanFactory;
import com.lucky.jacklamb.sqlcore.datasource.LuckyDataSource;
import com.lucky.jacklamb.sqlcore.datasource.enums.Pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class C3p0DataSource extends LuckyDataSource {


	private Integer acquireIncrement;

	private Integer initialPoolSize;

	private Integer maxPoolSize;

	private Integer minPoolSize;

	private Integer maxidleTime;

	private Integer maxConnectionAge;

	private Integer maxStatements;

	private Integer maxStatementsPerConnection;

	private Integer checkoutTimeout;


	public int getAcquireIncrement() {
		return acquireIncrement;
	}

	/**
	 * 连接池在无空闲连接可用时一次性创建的新数据库连接数,default : 3
	 * @param acquireIncrement
	 */
	public void setAcquireIncrement(int acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
	}

	public int getInitialPoolSize() {
		return initialPoolSize;
	}

	/**
	 * 连接池初始化时创建的连接数,default : 3，取值应在minPoolSize与maxPoolSize之间
	 * @param initialPoolSize
	 */
	public void setInitialPoolSize(int initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	/**
	 * 连接池中拥有的最大连接数，如果获得新连接时会使连接总数超过这个值则不会再获取新连接，而是等待其他连接释放 default : 15
	 * @param maxPoolSize
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}

	/**
	 * 连接池保持的最小连接数,default : 3
	 * @param minPoolSize
	 */
	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	public int getMaxidleTime() {
		return maxidleTime;
	}
	
	/**
	 * 连接的最大空闲时间，如果超过这个时间，某个数据库连接还没有被使用，则会断开掉这个连接。如果为0，则永远不会断开连接,即回收此连接。default : 0s
	 * @param maxidleTime
	 */
	public void setMaxidleTime(int maxidleTime) {
		this.maxidleTime = maxidleTime;
	}

	public int getMaxConnectionAge() {
		return maxConnectionAge;
	}

	/**
	 * 这个配置主要时为了减轻连接池的负载，配置不为 0 则会将连接池中的连接数量保持到minPoolSize，为 0 则不处理
	 * @param maxConnectionAge
	 */
	public void setMaxConnectionAge(int maxConnectionAge) {
		this.maxConnectionAge = maxConnectionAge;
	}

	public int getMaxStatements() {
		return maxStatements;
	}
	
	/**
	 * JDBC的标准参数，用以控制数据源内加载的PreparedStatements数量。
	 * 但由于预缓存的statements属于单个connection而不是整个连接池。所以设置这个参数需要考虑到多方面的因素。
	 * 果maxStatements与maxStatementsPerConnection均为0，则缓存被关闭。Default: 0
	 * @param maxStatements
	 */
	public void setMaxStatements(int maxStatements) {
		this.maxStatements = maxStatements;
	}

	public int getMaxStatementsPerConnection() {
		return maxStatementsPerConnection;
	}


	/**
	 * maxStatementsPerConnection定义了连接池内单个连接所拥有的最大缓存statements数。Default: 0
	 * @param maxStatementsPerConnection
	 */
	public void setMaxStatementsPerConnection(int maxStatementsPerConnection) {
		this.maxStatementsPerConnection = maxStatementsPerConnection;
	}
	
	public int getCheckoutTimeout() {
		return checkoutTimeout;
	}

	/**
	 * 当连接池用完时客户端调用getConnection()后等待获取新连接的时间，超时后将抛出
	 * SQLException,如设为0则无限期等待。单位毫秒。Default: 0
	 * @param checkoutTimeout
	 */
	public void setCheckoutTimeout(int checkoutTimeout) {
		this.checkoutTimeout = checkoutTimeout;
	}




	public C3p0DataSource() {
		super();
		setPoolType(Pool.C3P0);
		checkoutTimeout=30000;
		acquireIncrement=3;
		initialPoolSize=3;
		minPoolSize=1;
		maxPoolSize=15;
		maxidleTime=0;
		maxConnectionAge=0;
		maxStatements=0;
		maxStatementsPerConnection=0;
	}

}

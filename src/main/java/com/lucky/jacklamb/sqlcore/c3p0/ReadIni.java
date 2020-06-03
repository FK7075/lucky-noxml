package com.lucky.jacklamb.sqlcore.c3p0;

import static com.lucky.jacklamb.sqlcore.c3p0.IniKey.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lucky.jacklamb.exception.NoDataSourceException;
import com.lucky.jacklamb.exception.NotFindBeanPropertyException;
import com.lucky.jacklamb.file.ini.IniFilePars;
import com.lucky.jacklamb.ioc.ApplicationBeans;

public class ReadIni {

	private static List<DataSource> allDataSource;
	
	private static IniFilePars iniFilePars;
	
	public static List<DataSource> getAllDataSource() {
		allDataSources();
		return allDataSource;
	}
 
	public static List<DataSource> readList() {
		List<DataSource> dataList = new ArrayList<>();
		iniFilePars=IniFilePars.getIniFilePars();
		if(!iniFilePars.iniExist()||!iniFilePars.isHasSection(SECTION_JDBC))
			return null;
		if(!iniFilePars.isHasSection(SECTION_JDBC))
			throw new NotFindBeanPropertyException("在calsspath:appconfig.ini的配置文件中找不到必须的节["+SECTION_JDBC+"]！");
		dataList.add(readIni(SECTION_JDBC));
		String datasStr = iniFilePars.getValue(SECTION_DATASOURCES, DATA);
		if(datasStr!=null) {
			String[] datas=datasStr.split(",");
			for(String data:datas) {
				dataList.add(readIni(data));
			}
		}
		return dataList;
	}

	public static DataSource readIni(String section) {
		DataSource dataSource = new DataSource();
		String name, driverClass, jdbcUrl, user, password, acquireIncrement, initialPoolSize, maxPoolSize, minPoolSize,
		maxidleTime, maxConnectionAge, maxStatements,checkoutTimeout, maxStatementsPerConnection, reversePckage, log, formatSqlLog, cache,
		srcpath, createTable,poolMethod;
		if(SECTION_JDBC.equals(section))
			name="defaultDB";
		else
			name=section;
		Map<String, String> sectionMap = iniFilePars.getSectionMap(section);
		driverClass = sectionMap.get(DRIVER_CLASS);
		jdbcUrl = sectionMap.get(JDBC_URL);
		user = sectionMap.get(USER);
		password = sectionMap.get(PASSWORD);
		acquireIncrement = sectionMap.get(ACQUIREINCREMENT);
		initialPoolSize =sectionMap.get(INITIALPOOLSIZE);
		maxPoolSize = sectionMap.get(MAXPOOLSIZE);
		minPoolSize = sectionMap.get(MINPOOLSIZE);
		maxidleTime = sectionMap.get(MAXIDLETIME);
		maxConnectionAge = sectionMap.get(MAXCONNECTIONAGE);
		checkoutTimeout= sectionMap.get(CHECKOUTTIMEOUT);
		maxStatements = sectionMap.get(MAXSTATEMENTS);
		maxStatementsPerConnection = sectionMap.get(MAXSTATEMENTSPERCONNECTION);
		reversePckage = sectionMap.get(REVERSE_PCKAGE);
		log = sectionMap.get(LOG);
		formatSqlLog=sectionMap.get(FORMATSQLLOG);
		cache = sectionMap.get(CACHE);
		srcpath = sectionMap.get(SRCPATH);
		createTable = sectionMap.get(CREATE_TABLE);
		poolMethod=sectionMap.get(POOLMETHOD);
		if (driverClass == null || jdbcUrl == null || user == null || password == null || driverClass == ""
				|| jdbcUrl == "" || user == "" || password == "")
			throw new NotFindBeanPropertyException("在calsspath:appconfig.ini的配置文件的["+section+"]节中找不到必须属性\"driverClass\",\"jdbcUrl\",\"user\",\"password\"");
		dataSource.setName(name);
		dataSource.setDriverClass(driverClass);
		dataSource.setJdbcUrl(jdbcUrl);
		dataSource.setUser(user);
		dataSource.setPassword(password);
		if (acquireIncrement != null && acquireIncrement != "")
			dataSource.setAcquireIncrement(Integer.parseInt(acquireIncrement));
		if (initialPoolSize != null && initialPoolSize != "")
			dataSource.setInitialPoolSize(Integer.parseInt(initialPoolSize));
		if (maxPoolSize != null && maxPoolSize != "")
			dataSource.setMaxPoolSize(Integer.parseInt(maxPoolSize));
		if (checkoutTimeout != null && checkoutTimeout != "")
			dataSource.setCheckoutTimeout(Integer.parseInt(checkoutTimeout));
		if (minPoolSize != null && minPoolSize != "")
			dataSource.setMinPoolSize(Integer.parseInt(minPoolSize));
		if (maxidleTime != null && maxidleTime != "")
			dataSource.setMaxidleTime(Integer.parseInt(maxidleTime));
		if (maxConnectionAge != null && maxConnectionAge != "")
			dataSource.setMaxConnectionAge(Integer.parseInt(maxConnectionAge));
		if (maxStatements != null && maxStatements != "")
			dataSource.setMaxStatements(Integer.parseInt(maxStatements));
		if (maxStatementsPerConnection != null && maxStatementsPerConnection != "")
			dataSource.setMaxStatementsPerConnection(Integer.parseInt(maxStatementsPerConnection));
		if (log != null && log != "")
			dataSource.setLog(Boolean.parseBoolean(log));
		if (formatSqlLog != null || formatSqlLog != "")
			dataSource.setFormatSqlLog(Boolean.parseBoolean(formatSqlLog));
		if (cache != null && cache != "")
			dataSource.setCache(Boolean.parseBoolean(cache));
		if (reversePckage != null && reversePckage != "")
			dataSource.setReversePack(reversePckage);
		if (srcpath != null && srcpath != "")
			dataSource.setSrcPath(srcpath);
		if (poolMethod != null && poolMethod != "")
			dataSource.setPoolMethod(Boolean.parseBoolean(poolMethod));
		if (createTable != null && createTable != "") {
			dataSource.getCaeateTable().clear();
			String[] split = createTable.trim().split(",");
			for (String st : split) {
				try {
					dataSource.getCaeateTable().add(Class.forName(sectionMap.get(st)));
				} catch (ClassNotFoundException e) {
					throw new NoDataSourceException("不正确的自动建表配置信息，无法执行建表程序，请检查classpath下的appconfig.ini配置文件中["+section+"]节中的'create.table'属性的配置信息。err=>"+sectionMap.get(st));
				}
				
			}
		}
		return dataSource;
	}

	public static DataSource getDataSource(String name) {
		allDataSources();
		for (DataSource curr : allDataSource) {
			if (name.equals(curr.getName()))
				return curr;
		}
		throw new NoDataSourceException("在Ioc容器中找不到name=" + name + "的DataSource！");
	}

	public static void allDataSources() {
		if (allDataSource == null) {
			boolean haveDefaultDB=false;
			List<DataSource> iocDataSources = ApplicationBeans.createApplicationBeans().getDataSources();
			List<DataSource> dbDataSource = readList();
			if (dbDataSource != null)
				dbDataSource.stream().filter(f -> filter(iocDataSources, f.getName())).forEach(iocDataSources::add);
			for(DataSource data:iocDataSources) {
				if(DEFAULT_NAME.equals(data.getName())) {
					haveDefaultDB=true;
					break;
				}
			}
			if(!haveDefaultDB&&iocDataSources.size()!=1)
				throw new NoDataSourceException("找不到默认的数据源，请检查是否配置了name属性为\"defaultDB\"的数据源");
			if(!haveDefaultDB&&iocDataSources.size()==1)
				iocDataSources.get(0).setName(DEFAULT_NAME);
			allDataSource = iocDataSources;
		}
	}

	public static boolean filter(List<DataSource> list, String name) {
		for (DataSource data : list) {
			if (name.equals(data.getName()))
				return false;
		}
		return true;
	}

}

package com.lucky.jacklamb.sqlcore.datasource;

import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;
import com.lucky.jacklamb.exception.NoDataSourceException;
import com.lucky.jacklamb.exception.NotFindBeanPropertyException;
import com.lucky.jacklamb.file.ini.IniFilePars;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.sqlcore.datasource.enums.Pool;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.reflect.MethodUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.lucky.jacklamb.sqlcore.datasource.SectionKey.SECTION_DATASOURCE;
import static com.lucky.jacklamb.sqlcore.datasource.SectionKey.SECTION_JDBC;

public class ReaderInI {

	private static List<LuckyDataSource> allDataSource;
	
	private static IniFilePars iniFilePars;
	
	public static List<LuckyDataSource> getAllDataSource() {
		allDataSources();
		return allDataSource;
	}
 
	public static List<LuckyDataSource> readList() {
		List<LuckyDataSource> dataList = new ArrayList<>();
		iniFilePars=IniFilePars.getIniFilePars();
		if(!iniFilePars.iniExist()||!iniFilePars.isHasSection(SECTION_JDBC))
			return null;
		if(!iniFilePars.isHasSection(SECTION_JDBC))
			throw new NotFindBeanPropertyException("在calsspath:appconfig.ini的配置文件中找不到必须的节[Jdbc]！");
		dataList.add(readIni(SECTION_JDBC));
		String datasStr = iniFilePars.getValue(SECTION_DATASOURCE, "dbname");
		if(datasStr!=null) {
			String[] datas=datasStr.split(",");
			for(String data:datas) {
				dataList.add(readIni(data));
			}
		}
		return dataList;
	}

	public static LuckyDataSource readIni(String section) {
		Map<String, String> sectionMap = iniFilePars.getSectionMap(section);
		LuckyDataSource dataSource=Pool.getDataSource(sectionMap.get("poolType"));
		String dbname=SECTION_JDBC.equals(section)?"defaultDB":section;
		dataSource=iniSection2LuckyDataSource(sectionMap,dataSource.getClass());
		dataSource.setDbname(dbname);
		if(dataSource.getDriverClass()==null|| dataSource.getJdbcUrl()==null
		   ||dataSource.getUsername()==null||dataSource.getPassword()==null)
			throw new NotFindBeanPropertyException("在calsspath:appconfig.ini的配置文件的["+section+"]节中找不到必须属性\"driverClass\",\"jdbcUrl\",\"username\",\"password\"");
		return dataSource;
	}

	public static LuckyDataSource getDataSource(String name) {
		allDataSources();
		for (LuckyDataSource curr : allDataSource) {
			if (name.equals(curr.getDbname()))
				return curr;
		}
		throw new NoDataSourceException("在Ioc容器中找不到name=" + name + "的DataSource！");
	}

	public static void allDataSources() {
		if (allDataSource == null) {
			boolean haveDefaultDB=false;
			List<LuckyDataSource> iocDataSources = ApplicationBeans.createApplicationBeans().getDataSources();
			List<LuckyDataSource> dbDataSource = readList();
			if (dbDataSource != null)
				dbDataSource.stream().filter(f -> filter(iocDataSources, f.getDbname())).forEach(iocDataSources::add);
			for(LuckyDataSource data:iocDataSources) {
				if("defaultDB".equals(data.getDbname())) {
					haveDefaultDB=true;
					break;
				}
			}
			if(!haveDefaultDB&&iocDataSources.size()!=1)
				throw new NoDataSourceException("找不到默认的数据源，请检查是否配置了name属性为\"defaultDB\"的数据源! \n\t1.检查您的appconfig.ini配置文件中是否配置了[Jdbc]节。\n\t2.检查您的配置类中是否配置了数据源配置。");
				iocDataSources.get(0).setDbname("defaultDB");
			allDataSource = iocDataSources;
		}
	}

	/**
	 * 配置文件配置信息转LuckyDataSource
	 * @param dataSectionMap 类似[Jdbc]的key-value组成的Map
	 * @return
	 */
	public static LuckyDataSource iniSection2LuckyDataSource(Map<String,String> dataSectionMap,Class<? extends LuckyDataSource> luckyDataSourceClass) {
		Field[] fields = ClassUtils.getAllFields(luckyDataSourceClass);
		LuckyDataSource luckyDataSource = ClassUtils.newObject(luckyDataSourceClass);
		String fieldName;
		for (Field field : fields) {
			fieldName=field.getName();
			if(dataSectionMap.containsKey(fieldName)){
				String valueStr = dataSectionMap.get(fieldName);
				if("createTable".equals(fieldName)){
					List<Class<?>> tables=new ArrayList<>();
					String[] split = valueStr.split(",");
					for (String tab : split) {
						String classPath = null;
						try {
							classPath= dataSectionMap.get(tab);
							tables.add(Class.forName(classPath));
						} catch (ClassNotFoundException e) {
							throw new NoDataSourceException("不正确的自动建表配置信息，无法执行建表程序，请检查classpath下的appconfig.ini配置文件中["+luckyDataSource.getDbname()+"]节中的'createTable'属性的配置信息。err=>"+tab+"=\""+classPath+"\"");
						}
					}
					FieldUtils.setValue(luckyDataSource,field,tables);
				}else if("poolType".equals(fieldName)){
					continue;
				}else if("formatSqlLog".equals(fieldName)){
					Boolean log= (boolean) JavaConversion.strToBasic(dataSectionMap.get("formatSqlLog"),Boolean.class);
					Boolean[] p={log};
					MethodUtils.invoke(luckyDataSource,"setFormatSqlLog",p);
				}else{
					if(FieldUtils.isCanOperation(field)){
						FieldUtils.setValue(luckyDataSource,field, JavaConversion.strToBasic(valueStr,field.getType(),true));
					}else{
						FieldUtils.setValue(luckyDataSource,field,JavaConversion.strToBasic(valueStr,field.getType()));
					}
				}
			}
		}
		return luckyDataSource;
	}

	public static boolean filter(List<LuckyDataSource> list, String name) {
		for (LuckyDataSource data : list) {
			if (name.equals(data.getDbname()))
				return false;
		}
		return true;
	}

}

package com.lucky.jacklamb.sqlcore.abstractionlayer.util;


import org.apache.log4j.Logger;

import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl.AccessSqlCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl.DB2Core;
import com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl.MySqlCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl.OracleCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl.PostgreSqlCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl.SqlServerCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.dynamiccoreImpl.SybaseCore;
import com.lucky.jacklamb.sqlcore.c3p0.ReadIni;
import com.lucky.jacklamb.sqlcore.exception.DatabaseTypeUnableIdentifyException;

public class SqlCoreFactory {
	
	private static Logger log=Logger.getLogger(SqlCoreFactory.class);
	
	public static SqlCore createSqlCore() {
		return createSqlCore("defaultDB");
	}
	
	public static SqlCore createSqlCore(String dbname) {
		String dbType=PojoManage.getDatabaseType(dbname);
		StringBuilder sb=new StringBuilder();
		switch (dbType) {
		case "MySql":
			log.debug(sb.append("Create SqlCore ==> dbType=MySql,dbname=").append(dbname).append(",class=").append(MySqlCore.class).toString());
			return new MySqlCore(dbname);
		case "DB2":
			log.debug(sb.append("Create SqlCore ==> dbType=DB2,dbname=").append(dbname).append(",class=").append(DB2Core.class).toString());
			return new DB2Core(dbname);
		case "Oracle":
			log.debug(sb.append("Create SqlCore ==> dbType=Oracle,dbname=").append(dbname).append(",class=").append(OracleCore.class).toString());
			return new OracleCore(dbname);
		case "PostgreSql":
			log.debug(sb.append("Create SqlCore ==> dbType=PostgreSql,dbname=").append(dbname).append(",class=").append(PostgreSqlCore.class).toString());
			return new PostgreSqlCore(dbname);
		case "Sql Server":
			log.debug(sb.append("Create SqlCore ==> dbType=Sql Server,dbname=").append(dbname).append(",class=").append(SqlServerCore.class).toString());
			return new SqlServerCore(dbname);
		case "Sybase":
			log.debug(sb.append("Create SqlCore ==> dbType=Sybase,dbname=").append(dbname).append(",class=").append(SybaseCore.class).toString());
			return new SybaseCore(dbname);
		case "Access":
			log.debug(sb.append("Create SqlCore ==> dbType=Access,dbname=").append(dbname).append(",class=").append(AccessSqlCore.class).toString());
			return new AccessSqlCore(dbname);
		default:
			log.error("无法识别的数据库类型，Lucky目前还不支持该类型的数据库驱动 : "+ReadIni.getDataSource(dbname).getDriverClass());
			throw new DatabaseTypeUnableIdentifyException("Lucky目前还不支持该类型的数据库，我们正在拼命更新中！DatabaseType:"+ReadIni.getDataSource(dbname).getDriverClass());
		}
	}
}

 package com.lucky.jacklamb.sqlcore.abstractionlayer.transaction;

 import java.sql.Connection;
 import java.sql.SQLException;
/**
 * Lucky的事务处理类
 * @author fk-7075
 *
 */
public interface Transaction {

	Connection getConnection();

	void open();

	void open(int isolationLevel);

	void commit();

	void rollback();

	void close();
}

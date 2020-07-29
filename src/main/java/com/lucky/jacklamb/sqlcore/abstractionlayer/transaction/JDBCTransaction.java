package com.lucky.jacklamb.sqlcore.abstractionlayer.transaction;

import com.lucky.jacklamb.ioc.ControllerIOC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class JDBCTransaction implements Transaction {

    private static final Logger log= LogManager.getLogger("c.l.j.a.transaction.JDBCTransaction");

    private Connection connection;

    public JDBCTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void open() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            log.error("开启事务失败！",e);
            e.printStackTrace();
        }
    }

    @Override
    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            log.error("提交事务失败！",e);
            e.printStackTrace();
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error("事务回滚失败！",e);
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("关闭连接失败！",e);
            e.printStackTrace();
        }
    }
}

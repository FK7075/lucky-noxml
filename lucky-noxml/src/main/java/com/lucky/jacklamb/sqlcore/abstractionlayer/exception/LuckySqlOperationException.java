package com.lucky.jacklamb.sqlcore.abstractionlayer.exception;

import com.lucky.jacklamb.sqlcore.util.PojoManage;

import java.util.Arrays;

public class LuckySqlOperationException extends RuntimeException {

    public LuckySqlOperationException(String dbname,String sql,Object[] params,Throwable e){
        super("\nDatabase ："+ PojoManage.getDatabaseName(dbname) +"##[dbname="+dbname+"]\nSQL      : "+sql+"\nParam    : "+ Arrays.toString(params),e);
    }

    public LuckySqlOperationException(String[] sqls,Throwable e){
        super("\nSQL: \n"+Arrays.toString(sqls),e);
    }

    public LuckySqlOperationException(Throwable e){
        super(e);
    }
}

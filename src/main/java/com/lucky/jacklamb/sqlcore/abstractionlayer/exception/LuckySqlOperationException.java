package com.lucky.jacklamb.sqlcore.abstractionlayer.exception;

import java.util.Arrays;

public class LuckySqlOperationException extends RuntimeException {

    public LuckySqlOperationException(String sql,Object[] params,Throwable e){
        super("\nSQL: "+sql+"\nParam: "+ Arrays.toString(params),e);
    }

    public LuckySqlOperationException(String[] sqls,Throwable e){
        super("\nSQL: \n"+Arrays.toString(sqls),e);
    }

    public LuckySqlOperationException(Throwable e){
        super(e);
    }
}

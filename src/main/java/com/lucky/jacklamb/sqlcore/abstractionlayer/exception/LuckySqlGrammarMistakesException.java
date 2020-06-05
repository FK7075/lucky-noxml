package com.lucky.jacklamb.sqlcore.abstractionlayer.exception;

public class LuckySqlGrammarMistakesException extends RuntimeException {

    public LuckySqlGrammarMistakesException(String errSql){
        super("错误的预编译SQl,SQL中的参数描述错误！索引超过参数列表的范围！ERROR-SQL："+errSql);
    }
}

package com.lucky.jacklamb.sqlcore.jdbc.core;

import java.util.Map;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/8/19 10:20
 */
@FunctionalInterface
public interface DynamicSqlWrapper {

    public SP dySql(Map<String,Object> data);

}

package com.lucky.jacklamb.sqlcore.jdbc.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/8/19 10:24
 */
public class SP {

    private String pSql;

    private List<Object> params;

    public SP() {
        pSql="";
        params=new ArrayList<>();
    }


    public SP(String pSql, List<Object> params) {
        this.pSql = pSql;
        this.params = params;
    }

    public String getpSql() {
        return pSql;
    }

    public void setpSql(String pSql) {
        this.pSql = pSql;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SP{");
        sb.append("pSql='").append(pSql).append('\'');
        sb.append(", params=").append(params);
        sb.append('}');
        return sb.toString();
    }
}

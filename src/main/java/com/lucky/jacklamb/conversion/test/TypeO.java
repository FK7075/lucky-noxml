package com.lucky.jacklamb.conversion.test;

import java.util.Map;

public class TypeO {

    private int typeID;
    private String typeName;
    private Map<String,Double> map;

    public Map<String, Double> getMap() {
        return map;
    }

    public void setMap(Map<String, Double> map) {
        this.map = map;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TypeO{");
        sb.append("typeID=").append(typeID);
        sb.append(", typeName='").append(typeName).append('\'');
        sb.append(", map=").append(map);
        sb.append('}');
        return sb.toString();
    }
}

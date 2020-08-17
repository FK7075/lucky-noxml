package com.lucky.jacklamb.query.translator;

import com.lucky.jacklamb.annotation.orm.Id;
import com.lucky.jacklamb.annotation.orm.Table;
import com.lucky.jacklamb.enums.PrimaryType;
import com.lucky.jacklamb.rest.XStreamAllowType;
import com.lucky.jacklamb.sqlcore.activerecord.BaseEntity;

@Table(index="name")
public class Book{

    @Id(type = PrimaryType.AUTO_UUID)
    private String id;

    private String name;

    private Double price;

    private String type;

    private Integer inventory;

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setInventory(Integer inventory) {
        this.inventory = inventory;
    }
}

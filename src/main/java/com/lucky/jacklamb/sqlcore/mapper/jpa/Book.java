package com.lucky.jacklamb.sqlcore.mapper.jpa;

import com.lucky.jacklamb.annotation.orm.Column;
import com.lucky.jacklamb.annotation.orm.Id;
import com.lucky.jacklamb.annotation.orm.Table;
import com.lucky.jacklamb.enums.PrimaryType;

@Table("book")
public class Book {

    @Id(type = PrimaryType.AUTO_UUID)
    private String id;

    @Column("b_name")
    private String name;

    @Column("b_price")
    private Double price;

    @Column("b_age")
    private Integer age;
}

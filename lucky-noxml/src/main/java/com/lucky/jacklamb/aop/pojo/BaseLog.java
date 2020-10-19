package com.lucky.jacklamb.aop.pojo;

import com.lucky.jacklamb.annotation.orm.Id;
import com.lucky.jacklamb.enums.PrimaryType;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/10/3 11:33 上午
 */
public class BaseLog {

    @Id(type = PrimaryType.AUTO_UUID,length = 32)
    private Integer id;
}

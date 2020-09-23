package com.lucky.jacklamb.annotation.ioc;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SSH {

    /**
     * 配置文件中的Remote配置节
     * @return
     */
    String value();
}

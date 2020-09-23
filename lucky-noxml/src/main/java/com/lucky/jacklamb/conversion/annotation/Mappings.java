package com.lucky.jacklamb.conversion.annotation;


import com.lucky.jacklamb.conversion.annotation.Mapping;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mappings {
    Mapping[] value();
}

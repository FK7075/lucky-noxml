package com.lucky.jacklamb.conversion.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {

    String source();

    String  target();
}

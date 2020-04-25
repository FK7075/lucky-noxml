package com.lucky.jacklamb.annotation.conversion;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {

    String source();

    String  target();
}

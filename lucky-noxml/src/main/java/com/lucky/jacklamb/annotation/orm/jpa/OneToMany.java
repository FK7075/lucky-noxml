package com.lucky.jacklamb.annotation.orm.jpa;


import com.lucky.jacklamb.enums.jpa.CascadeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {

    CascadeType[] cascade() default {};
}

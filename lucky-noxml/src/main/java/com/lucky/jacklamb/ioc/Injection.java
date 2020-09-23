package com.lucky.jacklamb.ioc;

/**
 * 该类的子对象在使用无参构造获得对象时，Lucky会为该对象注入属性
 * 并将注入后的对象返回
 * @author fk7075
 * @version 1.0
 * @date 2020/9/16 15:48
 */
public abstract class Injection {

    public Injection(){
        IOCContainers.injection(this);
    }
}

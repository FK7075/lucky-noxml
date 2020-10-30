package com.lucky.jacklamb.ioc;

import java.util.List;
import java.util.Map;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/10/29 10:43
 */
public interface IOC {

    /**
     * 判断容器中是否含有该ID的Bean
     * @param id ID
     * @return
     */
    boolean contain(String id);

    /**
     * 根据ID得到一个Bean
     * @param id ID
     * @return
     */
    Object getBean(String id);

    /**
     * 得到整个BeanMap
     * @return
     */
    Map<String,Object> getBeanMap();

    /**
     * 添加一个Bean
     * @param id
     * @param bean
     */
    void addBean(String id,Object bean);

    /**
     * 注册，将所有用户组件注册到IOC容器中
     * @param beanClass
     */
    void registered(List<Class<?>> beanClass);
}

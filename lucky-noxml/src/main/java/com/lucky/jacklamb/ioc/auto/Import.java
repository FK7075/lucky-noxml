package com.lucky.jacklamb.ioc.auto;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/14 4:46 上午
 */
public interface Import {

    void importClass(Class<?>[] pointClasses);

    void importFactory(Class<?>[] factoryClasses);

    void importClasspath(String[] classPathFiles);
}

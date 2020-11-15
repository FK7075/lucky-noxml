package com.lucky.jacklamb.ioc.auto;


import com.lucky.jacklamb.aop.core.InjectionAopPoint;
import com.lucky.jacklamb.ioc.auto.factory.InjectionAopPointFactory;
import com.lucky.jacklamb.ioc.AspectAOP;
import com.lucky.jacklamb.rest.LSON;
import com.lucky.jacklamb.utils.file.Resources;
import com.lucky.jacklamb.utils.reflect.ClassUtils;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/14 4:57 上午
 */
public class AopAutoImport implements Import {

    private static final LSON lson=new LSON();

    @Override
    public void importClass(Class<?>[] pointClasses) {
        for (Class<?> pointClass : pointClasses) {
            AspectAOP.addIAPoint((InjectionAopPoint) ClassUtils.newObject(pointClass));
        }
    }

    @Override
    public void importFactory(Class<?>[] factoryClasses) {
        for (Class<?> factoryClass : factoryClasses) {
            InjectionAopPointFactory factory= (InjectionAopPointFactory) ClassUtils.newObject(factoryClass);
            AspectAOP.addAllIAPoint(factory.factory());
        }
    }

    @Override
    public void importClasspath(String[] classPathFiles) {
        for (String classPathFile : classPathFiles) {
            classPathFile=classPathFile.startsWith("/")?classPathFile:"/"+classPathFile;
            String[] classStrings=lson.fromJson(String[].class, Resources.getReader(classPathFile));
            for (String classString : classStrings) {
                try {
                    Class<?> aClass=Class.forName(classString);
                    if(InjectionAopPoint.class.isAssignableFrom(aClass)){
                        InjectionAopPoint iap= (InjectionAopPoint) ClassUtils.newObject(aClass);
                        AspectAOP.addIAPoint(iap);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("classpath:"+classPathFile+"文件中配置的\""+classString+"\"不存在，所以无法加载！",e);
                }
            }
        }
    }
}

package com.lucky.jacklamb.start;

import com.lucky.jacklamb.ioc.auto.AopAutoImport;
import com.lucky.jacklamb.ioc.auto.Import;
import com.lucky.jacklamb.utils.reflect.AnnotationUtils;

import java.util.List;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/14 4:01 上午
 */
public class Preprocessor {

    private Class<?> applicationClass;

    private Import iAopImport;

    public Preprocessor(Class<?> applicationClass) {
        this.applicationClass = applicationClass;
        iAopImport=new AopAutoImport();
    }

    public void init(){
        importAopComponent();
    }

    private void importAopComponent(){
        List<com.lucky.jacklamb.ioc.auto.ann.AopImport> aopImports = AnnotationUtils.strengthenGet(applicationClass, com.lucky.jacklamb.ioc.auto.ann.AopImport.class);
        for (com.lucky.jacklamb.ioc.auto.ann.AopImport aopImport : aopImports) {
            iAopImport.importClasspath(aopImport.classpath());
            iAopImport.importClass(aopImport.points());
            iAopImport.importFactory(aopImport.factory());
        }

    }

}

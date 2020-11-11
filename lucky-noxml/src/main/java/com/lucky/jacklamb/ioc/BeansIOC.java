package com.lucky.jacklamb.ioc;

import com.lucky.jacklamb.annotation.ioc.Bean;
import com.lucky.jacklamb.annotation.ioc.Configuration;
import com.lucky.jacklamb.cglib.ASMUtil;
import com.lucky.jacklamb.exception.NotAddIOCComponent;
import com.lucky.jacklamb.exception.NotFindBeanException;
import com.lucky.jacklamb.ioc.config.*;
import com.lucky.jacklamb.ioc.enums.IocCode;
import com.lucky.jacklamb.servlet.mapping.Mapping;
import com.lucky.jacklamb.utils.base.LuckyUtils;
import com.lucky.jacklamb.utils.file.ini.INIConfig;
import com.lucky.jacklamb.utils.reflect.AnnotationUtils;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.MethodUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置类@Bean
 *
 * @author fk7075
 * @version 1.0
 * @date 2020/10/29 10:37
 */
public class BeansIOC implements IOC {

    private static final Logger log = LogManager.getLogger(BeansIOC.class);
    private Map<String, Object> beansMap;
    private String IOC_CODE = "bean";
    private List<String> beansIds;

    public BeansIOC() {
        beansMap = new HashMap<>(16);
        beansIds = new ArrayList<>(16);
    }

    @Override
    public boolean contain(String id) {
        return beansIds.contains(id);
    }

    @Override
    public Object getBean(String id) {
        if (!contain(id))
            throw new NotFindBeanException("在Bean(ioc)容器中找不到ID为--" + id + "--的Bean...");
        return beansMap.get(id);
    }

    @Override
    public Map<String, Object> getBeanMap() {
        return beansMap;
    }

    @Override
    public void addBean(String id, Object bean) {
        if (contain(id))
            throw new NotAddIOCComponent("Bean(ioc)容器中已存在ID为--" + id + "--的组件，无法重复添加（您可能配置了同名的@Component组件，这将会导致异常的发生！）......");
        beansMap.put(id, bean);
        addID(id);
    }

    public void addID(String id) {
        beansIds.add(id);
    }

    @Override
    public void registered(List<Class<?>> beanClass) {
        String beanID;

        List<Object> beanList=new ArrayList<>(beanClass.size());
        beanClass.forEach((aClass)->{
            Object obj = ClassUtils.newObject(aClass);
            //属性注入
            IOCContainers.injection(obj);
            beanList.add(obj);
        });

        //第一次循环，注册所有无参的Bean方法返回的实例
        for (Object obj : beanList) {
            Class<?> bean=obj.getClass();
            Configuration cfg = bean.getAnnotation(Configuration.class);
            if (!"".equals(cfg.section())) {
                obj = new INIConfig(cfg.ini()).getObject(bean, cfg.section());
                beanID = "".equals(cfg.value()) ? LuckyUtils.TableToClass1(bean.getSimpleName()) : cfg.value();
                addBean(beanID, obj);
                log.info("@Configuration \"[id=" + beanID + " class=" + obj + "]\"");
            }
            List<Method> notParamMethods = ClassUtils.getMethodByAnnotation(bean, Bean.class)
                    .stream()
                    .filter((m) -> {
                        return AnnotationUtils.get(m, Bean.class).iocCode() == IocCode.COMPONENT
                                && MethodUtils.getParameter(m).length == 0;
                    })
                    .collect(Collectors.toList());
            for (Method notParamMethod : notParamMethods) {
                Object invoke = MethodUtils.invoke(obj, notParamMethod);
                Bean annBean = AnnotationUtils.get(notParamMethod, Bean.class);
                beanID = getBeanId(bean, notParamMethod, annBean);
                addBean(invoke,notParamMethod.getReturnType(),beanID);
            }
        }

        ApplicationBeans apps = ApplicationBeans.createApplicationBeans();

        //第二次循环，注册所有有参数的Bean方法的返回实例
        for (Object obj : beanList) {
            Class<?> bean=obj.getClass();
            List<Method> haveParamMethods = ClassUtils.getMethodByAnnotation(bean, Bean.class)
                    .stream()
                    .filter((m) -> {
                        return AnnotationUtils.get(m, Bean.class).iocCode() == IocCode.COMPONENT
                                && MethodUtils.getParameter(m).length != 0;
                    })
                    .collect(Collectors.toList());
            for (Method haveParamMethod : haveParamMethods) {
                Bean annBean = AnnotationUtils.get(haveParamMethod, Bean.class);
                beanID = getBeanId(bean, haveParamMethod, annBean);
                Parameter[] parameter = MethodUtils.getParameter(haveParamMethod);
                String[] paramNames = ASMUtil.getMethodParamNames(haveParamMethod);
                Object[] params=new Object[parameter.length];
                for (int i = 0,j=parameter.length; i < j; i++) {
                    List<Object> beans = apps.getBeans(parameter[i].getType());
                    if(beans.isEmpty()){
                        throw new NotFindBeanException("无法执行的@Bean方法，参数注入注入失败，在IOC容器中找不到类型为--"+parameter[i].getType()+"--的Bean，错误位置："+haveParamMethod);
                    }else if(beans.size()==1){
                        params[i]=beans.get(0);
                    }else{
                        String iocId=Mapping.getParamName(parameter[i],paramNames[i]);
                        if(!apps.isIocBean(iocId)){
                            throw new NotFindBeanException("无法执行的@Bean方法，参数注入注入失败，在IOC容器中找不到ID为--"+iocId+"--的Bean，错误位置："+haveParamMethod);
                        }
                        params[i]=apps.getBean(iocId);
                    }
                }
                Object invoke = MethodUtils.invoke(obj, haveParamMethod,params);
                beanID = getBeanId(bean, haveParamMethod, annBean);
                addBean(invoke,haveParamMethod.getReturnType(),beanID);
            }
        }
    }

    private String getBeanId(Class<?> beanClass, Method method, Bean bean) {
        if ("".equals(bean.value())) {
            return beanClass.getSimpleName() + "_" + method.getName();
        } else {
            return bean.value();
        }
    }

    private void addBean(Object bean,Class<?>returnClass,String beanId){
        if(returnClass==void.class){
            log.info("@Bean \"[id=" + beanId + " (VOID) ]\"");
            return;
        }
        if (!LuckyConfig.class.isAssignableFrom(returnClass)) {
            addBean(beanId, bean);
        } else if (ScanConfig.class.isAssignableFrom(returnClass)) {
            addBean(beanId, AppConfig.getAppConfig().getScanConfig());
        } else if (WebConfig.class.isAssignableFrom(returnClass)) {
            addBean(beanId, AppConfig.getAppConfig().getWebConfig());
        } else if (ServerConfig.class.isAssignableFrom(returnClass)) {
            addBean(beanId, AppConfig.getAppConfig().getServerConfig());
        }
        log.info("@Bean \"[id=" + beanId + " class=" + bean + "]\"");
    }
}



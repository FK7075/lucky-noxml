package com.lucky.jacklamb.junit;

import com.lucky.jacklamb.annotation.ioc.Autowired;
import com.lucky.jacklamb.annotation.ioc.SSH;
import com.lucky.jacklamb.annotation.ioc.Value;
import com.lucky.jacklamb.aop.core.PointRunFactory;
import com.lucky.jacklamb.aop.core.AopProxyFactory;
import com.lucky.jacklamb.expression.$Expression;
import com.lucky.jacklamb.file.ini.INIConfig;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.IOCContainers;
import com.lucky.jacklamb.ssh.Remote;
import com.lucky.jacklamb.ssh.SSHClient;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Field;
import java.util.*;

public class LuckyJUnit4ClassRunner extends BlockJUnit4ClassRunner{
	
	
	public LuckyJUnit4ClassRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected Object createTest() throws Exception {
		Object createTest = super.createTest();
		Class<?> aClass = createTest.getClass();

		//当前测试类如果存在事务注解@Transaction则执行事务代理
		if(AopProxyFactory.isTransaction(aClass)||AopProxyFactory.isCacheable(aClass)){
			createTest= PointRunFactory.createProxyFactory().getProxy(createTest.getClass());
		}
		ApplicationBeans applicationBeans=ApplicationBeans.createApplicationBeans();
		IOCContainers.injection(createTest);
		return createTest;
	}
}

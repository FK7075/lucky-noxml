package com.lucky.jacklamb.junit;

import com.lucky.jacklamb.aop.core.AopProxyFactory;
import com.lucky.jacklamb.aop.core.PointRunFactory;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.ioc.IOCContainers;
import com.lucky.jacklamb.sqlcore.datasource.abs.LuckyDataSource;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class LuckyJUnit4ClassRunner extends BlockJUnit4ClassRunner{
	
	
	public LuckyJUnit4ClassRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected Object createTest() throws Exception {
		Object createTest = super.createTest();
		Class<?> aClass = createTest.getClass();

		//当前测试类如果存在事务注解@Transaction则执行事务代理
		if(AopProxyFactory.isTransaction(aClass)){
			createTest= PointRunFactory.createProxyFactory().getProxy(createTest.getClass());
		}
		ApplicationBeans.createApplicationBeans();
		IOCContainers.injection(createTest);
		return createTest;
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		super.runChild(method, notifier);
		LuckyDataSource.close();
	}
}

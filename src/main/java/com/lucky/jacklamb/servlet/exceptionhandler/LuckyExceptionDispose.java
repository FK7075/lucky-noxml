package com.lucky.jacklamb.servlet.exceptionhandler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.lucky.jacklamb.annotation.mvc.ControllerExceptionHandler;
import com.lucky.jacklamb.exception.NotFindBeanException;
import com.lucky.jacklamb.ioc.ApplicationBeans;

public class LuckyExceptionDispose extends BaseExceptionHand {
	
	private static Logger log=Logger.getLogger(LuckyExceptionDispose.class);
	
	private List<Object> beans;
	
	public LuckyExceptionDispose() {
		try {
			beans = ApplicationBeans.createApplicationBeans().getBeans(LuckyExceptionHandler.class);
		}catch(NotFindBeanException e) {
			beans=new ArrayList<>();
			log.debug("用户没有注册ExceptionDispose组件，Controller中产生异常需要在Controller中进行处理，如果需要解耦异常处理请使用@ControllerExceptionHandler注解定义一组ExceptionDispose");
		}
	}



	/**
	 * 全局异常处理
	 */
	@Override
	protected void globalExceptionHand(Throwable e) {
		LuckyExceptionHandler exceobj;
		ControllerExceptionHandler eh;
		for(Object obj:beans) {
			exceobj=(LuckyExceptionHandler) obj;
			eh=exceobj.getClass().getAnnotation(ControllerExceptionHandler.class);
			if(eh.value().length==0) {
				if(exceobj.dispose(e))
					return;
			}
		}
		super.globalExceptionHand(e);
	}


	/**
	 * 指定异常处理注册
	 */
	@Override
	public void exceptionHand() {
		LuckyExceptionHandler exceobj;
		ExceptionDisposeHand edh;
		ControllerExceptionHandler eh;
		String[] scope;
		for(Object obj:beans) {
			exceobj=(LuckyExceptionHandler) obj;
			eh=exceobj.getClass().getAnnotation(ControllerExceptionHandler.class);
			scope=eh.value();
			exceobj.init(model,controllerObj, currClass, currMethod, params);
			edh=new ExceptionDisposeHand(scope,exceobj);
			registered(edh);
		}
	}
	
	

}

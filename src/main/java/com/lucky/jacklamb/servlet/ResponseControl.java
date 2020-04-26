package com.lucky.jacklamb.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletException;

import com.lucky.jacklamb.enums.Rest;
import com.lucky.jacklamb.ioc.ControllerAndMethod;

/**
 * 处理响应相关的类
 * @author fk-7075
 *
 */
public class ResponseControl {
	
	
	/**
	 * 响应当前请求
	 * @param req Request对象
	 * @param resp Response对象
	 * @param info 响应的目标
	 * @param pre_suf 前后缀配置
	 * @throws IOException
	 * @throws ServletException
	 */
	private void toPage(Model model,String info,List<String> pre_suf) {
		String topage="";
		if(info.contains("page:")) {//重定向到页面
			info=info.replaceAll("page:", "");
			topage=model.getRequest().getContextPath()+pre_suf.get(0)+info+pre_suf.get(1);
			topage=topage.replaceAll(" ", "");
			model.redirect(topage);
		}else if(info.contains("forward:")) {//转发到本Controller的某个方法
			info=info.replaceAll("forward:", "");
			model.forward(info);
		}else if(info.contains("redirect:")) {//重定向到本Controller的某个方法
			info=info.replaceAll("redirect:", "");
			model.redirect(info);
		}else {//转发到页面
			topage=pre_suf.get(0)+info+pre_suf.get(1);
			topage=topage.replaceAll(" ", "");
			model.forward(topage);
		}
	}


	/**
	 * 处理响应信息
	 * @param model Model对象
	 * @param controllerAndMethod ControllerAndMethod对象
	 * @param method 响应请求的方法
	 * @param obj 方法返回的结果
	 */
	public void jump(Model model,ControllerAndMethod controllerAndMethod, Method method, Object obj)
			{
		if (obj != null) {
			if(controllerAndMethod.getRest()==Rest.JSON) {
				model.writerJson(obj);
				return;
			}
			if(controllerAndMethod.getRest()==Rest.XML) {
				model.witerXml(obj);
				return;
			}
			if(controllerAndMethod.getRest()==Rest.TXT) {
				model.writer(obj.toString());
				return;
			}
			if(controllerAndMethod.getRest()==Rest.NO) {
				if(String.class.isAssignableFrom(obj.getClass())) {
					toPage(model,obj.toString(),controllerAndMethod.getPreAndSuf());
				}else {
					throw new RuntimeException("返回值类型错误，无法完成转发和重定向操作!合法的返回值类型为String，错误位置："+controllerAndMethod.getMethod());
				}
			}
				
		}
	}
}

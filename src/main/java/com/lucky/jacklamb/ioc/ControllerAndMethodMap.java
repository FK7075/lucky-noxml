package com.lucky.jacklamb.ioc;

import java.util.ArrayList;
import java.util.List;

public class ControllerAndMethodMap {
	
	private List<URLAndRequestMethod> urlList;
	
	private List<ControllerAndMethod> controllerMethodList;	
	
	
	public List<URLAndRequestMethod> getUrlList() {
		return urlList;
	}

	public ControllerAndMethodMap() {
		urlList=new ArrayList<>();
		controllerMethodList=new ArrayList<>();
	}
	
	public void put(URLAndRequestMethod uRLAndRequestMethod, ControllerAndMethod controllerAndMethod ) {
		urlList.add(uRLAndRequestMethod);
		controllerMethodList.add(controllerAndMethod);
	}
	
	public ControllerAndMethod get(URLAndRequestMethod uRLAndRequestMethod) {
		for(int i=0;i<urlList.size();i++) {
			if(urlList.get(i).myEquals(uRLAndRequestMethod))
				return controllerMethodList.get(i);
		}
		return null;
	}
	
	public boolean containsKey(URLAndRequestMethod uRLAndRequestMethod) {
		for(URLAndRequestMethod thisUrlMethod:urlList) {
			if(thisUrlMethod.myEquals(uRLAndRequestMethod))
				return true;
		}
		return false;
	}
}

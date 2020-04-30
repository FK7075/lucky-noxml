package com.lucky.jacklamb.servlet;

import com.lucky.jacklamb.rest.LSON;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public abstract class LuckyController {
	
	protected Model model;
	
	protected HttpServletRequest request;
	
	protected HttpServletResponse response;
	
	protected HttpSession session;

	protected ServletContext application;

	protected LSON lson=new LSON();

}

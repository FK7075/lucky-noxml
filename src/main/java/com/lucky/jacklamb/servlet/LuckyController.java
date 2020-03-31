package com.lucky.jacklamb.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public abstract class LuckyController {
	
	protected Model model;
	
	protected HttpServletRequest request;
	
	protected HttpServletResponse response;
	
	protected HttpSession session;

}

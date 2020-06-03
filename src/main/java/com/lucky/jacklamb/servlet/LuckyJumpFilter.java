package com.lucky.jacklamb.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 控制请求转发的Filter
 */
public abstract class LuckyJumpFilter implements Filter {

	protected FilterConfig filterConfig;

	protected Model model;

	protected FilterChain filterChain;

	protected String uri;

	protected HttpServletRequest request;

	protected HttpServletResponse response;
	
	protected HttpSession session;

	@Override
	public void destroy() {
		
	}

	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}


	/**
	 * 原始的过滤器逻辑
	 */
	@Override
	public final void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		request = (HttpServletRequest) req;
		response = (HttpServletResponse) resp;
		request.setCharacterEncoding("utf8");
		response.setCharacterEncoding("utf8");
		response.setHeader("Content-Type", "text/html;charset=utf-8");
		uri = request.getRequestURI();
		model = new Model(request, response);
		session=model.getSession();
		filterChain = chain;
		filter();
	}
	
	/**
	 * 过滤逻辑(用户实现)
	 * @throws IOException
	 * @throws ServletException
	 */
	public abstract void filter() throws IOException, ServletException;


	/**
	 * 放行
	 * @throws IOException
	 * @throws ServletException
	 */
	protected final void pass() throws IOException, ServletException {
		filterChain.doFilter(request, response);
	}

	/**
	 * 放行部分请求，其他请求将会被转发到指定Url
	 * @param passUrls 可以通过的请求
	 * @param forwardUrl 转发Url
	 * @throws IOException
	 * @throws ServletException
	 */
	protected final void passForward(String[] passUrls, String forwardUrl) throws IOException, ServletException {
		if(passUrls==null) {
			model.forward(forwardUrl);
			return;
		}
		boolean isPass = true;
		String type;
		for (String url : passUrls) {
			if (uri.equals(url)) {
				pass();
				return;
			}
			if (StaticResourceManage.isStaticResource(response,url)) {
				type = requestLastStr(url);
				type = type.substring(type.indexOf("."));
				isPass = isPass && !uri.endsWith(type);
			}
		}
		if (StaticResourceManage.isStaticResource(response, uri)&&isPass) {
			StaticResourceManage.response(model, uri);
			return;
		} else {
			model.forward(forwardUrl);
		}
	}
	
	/**
	 * 通过部分请求，其他请求将会被重定向到指定Url
	 * @param passUrls 可以通过的请求
	 * @param redirectUrl 重定向Url
	 * @throws IOException
	 * @throws ServletException
	 */
	protected final void passRedirect(String[] passUrls, String redirectUrl) throws IOException, ServletException {
		if(passUrls==null){
			model.redirect(redirectUrl);
			return;
		}

		boolean isPass = true;
		String type;
		for (String url : passUrls) {
			if (uri.equals(url)) {
				pass();
				return;
			}
			if (StaticResourceManage.isStaticResource(response,url)) {
				type = requestLastStr(url);
				type = type.substring(type.indexOf("."));
				isPass = isPass && !uri.endsWith(type);
			}
		}
		if (StaticResourceManage.isStaticResource(response, uri)&&isPass) {
			StaticResourceManage.response(model, uri);
			return;
		} else {
			model.redirect(redirectUrl);
		}
	}
	

	private String requestLastStr(String url) {
		return url.substring(url.lastIndexOf("/"));
	}

}

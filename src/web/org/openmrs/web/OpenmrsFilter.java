package org.openmrs.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.context.Context;
import org.openmrs.context.ContextFactory;

public class OpenmrsFilter implements Filter {

	protected final Log log = LogFactory.getLog(getClass());
		
	public void destroy() {	
		log.debug("Destroying filter");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	
		Context context = ContextFactory.getContext(); 
		context.startTransaction();
        HttpServletRequest hr = (HttpServletRequest) request;
        HttpSession httpSession = hr.getSession();
        //TODO how to only open a context for pages that need it ?
		httpSession.setAttribute("__openmrs_context", context);
		
		log.debug("before doFilter");
		chain.doFilter(request, response);
		log.debug("after doFilter");
		
		httpSession.removeAttribute("__openmrs_context");
		context.endTransaction();
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializating filter");
	}

}

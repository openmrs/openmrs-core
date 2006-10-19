package org.openmrs.web;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class OpenmrsFilter implements Filter {

	protected final Log log = LogFactory.getLog(getClass());
		
	public void destroy() {	
		log.debug("Destroying filter");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpSession httpSession = httpRequest.getSession();
		UserContext userContext = null;
		boolean initialRequest = false;
		
		Object val = httpRequest.getAttribute( WebConstants.INIT_REQ_UNIQUE_ID );
		
		//the request will not have the value if this is the initial request
        initialRequest = ( val == null );
        
        log.debug("initial Request? " + initialRequest);
        log.debug("requestURI" + httpRequest.getRequestURI());
        log.debug("requestURL" + httpRequest.getRequestURL());
        log.debug("request path info" + httpRequest.getPathInfo());
        
        //set/forward the request init attribute
        if (initialRequest)
        	httpRequest.setAttribute( WebConstants.INIT_REQ_UNIQUE_ID, String.valueOf(new Date().getTime()) );
        
        //context = (Context)httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
        //context = (Context)httpRequest.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
        
        if (initialRequest == true) {
        	// User context is created if it doesn't already exist and added to the session
			userContext = (UserContext) httpSession.getAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR);
			if (userContext == null) { 
				userContext = new UserContext();
				httpSession.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);
			}
        	log.info("Set user context " + userContext + " as attribute on session");
        	
        	// Add the user context to the current thread 
        	Context.setUserContext(userContext);
        }
        
		log.debug("before doFilter");
		
		chain.doFilter(request, response);
		
		httpSession.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);

		if (initialRequest == true) {
			// Clears the context so there's no user information left on the thread
			Context.clearUserContext();
		}
	
		log.debug("after doFilter");
		
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializating filter");
	}
	
	/**
	 * Get the application context.
	 * 
	 * @param httpRequest
	 * @return
	 */
	public ApplicationContext getApplicationContext(HttpServletRequest httpRequest) { 
		ServletContext servletContext = httpRequest.getSession().getServletContext();
		return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
	}	

}

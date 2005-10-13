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

	private static String INIT_REQ_ATTR_NAME = "__INIT_REQ__";
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpSession httpSession = httpRequest.getSession();
		Context context = null;		
		boolean initialRequest = false;
		
		Object val = httpRequest.getAttribute( INIT_REQ_ATTR_NAME );
		
		//the request will not have the value if this is the initial request
        initialRequest = ( val == null );
        
        //set/forward the request init attribute
        httpRequest.setAttribute( INIT_REQ_ATTR_NAME, INIT_REQ_ATTR_NAME );
        
        //TODO how to only open a context for pages that need it ?
        
        context = (Context)httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
        
        if (initialRequest == true && context == null) {
        	log.debug("setting context in httpSession");
        	//set the context it needs one
       		context = ContextFactory.getContext();
       		httpSession.setAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR, context);
        }

		log.debug("before doFilter");
		try {
			chain.doFilter(request, response);
		}
		catch (Exception e) {
			log.error(e.getMessage());
			throw new ServletException(e);
		}
		finally {
			//only close the transaction if this was the initial request
			if (initialRequest == true) {
				log.debug("ending transaction - file: " + httpRequest.getRequestURI());
//				httpSession.removeAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
				context.endTransaction();
			}
		}
		log.debug("after doFilter");
		
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializating filter");
	}

}

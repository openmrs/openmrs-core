/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

public class RequireTag extends TagSupport {

	public static final long serialVersionUID = 122998L;
	
	private final Log log = LogFactory.getLog(getClass());

	private String privilege;
	private String otherwise;
	private String redirect;
	private boolean errorOccurred;
	public int doStartTag() {
		
		errorOccurred = false;
		HttpServletResponse httpResponse = (HttpServletResponse)pageContext.getResponse();
		HttpSession httpSession = pageContext.getSession();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String request_ip_addr = request.getLocalAddr();
		String session_ip_addr = (String)httpSession.getAttribute(WebConstants.OPENMRS_CLIENT_IP_HTTPSESSION_ATTR);
		
		UserContext userContext = Context.getUserContext();
		
		if (userContext == null && privilege != null) {
			log.error("userContext is null. Did this pass through a filter?");
			//httpSession.removeAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
			//TODO find correct error to throw 
			throw new APIException("The context is currently null.  Please try reloading the site.");
		}
		
		if (!userContext.hasPrivilege(privilege)) {
			errorOccurred = true;
			if (userContext.isAuthenticated())
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "require.unauthorized");
			else
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "require.login");
		}
		else if (userContext.hasPrivilege(privilege) && userContext.isAuthenticated()) {
			// redirect users to password change form
			User user = userContext.getAuthenticatedUser();
			Boolean forcePasswordChange = new Boolean(user.getUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD));
			log.debug("Login redirect: " + redirect);
			if (forcePasswordChange && !redirect.contains("options.form")) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "User.password.change");
				errorOccurred = true;
				redirect = request.getContextPath() + "/options.form#Change Login Info";
				otherwise = redirect;
				try {
					httpResponse.sendRedirect(redirect);
					return SKIP_PAGE;
				}
				catch (IOException e) {
					// oops, cannot redirect
					log.error("Unable to redirect for password change: " + redirect, e);
					throw new APIException(e);
				}
			}
		}
		
		if (differentIpAddresses(session_ip_addr, request_ip_addr)) {
			errorOccurred = true;
			// stops warning message in IE when refreshing repeatedly
			if ("0.0.0.0".equals(request_ip_addr) == false) {
				log.warn("Invalid ip addr: expected " + session_ip_addr + ", but found: " + request_ip_addr);
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "require.ip_addr");
			}
		}
	
		log.debug("session ip addr: " + session_ip_addr);
		
		if (errorOccurred) {
			
			String url = "";
			if (redirect != null && !redirect.equals(""))
				url = request.getContextPath() + redirect;
			else
				url = request.getRequestURI();
			
			if (request.getQueryString() != null)
				url = url + "?" + request.getQueryString();
			httpSession.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, url);
			try {
				httpResponse.sendRedirect(request.getContextPath() + otherwise);
				return SKIP_PAGE;
			}
			catch (IOException e) {
				// oops, cannot redirect
				throw new APIException(e);
			}
		}
		
		return SKIP_BODY;
	}

	/**
     * Determines if the given ip addresses are the same.
     * 
     * @param session_ip_addr
     * @param request_ip_addr
     * @return true/false whether these IPs are different
     */
    private boolean differentIpAddresses(String sessionIpAddr, String requestIpAddr) {
    	if (sessionIpAddr == null || requestIpAddr == null)
    		return false;
    	
    	// IE7 and firefox store "localhost" IP addresses differently.
    	// To accomodate switching from firefox browing to IE taskpane,
    	// we assume these addresses to be equivalent
    	List<String> equivalentAddresses = new ArrayList<String>();
    	equivalentAddresses.add("127.0.0.1");
    	equivalentAddresses.add("0.0.0.0");
    	
    	// if the addresses are equal, all is well
    	if (sessionIpAddr.equals(requestIpAddr))
    		return false;
    	// if they aren't equal, but we consider them to be, also all is well
    	else if (equivalentAddresses.contains(sessionIpAddr) && 
    			equivalentAddresses.contains(requestIpAddr)){
    		return false;
    	}
    	
    	// the IP addresses were not equal, (don't continue with this user)
	    return true;
    }

	public int doEndTag() {
		if ( errorOccurred )
			return SKIP_PAGE;
		else
			return EVAL_PAGE;
	}
	
	public String getPrivilege() {
		return privilege;
	}

	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}

	public String getOtherwise() {
		return otherwise;
	}

	public void setOtherwise(String otherwise) {
		this.otherwise = otherwise;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}
	
}

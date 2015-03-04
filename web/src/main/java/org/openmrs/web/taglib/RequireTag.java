/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
import org.openmrs.web.WebConstants;
import org.openmrs.web.user.UserProperties;
import org.springframework.util.StringUtils;

/**
 * Controller for the <openmrs:require> taglib used on jsp pages. This taglib restricts the page
 * view to currently logged in (or anonymous) users that have the given privileges. <br/>
 * <br/>
 * Example use case:
 *
 * <pre>
 * &lt;openmrs:require privilege="Manage Concept Classes" otherwise="/login.htm" redirect="/admin/concepts/conceptClass.form" />
 * </pre>
 *
 * This will demand that the user have the "Manage Concept Classes" privilege. If they don't, kick
 * the user back to the "/login.htm" page. Then, after they log in on that page, send the user to
 * "/admin/concepts/conceptClass.form".
 */
public class RequireTag extends TagSupport {
	
	public static final long serialVersionUID = 122998L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private String privilege;
	
	private String allPrivileges;
	
	private String anyPrivilege;
	
	private String otherwise;
	
	private String redirect;
	
	private boolean errorOccurred;
	
	//these can only be multiple if the anyPrivilege attribute has more than one value
	private StringBuffer missingPrivilegesBuffer;
	
	/**
	 * This is where all the magic happens. The privileges are checked and the user is redirected if
	 * need be. <br/>
	 * <br/>
	 * Returns SKIP_PAGE if the user doesn't have the privilege and SKIP_BODY if it does.
	 *
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 * @should allow user with the privilege
	 * @should allow user to have any privilege
	 * @should allow user with all privileges
	 * @should reject user without the privilege
	 * @should reject user without any of the privileges
	 * @should reject user without all of the privileges
	 * @should set the right session attributes if the authenticated user misses some privileges
	 * @should set the referer as the denied page url if no redirect url is specified
	 */
	public int doStartTag() {
		
		errorOccurred = false;
		HttpServletResponse httpResponse = (HttpServletResponse) pageContext.getResponse();
		HttpSession httpSession = pageContext.getSession();
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		String request_ip_addr = request.getLocalAddr();
		String session_ip_addr = (String) httpSession.getAttribute(WebConstants.OPENMRS_CLIENT_IP_HTTPSESSION_ATTR);
		
		UserContext userContext = Context.getUserContext();
		
		if (userContext == null && privilege != null) {
			log.error("userContext is null. Did this pass through a filter?");
			//httpSession.removeAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
			//TODO find correct error to throw 
			throw new APIException("context.is.null", (Object[]) null);
		}
		
		// Parse comma-separated list of privileges in allPrivileges and anyPrivileges attributes
		String[] allPrivilegesArray = StringUtils.commaDelimitedListToStringArray(allPrivileges);
		String[] anyPrivilegeArray = StringUtils.commaDelimitedListToStringArray(anyPrivilege);
		
		boolean hasPrivilege = hasPrivileges(userContext, privilege, allPrivilegesArray, anyPrivilegeArray);
		if (!hasPrivilege) {
			errorOccurred = true;
			if (userContext.isAuthenticated()) {
				httpSession.setAttribute(WebConstants.INSUFFICIENT_PRIVILEGES, true);
				if (missingPrivilegesBuffer != null) {
					httpSession.setAttribute(WebConstants.REQUIRED_PRIVILEGES, missingPrivilegesBuffer.toString());
				}
				
				String referer = request.getHeader("Referer");
				httpSession.setAttribute(WebConstants.REFERER_URL, referer);
				if (StringUtils.hasText(redirect)) {
					httpSession.setAttribute(WebConstants.DENIED_PAGE, redirect);
				} else if (StringUtils.hasText(referer)) {
					//This is not exactly correct all the time
					httpSession.setAttribute(WebConstants.DENIED_PAGE, referer);
				}
				
				log.warn("The user: '" + Context.getAuthenticatedUser() + "' has attempted to access: " + redirect
				        + " which requires privilege: " + privilege + " or one of: " + allPrivileges + " or any of "
				        + anyPrivilege);
			} else {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "require.login");
			}
		} else if (hasPrivilege && userContext.isAuthenticated()) {
			// redirect users to password change form
			User user = userContext.getAuthenticatedUser();
			log.debug("Login redirect: " + redirect);
			if (new UserProperties(user.getUserProperties()).isSupposedToChangePassword()
			        && !redirect.contains("options.form")) {
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
			if (!"0.0.0.0".equals(request_ip_addr)) {
				log.warn("Invalid ip addr: expected " + session_ip_addr + ", but found: " + request_ip_addr);
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "require.ip_addr");
			}
		}
		
		log.debug("session ip addr: " + session_ip_addr);
		
		if (errorOccurred) {
			String url = "";
			if (redirect != null && !"".equals(redirect)) {
				url = request.getContextPath() + redirect;
			} else {
				url = request.getRequestURI();
			}
			
			if (request.getQueryString() != null) {
				url = url + "?" + request.getQueryString();
			}
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
		if (sessionIpAddr == null || requestIpAddr == null) {
			return false;
		}
		
		// IE7 and firefox store "localhost" IP addresses differently.
		// To accomodate switching from firefox browing to IE taskpane,
		// we assume these addresses to be equivalent
		List<String> equivalentAddresses = new ArrayList<String>();
		equivalentAddresses.add("127.0.0.1");
		equivalentAddresses.add("0.0.0.0");
		
		// if the addresses are equal, all is well
		if (sessionIpAddr.equals(requestIpAddr)) {
			return false;
		}
		// if they aren't equal, but we consider them to be, also all is well
		else if (equivalentAddresses.contains(sessionIpAddr) && equivalentAddresses.contains(requestIpAddr)) {
			return false;
		}
		
		// the IP addresses were not equal, (don't continue with this user)
		return true;
	}
	
	/**
	 * Returns true if all of the following three are true:
	 * <ul>
	 * <li>privilege is not defined OR user has privilege</li>
	 * <li>allPrivileges is not defined OR user has every privilege in allPrivileges</li>
	 * <li>anyPrivilege is not defined OR user has at least one of the privileges in anyPrivileges</li>
	 * </ul>
	 *
	 * @param userContext current user context
	 * @param privilege a single required privilege
	 * @param allPrivilegesArray an array of required privileges
	 * @param anyPrivilegeArray an array of privileges, at least one of which is required
	 * @return true if privilege conditions are met
	 */
	private boolean hasPrivileges(UserContext userContext, String privilege, String[] allPrivilegesArray,
	        String[] anyPrivilegeArray) {
		if (privilege != null && !userContext.hasPrivilege(privilege.trim())) {
			addMissingPrivilege(privilege);
			return false;
		}
		if (allPrivilegesArray.length > 0 && !hasAllPrivileges(userContext, allPrivilegesArray)) {
			return false;
		}
		if (anyPrivilegeArray.length > 0 && !hasAnyPrivilege(userContext, anyPrivilegeArray)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns true if user has all privileges
	 *
	 * @param userContext current user context
	 * @param allPrivilegesArray list of privileges
	 * @return true if user has all of the privileges
	 */
	private boolean hasAllPrivileges(UserContext userContext, String[] allPrivilegesArray) {
		for (String p : allPrivilegesArray) {
			if (!userContext.hasPrivilege(p.trim())) {
				addMissingPrivilege(p);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns true if user has any of the privileges
	 *
	 * @param userContext current user context
	 * @param anyPriviegeArray list of privileges
	 * @return true if user has at least one of the privileges
	 */
	private boolean hasAnyPrivilege(UserContext userContext, String[] anyPriviegeArray) {
		for (String p : anyPriviegeArray) {
			if (userContext.hasPrivilege(p.trim())) {
				return true;
			} else {
				addMissingPrivilege(p);
			}
		}
		return false;
	}
	
	private void addMissingPrivilege(String p) {
		if (!StringUtils.hasText(p)) {
			return;
		}
		
		if (missingPrivilegesBuffer == null) {
			missingPrivilegesBuffer = new StringBuffer();
			missingPrivilegesBuffer.append(p.trim());
			return;
		}
		missingPrivilegesBuffer.append("," + p.trim());
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() {
		missingPrivilegesBuffer = null;
		if (errorOccurred) {
			return SKIP_PAGE;
		} else {
			return EVAL_PAGE;
		}
	}
	
	public String getPrivilege() {
		return privilege;
	}
	
	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}
	
	public String getAllPrivileges() {
		return allPrivileges;
	}
	
	public void setAllPrivileges(String allPrivileges) {
		this.allPrivileges = allPrivileges;
	}
	
	public String getAnyPrivilege() {
		return anyPrivilege;
	}
	
	public void setAnyPrivilege(String anyPrivilege) {
		this.anyPrivilege = anyPrivilege;
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

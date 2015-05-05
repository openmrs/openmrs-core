/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.JavaScriptUtils;

/**
 * {@link Controller} for the login page
 */
@Controller
public class LoginController {
	
	private static final String LOGIN_FORM = "/login";
	
	/**
	 * Generates an appropriate alert message and send it to users with the system developer role
	 *
	 * @param webRequest the {@link WebRequest} object
	 * @param model the {@link ModelMap} object
	 * @return the view name
	 */
	@RequestMapping(LOGIN_FORM)
	public String handleRequest(WebRequest webRequest, ModelMap model) {
		boolean failedPrivilegeCheck = false;
		Object attributeValue = webRequest.getAttribute(WebConstants.INSUFFICIENT_PRIVILEGES, WebRequest.SCOPE_SESSION);
		if (attributeValue != null) {
			if (Boolean.valueOf(attributeValue.toString().trim())) {
				failedPrivilegeCheck = true;
			}
			webRequest.removeAttribute(WebConstants.INSUFFICIENT_PRIVILEGES, WebRequest.SCOPE_SESSION);
		}
		
		//If there is a currently logged in user and they failed a privilege check, else go to login in page
		if (Context.getAuthenticatedUser() != null && failedPrivilegeCheck) {
			model.addAttribute("foundMissingPrivileges", true);
			webRequest.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.insufficientPrivileges",
			    WebRequest.SCOPE_SESSION);
			
			String deniedPage = null;
			String requiredPrivileges = null;
			String exceptionMsg = null;
			String refererUrl = null;
			if (webRequest.getAttribute(WebConstants.DENIED_PAGE, WebRequest.SCOPE_SESSION) != null) {
				String deniedPageTemp = webRequest.getAttribute(WebConstants.DENIED_PAGE, WebRequest.SCOPE_SESSION)
				        .toString();
				webRequest.removeAttribute(WebConstants.DENIED_PAGE, WebRequest.SCOPE_SESSION);
				if (StringUtils.isNotBlank(deniedPageTemp)) {
					deniedPage = deniedPageTemp;
				}
				
			}
			if (webRequest.getAttribute(WebConstants.REQUIRED_PRIVILEGES, WebRequest.SCOPE_SESSION) != null) {
				String requiredPrivilegesTemp = webRequest.getAttribute(WebConstants.REQUIRED_PRIVILEGES,
				    WebRequest.SCOPE_SESSION).toString();
				webRequest.removeAttribute(WebConstants.REQUIRED_PRIVILEGES, WebRequest.SCOPE_SESSION);
				if (StringUtils.isNotBlank(requiredPrivilegesTemp)) {
					requiredPrivileges = requiredPrivilegesTemp;
				}
			}
			if (webRequest.getAttribute(WebConstants.UNCAUGHT_EXCEPTION_MESSAGE, WebRequest.SCOPE_SESSION) != null) {
				String exceptionMsgTemp = webRequest.getAttribute(WebConstants.UNCAUGHT_EXCEPTION_MESSAGE,
				    WebRequest.SCOPE_SESSION).toString();
				webRequest.removeAttribute(WebConstants.UNCAUGHT_EXCEPTION_MESSAGE, WebRequest.SCOPE_SESSION);
				if (StringUtils.isNotBlank(exceptionMsgTemp)) {
					exceptionMsg = exceptionMsgTemp;
				}
			}
			if (webRequest.getAttribute(WebConstants.REFERER_URL, WebRequest.SCOPE_SESSION) != null) {
				String refererUrlTemp = webRequest.getAttribute(WebConstants.REFERER_URL, WebRequest.SCOPE_SESSION)
				        .toString();
				webRequest.removeAttribute(WebConstants.REFERER_URL, WebRequest.SCOPE_SESSION);
				if (StringUtils.isNotBlank(refererUrlTemp) && !refererUrlTemp.contains("login.")) {
					refererUrl = refererUrlTemp;
				}
			}
			
			String alertMessage = null;
			if (requiredPrivileges != null && deniedPage != null) {
				alertMessage = Context.getMessageSourceService().getMessage("general.alert.requestPrivilegesForPage",
				    new String[] { Context.getAuthenticatedUser().getUsername(), requiredPrivileges, deniedPage }, null);
			} else if (exceptionMsg != null && deniedPage != null) {
				alertMessage = Context.getMessageSourceService().getMessage("general.alert.privilegesForPageOnException",
				    new String[] { exceptionMsg, Context.getAuthenticatedUser().getUsername(), deniedPage }, null);
			} else if (deniedPage != null) {
				alertMessage = Context.getMessageSourceService().getMessage("general.alert.requestUnKnownPrivilegesForPage",
				    new String[] { Context.getAuthenticatedUser().getUsername(), deniedPage }, null);
			} else if (requiredPrivileges != null) {
				alertMessage = Context.getMessageSourceService().getMessage("general.alert.requestPrivileges",
				    new String[] { Context.getAuthenticatedUser().getUsername(), requiredPrivileges }, null);
			} else if (exceptionMsg != null) {
				alertMessage = Context.getMessageSourceService().getMessage("general.alert.requestPrivileges",
				    new String[] { Context.getAuthenticatedUser().getUsername(), exceptionMsg }, null);
			}
			
			String reason = null;
			if (requiredPrivileges != null) {
				reason = Context.getMessageSourceService().getMessage("error.privilegesRequired",
				    new Object[] { requiredPrivileges }, null);
			} else if (exceptionMsg != null) {
				reason = exceptionMsg;
			} else {
				reason = Context.getMessageSourceService().getMessage("error.extraPrivilegesRequired");
			}
			
			//else we don't know both the page and privileges required, and there 
			//was no exception message that might contain the required privilege
			
			//will be sending the alert via ajax, so we need to escape js special chars
			model.put("alertMessage", JavaScriptUtils.javaScriptEscape(alertMessage));
			model.put("reason", reason);
			model.put("refererUrl", refererUrl);
		}
		
		return LOGIN_FORM;
	}
}

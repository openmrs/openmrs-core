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
package org.openmrs.web.controller;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

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
		if (Context.getAuthenticatedUser() != null) {
			model.addAttribute("foundMissingPrivileges", webRequest.getAttribute(WebConstants.FOUND_MISSING_PRIVILEGES,
			    WebRequest.SCOPE_SESSION));
			webRequest.removeAttribute(WebConstants.FOUND_MISSING_PRIVILEGES, WebRequest.SCOPE_SESSION);
			
			String deniedPage = null;
			String requiredPrivileges = null;
			String exceptionMsg = null;
			if (webRequest.getAttribute(WebConstants.DENIED_PAGE, WebRequest.SCOPE_SESSION) != null) {
				String deniedPageTemp = webRequest.getAttribute(WebConstants.DENIED_PAGE, WebRequest.SCOPE_SESSION)
				        .toString();
				webRequest.removeAttribute(WebConstants.DENIED_PAGE, WebRequest.SCOPE_SESSION);
				if (StringUtils.isNotBlank(deniedPageTemp))
					deniedPage = deniedPageTemp;
				
			}
			
			if (webRequest.getAttribute(WebConstants.REQUIRED_PRIVILEGES, WebRequest.SCOPE_SESSION) != null) {
				String requiredPrivilegesTemp = webRequest.getAttribute(WebConstants.REQUIRED_PRIVILEGES,
				    WebRequest.SCOPE_SESSION).toString();
				webRequest.removeAttribute(WebConstants.REQUIRED_PRIVILEGES, WebRequest.SCOPE_SESSION);
				if (StringUtils.isNotBlank(requiredPrivilegesTemp))
					requiredPrivileges = requiredPrivilegesTemp;
			}
			if (webRequest.getAttribute(WebConstants.UNCAUGHT_EXCEPTION_MESSAGE, WebRequest.SCOPE_SESSION) != null) {
				String exceptionMsgTemp = webRequest.getAttribute(WebConstants.UNCAUGHT_EXCEPTION_MESSAGE,
				    WebRequest.SCOPE_SESSION).toString();
				webRequest.removeAttribute(WebConstants.UNCAUGHT_EXCEPTION_MESSAGE, WebRequest.SCOPE_SESSION);
				if (StringUtils.isNotBlank(exceptionMsgTemp))
					exceptionMsg = exceptionMsgTemp;
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
			//else we don't know both the page and privileges required, and there 
			//was no exception message that might contain the required privilege
			
			if (alertMessage != null) {
				try {
					Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
					Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
					Context.addProxyPrivilege(PrivilegeConstants.GET_ROLES);
					
					Role role = Context.getUserService().getRole("System Developer");
					Collection<User> users = Context.getUserService().getUsersByRole(role);
					Context.getAlertService().saveAlert(new Alert(alertMessage, users));
				}
				finally {
					Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
					Context.removeProxyPrivilege(PrivilegeConstants.GET_USERS);
					Context.removeProxyPrivilege(PrivilegeConstants.GET_ROLES);
				}
			}
		}
		
		return LOGIN_FORM;
	}
}

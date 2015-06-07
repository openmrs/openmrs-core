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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controls the forgotten password form Initially a form with just a username box is shown Then a
 * box for the answer to the secret question is shown
 */
public class ForgotPasswordFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected static final Log log = LogFactory.getLog(ForgotPasswordFormController.class);
	
	/**
	 * Not used with the forgot password form controller.
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		return "";
	}
	
	/**
	 * The mapping from user's IP address to the number of attempts at logging in from that IP
	 */
	private Map<String, Integer> loginAttemptsByIP = new HashMap<String, Integer>();
	
	/**
	 * The mapping from user's IP address to the time that they were locked out
	 */
	private Map<String, Date> lockoutDateByIP = new HashMap<String, Date>();
	
	/**
	 * This takes in the form twice. The first time when the input their username and the second
	 * when they submit both their username and their secret answer
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String username = request.getParameter("uname");
		
		String ipAddress = request.getRemoteAddr();
		Integer forgotPasswordAttempts = loginAttemptsByIP.get(ipAddress);
		if (forgotPasswordAttempts == null) {
			forgotPasswordAttempts = 1;
		}
		
		boolean lockedOut = false;
		
		if (forgotPasswordAttempts > 5) {
			lockedOut = true;
			
			Date lockedOutTime = lockoutDateByIP.get(ipAddress);
			if (lockedOutTime != null && System.currentTimeMillis() - lockedOutTime.getTime() > 300000) {
				lockedOut = false;
				forgotPasswordAttempts = 0;
				lockoutDateByIP.put(ipAddress, null);
			} else {
				// they haven't been locked out before, or they're trying again
				// within the time limit.  Set the locked-out date to right now
				lockoutDateByIP.put(ipAddress, new Date());
			}
			
		}
		
		if (lockedOut) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.forgotPassword.tooManyAttempts");
		} else {
			// if the previous logic didn't determine that the user should be locked out,
			// then continue with the check
			
			forgotPasswordAttempts++;
			
			String secretAnswer = request.getParameter("secretAnswer");
			if (secretAnswer == null) {
				// if they are seeing this page for the first time
				
				User user = null;
				
				try {
					Context.addProxyPrivilege(PrivilegeConstants.VIEW_USERS);
					
					// only search if they actually put in a username
					if (username != null && username.length() > 0) {
						user = Context.getUserService().getUserByUsername(username);
					}
				}
				finally {
					Context.removeProxyPrivilege(PrivilegeConstants.VIEW_USERS);
				}
				
				if (user == null || StringUtils.isEmpty(user.getSecretQuestion())) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.question.empty");
				} else {
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.question.fill");
					request.setAttribute("secretQuestion", user.getSecretQuestion());
					
					// reset the forgotPasswordAttempts because they have a right user.
					// they will now have 5 more chances to get the question right
					forgotPasswordAttempts = 0;
				}
				
			} else {
				// if they've filled in the username and entered their secret answer
				
				User user = null;
				
				try {
					Context.addProxyPrivilege(PrivilegeConstants.VIEW_USERS);
					user = Context.getUserService().getUserByUsername(username);
				}
				finally {
					Context.removeProxyPrivilege(PrivilegeConstants.VIEW_USERS);
				}
				
				// check the secret question again in case the user got here "illegally"
				if (user == null || StringUtils.isEmpty(user.getSecretQuestion())) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.question.empty");
				} else if (user.getSecretQuestion() != null && Context.getUserService().isSecretAnswer(user, secretAnswer)) {
					
					StringBuilder randomPassword = new StringBuilder();
					for (int i = 0; i < 8; i++) {
						randomPassword.append(String.valueOf((Math.random() * (127 - 48) + 48)));
					}
					
					try {
						Context.addProxyPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS);
						Context.getUserService().changePassword(user, randomPassword.toString());
					}
					finally {
						Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS);
					}
					
					httpSession.setAttribute("resetPassword", randomPassword);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.password.reset");
					Context.authenticate(username, randomPassword.toString());
					httpSession.setAttribute("loginAttempts", 0);
					return new ModelAndView(new RedirectView(request.getContextPath() + "/options.form#Change Login Info"));
				} else {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.answer.invalid");
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.question.fill");
					request.setAttribute("secretQuestion", user.getSecretQuestion());
				}
			}
		}
		
		loginAttemptsByIP.put(ipAddress, forgotPasswordAttempts);
		request.setAttribute("uname", username);
		return showForm(request, response, errors);
	}
	
}

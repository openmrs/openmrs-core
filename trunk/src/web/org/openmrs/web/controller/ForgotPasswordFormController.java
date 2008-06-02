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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controls the forgotten password form
 * Initially a form with just a username box is shown
 * Then a box for the answer to the secret question is shown
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
	 * 
	 * This takes in the form twice.  The first time when the input their username and
	 * the second when they submit both their username and their secret answer
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String username = request.getParameter("uname");
		
		Integer loginAttempts = (Integer)httpSession.getAttribute("forgotPasswordAttempts");
		if (loginAttempts == null)
			loginAttempts = 0;
		
		try {
			String secretAnswer = request.getParameter("secretAnswer");
			if (secretAnswer == null) {
				// if they are seeing this page for the first time
				
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
				User user = null;
				
				// only search if they actually put in a username
				if (username != null && username.length() > 0)
					user = Context.getUserService().getUserByUsername(username);
				
				httpSession.setAttribute("loginAttempts", loginAttempts++);
				
				if (user == null || user.getSecretQuestion() == null || user.getSecretQuestion().equals("")) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.question.empty");
					request.setAttribute("uname", username);
					return showForm(request, response, errors);
				}
				else {
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.question.fill");
					request.setAttribute("uname", username);
					request.setAttribute("secretQuestion", user.getSecretQuestion());
					return showForm(request, response, errors);
				}
			}
			else if(secretAnswer != null) {
				// if they've filled in the username and entered their secret answer
				
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
				User user = Context.getUserService().getUserByUsername(username);
				httpSession.setAttribute("loginAttempts", loginAttempts++);
				
				// check the secret question again in case the user got her "illegally"
				if (user == null || user.getSecretQuestion() == null || user.getSecretQuestion().equals("")) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.question.empty");
					request.setAttribute("uname", username);
					return showForm(request, response, errors);
				}
				else if (user.getSecretQuestion() != null && Context.getUserService().isSecretAnswer(user, secretAnswer)) {
					
					Context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
					String randomPassword = "";
					for (int i=0; i<8; i++) {
						randomPassword += String.valueOf((Math.random() * (127-48) + 48));
					}
					Context.getUserService().changePassword(user, randomPassword);
					httpSession.setAttribute("resetPassword", randomPassword);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.password.reset");
					Context.authenticate(username, randomPassword);
					httpSession.setAttribute("loginAttempts", 0);
					return new ModelAndView(new RedirectView(request.getContextPath() + "/options.form#Change Login Info"));
				}
				else {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.answer.invalid");
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.question.fill");
					request.setAttribute("uname", username);
					request.setAttribute("secretQuestion", user.getSecretQuestion());
					return showForm(request, response, errors);
				}
			}
				
		}
		finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
		}
		
		String view = getFormView();
		
		return new ModelAndView(new RedirectView(view));
	}
	
}
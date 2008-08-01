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
package org.openmrs.web.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.OpenmrsCookieLocaleResolver;
import org.openmrs.web.WebConstants;

/**
 * This servlet accepts the username and password from the login form
 * and authenticates the user to OpenMRS
 * 
 * @see org.openmrs.api.context.Context#authenticate(String, String)
 */
public class LoginServlet extends HttpServlet {

	public static final long serialVersionUID = 134231247523L;
	protected static final Log log = LogFactory.getLog(LoginServlet.class);

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();

		String username = request.getParameter("uname");
		String password = request.getParameter("pw");
		
		// first option for redirecting is the "redirect" parameter (set on login.jsp from the session attr)
		String redirect = request.getParameter("redirect"); 
		
		// second option for redirecting is the referrer parameter set at login.jsp
		if (redirect == null || redirect.equals("")) {
			redirect = request.getParameter("refererURL");
			int index = redirect.indexOf(request.getContextPath(), 9);
			if (index != -1)
				redirect = redirect.substring(index);
		}
		
		// third option for redirecting is the main page of the webapp
		if (redirect == null || redirect.equals("")) {
			redirect = request.getContextPath();
		}
		
		log.debug("Going to use redirect: '" + redirect + "'");
		
		if (redirect != null) {
			// don't redirect back to the login page on success. (I assume the login page is {something}login.{something}
			if (redirect.contains("login.")) {
				log.debug("Redirect contains 'login.', redirecting to main page");
				redirect = request.getContextPath();
			}
			
			// don't redirect to pages outside of openmrs
			if (!redirect.startsWith(request.getContextPath())) {
				log.debug("redirect is outside of openmrs, redirecting to main page");
				redirect = request.getContextPath();
			}
		}
				
		Integer loginAttempts = (Integer)httpSession.getAttribute("loginAttempts");
		if (loginAttempts == null)
			loginAttempts = 0;
		
		try {
			// only try to authenticate if they actually typed in a username
			if (username == null || username.length() == 0)
				throw new ContextAuthenticationException("Unable to authenticate with an empty username");
				
			Context.authenticate(username, password);
			
			if (Context.isAuthenticated()) {
				httpSession.setAttribute("loginAttempts", 0);
				
				User user = Context.getAuthenticatedUser();
				
				// load the user's default locale if possible
				if (user.getUserProperties() != null) {
					if (user.getUserProperties().containsKey(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)) {
						String localeString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE);
						Locale locale = null;
						if (localeString.length() == 5) {
							//user's locale is language_COUNTRY (i.e. en_US)
							String lang = localeString.substring(0,2);
							String country = localeString.substring(3,5);
							locale = new Locale(lang, country);
						}
						else {
							// user's locale is only the language (language plus greater than 2 char country code
							locale = new Locale(localeString);
						}
						OpenmrsCookieLocaleResolver oclr = new OpenmrsCookieLocaleResolver();
						oclr.setLocale(request, response, locale);
					}
				}
				
				Boolean forcePasswordChange = new Boolean(user.getUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD)); 
				if (forcePasswordChange) {
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.password.change");
					redirect = request.getContextPath() + "/options.form#Change Login Info";
				}
				
				// In case the user has no preferences, make sure that the context has some locale set
				if (Context.getLocale() == null) {
					Context.setLocale(OpenmrsConstants.GLOBAL_DEFAULT_LOCALE);
				}
				
				if (log.isDebugEnabled()) {
					log.debug("Redirecting after login to: " + redirect);
					log.debug("Locale address: " + request.getLocalAddr());
				}
				
				response.sendRedirect(redirect);
			
				httpSession.setAttribute(WebConstants.OPENMRS_CLIENT_IP_HTTPSESSION_ATTR, request.getLocalAddr());
				httpSession.removeAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR);
				
				return;
			}
		} catch (ContextAuthenticationException e) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.password.invalid");
			httpSession.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, redirect);
			response.sendRedirect(request.getContextPath() + "/login.htm");
		}
		finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
		}
	}

}

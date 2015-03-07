/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.servlet;

import static org.openmrs.web.WebConstants.GP_ALLOWED_LOGIN_ATTEMPTS_PER_IP;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.OpenmrsCookieLocaleResolver;
import org.openmrs.web.WebConstants;
import org.openmrs.web.WebUtil;
import org.openmrs.web.user.CurrentUsers;
import org.openmrs.web.user.UserProperties;

/**
 * This servlet accepts the username and password from the login form and authenticates the user to
 * OpenMRS
 *
 * @see org.openmrs.api.context.Context#authenticate(String, String)
 */
public class LoginServlet extends HttpServlet {
	
	public static final long serialVersionUID = 134231247523L;
	
	protected static final Log log = LogFactory.getLog(LoginServlet.class);
	
	/**
	 * The mapping from user's IP address to the number of attempts at logging in from that IP
	 */
	private Map<String, Integer> loginAttemptsByIP = new HashMap<String, Integer>();
	
	/**
	 * The mapping from user's IP address to the time that they were locked out
	 */
	private Map<String, Date> lockoutDateByIP = new HashMap<String, Date>();
	
	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		
		String ipAddress = request.getRemoteAddr();
		Integer loginAttempts = loginAttemptsByIP.get(ipAddress);
		if (loginAttempts == null) {
			loginAttempts = 1;
		}
		
		loginAttempts++;
		
		boolean lockedOut = false;
		// look up the allowed # of attempts per IP
		Integer allowedLockoutAttempts = 100;
		
		String allowedLockoutAttemptsGP = Context.getAdministrationService().getGlobalProperty(
		    GP_ALLOWED_LOGIN_ATTEMPTS_PER_IP, "100");
		try {
			allowedLockoutAttempts = Integer.valueOf(allowedLockoutAttemptsGP.trim());
		}
		catch (NumberFormatException nfe) {
			log.error("Unable to format '" + allowedLockoutAttemptsGP + "' from global property "
			        + GP_ALLOWED_LOGIN_ATTEMPTS_PER_IP + " as an integer");
		}
		
		// allowing for configurable login attempts here in case network setups are such that all users have the same IP address. 
		if (allowedLockoutAttempts > 0 && loginAttempts > allowedLockoutAttempts) {
			lockedOut = true;
			
			Date lockedOutTime = lockoutDateByIP.get(ipAddress);
			if (lockedOutTime != null && System.currentTimeMillis() - lockedOutTime.getTime() > 300000) {
				lockedOut = false;
				loginAttempts = 0;
				lockoutDateByIP.put(ipAddress, null);
			} else {
				// they haven't been locked out before, or they're trying again
				// within the time limit.  Set the locked-out date to right now
				lockoutDateByIP.put(ipAddress, new Date());
			}
			
		}
		
		// get the place to redirect to either now, or after they eventually
		// authenticate correctly
		String redirect = determineRedirect(request);
		
		if (lockedOut) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.login.tooManyAttempts");
		} else {
			try {
				
				String username = request.getParameter("uname");
				String password = request.getParameter("pw");
				
				// only try to authenticate if they actually typed in a username
				if (username == null || username.length() == 0) {
					throw new ContextAuthenticationException("Unable to authenticate with an empty username");
				}
				
				Context.authenticate(username, password);
				
				if (Context.isAuthenticated()) {
					regenerateSession(request);
					httpSession = request.getSession();//get the newly generated session
					httpSession.setAttribute("loginAttempts", 0);
					User user = Context.getAuthenticatedUser();
					
					// load the user's default locale if possible
					if (user.getUserProperties() != null
					        && user.getUserProperties().containsKey(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)) {
						String localeString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE);
						Locale locale = WebUtil.normalizeLocale(localeString);
						// if locale object is valid we should store it
						if (locale != null) {
							OpenmrsCookieLocaleResolver oclr = new OpenmrsCookieLocaleResolver();
							oclr.setLocale(request, response, locale);
						}
					}
					
					if (new UserProperties(user.getUserProperties()).isSupposedToChangePassword()) {
						httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.password.change");
						redirect = request.getContextPath() + "/changePassword.form";
					}
					
					// In case the user has no preferences, make sure that the context has some locale set
					if (Context.getLocale() == null) {
						Context.setLocale(LocaleUtility.getDefaultLocale());
					}
					
					CurrentUsers.addUser(httpSession, user);
					
					if (log.isDebugEnabled()) {
						log.debug("Redirecting after login to: " + redirect);
						log.debug("Locale address: " + request.getLocalAddr());
					}
					
					response.sendRedirect(redirect);
					
					httpSession.setAttribute(WebConstants.OPENMRS_CLIENT_IP_HTTPSESSION_ATTR, request.getLocalAddr());
					httpSession.removeAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR);
					
					// unset login attempts by this user because they were
					// able to successfully log in
					
					loginAttemptsByIP.remove(ipAddress);
					
					return;
				}
			}
			catch (ContextAuthenticationException e) {
				// set the error message for the user telling them
				// to try again
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.password.invalid");
			}
			
		}
		
		// send the user back the login page because they either 
		// had a bad password or are locked out
		loginAttemptsByIP.put(ipAddress, loginAttempts);
		httpSession.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, redirect);
		response.sendRedirect(request.getContextPath() + "/login.htm");
	}
	
	/**
	 * Convenience method for pulling the correct page to redirect to out of the request
	 *
	 * @param request the current request
	 * @return the page to redirect to as determined by parameters in the request
	 */
	private String determineRedirect(HttpServletRequest request) {
		// first option for redirecting is the "redirect" parameter (set on login.jsp from the session attr)
		String redirect = request.getParameter("redirect");
		
		// second option for redirecting is the referrer parameter set at login.jsp
		if (redirect == null || "".equals(redirect)) {
			redirect = request.getParameter("refererURL");
			if (redirect != null && !redirect.startsWith("/")) {
				// if we have an absolute url, make sure its in our domain
				Integer requestURLLength = request.getRequestURL().length();
				StringBuffer domainAndPort = request.getRequestURL();
				domainAndPort.delete(requestURLLength - request.getRequestURI().length(), requestURLLength);
				if (!redirect.startsWith(domainAndPort.toString())) {
					redirect = null; // send them to the homepage
				} else {
					// now cut out everything but the path
					// get the first slash after https:// or http://
					redirect = redirect.substring(redirect.indexOf("/", 9));
				}
			}
		}
		
		// third option for redirecting is the main page of the webapp
		if (StringUtils.isEmpty(redirect)) {
			redirect = request.getContextPath();
		}

		// don't redirect back to the login page on success. (I assume the login page is {something}login.{something}
		else if (redirect.contains("login.")) {
			log.debug("Redirect contains 'login.', redirecting to main page");
			redirect = request.getContextPath();
		}

		// don't redirect to pages outside of openmrs
		else if (!redirect.startsWith(request.getContextPath())) {
			log.debug("redirect is outside of openmrs, redirecting to main page");
			redirect = request.getContextPath();
		}

		// don't redirect back to the initialsetup page
		else if (redirect.endsWith(WebConstants.SETUP_PAGE_URL)) {
			log.debug("redirect is back to the setup page because this is their first ever login");
			redirect = request.getContextPath();
		} else if (redirect.contains("/options.form") || redirect.contains("/changePassword.form")
		        || redirect.contains("/forgotPassword.form")) {
			log
			        .debug("The user was on a page for setting/changing passwords. Send them to the homepage to reduce confusion");
			redirect = request.getContextPath();
		}
		
		log.debug("Going to use redirect: '" + redirect + "'");
		
		return redirect;
	}
	
	/**
	 * Regenerates session id after each login attempt.
	 * @param request
	 */
	private void regenerateSession(HttpServletRequest request) {
		
		HttpSession oldSession = request.getSession();
		
		Enumeration attrNames = oldSession.getAttributeNames();
		Properties props = new Properties();
		
		if (attrNames != null) {
			while (attrNames.hasMoreElements()) {
				String key = (String) attrNames.nextElement();
				props.put(key, oldSession.getAttribute(key));
			}
			
			//Invalidating previous session
			oldSession.invalidate();
			//Generate new session
			HttpSession newSession = request.getSession(true);
			attrNames = props.keys();
			
			while (attrNames.hasMoreElements()) {
				String key = (String) attrNames.nextElement();
				newSession.setAttribute(key, props.get(key));
			}
		}
	}
}

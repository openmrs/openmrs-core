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

public class LoginServlet extends HttpServlet {

	public static final long serialVersionUID = 1L;
	protected Log log = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();

		String username = request.getParameter("uname");
		String password = request.getParameter("pw");
		
		// first option for redirecting is the "redirect" parameter (set from the session attr)
		String redirect = request.getParameter("redirect"); 
		
		// second option for redirecting is the referer paramater set at login.jsp
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
		
		log.debug("4 redirect: '" + redirect + "'");
		
		if (redirect != null) {
			// don't redirect back to the login page on success. (I assume the login page is {something}login.{something}
			String s = redirect;
			if (s.indexOf('/') >= 0) {
				s = s.substring(s.lastIndexOf('/') + 1);
			}
			if (s.indexOf('.') >= 0) {
				s = s.substring(0, s.indexOf('.'));
			}
			if (s.equals("login")) {
				redirect = request.getContextPath();
			}
			
			// don't redirect to pages outside of openmrs
			if (!redirect.startsWith(request.getContextPath())) {
				redirect = request.getContextPath();
			}
		}
				
		Object attempts = httpSession.getAttribute("loginAttempts");
		Integer loginAttempts = 0;
		if (attempts != null)
			loginAttempts = (Integer)attempts;
		
		try {
			String forgotPassword = request.getParameter("forgotPassword");
			String secretAnswer = request.getParameter("secretAnswer");
			if (forgotPassword != null && new Boolean(forgotPassword).booleanValue()) {
				// if they checked the box for "I forgot my password"
				
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
				User user = Context.getUserService().getUserByUsername(username);
				httpSession.setAttribute("loginAttempts", loginAttempts++);
				
				if (user != null && user.getSecretQuestion() == null || user.getSecretQuestion().equals("")) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.question.empty");
					response.sendRedirect(request.getContextPath() + "/login.htm?username=" + username);
				}
				else {
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.question.fill");
					response.sendRedirect(request.getContextPath() + "/login.htm?username=" + username + "&forgotPassword=true&secretQuestion=" + user.getSecretQuestion());
				}
			}
			else if (secretAnswer != null) {
				// if they've checked the box and then entered their secret answer
				
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
				User user = Context.getUserService().getUserByUsername(username);
				httpSession.setAttribute("loginAttempts", loginAttempts++);
				
				if (user.getSecretQuestion() != null && Context.getUserService().isSecretAnswer(user, secretAnswer)) {
					
					Context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
					String randomPassword = "";
					for (int i=0; i<8; i++) {
						randomPassword += String.valueOf((Math.random() * (127-48) + 48));
					}
					Context.getUserService().changePassword(user, randomPassword);
					httpSession.setAttribute("resetPassword", randomPassword);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.password.reset");
					Context.authenticate(username, randomPassword);
					response.sendRedirect(request.getContextPath() + "/options.form#Change Login Info");
				}
				else {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.answer.invalid");
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.question.fill");
					response.sendRedirect(request.getContextPath() + "/login.htm?username=" + username + "&forgotPassword=true&secretQuestion=" + user.getSecretQuestion());
				}
				
			}
			else {
				Context.authenticate(username, password);
				
				if (Context.isAuthenticated()) {
					
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
					
					log.debug("Redirecting after login to: " + redirect);
					response.sendRedirect(redirect);
				
					log.debug(request.getLocalAddr());
					httpSession.setAttribute(WebConstants.OPENMRS_CLIENT_IP_HTTPSESSION_ATTR, request.getLocalAddr());
					httpSession.removeAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR);
					
					return;
				}
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

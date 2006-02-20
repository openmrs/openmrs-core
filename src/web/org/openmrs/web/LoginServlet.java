package org.openmrs.web;

import java.io.IOException;

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

		String username = request.getParameter("uname");
		String password = request.getParameter("pw");
		String redirect = request.getParameter("redirect");
		if (redirect == null || request.equals(""))
			redirect = request.getContextPath();
		
		HttpSession httpSession = request.getSession();

		Context context = (Context)httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
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
				
				context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
				User user = context.getUserService().getUserByUsername(username);
				httpSession.setAttribute("loginAttempts", loginAttempts++);
				
				if (user.getSecretQuestion() == null || user.getSecretQuestion().equals("")) {
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
				
				context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
				User user = context.getUserService().getUserByUsername(username);
				httpSession.setAttribute("loginAttempts", loginAttempts++);
				
				if (user.getSecretQuestion() != null && context.getUserService().isSecretAnswer(user, secretAnswer)) {
					
					context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
					String randomPassword = "";
					for (int i=0; i<8; i++) {
						randomPassword += String.valueOf((Math.random() * (127-48) + 48));
					}
					context.getUserService().changePassword(user, randomPassword);
					httpSession.setAttribute("resetPassword", randomPassword);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.password.reset");
					context.authenticate(username, randomPassword);
					response.sendRedirect(request.getContextPath() + "/options.form#Change Login Info");
				}
				else {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.answer.invalid");
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.question.fill");
					response.sendRedirect(request.getContextPath() + "/login.htm?username=" + username + "&forgotPassword=true&secretQuestion=" + user.getSecretQuestion());
				}
				
			}
			else {
				context.authenticate(username, password);
				
				if (context.isAuthenticated()) {
					
					User user = context.getAuthenticatedUser();
					
					Boolean forcePasswordChange = new Boolean(user.getProperties().get(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD)); 
					if (forcePasswordChange) {
						httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "User.password.change");
						redirect = request.getContextPath() + "/options.form#Change Login Info";
					}
					
					response.sendRedirect(redirect);
				
					log.debug(request.getLocalAddr());
					httpSession.setAttribute(WebConstants.OPENMRS_CLIENT_IP_HTTPSESSION_ATTR, request.getLocalAddr());
					httpSession.removeAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR);
					
					return;
				}
			}
		} catch (ContextAuthenticationException e) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.password.invalid");
			response.sendRedirect(request.getContextPath() + "/login.htm");
		}
		finally {
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
		}
	}

}

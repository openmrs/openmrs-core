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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

/**
 * Servlet called by the logout link in the webapp. This will call Context.logout() and then make
 * sure the current user's http session is cleaned up and ready for another user to log in
 * 
 * @see Context#logout()
 */
public class LogoutServlet extends HttpServlet {
	
	public static final long serialVersionUID = 123423L;
	
	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession httpSession = request.getSession();
		
		Context.logout();
		
		httpSession.removeAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR);
		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.logged.out");
		httpSession.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, request.getContextPath());
		response.sendRedirect(request.getContextPath() + "/login.htm");
		
		httpSession.invalidate();
	}
	
}

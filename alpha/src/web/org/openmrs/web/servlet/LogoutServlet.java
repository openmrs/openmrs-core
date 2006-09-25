package org.openmrs.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

public class LogoutServlet extends HttpServlet {

	public static final long serialVersionUID = 123423L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();
		
		Context context = (Context)httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (context != null)
			context.logout();
		httpSession.removeAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "auth.logged.out");
		httpSession.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, request.getContextPath());
		response.sendRedirect(request.getContextPath() + "/login.htm");
	}

}

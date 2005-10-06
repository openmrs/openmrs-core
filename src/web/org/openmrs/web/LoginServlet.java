package org.openmrs.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.context.Context;
import org.openmrs.context.ContextAuthenticationException;

public class LoginServlet extends HttpServlet {

	public static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String redirect = request.getParameter("redirect");
		if (redirect == "" || redirect == null)
			redirect = "formentry";
		
		HttpSession httpSession = request.getSession();
		
		Context context = (Context)httpSession.getAttribute("__openmrs_context");
		
		try {
			context.authenticate(username, password);
			if (context.isAuthenticated()) {
				response.sendRedirect(redirect);
				return;
			}
		} catch (ContextAuthenticationException e) {
			httpSession.setAttribute("openmrs_msg", "Invalid credentials. Please try again.");
			response.sendRedirect(request.getContextPath() + "/login.jsp");
		}
	}

}

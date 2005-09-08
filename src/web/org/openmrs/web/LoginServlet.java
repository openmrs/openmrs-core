package org.openmrs.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.context.ContextAuthenticationException;
import org.openmrs.context.ContextFactory;

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

		try {
			ContextFactory.getContext().authenticate(username, password);
			if (ContextFactory.getContext().isAuthenticated()) {
				response.sendRedirect("../formentry/index.jsp");
				return;
			}
		} catch (ContextAuthenticationException e) {
			response
					.sendRedirect("login.jsp?msg=Invalid+credentials.+Try again.");
		}
	}

}

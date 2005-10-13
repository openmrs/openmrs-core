package org.openmrs.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.context.Context;

public class EditUserServlet extends HttpServlet {

	public static final long serialVersionUID = 1123432456L;

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
		Context context = (Context)httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Your session has expired.");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}		
		String userId   = request.getParameter("userId");
		UserService userService = context.getUserService();
		User user = userService.getUser(Integer.valueOf(request.getParameter("userId")));
		user.setUsername(request.getParameter("username"));
		user.setFirstName(request.getParameter("firstName"));
		user.setMiddleName(request.getParameter("middleName"));
		user.setLastName(request.getParameter("lastName"));
		
		//get the roles from the parameter list
		String[] roles = request.getParameterValues("roles");
		//the role objects to save
		Set<Role> roleObjs = new HashSet<Role>();
		//iterate over the parameter strings making new role objects
		if (roles != null) {
			for(int x = 0; x < roles.length; x++) {
				roleObjs.add(new Role(roles[x]));
			}
		}
		user.setRoles(roleObjs);
		
		try {
			if (context.isAuthenticated()) {
				context.getUserService().updateUser(user);
				httpSession.setAttribute("openmrs_msg", "'" + user.getUsername() + "' updated");
				response.sendRedirect("editUser.jsp?id=" + userId);
				return;
			}
		} catch (Exception e) {
			response.sendRedirect(request.getContextPath() + "/login.jsp?msg=Invalid+credentials.+Try again.");
		}
	}

}

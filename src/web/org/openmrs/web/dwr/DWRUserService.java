package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRUserService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findUsers(String searchValue, List<String> roles, boolean includeVoided) {
		
		Vector userList = new Vector();

		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		if (context == null) {
			userList.add("Your session has expired.");
			userList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				UserService us = context.getUserService();
				List<User> users = new Vector<User>();
				User user = null;
				try {
					user = us.getUserByUsername(searchValue);
				}
				catch (Exception e) {}
				
				if (user != null)
					users.add(user);
				
				if (roles == null) 
					roles = new Vector<String>();
				
				users.addAll(us.findUsers(searchValue, roles, includeVoided));
				userList = new Vector(users.size());
				for (User u : users) {
					userList.add(new UserListItem(u));
				}
			} catch (Exception e) {
				log.error(e);
			}
		}
		return userList;
	}

}

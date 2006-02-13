package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.FormEntryService;
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
				Integer userId = -1;
				User us = context.getAuthenticatedUser();
				if (us != null)
					userId = us.getUserId();
				
				log.info(userId + "|" + searchValue + "|" + roles.toString());
				
				FormEntryService fs = context.getFormEntryService();
				List<User> users = new Vector<User>();
				
				if (roles == null) 
					roles = new Vector<String>();
				
				users.addAll(fs.findUsers(searchValue, roles, includeVoided));
				
				userList = new Vector(users.size());
				
				for (User u : users) {
					userList.add(new UserListItem(u));
				}
			} catch (Exception e) {
				log.error(e);
				userList.add("Error while attempting to find users - " + e.getMessage());
			}
			
			if (userList.size() == 0) {
				userList.add("No users found. Please search again.");
			}
		}
		return userList;
	}

	
	public Vector getAllUsers(List<String> roles, boolean includeVoided) {
		
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
				FormEntryService fs = context.getFormEntryService();
				List<User> users = new Vector<User>();
				
				if (roles == null) 
					roles = new Vector<String>();
				
				users.addAll(fs.getAllUsers(roles, includeVoided));
				
				userList = new Vector(users.size());
				
				for (User u : users) {
					userList.add(new UserListItem(u));
				}
				
			} catch (Exception e) {
				log.error(e);
				userList.add("Error while attempting to get users - " + e.getMessage());
			}
		}
		return userList;
	}
}

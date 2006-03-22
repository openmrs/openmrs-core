package org.openmrs.web.dwr;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRUserService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Collection<UserListItem> findUsers(String searchValue, List<String> roles, boolean includeVoided) {
		
		Vector userList = new Vector();

		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		Context context = (Context) request.getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		if (context == null) {
			userList.add("Your session has expired.");
			userList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				String userId = "Anonymous";
				if (context.isAuthenticated()) {
					User us = context.getAuthenticatedUser();
					userId = us.getUserId().toString();
				}
				
				log.info(userId + "|" + searchValue + "|" + roles.toString());
				
				FormEntryService fs = context.getFormEntryService();
				Set<User> users = new HashSet<User>();
				
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

	
	public Collection<UserListItem> getAllUsers(List<String> roles, boolean includeVoided) {
		
		Vector userList = new Vector();

		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		Context context = (Context) request.getSession(false)
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		if (context == null) {
			userList.add("Your session has expired.");
			userList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				FormEntryService fs = context.getFormEntryService();
				Set<User> users = new HashSet<User>();
				
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

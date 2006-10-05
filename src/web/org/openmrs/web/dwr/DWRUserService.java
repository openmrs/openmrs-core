package org.openmrs.web.dwr;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryService;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRUserService {

	protected final Log log = LogFactory.getLog(getClass());
	
	@SuppressWarnings("unchecked")
	public Collection<UserListItem> findUsers(String searchValue, List<String> roles, boolean includeVoided) {
		
		Vector userList = new Vector();

		try {
			String userId = "Anonymous";
			if (Context.isAuthenticated()) {
				User us = Context.getAuthenticatedUser();
				userId = us.getUserId().toString();
			}
			
			log.info(userId + "|" + searchValue + "|" + roles.toString());
			
			FormEntryService fs = Context.getFormEntryService();
			Set<User> users = new HashSet<User>();
			
			if (roles == null) 
				roles = new Vector<String>();
			
			users.addAll(fs.findUsers(searchValue, roles, includeVoided));
			
			userList = new Vector(users.size());
			
			for (User u : users) {
				userList.add(new UserListItem(u));
			}
		} catch (Exception e) {
			log.error("Error while searching for users", e);
			userList.add("Error while attempting to find users - " + e.getMessage());
		}
		
		if (userList.size() == 0) {
			userList.add("No users found. Please search again.");
		}
		
		return userList;

	}

	
	@SuppressWarnings("unchecked")
	public Collection<UserListItem> getAllUsers(List<String> roles, boolean includeVoided) {
		
		Vector userList = new Vector();

		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		if (!Context.isAuthenticated()) {
			userList.add("Your session has expired.");
			userList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				FormEntryService fs = Context.getFormEntryService();
				Set<User> users = new TreeSet<User>(new UserComparator());
				
				if (roles == null) 
					roles = new Vector<String>();
				
				users.addAll(fs.getAllUsers(roles, includeVoided));
				
				userList = new Vector(users.size());
				
				for (User u : users) {
					userList.add(new UserListItem(u));
				}
				
			} catch (Exception e) {
				log.error("Error while getting all users", e);
				userList.add("Error while attempting to get users - " + e.getMessage());
			}
		}
		return userList;
	}
	
	public UserListItem getUser(Integer userId) {
		UserListItem user = new UserListItem();
		
		if (Context.isAuthenticated()) {
			try {
				user = new UserListItem(Context.getUserService().getUser(userId));
			}
			catch (Exception e) {
				log.error("Error while getting user", e);
			}
		}
		
		return user;
	}
	
	private class UserComparator implements Comparator<User> {

		public int compare(User user1, User user2) {
			String name1 = "" + user1.getLastName() + user1.getFirstName() + user1.getMiddleName();
			String name2 = "" + user2.getLastName() + user2.getFirstName() + user2.getMiddleName();
			return name1.compareTo(name2);
		}
		
	}
}
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.WebContextFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;

/**
 * A collection of methods used by DWR for access users. These methods are similar to the
 * {@link UserService} methods and have been chosen to be exposed via dwr to allow for access via
 * javascript.
 */
public class DWRUserService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Find users in the database that match the given search values.
	 *
	 * @see UserService#getUsers(String, List, boolean)
	 * @param searchValue a query string like 'john doe'
	 * @param rolesStrings list of role names to restrict to like '[Provider, Manager]'
	 * @param includeVoided true/false to include voided users in the search
	 * @return list of {@link UserListItem}s (or String warning message if none found)
	 */
	@SuppressWarnings("unchecked")
	public Collection<UserListItem> findUsers(String searchValue, List<String> rolesStrings, boolean includeVoided) {
		
		Vector userList = new Vector();
		
		try {
			UserService userService = Context.getUserService();
			
			if (rolesStrings == null) {
				rolesStrings = new Vector<String>();
			}
			
			List<Role> roles = new Vector<Role>();
			for (String r : rolesStrings) {
				if (!"".equals(r)) {
					Role role = userService.getRole(r);
					if (role != null) {
						roles.add(role);
					}
				}
			}
			
			userList = new Vector();
			
			for (User u : userService.getUsers(searchValue, roles, includeVoided)) {
				userList.add(new UserListItem(u));
			}
		}
		catch (Exception e) {
			log.error("Error while searching for users", e);
			userList.add("Error while attempting to find users - " + e.getMessage());
		}
		
		if (userList.size() == 0) {
			userList.add("No users found. Please search again.");
		}
		
		return userList;
		
	}
	
	@SuppressWarnings("unchecked")
	public Collection<UserListItem> getAllUsers(List<String> roleStrings, boolean includeVoided) {
		
		Vector userList = new Vector();
		
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		if (!Context.isAuthenticated()) {
			userList.add("Your session has expired.");
			userList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		} else {
			try {
				UserService us = Context.getUserService();
				Set<User> users = new TreeSet<User>(new UserComparator());
				
				if (roleStrings == null) {
					roleStrings = new Vector<String>();
				}
				
				List<Role> roles = new Vector<Role>();
				for (String r : roleStrings) {
					if (!"".equals(r)) {
						Role role = us.getRole(r);
						if (role != null) {
							roles.add(role);
						}
					}
				}
				
				users.addAll(us.getUsers(null, roles, includeVoided));
				
				userList = new Vector(users.size());
				
				for (User u : users) {
					userList.add(new UserListItem(u));
				}
				
			}
			catch (Exception e) {
				log.error("Error while getting all users", e);
				userList.add("Error while attempting to get users - " + e.getMessage());
			}
		}
		return userList;
	}
	
	/**
	 * Get the user identified by <code>userId</code>
	 *
	 * @param userId
	 * @return
	 */
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
	
	/**
	 * Determines the order of the user's in the user list
	 */
	private class UserComparator implements Comparator<User> {
		
		public int compare(User user1, User user2) {
			
			// compare on full name (and then on user id in case the names are identical) 
			String name1 = "" + user1.getPersonName() + user1.getUserId();
			String name2 = "" + user2.getPersonName() + user2.getUserId();
			
			return name1.compareTo(name2);
		}
		
	}
	
}

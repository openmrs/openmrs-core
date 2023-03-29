/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.CurrentUsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;

/**
 * A service that manages the list of current users.
 */
public class CurrentUsersService extends User{
	
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(CurrentUsersService.class);
	
	private static final Map<String, String> currentUserMap = new ConcurrentHashMap<>();
	
	private static List<HttpSession> currentUsers = new ArrayList<>();
	
	public static Map<String, String> getCurrentUserMap() {
		return currentUserMap;
	}

	/**
	 * Add the user to the current users.
	 * 
	 * @param user the user that just logged in
	 */
	public void addUser(User user) {
		String currentUserName = user.getUsername();
		// if user name is blank then print their system id
		if (StringUtils.isBlank(currentUserName)) {
			currentUserName = "systemid:" + user.getSystemId();
		}
		if (log.isDebugEnabled()) {
			log.debug("Adding current user " + currentUserName);
		}
		currentUserMap.put(user.getSessionId(), currentUserName);
	}
	
	/**
	 * Remove the current user from the list of current users.
	 * 
	 * @param sessionId the session ID of the user to remove
	 */
	public void removeUser(String sessionId) {
		if (log.isDebugEnabled()) {
			log.debug(
			    "Removing user from the current users. session: " + sessionId + " user: " + currentUserMap.get(sessionId));
		}
		currentUserMap.remove(sessionId);
	}
	
	public static List<HttpSession> getCurrentUsers() {
		return currentUsers;
	}
	
	/**
	 * Get sorted user names list.
	 * 
	 * @return sorted user names
	 */
	public List<String> getCurrentUsernames() {
		List<String> userNames = new ArrayList<String>(currentUserMap.values());
		Collections.sort(userNames);
		return userNames;
	}

	
	public boolean isUserLoggedIn(HttpSession session) {
		String username = (String) session.getAttribute("username");
		return username != null && currentUsers.contains(username);
	}
	
	public void setCurrentUsers(List<HttpSession> currentUsers) {
		this.currentUsers = currentUsers;
	}
	
}


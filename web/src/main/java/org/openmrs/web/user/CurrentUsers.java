/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.web.WebConstants;

/**
 * Utility methods which maintain the list of current users.
 */
public class CurrentUsers {
	
	private static final Log log = LogFactory.getLog(CurrentUsers.class);
	
	/**
	 * Initialize the current users list.
	 * 
	 * @param servletContext
	 */
	public static Map<String, String> init(ServletContext servletContext) {
		Map<String, String> currentUserMap = Collections.synchronizedMap(new TreeMap<String, String>());
		servletContext.setAttribute(WebConstants.CURRENT_USERS, currentUserMap);
		return currentUserMap;
	}
	
	/**
	 * Get the current list of map of users stored in the session
	 * 
	 * @param httpSession the current session
	 * @return map of users logged in
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, String> getCurrentUsers(HttpSession httpSession) {
		Map<String, String> currentUsers = (Map<String, String>) httpSession.getServletContext().getAttribute(
		    WebConstants.CURRENT_USERS);
		if (currentUsers == null) {
			currentUsers = init(httpSession.getServletContext());
		}
		return currentUsers;
	}
	
	/**
	 * Add the user to the current users.
	 * 
	 * @param httpSession
	 * @param user the user that just logged in
	 */
	public static void addUser(HttpSession httpSession, User user) {
		Map<String, String> currentUsers = getCurrentUsers(httpSession);
		String currentUserName = user.getUsername();
		// if user name is blank then print their system id
		if (StringUtils.isBlank(currentUserName)) {
			currentUserName = "systemid:" + user.getSystemId();
		}
		if (log.isDebugEnabled()) {
			log.debug("Adding current user " + currentUserName);
		}
		currentUsers.put(httpSession.getId(), currentUserName);
	}
	
	/**
	 * Remove the current user from the list of current users.
	 * 
	 * @param httpSession
	 */
	public static void removeUser(HttpSession httpSession) {
		String sessionId = httpSession.getId();
		Map<String, String> currentUsers = getCurrentUsers(httpSession);
		if (log.isDebugEnabled()) {
			log.debug("Removing user from the current users. session: " + sessionId + " user: "
			        + currentUsers.get(sessionId));
		}
		currentUsers.remove(sessionId);
	}
	
	/**
	 * Get sorted user names list.
	 * 
	 * @param httpSession
	 * @return sorted user names
	 */
	public static List<String> getCurrentUsernames(HttpSession httpSession) {
		Map<String, String> currentUsers = getCurrentUsers(httpSession);
		List<String> userNames = new ArrayList<String>();
		synchronized (currentUsers) {
			for (String value : currentUsers.values()) {
				userNames.add(value);
			}
		}
		Collections.sort(userNames);
		return userNames;
	}
	
}

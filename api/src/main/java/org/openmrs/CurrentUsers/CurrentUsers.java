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

import javax.servlet.http.HttpSession;

import org.openmrs.User;

/**
 * Utility methods which maintain the list of current users.
 */
public class CurrentUsers extends CurrentUsersService {
	
	
	/*private static final CurrentUsersService currentUserMap = (CurrentUsersService) Collections
	        .synchronizedMap(new TreeMap<>());*/
	
	public static void addUser(HttpSession httpSession, User user) {
		CurrentUsersService currentUsers = (CurrentUsersService) getCurrentUserMap();
		user.setSessionId(httpSession.getId());
		currentUsers.put(httpSession.getId(), user);
	}
	
	public static void removeUser(HttpSession httpSession) {
		String sessionId = httpSession.getId();
		CurrentUsersService currentUsers = (CurrentUsersService) getCurrentUsers();
		User user = currentUsers.getSessionId();
		if (user != null) {
			user.setSessionId(null);
		}
		currentUsers.removeUser(sessionId);
	}
	

	/**
	 * Get sorted user names list.
	 * 
	 * @param httpSession
	 * @return sorted user names
	 */
	@Override
	public List<String> getCurrentUsernames() {
		List<String> userNames = new ArrayList<>();
		synchronized (CurrentUsersService.getCurrentUserMap()) {
			for (String value : CurrentUsersService.getCurrentUserMap().values()) {
				userNames.add(value);
			}
		}
		Collections.sort(userNames);
		return userNames;
	}
	
}

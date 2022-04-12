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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.openmrs.User;
import org.openmrs.web.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods which maintain the list of current users.
 * @since 2.6.0
 */
public class CurrentUsers {

	private static final Logger log = LoggerFactory.getLogger(CurrentUsers.class);

	/**
	 * Initialize the current users list.
	 *
	 * @param servletContext
	 */
	public static Map<String, CopyOnWriteArrayList<HttpSession>> init(ServletContext servletContext) {
		Map<String, CopyOnWriteArrayList<HttpSession>> currentUserMap = Collections.synchronizedMap(new TreeMap<>());
		servletContext.setAttribute(WebConstants.CURRENT_USERS, currentUserMap);
		return currentUserMap;
	}

	/**
	 * Get the current list of map of users stored in the session
	 *
	 * @param httpSession the current session
	 * @return map of users logged in and their sessions
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, CopyOnWriteArrayList<HttpSession>> getCurrentUsers(HttpSession httpSession) {
		Map<String, CopyOnWriteArrayList<HttpSession>> currentUsers = (Map<String, CopyOnWriteArrayList<HttpSession>>) httpSession
			.getServletContext().getAttribute(WebConstants.CURRENT_USERS);
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
		String username = user.getUsername();
		// if user name is blank then print their system id
		if (StringUtils.isBlank(username)) {
			username = "systemid:" + user.getSystemId();
		}
		if (log.isDebugEnabled()) {
			log.debug("Adding current user " + username);
		}
		Map<String, CopyOnWriteArrayList<HttpSession>> currentUsers = getCurrentUsers(httpSession);
		if (!currentUsers.containsKey(username)) {
			currentUsers.put(username, new CopyOnWriteArrayList<>());
		}
		CopyOnWriteArrayList<HttpSession> sessions = currentUsers.get(username);
		sessions.addIfAbsent(httpSession);
		log.debug("Added new session. Total sessions for user: {} = {}", username, currentUsers.get(username).size());
	}

	/**
	 * Remove the current user from the list of current users.
	 *
	 * @param httpSession
	 */
	public static void removeUser(HttpSession httpSession) {
		Map<String, CopyOnWriteArrayList<HttpSession>> currentUsers = getCurrentUsers(httpSession);
		String username = getUsernameForSessionUser(httpSession, currentUsers);
		CopyOnWriteArrayList<HttpSession> sessions = currentUsers.get(username);
		if (!sessions.isEmpty()) {
			log.debug("Found {} sessions for the user: {}", sessions.size(), username);
			for (HttpSession session : sessions) {
				if (session != null) {
					session.invalidate();
				}
			}
		} else {
			log.debug("No sessions found for this user: {}", username);
		}
	}

	/**
	 * Get sorted user names list.
	 *
	 * @param httpSession
	 * @return sorted user names
	 */
	public static List<String> getCurrentUsernames(HttpSession httpSession) {
		Map<String, CopyOnWriteArrayList<HttpSession>> currentUsers = getCurrentUsers(httpSession);
		List<String> usernames = currentUsers.keySet().stream().collect(Collectors.toList());
		Collections.sort(usernames);
		return usernames;
	}

	/**
	 * Remove a session from the list of user sessions
	 *
	 * @param httpSession
	 */
	public static void removeSessionFromList(HttpSession httpSession) {
		Map<String, CopyOnWriteArrayList<HttpSession>> currentUsers = getCurrentUsers(httpSession);
		String username = getUsernameForSessionUser(httpSession, currentUsers);
		if (username != null) {
			CopyOnWriteArrayList<HttpSession> sessions = currentUsers.get(username);
			sessions.remove(httpSession);
			log.debug("Removed session: {}. Remaining sessions: {}", httpSession, sessions.size());
			httpSession.invalidate();
			if (sessions.size() == 0) {
				currentUsers.remove(username);
				log.debug("Removed user: {} from Current Users", username);
				return;
			}
		}
	}

	private static String getUsernameForSessionUser(HttpSession httpSession,
		Map<String, CopyOnWriteArrayList<HttpSession>> currentUsers) {
		for (Map.Entry<String, CopyOnWriteArrayList<HttpSession>> entry : currentUsers.entrySet()) {
			List<HttpSession> sessions = entry.getValue();
			if (sessions != null && sessions.contains(httpSession)) {
				return entry.getKey();
			}
		}
		return null;
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.openmrs.api.context.Context;

/**
 * Allows tracking down both login and logout events including their status
 * 
 * Beans implementing this class are picked up by {@link Context#logout()} and
 * {@link Context#authenticate(String, String)}
 * 
 * @since 2.2
 */
public interface UserSessionListener {

	public void loggedInOrOut(User user, Event event, Status status);

	public enum Event {
		LOGIN, LOGOUT
	}

	public enum Status {
		SUCCESS, FAIL
	}
}

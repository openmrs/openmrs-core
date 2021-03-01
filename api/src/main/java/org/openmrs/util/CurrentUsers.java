/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openmrs.User;
import org.openmrs.UserSessionListener;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

@Component
public class CurrentUsers implements UserSessionListener {

	private static Set<String> currentlyLoggedInUsers = Collections.synchronizedSet(new LinkedHashSet(500));

	@Override
	public void loggedInOrOut(User user, Event event, Status status) {
		if(!(status == Status.SUCCESS)) {
			return;
		}
		if (event != null && user != null) {
		    if(event == Event.LOGIN) {
		    	
			currentlyLoggedInUsers.add(user.getUsername());
		    } else if(event == Event.LOGOUT) {
		    	
			 currentlyLoggedInUsers.remove(user.getUsername());
			}
		}
	}
	public static  Set<String> getCurrentUsernames(){
		return Collections.unmodifiableSet(new LinkedHashSet(currentlyLoggedInUsers));
	}

}

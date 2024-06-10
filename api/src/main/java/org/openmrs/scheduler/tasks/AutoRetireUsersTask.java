/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.tasks;

import org.apache.commons.lang.StringUtils;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.util.DateUtil;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openmrs.util.OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS;

/**
 * A scheduled task that automatically retires users after the set number of days of inactivity. 
 * The inactivity duration is set as a global property. 
 * <a href="https://openmrs.atlassian.net/wiki/spaces/docs/pages/101318663/Creating+Auto-Deactivating+User+Task">Documentation</a>
 * {@link OpenmrsConstants#GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS}
 * 
 * @since 2.7.0
 */
public class AutoRetireUsersTask extends AbstractTask {
	
	private static final Logger log = LoggerFactory.getLogger(AutoRetireUsersTask.class);
	private static final String AUTO_RETIRE_REASON = "User retired due to inactivity";

	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		if (!isExecuting) {
			log.debug("Starting auto-retiring users");
			
			startExecuting();
			
			try {
				UserService userService = Context.getUserService();
				Set<User> usersToRetire = getUsersToRetire(userService);
				
				usersToRetire.forEach(user -> userService.retireUser(user, AUTO_RETIRE_REASON));
			} catch (Exception e) {
				log.error("Error occurred while auto-retiring users: ", e);
			} finally {
				log.debug("Auto-retiring users task ended");
				stopExecuting();
			}
		}
	}

	Set<User> getUsersToRetire(UserService userService) {
		final List<User> allUsers = userService.getAllUsers();
		String numberOfDaysToRetire = Context.getAdministrationService().getGlobalProperty(GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS);
		
		if (numberOfDaysToRetire == null || numberOfDaysToRetire.isEmpty()) {
			return Collections.emptySet();
		}
		
		long numberOfMillisecondsToRetire = DateUtil.daysToMilliseconds(Double.parseDouble(numberOfDaysToRetire));

		return allUsers.stream()
			.filter(user -> !user.isRetired() && userInactivityExceedsDaysToRetire(user, numberOfMillisecondsToRetire))
			.collect(Collectors.toSet());
	}

	boolean userInactivityExceedsDaysToRetire(User user, long numberOfMillisecondsToRetire) {
		String lastLoginTimeString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP);

		if (StringUtils.isNotBlank(lastLoginTimeString)) {
			long lastLoginTime = Long.parseLong(lastLoginTimeString);

			return System.currentTimeMillis() - lastLoginTime >= numberOfMillisecondsToRetire;
		}
		return false;
	}
}

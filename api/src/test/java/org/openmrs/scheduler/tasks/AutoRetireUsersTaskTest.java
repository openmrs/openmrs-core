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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoRetireUsersTaskTest extends BaseContextSensitiveTest {
	private static final long ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
	private static final long TWENTY_THREE_HOURS_IN_MILLISECONDS = 23 * 60 * 60 * 1000;
	private static final long TWO_DAYS_IN_MILLISECONDS = 2 * ONE_DAY_IN_MILLISECONDS;
	private static final String ONE_DAY_PROPERTY_VALUE = "1";
	private static final String TWO_DAYS_PROPERTY_VALUE = "2";
	private static final String AUTO_RETIRE_REASON = "User retired due to inactivity";

	private UserService userService;
	private AdministrationService administrationService;
	private AutoRetireUsersTask autoRetireUsersTask;

	@BeforeEach
	public void setup() {
		administrationService = Context.getAdministrationService();
		autoRetireUsersTask = new AutoRetireUsersTask();
		userService = Context.getUserService();
	}

	@Test
	public void shouldRetireUsersWhoseInactivityExceedNumberOfDaysToRetire() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, ONE_DAY_PROPERTY_VALUE));

		User user = getDefaultUser();
		userService.setUserProperty(
			user,
			OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP,
			String.valueOf((System.currentTimeMillis() - TWO_DAYS_IN_MILLISECONDS))
		);

		user.getAllRoles().forEach(user::removeRole);

		autoRetireUsersTask.execute();

		assertTrue(user.isRetired());
		assertEquals(user.getRetireReason(), AUTO_RETIRE_REASON);
	}

	@Test
	public void shouldNotRetireUsersWhoseInactivityDoNotExceedNumberOfDaysToRetire() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, TWO_DAYS_PROPERTY_VALUE));

		User user = getDefaultUser();
		userService.setUserProperty(
			user,
			OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP,
			String.valueOf((System.currentTimeMillis() - ONE_DAY_IN_MILLISECONDS))
		);

		user.getAllRoles().forEach(user::removeRole);

		autoRetireUsersTask.execute();

		assertFalse(user.isRetired());
	}

	@Test
	public void shouldNotRetireAlreadyRetiredUsers() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, ONE_DAY_PROPERTY_VALUE));

		String retireReason = "Retire Test users";

		User retiredUser = userService.getUser(1);
		userService.setUserProperty(
			retiredUser,
			OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP,
			String.valueOf((System.currentTimeMillis() - TWO_DAYS_IN_MILLISECONDS))
		);

		retiredUser.getAllRoles().forEach(retiredUser::removeRole);

		retiredUser.setRetired(true);
		retiredUser.setRetireReason(retireReason);
		
		userService.saveUser(retiredUser);

		autoRetireUsersTask.execute();

		User fetchedUser = userService.getUser(retiredUser.getUserId());
		assertTrue(fetchedUser.isRetired(), "User should remain retired after the task runs");
		assertEquals(fetchedUser.getRetireReason(), retireReason);
	}

	@Test
	public void shouldNotRetireUsersThatAreInactiveSinceCreationAndInactivityDoesNotExceedNumberOfDaysToRetire() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, ONE_DAY_PROPERTY_VALUE));

		User user = getDefaultUser();

		user.getAllRoles().forEach(user::removeRole);

		user.setDateCreated(new Date(System.currentTimeMillis() - TWENTY_THREE_HOURS_IN_MILLISECONDS));

		autoRetireUsersTask.execute();

		assertFalse(user.isRetired());
	}

	@Test
	public void shouldRetireUsersThatAreInactiveSinceCreationAndInactivityExceedsNumberOfDaysToRetire() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, ONE_DAY_PROPERTY_VALUE));

		User user = getDefaultUser();

		user.getAllRoles().forEach(user::removeRole);

		user.setDateCreated(new Date(System.currentTimeMillis() - TWO_DAYS_IN_MILLISECONDS));

		autoRetireUsersTask.execute();

		assertTrue(user.isRetired());
		assertEquals(user.getRetireReason(), AUTO_RETIRE_REASON);
	}

	@Test
	public void shouldNotRetireSuperUsers() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, ONE_DAY_PROPERTY_VALUE));
		
		User adminUser = userService.getUser(1);
		userService.setUserProperty(
			adminUser, 
			OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP,
			String.valueOf((System.currentTimeMillis() - TWO_DAYS_IN_MILLISECONDS))
		);

		autoRetireUsersTask.execute();
		
		assertFalse(adminUser.isRetired());
	}

	private User getDefaultUser() {
		return userService.getUser(1);
	}
}

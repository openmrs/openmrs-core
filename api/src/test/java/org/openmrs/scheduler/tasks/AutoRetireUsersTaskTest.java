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
import org.mockito.Mock;
import org.openmrs.GlobalProperty;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.RoleConstants;

import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutoRetireUsersTaskTest extends BaseContextSensitiveTest {
	private static final long ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
	private static final long TWENTY_THREE_HOURS_IN_MILLISECONDS = 23 * 60 * 60 * 1000;
	private static final long TWO_DAYS_IN_MILLISECONDS = 2 * ONE_DAY_IN_MILLISECONDS;
	private static final String ONE_DAY_PROPERTY_VALUE = "1";
	private static final String TWO_DAYS_PROPERTY_VALUE = "2";

	@Mock
	private UserService userService;
	private AdministrationService administrationService;
	private AutoRetireUsersTask autoRetireUsersTask;

	@BeforeEach
	public void setup() {
		administrationService = Context.getAdministrationService();
		autoRetireUsersTask = new AutoRetireUsersTask();
	}

	@Test
	public void shouldRetireUsersWhoseInactivityExceedNumberOfDaysToRetire() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, ONE_DAY_PROPERTY_VALUE));

		User inactiveUser = getDefaultUser();

		when(userService.getLastLoginTime(any())).thenReturn(String.valueOf(System.currentTimeMillis() - TWO_DAYS_IN_MILLISECONDS));
		when(userService.getAllUsers()).thenReturn(Collections.singletonList(inactiveUser));

		autoRetireUsersTask.execute();

		verify(userService, atLeastOnce()).retireUser(inactiveUser, AutoRetireUsersTask.AUTO_RETIRE_REASON);
	}

	@Test
	public void shouldNotRetireUsersWhoseInactivityDoNotExceedNumberOfDaysToRetire() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, TWO_DAYS_PROPERTY_VALUE));

		User activeUser = getDefaultUser();

		when(userService.getLastLoginTime(any())).thenReturn(String.valueOf(System.currentTimeMillis() - ONE_DAY_IN_MILLISECONDS));
		when(userService.getAllUsers()).thenReturn(Collections.singletonList(activeUser));

		autoRetireUsersTask.execute();

		verify(userService, never()).retireUser(activeUser, AutoRetireUsersTask.AUTO_RETIRE_REASON);
	}

	@Test
	public void shouldNotRetireAlreadyRetiredUsers() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, ONE_DAY_PROPERTY_VALUE));

		User retiredUser = getDefaultUser();
		retiredUser.setRetired(true);

		when(userService.getAllUsers()).thenReturn(Collections.singletonList(retiredUser));

		autoRetireUsersTask.execute();

		verify(userService, never()).retireUser(retiredUser, AutoRetireUsersTask.AUTO_RETIRE_REASON);
	}

	@Test
	public void shouldNotRetireUsersThatAreInactiveSinceCreationAndInactivityDoesNotExceedNumberOfDaysToRetire() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, ONE_DAY_PROPERTY_VALUE));

		User adminUser = getDefaultUser();
		adminUser.setDateCreated(new Date(System.currentTimeMillis() - TWENTY_THREE_HOURS_IN_MILLISECONDS));

		when(userService.getAllUsers()).thenReturn(Collections.singletonList(adminUser));

		autoRetireUsersTask.execute();

		verify(userService, never()).retireUser(adminUser, AutoRetireUsersTask.AUTO_RETIRE_REASON);
	}

	@Test
	public void shouldRetireUsersThatAreInactiveSinceCreationAndInactivityExceedsNumberOfDaysToRetire() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, ONE_DAY_PROPERTY_VALUE));

		User adminUser = getDefaultUser();
		adminUser.setDateCreated(new Date(System.currentTimeMillis() - TWO_DAYS_IN_MILLISECONDS));

		when(userService.getAllUsers()).thenReturn(Collections.singletonList(adminUser));

		autoRetireUsersTask.execute();

		verify(userService, atLeastOnce()).retireUser(eq(adminUser), anyString());
	}

	@Test
	public void shouldNotRetireSuperUsers() {
		administrationService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS, ONE_DAY_PROPERTY_VALUE));

		User adminUser = getDefaultUser();
		adminUser.addRole(new Role(RoleConstants.SUPERUSER));
		
		when(userService.getAllUsers()).thenReturn(Collections.singletonList(adminUser));

		autoRetireUsersTask.execute();

		verify(userService, never()).retireUser(adminUser, AutoRetireUsersTask.AUTO_RETIRE_REASON);
	}

	private User getDefaultUser() {
		User user = new User();
		user.setUserId(1);
		user.setSystemId("admin");
		user.setUsername("admin");
		user.setDateCreated(new Date());
		return user;
	}
}

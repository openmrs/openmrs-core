/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.APIException;
import org.openmrs.api.db.UserDAO;
import org.openmrs.test.jupiter.BaseContextMockTest;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.RoleConstants;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceImplTest extends BaseContextMockTest {

	@Mock
	private UserDAO dao;

	private UserServiceImpl userService;

	@BeforeEach
	public void before() {
		userService = new UserServiceImpl();
		userService.setUserDAO(dao);
	}

	@Test
	public void purgeRole_shouldThrowIfTheStoredNameIsACoreRole() {
		// MySQL's utf8_general_ci matches "Authenticated " to the stored "Authenticated"
		when(dao.getStoredRoleNames("Authenticated ")).thenReturn(List.of(RoleConstants.AUTHENTICATED));

		assertThrows(APIException.class, () -> userService.purgeRole(new Role("Authenticated ")));
		verify(dao, never()).deleteRole(any());
	}

	@Test
	public void purgePrivilege_shouldThrowIfTheStoredNameIsACorePrivilege() {
		when(dao.getStoredPrivilegeNames("Add Cóhorts")).thenReturn(List.of(PrivilegeConstants.ADD_COHORTS));

		assertThrows(APIException.class, () -> userService.purgePrivilege(new Privilege("Add Cóhorts")));
		verify(dao, never()).deletePrivilege(any());
	}

	@Test
	public void purgeRole_shouldThrowWhenNameMatchesCoreRoleCaseInsensitively() {
		when(dao.getStoredRoleNames(RoleConstants.ANONYMOUS.toLowerCase())).thenReturn(List.of());

		assertThrows(APIException.class, () -> userService.purgeRole(new Role(RoleConstants.ANONYMOUS.toLowerCase())));
		verify(dao, never()).deleteRole(any());
	}

	@Test
	public void purgePrivilege_shouldThrowWhenNameMatchesCorePrivilegeCaseInsensitively() {
		when(dao.getStoredPrivilegeNames(PrivilegeConstants.ADD_COHORTS.toLowerCase())).thenReturn(List.of());

		assertThrows(APIException.class,
		    () -> userService.purgePrivilege(new Privilege(PrivilegeConstants.ADD_COHORTS.toLowerCase())));
		verify(dao, never()).deletePrivilege(any());
	}
}

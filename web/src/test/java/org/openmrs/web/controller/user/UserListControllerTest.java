/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.user;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.ui.ModelMap;

/**
 * Tests the controller behind the /admin/users/users.list page
 */
public class UserListControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see UserListController#displayUsers(ModelMap,String,String,Role,Boolean)
	 * @verifies get all users if no name given
	 */
	@Test
	public void displayUsers_shouldGetAllUsersIfNoNameGiven() throws Exception {
		UserListController controller = new UserListController();
		List<User> users = controller.getUsers("Search", "", null, false);
		Assert.assertEquals(3, users.size());
	}
	
	/**
	 * @see UserListController#displayUsers(ModelMap,String,String,Role,Boolean)
	 * @verifies get users just given action parameter
	 */
	@Test
	public void displayUsers_shouldGetUsersJustGivenActionParameter() throws Exception {
		UserListController controller = new UserListController();
		List<User> users = controller.getUsers("Search", null, null, null);
		Assert.assertEquals(3, users.size());
	}
	
	/**
	 * @see UserListController#displayUsers(ModelMap,String,String,Role,Boolean)
	 * @verifies get users with a given role
	 */
	@Test
	public void displayUsers_shouldGetUsersWithAGivenRole() throws Exception {
		UserListController controller = new UserListController();
		List<User> users = controller.getUsers("Search", null, new Role("Provider"), null);
		Assert.assertEquals(1, users.size());
	}
	
	/**
	 * @see UserListController#displayUsers(ModelMap,String,String,Role,Boolean)
	 * @verifies include disabled users if requested
	 */
	@Test
	public void displayUsers_shouldIncludeDisabledUsersIfRequested() throws Exception {
		UserListController controller = new UserListController();
		List<User> users = controller.getUsers("Search", "", new Role(""), true);
		Assert.assertEquals(4, users.size());
	}
}

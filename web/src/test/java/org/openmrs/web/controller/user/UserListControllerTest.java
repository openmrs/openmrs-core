/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.controller.user;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.test.TestUtil;
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
		Assert.assertEquals(2, users.size());
	}

	/**
	 * @see UserListController#displayUsers(ModelMap,String,String,Role,Boolean)
	 * @verifies get users just given action parameter
	 */
	@Test
	public void displayUsers_shouldGetUsersJustGivenActionParameter()
			throws Exception {
		UserListController controller = new UserListController();
		List<User> users = controller.getUsers("Search", null, null, null);
		Assert.assertEquals(2, users.size());
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
	public void displayUsers_shouldIncludeDisabledUsersIfRequested()
			throws Exception {
		UserListController controller = new UserListController();
		List<User> users = controller.getUsers("Search", "", new Role(""), true);
		Assert.assertEquals(3, users.size());
	}
}
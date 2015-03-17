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

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Role;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.openmrs.web.test.WebTestHelper;
import org.openmrs.web.test.WebTestHelper.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.NotTransactional;

/**`
 * Tests {@link RoleFormController}.
 */
public class RoleFormControllerTest extends BaseWebContextSensitiveTest {
	
	@Autowired
	WebTestHelper wth;
	
	public UserService getUS() {
		return Context.getUserService();
	}
	
	@Test
	@NotTransactional
	public void shouldUpdateRoleWithParent() throws Exception {
		Role child = new Role("child", "child");
		getUS().saveRole(child);
		Role parent = new Role("parent", "parent");
		parent.setChildRoles(new HashSet<Role>());
		parent.getChildRoles().add(child);
		getUS().saveRole(parent);
		
		MockHttpServletRequest requestGET = wth.newGET("/admin/users/role.form");
		requestGET.addParameter("roleName", "child");
		Response responseGET = wth.handle(requestGET);
		
		MockHttpServletRequest requestPOST = wth.newPOST("/admin/users/role.form", responseGET);
		requestPOST.addParameter("roleName", "child");
		requestPOST.addParameter("description", "updated child");
		requestPOST.addParameter("inheritedRoles", "parent");
		
		wth.handle(requestPOST);
		
		Assert.assertEquals("updated child", getUS().getRole("child").getDescription());
		
		deleteAllData();
	}
	
}

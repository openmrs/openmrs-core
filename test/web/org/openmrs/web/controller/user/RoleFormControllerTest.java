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
	public void shouldUpdateRoleWithParent() throws Exception {
		Role parent = new Role("parent", "parent");
		getUS().saveRole(parent);
		Role child = new Role("child", "child");
		child.setInheritedRoles(new HashSet<Role>());
		child.getInheritedRoles().add(parent);
		getUS().saveRole(child);
		

		MockHttpServletRequest requestGET = wth.newGET("/admin/users/role.form");
		requestGET.addParameter("roleName", "child");
		Response responseGET = wth.handle(requestGET);
		
		MockHttpServletRequest requestPOST = wth.newPOST("/admin/users/role.form", responseGET);
		requestPOST.addParameter("roleName", "child");
		requestPOST.addParameter("description", "updated child");
		requestPOST.addParameter("inheritedRoles", "parent");
		
		wth.handle(requestPOST);
		
		Assert.assertEquals("updated child", getUS().getRole("child").getDescription());
	}
	
}

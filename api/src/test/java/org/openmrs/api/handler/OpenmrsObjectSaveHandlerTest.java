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
package org.openmrs.api.handler;

import java.util.Date;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.OpenmrsObject;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.test.Verifies;

/**
 * Tests for {@link OpenmrsObjectSaveHandler}
 */
public class OpenmrsObjectSaveHandlerTest {
	
	/**
	 * @see {@link OpenmrsObjectSaveHandler#handle(OpenmrsObject,User,Date,String)}
	 */
	@Ignore
	@Test
	@Verifies(value = "set empty string properties to null", method = "handle(OpenmrsObject,User,Date,String)")
	public void handle_shouldSetEmptyStringPropertiesToNull() {
		Role role = new Role();
		role.setName("");
		role.setDescription(" ");
		role.setRole("");
		
		new OpenmrsObjectSaveHandler().handle(role, null, null, null);
		
		Assert.assertNull(role.getName());
		Assert.assertNull(role.getDescription());
		Assert.assertNull(role.getRole());
	}
}

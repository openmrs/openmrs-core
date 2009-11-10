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
package org.openmrs.web.dwr;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * Test the different aspects of {@link DWRPersonServiceTest}
 */
public class DWRPersonServiceTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link DWRPersonService#findPeopleByRoles(String,null,String)}
	 */
	@Test
	@Verifies(value = "should match on patient identifiers", method = "findPeopleByRoles(String,null,String)")
	public void findPeopleByRoles_shouldMatchOnPatientIdentifiers() throws Exception {
		DWRPersonService dwrPersonService = new DWRPersonService();
		
		List<PersonListItem> persons = dwrPersonService.findPeopleByRoles("12345K", false, null);
		
		Assert.assertEquals(1, persons.size());
		Assert.assertEquals(new PersonListItem(6), persons.get(0));
	}
	
	/**
	 * @see {@link DWRPersonService#findPeopleByRoles(String,null,String)}
	 */
	@Test
	@Verifies(value = "should allow null roles parameter", method = "findPeopleByRoles(String,null,String)")
	public void findPeopleByRoles_shouldAllowNullRolesParameter() throws Exception {
		new DWRPersonService().findPeopleByRoles("some string", false, null);
	}
	
}

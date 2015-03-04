/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * Test the different aspects of {@link DWRPersonService}
 */
public class DWRPersonServiceTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link DWRPersonService#findPeopleByRoles(String,null,String)}
	 */
	@Test
	@Verifies(value = "should match on patient identifiers", method = "findPeopleByRoles(String,null,String)")
	public void findPeopleByRoles_shouldMatchOnPatientIdentifiers() throws Exception {
		DWRPersonService dwrPersonService = new DWRPersonService();
		
		List<Object> persons = dwrPersonService.findPeopleByRoles("12345K", false, null);
		
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

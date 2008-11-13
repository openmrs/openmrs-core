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
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.web.dwr.DWRPersonService;
import org.openmrs.web.dwr.PersonListItem;

/**
 * Test the different aspects of {@link DWRPersonServiceTest}
 */
public class DWRPersonServiceTest extends BaseContextSensitiveTest {

	/**
	 * @verifies findPeopleByRoles
	 * test = on patient identifiers
	 * 
	 */
	@Test
	public void findPeopleByRoles_shouldMatchOnPatientIdentifier() throws Exception {
		DWRPersonService dwrPersonService = new DWRPersonService();
		
		List<PersonListItem> persons = dwrPersonService.findPeopleByRoles("1234", false, null);
		
		Assert.assertEquals(1, persons.size());
		Assert.assertEquals(new PersonListItem(6), persons.get(0));
	}
	
	/**
	 * @verifies findPeopleByRoles
	 *  test = allow null roles parameter
	 * 
	 * @throws Exception
	 */
	@Test
	public void findPeopleByRoles_shouldAllNulllRolesParameter() throws Exception {
		new DWRPersonService().findPeopleByRoles("some string", false, null);
	}
}

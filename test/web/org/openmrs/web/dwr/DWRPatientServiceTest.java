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

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * Test the methods in {@link DWRPatientsServiceTest}
 */
public class DWRPatientServiceTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link DWRPatientService#findPatients(String,boolean)}
	 */
	@Test
	@Verifies(value = "should get results for patients that have edited themselves", method = "findPatients(String,null)")
	public void findPatients_shouldGetResultsForPatientsThatHaveEditedThemselves() throws Exception {
		executeDataSet("org/openmrs/web/dwr/include/DWRPatientService-patientisauser.xml");
		DWRPatientService dwrService = new DWRPatientService();
		Collection<Object> resultObjects = dwrService.findPatients("Other", false);
		Assert.assertEquals(1, resultObjects.size());
	}
	
	/**
	 * @see {@link DWRPatientService#findPatients(String,null)}
	 */
	@Test
	@Verifies(value = "should logged in user should load their own patient object", method = "findPatients(String,null)")
	public void findPatients_shouldLoggedInUserShouldLoadTheirOwnPatientObject() throws Exception {
		executeDataSet("org/openmrs/web/dwr/include/DWRPatientService-patientisauser.xml");
		DWRPatientService dwrService = new DWRPatientService();
		Collection<Object> resultObjects = dwrService.findPatients("Super", false);
		Assert.assertEquals(1, resultObjects.size());
	}
	
}

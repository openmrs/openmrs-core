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
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * Test the methods in {@link DWRPatientsServiceTest}
 */
public class DWRPatientServiceTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link DWRPatientService#findPatients(String,boolean)}
	 */
	// ignoring this test until we refactor person/patient/user
	@Ignore
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
	// ignoring this test until we refactor person/patient/user
	@Ignore
	@Test
	@Verifies(value = "should logged in user should load their own patient object", method = "findPatients(String,null)")
	public void findPatients_shouldLoggedInUserShouldLoadTheirOwnPatientObject() throws Exception {
		executeDataSet("org/openmrs/web/dwr/include/DWRPatientService-patientisauser.xml");
		DWRPatientService dwrService = new DWRPatientService();
		Collection<Object> resultObjects = dwrService.findPatients("Super", false);
		Assert.assertEquals(1, resultObjects.size());
	}
	
	/**
	 * @see {@link DWRPatientService#findCountAndPatients(String,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should not signal for a new search if it is not the first ajax call", method = "findCountAndPatients(String,Integer,Integer,null)")
	public void findCountAndPatients_shouldNotSignalForANewSearchIfItIsNotTheFirstAjaxCall() throws Exception {
		DWRPatientService dwrService = new DWRPatientService();
		Map<String, Object> resultObjects = dwrService.findCountAndPatients("Joht", 1, 10, true);
		Assert.assertEquals(0, resultObjects.get("count"));
		Assert.assertNull(resultObjects.get("searchAgain"));
		Assert.assertNull(resultObjects.get("notification"));
	}
	
	/**
	 * @see {@link DWRPatientService#findCountAndPatients(String,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should not signal for a new search if the new search value has no matches", method = "findCountAndPatients(String,Integer,Integer,null)")
	public void findCountAndPatients_shouldNotSignalForANewSearchIfTheNewSearchValueHasNoMatches() throws Exception {
		DWRPatientService dwrService = new DWRPatientService();
		Map<String, Object> resultObjects = dwrService.findCountAndPatients("Jopt", 0, 10, true);
		Assert.assertEquals(0, resultObjects.get("count"));
		Assert.assertNull(resultObjects.get("searchAgain"));
		Assert.assertNull(resultObjects.get("notification"));
	}
	
	/**
	 * @see {@link DWRPatientService#findCountAndPatients(String,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should signal for a new search if the new search value has matches and is a first call", method = "findCountAndPatients(String,Integer,Integer,null)")
	public void findCountAndPatients_shouldSignalForANewSearchIfTheNewSearchValueHasMatchesAndIsAFirstCall()
	        throws Exception {
		DWRPatientService dwrService = new DWRPatientService();
		Map<String, Object> resultObjects = dwrService.findCountAndPatients("Joht", 0, 10, true);
		Assert.assertEquals(0, resultObjects.get("count"));
		Assert.assertEquals("Joh", resultObjects.get("searchAgain"));
		Assert.assertNotNull(resultObjects.get("notification"));
	}
	
	/**
	 * @see {@link DWRPatientService#findCountAndPatients(String,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should match patient with identifiers that contain no digit", method = "findCountAndPatients(String,Integer,Integer,null)")
	public void findCountAndPatients_shouldMatchPatientWithIdentifiersThatContainNoDigit() throws Exception {
		PatientService ps = Context.getPatientService();
		final String identifier = "XYZ";
		//should have no patient with this identifiers
		Assert.assertEquals(0, ps.getCountOfPatients(identifier).intValue());
		
		Patient patient = ps.getPatient(2);
		PatientIdentifier pId = new PatientIdentifier(identifier, ps.getPatientIdentifierType(5), Context
		        .getLocationService().getLocation(1));
		patient.addIdentifier(pId);
		ps.savePatient(patient);
		
		//Let's do this in a case insensitive way
		Map<String, Object> resultObjects = new DWRPatientService().findCountAndPatients(identifier.toLowerCase(), 0, null,
		    true);
		Assert.assertEquals(1, resultObjects.get("count"));
		Assert.assertEquals(1, ((List<?>) resultObjects.get("objectList")).size());
	}
	
}

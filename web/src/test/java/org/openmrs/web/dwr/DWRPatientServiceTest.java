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

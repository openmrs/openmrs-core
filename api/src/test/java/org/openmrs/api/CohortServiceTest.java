/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods in the CohortService class TODO add all the rest of the tests
 */
public class CohortServiceTest extends BaseContextSensitiveTest {
	
	protected static final String CREATE_PATIENT_XML = "org/openmrs/api/include/PatientServiceTest-createPatient.xml";
	
	protected static final String COHORT_XML = "org/openmrs/api/include/CohortServiceTest-cohort.xml";
	
	protected static CohortService service = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeAllTests() throws Exception {
		service = Context.getCohortService();
	}
	
	/**
	 * @see {@link CohortService#getCohort(String)}
	 */
	@Test
	@Verifies(value = "should only get non voided cohorts by name", method = "getCohort(String)")
	public void getCohort_shouldOnlyGetNonVoidedCohortsByName() throws Exception {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts with the same name and the first is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).isVoided());
		assertFalse(allCohorts.get(1).isVoided());
		
		// now do the actual test: getCohort by name and expect a non voided cohort
		Cohort exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertEquals(2, exampleCohort.size());
		assertFalse(exampleCohort.isVoided());
	}
	
	/**
	 * @see {@link CohortService#getCohortByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getCohortByUuid(String)")
	public void getCohortByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		executeDataSet(COHORT_XML);
		String uuid = "h9a9m0i6-15e6-467c-9d4b-mbi7teu9lf0f";
		Cohort cohort = Context.getCohortService().getCohortByUuid(uuid);
		Assert.assertEquals(1, (int) cohort.getCohortId());
	}
	
	/**
	 * @see {@link CohortService#getCohortByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getCohortByUuid(String)")
	public void getCohortByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getCohortService().getCohortByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link CohortService#purgeCohort(Cohort)}
	 */
	@Test
	@Verifies(value = "should delete cohort from database", method = "purgeCohort(Cohort)")
	public void purgeCohort_shouldDeleteCohortFromDatabase() throws Exception {
		executeDataSet(COHORT_XML);
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertEquals(2, allCohorts.size());
		service.purgeCohort(allCohorts.get(0));
		allCohorts = service.getAllCohorts(true);
		assertEquals(1, allCohorts.size());
	}
	
	/**
	 * @see {@link CohortService#getCohorts(String)}
	 */
	@Test
	@Verifies(value = "should match cohorts by partial name", method = "getCohorts(String)")
	public void getCohorts_shouldMatchCohortsByPartialName() throws Exception {
		executeDataSet(COHORT_XML);
		List<Cohort> matchedCohorts = service.getCohorts("Example");
		assertEquals(2, matchedCohorts.size());
		matchedCohorts = service.getCohorts("e Coh");
		assertEquals(2, matchedCohorts.size());
		matchedCohorts = service.getCohorts("hort");
		assertEquals(2, matchedCohorts.size());
		matchedCohorts = service.getCohorts("Examples");
		assertEquals(0, matchedCohorts.size());
	}
	
	/**
	 * @see {@link CohortService#saveCohort(Cohort)}
	 * 
	 */
	@Test
	@Verifies(value = "should create new cohorts", method = "saveCohort(Cohort)")
	public void saveCohort_shouldCreateNewCohorts() throws Exception {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		
		// make and save a new one
		Integer[] ids = { 2, 3 };
		Cohort newCohort = new Cohort("a third cohort", "a  cohort to add for testing", ids);
		service.saveCohort(newCohort);
		
		// see if the new cohort shows up in the list of cohorts
		allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(3, allCohorts.size());
	}
	
	/**
	 * @see {@link CohortService#saveCohort(Cohort)}
	 * 
	 */
	@Test
	@Verifies(value = "should update an existing cohort", method = "saveCohort(Cohort)")
	public void saveCohort_shouldUpdateAnExistingCohort() throws Exception {
		executeDataSet(COHORT_XML);
		
		// get and modify a cohort in the  data set
		String modifiedCohortDescription = "This description has been modified in a test";
		Cohort cohortToModify = service.getCohort(2);
		cohortToModify.setDescription(modifiedCohortDescription);
		
		// save the modified cohort back to the data set, see if the modification is there
		service.saveCohort(cohortToModify);
		assertTrue(service.getCohort(2).getDescription().equals(modifiedCohortDescription));
	}
	
	/**
	 * @see {@link CohortService#voidCohort(Cohort,String)}
	 * 
	 */
	@Test
	@Verifies(value = "should fail if reason is empty", method = "voidCohort(Cohort,String)")
	public void voidCohort_shouldFailIfReasonIsEmpty() throws Exception {
		executeDataSet(COHORT_XML);
		
		// Get a non-voided, valid Cohort and try to void it with a null reason
		Cohort exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.isVoided());
		
		// Now get the Cohort and try to void it with an empty reason
		exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.isVoided());
		
		try {
			service.voidCohort(exampleCohort, "");
			Assert.fail("voidCohort should fail with exception if reason is empty");
		}
		catch (Exception e) {}
	}
	
	/**
	 * @see {@link CohortService#voidCohort(Cohort,String)}
	 * 
	 */
	@Test
	@Verifies(value = "should fail if reason is null", method = "voidCohort(Cohort,String)")
	public void voidCohort_shouldFailIfReasonIsNull() throws Exception {
		executeDataSet(COHORT_XML);
		
		// Get a non-voided, valid Cohort and try to void it with a null reason
		Cohort exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.isVoided());
		
		try {
			service.voidCohort(exampleCohort, null);
			Assert.fail("voidCohort should fail with exception if reason is null.");
		}
		catch (Exception e) {}
		
		// Now get the Cohort and try to void it with an empty reason
		exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.isVoided());
		
		try {
			service.voidCohort(exampleCohort, "");
			Assert.fail("voidCohort should fail with exception if reason is empty");
		}
		catch (Exception e) {}
	}
	
	/**
	 * @see {@link CohortService#voidCohort(Cohort,String)}
	 * 
	 */
	@Test
	@Verifies(value = "should not change an already voided cohort", method = "voidCohort(Cohort,String)")
	public void voidCohort_shouldNotChangeAnAlreadyVoidedCohort() throws Exception {
		executeDataSet(COHORT_XML);
		
		// make sure we have an already voided cohort
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).isVoided());
		
		// Make sure the void reason is different from the reason to be given in the test
		assertNotNull(allCohorts.get(0).getVoidReason());
		String reasonAlreadyVoided = allCohorts.get(0).getVoidReason();
		String voidedForTest = "Voided for test";
		assertFalse(voidedForTest.equals(reasonAlreadyVoided));
		
		// Try to void and see if the void reason changes as a result
		Cohort voidedCohort = service.voidCohort(allCohorts.get(0), voidedForTest);
		assertFalse(voidedCohort.getVoidReason().equals(voidedForTest));
		assertTrue(voidedCohort.getVoidReason().equals(reasonAlreadyVoided));
		
	}
	
	/**
	 * @see {@link CohortService#voidCohort(Cohort,String)}
	 * 
	 */
	@Test
	@Verifies(value = "should void cohort", method = "voidCohort(Cohort,String)")
	public void voidCohort_shouldVoidCohort() throws Exception {
		executeDataSet(COHORT_XML);
		
		// make sure we have a cohort that is not voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertFalse(allCohorts.get(1).isVoided());
		
		// now void the cohort and see if it's voided
		Cohort voidedCohort = service.voidCohort(allCohorts.get(1), "voided for Test");
		assertTrue(allCohorts.get(1).isVoided());
	}
	
	/**
	 * @see {@link CohortService#getCohort(Integer)}
	 * 
	 */
	@Test
	@Verifies(value = "should get cohort by id", method = "getCohort(Integer)")
	public void getCohort_shouldGetCohortById() throws Exception {
		executeDataSet(COHORT_XML);
		
		Cohort cohortToGet = service.getCohort(2);
		assertNotNull(cohortToGet);
		assertTrue(cohortToGet.getCohortId() == 2);
	}
	
	/**
	 * @see {@link CohortService#getCohort(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should get cohort given a name", method = "getCohort(String)")
	public void getCohort_shouldGetCohortGivenAName() throws Exception {
		executeDataSet(COHORT_XML);
		
		Cohort cohortToGet = service.getCohort("Example Cohort");
		assertTrue(cohortToGet.getCohortId() == 2);
	}
	
	/**
	 * @see {@link CohortService#getCohort(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should get the nonvoided cohort if two exist with same name", method = "getCohort(String)")
	public void getCohort_shouldGetTheNonvoidedCohortIfTwoExistWithSameName() throws Exception {
		executeDataSet(COHORT_XML);
		
		// check to see if both cohorts have the same name and if one is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(allCohorts.get(0).getName(), allCohorts.get(1).getName());
		assertTrue(allCohorts.get(0).isVoided());
		assertFalse(allCohorts.get(1).isVoided());
		// the non-voided cohort should have an id of 2
		assertTrue(allCohorts.get(1).getCohortId() == 2);
		
		// ask for the cohort by name
		Cohort cohortToGet = service.getCohort("Example Cohort");
		// see if the non-voided one got returned
		assertTrue(cohortToGet.getCohortId() == 2);
	}
	
	/**
	 * @verifies {@link CohortService#getAllCohorts()}
	 * test = should get all nonvoided cohorts in database
	 */
	@Test
	@Verifies(value = "should get all nonvoided cohorts in database", method = "getAllCohorts()")
	public void getAllCohorts_shouldGetAllNonvoidedCohortsInDatabase() throws Exception {
		executeDataSet(COHORT_XML);
		
		// call the method
		List<Cohort> allCohorts = service.getAllCohorts();
		assertNotNull(allCohorts);
		// there is only one non-voided cohort in the data set
		assertEquals(1, allCohorts.size());
		assertFalse(allCohorts.get(0).isVoided());
	}
	
	/**
	 * @see {@link CohortService#getAllCohorts()}
	 * 
	 */
	@Test
	@Verifies(value = "should not return any voided cohorts", method = "getAllCohorts()")
	public void getAllCohorts_shouldNotReturnAnyVoidedCohorts() throws Exception {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts, the first of which is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).isVoided());
		assertFalse(allCohorts.get(1).isVoided());
		
		// now call the target method and see if the voided cohort shows up
		allCohorts = service.getAllCohorts();
		assertNotNull(allCohorts);
		// only the non-voided cohort should be returned
		assertEquals(1, allCohorts.size());
		assertFalse(allCohorts.get(0).isVoided());
	}
	
	/**
	 * @see {@link CohortService#getAllCohorts(null)}
	 * 
	 */
	@Test
	@Verifies(value = "should return all cohorts and voided", method = "getAllCohorts(null)")
	public void getAllCohorts_shouldReturnAllCohortsAndVoided() throws Exception {
		executeDataSet(COHORT_XML);
		
		//data set should have two cohorts, one of which is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).isVoided());
		assertFalse(allCohorts.get(1).isVoided());
		
		// if called with false parameter, should not return the voided one
		allCohorts = service.getAllCohorts(false);
		assertNotNull(allCohorts);
		// only the non-voided cohort should be returned
		assertEquals(1, allCohorts.size());
		assertFalse(allCohorts.get(0).isVoided());
	}
	
	/**
	 * @see {@link CohortService#getCohorts(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should never return null", method = "getCohorts(String)")
	public void getCohorts_shouldNeverReturnNull() throws Exception {
		executeDataSet(COHORT_XML);
		
		String invalidFragment = "Not Present";
		//data set should have two cohorts, one of which is voided
		List<Cohort> allCohorts = service.getCohorts(invalidFragment);
		assertNotNull(allCohorts);
	}
	
	/**
	 * @see {@link CohortService#getCohortsContainingPatient(Patient)}
	 * 
	 */
	@Test
	@Verifies(value = "should not return voided cohorts", method = "getCohortsContainingPatient(Patient)")
	public void getCohortsContainingPatient_shouldNotReturnVoidedCohorts() throws Exception {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts, the first of which is voided
		assertTrue(service.getCohort(1).isVoided());
		assertFalse(service.getCohort(2).isVoided());
		
		// add a patient to both cohorts
		Patient patientToAdd = new Patient(4);
		service.addPatientToCohort(service.getCohort(1), patientToAdd);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		assertTrue(service.getCohort(1).contains(patientToAdd));
		assertTrue(service.getCohort(2).contains(patientToAdd));
		
		// call the method and it should not return the voided cohort
		List<Cohort> cohortsWithPatientAdded = service.getCohortsContainingPatient(patientToAdd);
		assertNotNull(cohortsWithPatientAdded);
		assertFalse(cohortsWithPatientAdded.contains(service.getCohort(1)));
		
	}
	
	/**
	 * @see {@link CohortService#getCohortsContainingPatient(Patient)}
	 * 
	 */
	@Test
	@Verifies(value = "should return cohorts that have given patient", method = "getCohortsContainingPatient(Patient)")
	public void getCohortsContainingPatient_shouldReturnCohortsThatHaveGivenPatient() throws Exception {
		executeDataSet(COHORT_XML);
		
		Patient patientToAdd = new Patient(4);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		assertTrue(service.getCohort(2).contains(patientToAdd));
		
		List<Cohort> cohortsWithGivenPatient = service.getCohortsContainingPatient(patientToAdd);
		assertTrue(cohortsWithGivenPatient.contains(service.getCohort(2)));
	}
	
	/**
	 * @see {@link CohortService#addPatientToCohort(Cohort,Patient)}
	 * 
	 */
	@Test
	@Verifies(value = "should add a patient and save the cohort", method = "addPatientToCohort(Cohort,Patient)")
	public void addPatientToCohort_shouldAddAPatientAndSaveTheCohort() throws Exception {
		executeDataSet(COHORT_XML);
		
		// make a patient, add it using the method
		Patient patientToAdd = new Patient(4);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		// proof of "save the cohort": see if the patient is in the cohort
		assertTrue(service.getCohort(2).contains(4));
	}
	
	/**
	 * @see {@link CohortService#addPatientToCohort(Cohort,Patient)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail if cohort already contains patient", method = "addPatientToCohort(Cohort,Patient)")
	public void addPatientToCohort_shouldNotFailIfCohortAlreadyContainsPatient() throws Exception {
		executeDataSet(COHORT_XML);
		
		// make a patient, add it using the method
		Patient patientToAdd = new Patient(4);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		assertTrue(service.getCohort(2).contains(4));
		
		// do it again to see if it fails
		try {
			service.addPatientToCohort(service.getCohort(2), patientToAdd);
		}
		catch (Exception e) {
			Assert.fail("addPatientToCohort(Cohort,Patient) fails when cohort already contains patient.");
		}
	}
	
	/**
	 * @verifies {@link CohortService#removePatientFromCohort(Cohort,Patient)}
	 * test = should not fail if cohort does not contain patient
	 */
	@Test
	@Verifies(value = "should not fail if cohort doesn't contain patient", method = "removePatientFromCohort(Cohort,Patient)")
	public void removePatientFromCohort_shouldNotFailIfCohortDoesNotContainPatient() throws Exception {
		executeDataSet(COHORT_XML);
		
		// make a patient
		Patient patientToAddThenRemove = new Patient(4);
		// verify that the patient is not already in the Cohort
		assertFalse(service.getCohort(2).contains(patientToAddThenRemove));
		// try to remove it from the cohort without failing
		try {
			service.removePatientFromCohort(service.getCohort(2), patientToAddThenRemove);
		}
		catch (Exception e) {
			Assert.fail("removePatientFromCohort(Cohort,Patient) should not fail if cohort doesn't contain patient");
		}
	}
	
	/**
	 * @verifies {@link CohortService#removePatientFromCohort(Cohort,Patient)}
	 * test = should save cohort after removing patient
	 */
	@Test
	@Verifies(value = "should save cohort after removing patient", method = "removePatientFromCohort(Cohort,Patient)")
	public void removePatientFromCohort_shouldSaveCohortAfterRemovingPatient() throws Exception {
		executeDataSet(COHORT_XML);
		
		// make a patient, add it using the method
		Patient patientToAddThenRemove = new Patient(4);
		service.addPatientToCohort(service.getCohort(2), patientToAddThenRemove);
		assertTrue(service.getCohort(2).contains(patientToAddThenRemove));
		service.removePatientFromCohort(service.getCohort(2), patientToAddThenRemove);
		assertFalse(service.getCohort(2).contains(patientToAddThenRemove));
	}
}

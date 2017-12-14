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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

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
	public void runBeforeAllTests() {
		service = Context.getCohortService();
	}
	
	/**
	 * @see CohortService#getCohort(String)
	 */
	@Test
	public void getCohort_shouldOnlyGetNonVoidedCohortsByName() {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts with the same name and the first is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).getVoided());
		assertFalse(allCohorts.get(1).getVoided());
		
		// now do the actual test: getCohort by name and expect a non voided cohort
		Cohort exampleCohort = service.getCohortByName("Example Cohort");
		assertNotNull(exampleCohort);
		assertEquals(1, exampleCohort.size());
		assertFalse(exampleCohort.getVoided());
	}
	
	/**
	 * @see CohortService#getCohortByUuid(String)
	 */
	@Test
	public void getCohortByUuid_shouldFindObjectGivenValidUuid() {
		executeDataSet(COHORT_XML);
		String uuid = "h9a9m0i6-15e6-467c-9d4b-mbi7teu9lf0f";
		Cohort cohort = Context.getCohortService().getCohortByUuid(uuid);
		Assert.assertEquals(1, (int) cohort.getCohortId());
	}
	
	/**
	 * @see CohortService#getCohortByUuid(String)
	 */
	@Test
	public void getCohortByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getCohortService().getCohortByUuid("some invalid uuid"));
	}
	
	/**
	 * @see CohortService#getCohortMembershipByUuid(String)
	 */
	@Test
	public void getCohortMembershipByUuid_shouldFindObjectGivenValidUuid() {
		executeDataSet(COHORT_XML);
		String uuid = "v9a9m5i6-17e6-407c-9d4v-hbi8teu9lf0f";
		CohortMembership byUuid = Context.getCohortService().getCohortMembershipByUuid(uuid);
		Assert.assertEquals(1, (int) byUuid.getId());
	}
	
	/**
	 * @see CohortService#getCohortMembershipByUuid(String)
	 */
	@Test
	public void getCohortMembershipByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getCohortService().getCohortMembershipByUuid("some invalid uuid"));
	}

	/**
	 * @see CohortService#purgeCohort(Cohort)
	 */
	@Test
	public void purgeCohort_shouldDeleteCohortFromDatabase() {
		executeDataSet(COHORT_XML);
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertEquals(2, allCohorts.size());
		service.purgeCohort(allCohorts.get(0));
		allCohorts = service.getAllCohorts(true);
		assertEquals(1, allCohorts.size());
	}
	
	/**
	 * @see CohortService#getCohorts(String)
	 */
	@Test
	public void getCohorts_shouldMatchCohortsByPartialName() {
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
	 * @see CohortService#saveCohort(Cohort)
	 */
	@Test
	public void saveCohort_shouldCreateNewCohorts() {
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
	 * @see CohortService#saveCohort(Cohort)
	 */
	@Test
	public void saveCohort_shouldUpdateAnExistingCohort() {
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
	 * @see CohortService#voidCohort(Cohort,String)
	 */
	@Test
	public void voidCohort_shouldFailIfReasonIsEmpty() {
		executeDataSet(COHORT_XML);
		
		// Get a non-voided, valid Cohort and try to void it with a null reason
		Cohort exampleCohort = service.getCohortByName("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.getVoided());
		
		// Now get the Cohort and try to void it with an empty reason
		exampleCohort = service.getCohortByName("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.getVoided());
		
		try {
			service.voidCohort(exampleCohort, "");
			Assert.fail("voidCohort should fail with exception if reason is empty");
		}
		catch (Exception e) {}
	}
	
	/**
	 * @see CohortService#voidCohort(Cohort,String)
	 */
	@Test
	public void voidCohort_shouldFailIfReasonIsNull() {
		executeDataSet(COHORT_XML);
		
		// Get a non-voided, valid Cohort and try to void it with a null reason
		Cohort exampleCohort = service.getCohortByName("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.getVoided());
		
		try {
			service.voidCohort(exampleCohort, null);
			Assert.fail("voidCohort should fail with exception if reason is null.");
		}
		catch (Exception e) {}
		
		// Now get the Cohort and try to void it with an empty reason
		exampleCohort = service.getCohortByName("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.getVoided());
		
		try {
			service.voidCohort(exampleCohort, "");
			Assert.fail("voidCohort should fail with exception if reason is empty");
		}
		catch (Exception e) {}
	}
	
	/**
	 * @see CohortService#voidCohort(Cohort,String)
	 */
	@Test
	public void voidCohort_shouldNotChangeAnAlreadyVoidedCohort() {
		executeDataSet(COHORT_XML);
		
		// make sure we have an already voided cohort
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).getVoided());
		
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
	 * @see CohortService#voidCohort(Cohort,String)
	 */
	@Test
	public void voidCohort_shouldVoidCohort() {
		executeDataSet(COHORT_XML);
		
		// make sure we have a cohort that is not voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertFalse(allCohorts.get(1).getVoided());
		
		service.voidCohort(allCohorts.get(1), "voided for Test");
		assertTrue(allCohorts.get(1).getVoided());
	}
	
	/**
	 * @see CohortService#getCohort(Integer)
	 */
	@Test
	public void getCohort_shouldGetCohortById() {
		executeDataSet(COHORT_XML);
		
		Cohort cohortToGet = service.getCohort(2);
		assertNotNull(cohortToGet);
		assertTrue(cohortToGet.getCohortId() == 2);
	}
	
	/**
	 * @see CohortService#getCohort(String)
	 */
	@Test
	public void getCohort_shouldGetCohortGivenAName() {
		executeDataSet(COHORT_XML);
		
		Cohort cohortToGet = service.getCohortByName("Example Cohort");
		assertTrue(cohortToGet.getCohortId() == 2);
	}
	
	/**
	 * @see CohortService#getCohort(String)
	 */
	@Test
	public void getCohort_shouldGetTheNonvoidedCohortIfTwoExistWithSameName() {
		executeDataSet(COHORT_XML);
		
		// check to see if both cohorts have the same name and if one is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(allCohorts.get(0).getName(), allCohorts.get(1).getName());
		assertTrue(allCohorts.get(0).getVoided());
		assertFalse(allCohorts.get(1).getVoided());
		// the non-voided cohort should have an id of 2
		assertTrue(allCohorts.get(1).getCohortId() == 2);
		
		// ask for the cohort by name
		Cohort cohortToGet = service.getCohortByName("Example Cohort");
		// see if the non-voided one got returned
		assertTrue(cohortToGet.getCohortId() == 2);
	}
	
	@Test
	public void getAllCohorts_shouldGetAllNonvoidedCohortsInDatabase() {
		executeDataSet(COHORT_XML);
		
		// call the method
		List<Cohort> allCohorts = service.getAllCohorts();
		assertNotNull(allCohorts);
		// there is only one non-voided cohort in the data set
		assertEquals(1, allCohorts.size());
		assertFalse(allCohorts.get(0).getVoided());
	}
	
	/**
	 * @see CohortService#getAllCohorts()
	 */
	@Test
	public void getAllCohorts_shouldNotReturnAnyVoidedCohorts() {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts, the first of which is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).getVoided());
		assertFalse(allCohorts.get(1).getVoided());
		
		// now call the target method and see if the voided cohort shows up
		allCohorts = service.getAllCohorts();
		assertNotNull(allCohorts);
		// only the non-voided cohort should be returned
		assertEquals(1, allCohorts.size());
		assertFalse(allCohorts.get(0).getVoided());
	}
	
	/**
	 * @see CohortService#getAllCohorts(boolean)
	 */
	@Test
	public void getAllCohorts_shouldReturnAllCohortsAndVoided() {
		executeDataSet(COHORT_XML);
		
		//data set should have two cohorts, one of which is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).getVoided());
		assertFalse(allCohorts.get(1).getVoided());
		
		// if called with false parameter, should not return the voided one
		allCohorts = service.getAllCohorts(false);
		assertNotNull(allCohorts);
		// only the non-voided cohort should be returned
		assertEquals(1, allCohorts.size());
		assertFalse(allCohorts.get(0).getVoided());
	}
	
	/**
	 * @see CohortService#getCohorts(String)
	 */
	@Test
	public void getCohorts_shouldNeverReturnNull() {
		executeDataSet(COHORT_XML);
		
		String invalidFragment = "Not Present";
		//data set should have two cohorts, one of which is voided
		List<Cohort> allCohorts = service.getCohorts(invalidFragment);
		assertNotNull(allCohorts);
	}
	
	/**
	 * @see CohortService#getCohortsContainingPatient(Patient)
	 */
	@Test
	public void getCohortsContainingPatient_shouldNotReturnVoidedCohorts() {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts, the first of which is voided
		assertTrue(service.getCohort(1).getVoided());
		assertFalse(service.getCohort(2).getVoided());
		
		// add a patient to both cohorts
		Patient patientToAdd = new Patient(7);
		service.addPatientToCohort(service.getCohort(1), patientToAdd);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		assertTrue(service.getCohort(1).contains(patientToAdd.getPatientId()));
		assertTrue(service.getCohort(2).contains(patientToAdd.getPatientId()));
		
		// call the method and it should not return the voided cohort
		List<Cohort> cohortsWithPatientAdded = service.getCohortsContainingPatientId(patientToAdd.getId());
		assertNotNull(cohortsWithPatientAdded);
		assertFalse(cohortsWithPatientAdded.contains(service.getCohort(1)));
		
	}
	
	/**
	 * @see CohortService#getCohortsContainingPatient(Patient)
	 */
	@Test
	public void getCohortsContainingPatient_shouldReturnCohortsThatHaveGivenPatient() {
		executeDataSet(COHORT_XML);
		
		Patient patientToAdd = new Patient(7);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		assertTrue(service.getCohort(2).contains(patientToAdd.getPatientId()));
		
		List<Cohort> cohortsWithGivenPatient = service.getCohortsContainingPatientId(patientToAdd.getId());
		assertTrue(cohortsWithGivenPatient.contains(service.getCohort(2)));
	}
	
	/**
	 * @see CohortService#addPatientToCohort(Cohort,Patient)
	 */
	@Test
	public void addPatientToCohort_shouldAddAPatientAndSaveTheCohort() {
		executeDataSet(COHORT_XML);
		
		// make a patient, add it using the method
		Patient patientToAdd = Context.getPatientService().getPatient(3);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		// proof of "save the cohort": see if the patient is in the cohort
		assertTrue(service.getCohort(2).contains(3));
	}
	
	/**
	 * @see CohortService#addPatientToCohort(Cohort,Patient)
	 */
	@Test
	public void addPatientToCohort_shouldNotFailIfCohortAlreadyContainsPatient() {
		executeDataSet(COHORT_XML);
		
		// make a patient, add it using the method
		Patient patientToAdd = Context.getPatientService().getPatient(3);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		assertTrue(service.getCohort(2).contains(3));
		
		// do it again to see if it fails
		try {
			service.addPatientToCohort(service.getCohort(2), patientToAdd);
		}
		catch (Exception e) {
			Assert.fail("addPatientToCohort(Cohort,Patient) fails when cohort already contains patient.");
		}
	}
	
	@Test
	public void removePatientFromCohort_shouldNotFailIfCohortDoesNotContainPatient() {
		executeDataSet(COHORT_XML);
		
		// make a patient
		Patient notInCohort = new Patient(4);
		// verify that the patient is not already in the Cohort
		assertFalse(service.getCohort(2).contains(notInCohort.getPatientId()));
		// try to remove it from the cohort without failing
		try {
			service.removePatientFromCohort(service.getCohort(2), notInCohort);
		}
		catch (Exception e) {
			Assert.fail("removePatientFromCohort(Cohort,Patient) should not fail if cohort doesn't contain patient");
		}
	}
	
	@Test
	public void removePatientFromCohort_shouldSaveCohortAfterRemovingPatient() {
		executeDataSet(COHORT_XML);
		
		Cohort cohort = service.getCohort(2);
		Integer patientId = cohort.getMemberships().iterator().next().getPatientId();
		Patient patient = Context.getPatientService().getPatient(patientId);
		service.removePatientFromCohort(cohort, patient);
		
		assertFalse(cohort.contains(patientId));
	}
	
	@Test
	public void purgeCohortMembership_shouldRemoveMembershipFromCohort() {
		executeDataSet(COHORT_XML);
		
		CohortMembership toPurge = service.getCohortMembershipByUuid("v9a9m5i6-17e6-407c-9d4v-hbi8teu9lf0f");
		Cohort owner = toPurge.getCohort();
		service.purgeCohortMembership(toPurge);
		
		Context.flushSession();
		assertNull(service.getCohortMembershipByUuid("v9a9m5i6-17e6-407c-9d4v-hbi8teu9lf0f"));
		assertFalse(service.getCohort(owner.getId()).contains(toPurge.getPatientId()));
	}
	
	@Test
	public void voidCohortMembership_shouldVoidCohortMembership() {
		executeDataSet(COHORT_XML);
		Cohort cohort = service.getCohort(1);
		CohortMembership cm = cohort.getActiveMemberships().iterator().next();
		final String reason = "Some reason";
		service.voidCohortMembership(cm, reason);
		assertTrue(cm.getVoided());
		assertNotNull(cm.getVoidedBy());
		assertNotNull(cm.getDateVoided());
		assertEquals(reason, cm.getVoidReason());
		assertFalse(cohort.contains(cm.getPatientId()));
	}
	
	@Test
	public void endCohortMembership_shouldEndTheCohortMembership() {
		Date endOnDate = new Date();
		executeDataSet(COHORT_XML);
		Cohort cohort = service.getCohort(1);
		CohortMembership cm = cohort.getActiveMemberships().iterator().next();
		assertNull(cm.getEndDate());
		service.endCohortMembership(cm, endOnDate);
		assertEquals(endOnDate, cm.getEndDate());
		assertFalse(cohort.contains(cm.getPatientId()));
	}
	
	@Test
	public void patientVoided_shouldVoidMemberships() {
		executeDataSet(COHORT_XML);
		
		Cohort cohort = Context.getCohortService().getCohort(2);
		Patient voidedPatient = new Patient(7);
		voidedPatient.setVoided(true);
		voidedPatient.setDateVoided(new Date());
		voidedPatient.setVoidedBy(Context.getAuthenticatedUser());
		voidedPatient.setVoidReason("Voided as a result of the associated patient getting voided");
		
		CohortMembership newMemberContainingVoidedPatient = new CohortMembership(voidedPatient.getPatientId());
		cohort.addMembership(newMemberContainingVoidedPatient);
		assertTrue(cohort.contains(voidedPatient.getPatientId()));
		
		assertEquals(1, service.getCohortsContainingPatientId(voidedPatient.getId()).size());
		
		service.notifyPatientVoided(voidedPatient);
		assertTrue(newMemberContainingVoidedPatient.getVoided());
		assertEquals(newMemberContainingVoidedPatient.getDateVoided(), voidedPatient.getDateVoided());
		assertEquals(newMemberContainingVoidedPatient.getVoidedBy(), voidedPatient.getVoidedBy());
		assertEquals(newMemberContainingVoidedPatient.getVoidReason(), voidedPatient.getVoidReason());
	}
	
	@Test
	public void patientUnvoided_shouldUnvoidMemberships() {
		executeDataSet(COHORT_XML);
		
		Cohort cohort = Context.getCohortService().getCohort(2);
		Patient unvoidedPatient = new Patient(7);
		User voidedBy = Context.getAuthenticatedUser();
		Date dateVoided = new Date();
		String voidReason = "Associated patient is voided";
		
		CohortMembership voidedMembership = new CohortMembership(unvoidedPatient.getPatientId());
		cohort.addMembership(voidedMembership);
		voidedMembership.setVoided(true);
		voidedMembership.setVoidedBy(voidedBy);
		voidedMembership.setDateVoided(dateVoided);
		voidedMembership.setVoidReason(voidReason);
		
		service.notifyPatientUnvoided(unvoidedPatient, voidedBy, dateVoided);
		
		assertFalse(voidedMembership.getVoided());
		assertNull(voidedMembership.getVoidedBy());
		assertNull(voidedMembership.getDateVoided());
		assertNull(voidedMembership.getVoidReason());
	}
	
	/**
	 * @verifies {@link Cohort#getActiveMemberships(Date)}
	 */
	@Test
	public void getMemberships_shouldGetMembershipsAsOfADate() throws ParseException {
		executeDataSet(COHORT_XML);
		
		Cohort cohort = Context.getCohortService().getCohort(1);
		
		CohortMembership newMember = new CohortMembership(4);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateToTest = dateFormat.parse("2016-11-01 00:00:00");
		newMember.setStartDate(dateToTest);
		cohort.addMembership(newMember);
		service.saveCohort(cohort);
		
		Collection<CohortMembership> membersAsOfDate = cohort.getActiveMemberships(dateToTest);
		assertFalse(membersAsOfDate.isEmpty());
		assertTrue(membersAsOfDate.stream().anyMatch(m -> m.getStartDate().equals(dateToTest)));
	}
	
	/**
	 * @verifies not get matching memberships of a cohort as of a date
	 * @see Cohort#getActiveMemberships(Date)
	 */
	@Test
	public void getMemberships_shouldNotGetMatchingMembershipsAsOfADate() throws Exception {
		executeDataSet(COHORT_XML);
		
		Cohort cohort = Context.getCohortService().getCohort(1);
		
		CohortMembership newMember = new CohortMembership(4);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = dateFormat.parse("2017-11-01 00:00:00");
		newMember.setStartDate(startDate);
		cohort.addMembership(newMember);
		service.saveCohort(cohort);
		
		Date dateToTest = dateFormat.parse("2016-11-01 00:00:00");
		Collection<CohortMembership> membersAsOfDate = cohort.getActiveMemberships(dateToTest);
		assertFalse(membersAsOfDate.stream().anyMatch(m -> m.getStartDate().equals(dateToTest)));
	}
	
	/**
	 * @verifies return voided memberships
	 * @see Cohort#getMemberships(boolean)
	 */
	@Test
	public void getMemberships_shouldReturnVoidedMemberships() throws Exception {
		executeDataSet(COHORT_XML);
		
		CohortMembership voidedMembership = new CohortMembership(7);
		voidedMembership.setVoided(true);
		voidedMembership.setVoidedBy(Context.getAuthenticatedUser());
		voidedMembership.setDateVoided(new Date());
		voidedMembership.setVoidReason("Void reason");
		CohortMembership nonVoidedMembership = new CohortMembership(4);
		
		Cohort cohort = Context.getCohortService().getCohort(1);
		cohort.addMembership(nonVoidedMembership);
		cohort.addMembership(voidedMembership);
		
		Context.getCohortService().saveCohort(cohort);
		Collection<CohortMembership> allMemberships = cohort.getMemberships(true);
		assertEquals(3, allMemberships.size());
	}
	
	/**
	 * @verifies return unvoided memberships
	 * @see Cohort#getMemberships(boolean)
	 */
	@Test
	public void getMemberships_shouldReturnUnvoidedMemberships() throws Exception {
		executeDataSet(COHORT_XML);
		
		Cohort cohort = Context.getCohortService().getCohort(1);
		
		CohortMembership nonVoidedMembership = new CohortMembership(4);
		CohortMembership voidedMembership = new CohortMembership(7);
		voidedMembership.setVoided(true);
		voidedMembership.setVoidedBy(Context.getAuthenticatedUser());
		voidedMembership.setDateVoided(new Date());
		voidedMembership.setVoidReason("Void reason");
		
		cohort.addMembership(nonVoidedMembership);
		cohort.addMembership(voidedMembership);
		
		Context.getCohortService().saveCohort(cohort);
		Collection<CohortMembership> unvoidedMemberships = cohort.getMemberships(false);
		assertEquals(2, unvoidedMemberships.size());
	}
	
	/**
	 * @verifies not return ended memberships
	 * @see CohortService#getCohortsContainingPatient(org.openmrs.Patient)
	 */
	@Test
	public void getCohortsContainingPatient_shouldNotReturnEndedMemberships() throws Exception {
		executeDataSet(COHORT_XML);
		
		Cohort cohort = service.getCohort(2);
		
		Patient patient = new Patient(7);
		CohortMembership membership = new CohortMembership(patient.getPatientId());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = dateFormat.parse("2017-01-01 00:00:00");
		membership.setStartDate(startDate);
		cohort.addMembership(membership);
		assertTrue(cohort.contains(patient.getPatientId()));
		
		Date endDate = dateFormat.parse("2017-01-31 00:00:00");
		membership.setEndDate(endDate);
		
		List<Cohort> cohortsWithPatientAdded = service.getCohortsContainingPatientId(patient.getId());
		assertEquals(0, cohortsWithPatientAdded.size());
	}
	
	@Test
	public void getCohortMemberships_shouldGetMembershipsContainingPatient() throws Exception {
		executeDataSet(COHORT_XML);
		List<CohortMembership> memberships = service.getCohortMemberships(6, null, false);
		assertThat(memberships.size(), is(2));
		assertThat(memberships.get(0).getCohortMemberId(), is(2));
		assertThat(memberships.get(1).getCohortMemberId(), is(3));
	}
	
	@Test
	public void getCohortMemberships_shouldGetMembershipsContainingPatientInDateRange() throws Exception {
		executeDataSet(COHORT_XML);
		List<CohortMembership> memberships = service.getCohortMemberships(6, new Date(), false);
		assertThat(memberships.size(), is(1));
		assertThat(memberships.get(0).getCohort().getCohortId(), is(2));
	}
	
	@Test
	public void getCohortMemberships_shouldNotGetMembershipsContainingPatientOutsideDateRange() throws Exception {
		executeDataSet(COHORT_XML);
		Date longAgo = DateUtils.parseDate("1999-12-31", "yyyy-MM-dd");
		List<CohortMembership> memberships = service.getCohortMemberships(6, longAgo, false);
		assertThat(memberships.size(), is(0));
	}
}

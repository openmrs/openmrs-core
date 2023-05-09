/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConditionService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for methods that are specific to the {@link ConditionServiceImpl}. General tests that
 * would span implementations should go on the {@link ConditionService}.
 */
public class ConditionServiceImplTest extends BaseContextSensitiveTest {
	
	private static final String EXISTING_CONDITION_UUID = "2cc6880e-2c46-11e4-9138-a6c5e4d20fb7";
	
	protected static final String CONDITION_XML = "org/openmrs/api/include/ConditionServiceImplTest-SetupCondition.xml";

	private static final String FORM_NAMESPACE_PATH_SEPARATOR = "^";
	
	@Autowired
	private ConditionService conditionService;
	
	@Autowired
	private PatientService patientService;

	@Autowired
	private EncounterService encounterService;

	@Autowired
	private ConceptService conceptService;
	
	@BeforeEach
	public void setup (){
		executeDataSet(CONDITION_XML);
	}

	/**
	 * @see ConditionService#saveCondition(Condition) 
	 */
	@Test
	public void saveCondition_shouldSaveNewCondition() {
		// setup
		Integer patientId = 2;
		String uuid = "08002000-4469-12q3-551f-0339000c9a76";
		CodedOrFreeText codedOrFreeText = new CodedOrFreeText();
		Condition condition = new Condition();
		condition.setCondition(codedOrFreeText);
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		condition.setUuid(uuid);
		condition.setPatient(new Patient(patientId));
		
		// perform
		conditionService.saveCondition(condition);
		
		// verify
		Condition savedCondition = conditionService.getConditionByUuid(uuid);
		assertThat(savedCondition.getPatient().getPatientId(), equalTo(patientId));
		assertThat(savedCondition.getUuid(), equalTo(uuid));
		assertThat(savedCondition.getCondition(), equalTo(codedOrFreeText));
		assertThat(savedCondition.getClinicalStatus(), is(ConditionClinicalStatus.ACTIVE));
		assertThat(savedCondition.getConditionId(), notNullValue());
	}
	
	@Test
	public void saveCondition_shouldReplaceExistingCondition() {

		// setup
		Condition condition = conditionService.getConditionByUuid(EXISTING_CONDITION_UUID);
		Integer oldConditionId = condition.getConditionId();
		
		// perform
		condition.setClinicalStatus(ConditionClinicalStatus.INACTIVE);
		condition = conditionService.saveCondition(condition);
		Integer newConditionId = condition.getConditionId();
		
		// verify
		assertNotEquals(oldConditionId, newConditionId);
		
		// existing condition should be unchanged, but voided
		Condition oldCondition = conditionService.getCondition(oldConditionId);
		assertEquals(EXISTING_CONDITION_UUID, oldCondition.getUuid());
		assertEquals(ConditionClinicalStatus.ACTIVE, oldCondition.getClinicalStatus());
		assertTrue(oldCondition.getVoided());
		assertNotNull(oldCondition.getOnsetDate());
		assertNull(oldCondition.getEndDate());
		
		// new condition should reflect changed existing condition and have it as a previous version
		Condition newCondition = conditionService.getCondition(newConditionId);
		assertNotEquals(EXISTING_CONDITION_UUID, newCondition.getUuid());
		assertEquals(newCondition.getPreviousVersion(), oldCondition);
		assertEquals(newCondition.getClinicalStatus(), ConditionClinicalStatus.INACTIVE);
		assertEquals(oldCondition.getOnsetDate().getTime(), newCondition.getOnsetDate().getTime());
		assertNull(newCondition.getEndDate());
	}

	@Test
	public void saveCondition_shouldRetainPropertiesOfCondition() throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		// setup
		Condition c = new Condition();
		CodedOrFreeText codedOrFreeText = new CodedOrFreeText();
		codedOrFreeText.setCoded(conceptService.getConcept(11));
		codedOrFreeText.setSpecificName(conceptService.getConceptName(2460));
		c.setCondition(codedOrFreeText);
		c.setClinicalStatus(ConditionClinicalStatus.INACTIVE);
		c.setVerificationStatus(ConditionVerificationStatus.CONFIRMED);
		c.setAdditionalDetail("Additional information");
		c.setOnsetDate(df.parse("2021-06-30"));
		c.setEndDate(df.parse("2022-01-25"));
		c.setEndReason("This is an end reason");
		c.setPatient(patientService.getPatient(2));
		c.setEncounter(encounterService.getEncounter(6));
		c.setFormNamespaceAndPath("form1/namespace2/path3");
		c = conditionService.saveCondition(c);
		Integer conditionId1 = c.getConditionId();
		
		// edit
		c.setAdditionalDetail("Edited info");
		c = conditionService.saveCondition(c);
		Integer conditionId2 = c.getConditionId();
		
		// verify
		assertNotEquals(conditionId1, conditionId2);
		Condition c1 = conditionService.getCondition(conditionId1);
		Condition c2 = conditionService.getCondition(conditionId2);
		assertNotEquals(c1.getUuid(), c2.getUuid());
		assertEquals(11, c2.getCondition().getCoded().getConceptId());
		assertNull(c2.getCondition().getNonCoded());
		assertEquals(2460, c2.getCondition().getSpecificName().getConceptNameId());
		assertEquals(ConditionClinicalStatus.INACTIVE, c2.getClinicalStatus());
		assertEquals(ConditionVerificationStatus.CONFIRMED, c2.getVerificationStatus());
		assertEquals("Additional information", c1.getAdditionalDetail());
		assertEquals("Edited info", c2.getAdditionalDetail());
		assertEquals("2021-06-30", df.format(c2.getOnsetDate()));
		assertEquals("2022-01-25", df.format(c2.getEndDate()));
		// assertEquals("This is an end reason", c2.getEndReason()); // End reason is not persisted in the DB
		assertEquals(2, c2.getPatient().getPatientId());
		assertEquals(6, c2.getEncounter().getEncounterId());
		assertEquals("form1/namespace2/path3", c2.getFormNamespaceAndPath());
		assertEquals(c1, c2.getPreviousVersion());
		assertTrue(c1.getVoided());
		assertFalse(c2.getVoided());

		// edit again
		codedOrFreeText = new CodedOrFreeText();
		codedOrFreeText.setNonCoded("Non-coded condition");
		c.setCondition(codedOrFreeText);
		c = conditionService.saveCondition(c);
		Integer conditionId3 = c.getConditionId();

		// verify again
		Condition c3 = conditionService.getCondition(conditionId3);
		assertEquals(c3.getPreviousVersion(), c2);
		assertNull(c3.getCondition().getCoded());
		assertNull(c3.getCondition().getSpecificName());
		assertEquals("Non-coded condition", c3.getCondition().getNonCoded());
	}

	@Test
	public void saveCondition_shouldNotVoidAndRecreateIfUnchanged() throws Exception {
		Condition condition = conditionService.getConditionByUuid(EXISTING_CONDITION_UUID);
		Integer conditionId = condition.getConditionId();
		int startingNum = conditionService.getAllConditions(condition.getPatient()).size();
		condition = conditionService.saveCondition(condition);
		int endingNum = conditionService.getAllConditions(condition.getPatient()).size();
		assertEquals(startingNum, endingNum);
		assertEquals(EXISTING_CONDITION_UUID, condition.getUuid());
		assertEquals(conditionId, condition.getConditionId());
	}
	
	@Test
	public void saveCondition_shouldVoidExistingConditionAndNotCreateNewCondition() {
		// setup
		Condition condition = conditionService.getConditionByUuid(EXISTING_CONDITION_UUID);
		Patient patient = condition.getPatient();
		int startingNum = conditionService.getAllConditions(patient).size();
		
		// perform
		condition.setVoided(true);
		condition.setVoidReason("Voided by a test");
		Condition voidedCondition = conditionService.saveCondition(condition);
		
		// verify
		assertTrue(voidedCondition.getVoided());
		assertEquals("Voided by a test", voidedCondition.getVoidReason());
		
		Condition oldCondition = conditionService.getConditionByUuid(EXISTING_CONDITION_UUID);
		assertEquals(voidedCondition.getId(), oldCondition.getId());
		assertTrue(oldCondition.getVoided());
		assertEquals(voidedCondition.getVoidReason(), oldCondition.getVoidReason());

		int endingNum = conditionService.getAllConditions(patient).size();
		assertEquals(startingNum, endingNum+1);
	}

	@Test
	public void saveCondition_shouldUnvoidExistingVoidedCondition() {
		// setup
		Condition condition = conditionService.getConditionByUuid(EXISTING_CONDITION_UUID);
		Patient patient = condition.getPatient();
		condition.setVoided(true);
		condition.setVoidReason("Voided by a test");
		condition = conditionService.saveCondition(condition);
		assertTrue(condition.getVoided());
		int startingNum = conditionService.getAllConditions(patient).size();

		// perform
		condition.setVoided(false);
		Condition unvoidedCondition = conditionService.saveCondition(condition);

		// verify
		assertFalse(unvoidedCondition.getVoided());
		assertNull(unvoidedCondition.getDateVoided());
		assertNull(unvoidedCondition.getVoidedBy());
		assertNull(unvoidedCondition.getVoidReason());

		Condition oldCondition = conditionService.getConditionByUuid(EXISTING_CONDITION_UUID);
		assertNotEquals(unvoidedCondition.getId(), oldCondition.getId());
		assertTrue(oldCondition.getVoided());
		assertEquals("Voided by a test", oldCondition.getVoidReason());
		int endingNum = conditionService.getAllConditions(patient).size();
		assertEquals(startingNum, endingNum-1);
	}

	/**
	 * @see ConditionService#saveCondition(Condition)
	 */
	@Test
	public void saveCondition_shouldSaveConditionAssociatedWithAnEncounter() {
		// setup
		String uuid = "fc281d91-cb1a-4cd1-b1ca-0f3cd5138fb2";
		Condition condition = new Condition();
		condition.setUuid(uuid);
		condition.setPatient(new Patient(2));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		
		// replay
		condition.setEncounter(encounterService.getEncounter(2039));
		conditionService.saveCondition(condition);
		
		// verify
		Condition savedCondition = conditionService.getConditionByUuid(uuid);
		assertEquals(Integer.valueOf(2039), savedCondition.getEncounter().getId());
		
		// edit and verify edit
		savedCondition.setOnsetDate(new Date());
		Condition editedCondition = conditionService.saveCondition(condition);
		assertNotNull(editedCondition.getEncounter());
		assertEquals(savedCondition.getEncounter(), editedCondition.getEncounter());
	}

	/**
	 * @see ConditionService#saveCondition(Condition)
	 */
	@Test
	public void saveCondition_shouldSaveConditionWithFormField(){
		// Create Condition to test
		String ns = "my ns";
		String path = "my path";
		Integer patientId = 2;
		String uuid = "08002000-4469-12q3-551f-0339000c9a76";
		CodedOrFreeText codedOrFreeText = new CodedOrFreeText();
		Condition condition = new Condition();
		condition.setFormField(ns, path);
		condition.setCondition(codedOrFreeText);
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		condition.setUuid(uuid);
		condition.setPatient(new Patient(patientId));

		// Perform test
		conditionService.saveCondition(condition);
		Condition savedCondition = conditionService.getConditionByUuid(uuid);

		// Validate test
		assertEquals(ns + FORM_NAMESPACE_PATH_SEPARATOR + path, savedCondition.getFormNamespaceAndPath());

		// edit and verify edit
		savedCondition.setOnsetDate(new Date());
		Condition editedCondition = conditionService.saveCondition(condition);
		assertNotNull(editedCondition.getFormNamespaceAndPath());
		assertEquals(savedCondition.getFormNamespaceAndPath(), editedCondition.getFormNamespaceAndPath());
	}
	
	/**
	 * @see ConditionService#getConditionByUuid(String)
	 */
	@Test
	public void getConditionByUuid_shouldFindConditionGivenValidUuid() {
		String uuid="2cc6880e-2c46-15e4-9038-a6c5e4d22fb7";
		Condition condition = conditionService.getConditionByUuid(uuid);
		assertEquals(uuid, condition.getUuid());
	}

	/**
	 * @see ConditionService#getConditionByUuid(String)
	 */
	@Test
	public void getConditionByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(conditionService.getConditionByUuid("invalid uuid"));
	}

	/**
	 * @see ConditionService#getCondition(Integer) 
	 */
	@Test
	public void getCondition_shouldFindConditionGivenValidId() {
		Condition condition = conditionService.getCondition(1);
		assertEquals(ConditionClinicalStatus.INACTIVE, condition.getClinicalStatus());
		assertEquals(ConditionVerificationStatus.PROVISIONAL, condition.getVerificationStatus());
		assertEquals("2cc6880e-2c46-15e4-9038-a6c5e4d22fb7", condition.getUuid());
	}

	/**
	 * @see ConditionService#getActiveConditions(Patient)
	 */
	@Test
	public void getActiveConditions_shouldGetActiveConditions() {
		List<Condition> activeConditions = conditionService.getActiveConditions(patientService.getPatient(2));
		assertThat(activeConditions, hasSize(1));
		assertEquals("2cc6880e-2c46-11e4-9138-a6c5e4d20fb7", activeConditions.get(0).getUuid());
	}

    /**
	 * @see ConditionService#getAllConditions(Patient)
	 */
	@Test
	public void getAllConditions_shouldGetAllUnvoidedConditions() {
		Patient patient = patientService.getPatient(2);
		List<Condition> conditions = conditionService.getAllConditions(patient);
		assertThat(conditions, hasSize(2));
		// Condition with uuid 2cb6880e-2cd6-11e4-9138-a6c5e4d20fb7 is voided
		assertThat(conditions.get(0).getUuid(), equalTo("2cc6880e-2c46-15e4-9038-a6c5e4d22fb7"));
		assertThat(conditions.get(1).getUuid(), equalTo("2cc6880e-2c46-11e4-9138-a6c5e4d20fb7"));
	}
	
	/**
	 * ConditionService#getConditionsByEncounter(Encounter)
	 */
	@Test
	public void getConditionsByEncounter_shouldGetAllConditionsAssociatedWithAnEncounter() {
		List<Condition> conditions = conditionService.getConditionsByEncounter(new Encounter(2039));

		assertThat(conditions, hasSize(2));

		assertThat(conditions.get(0).getUuid(), equalTo("054a376e-0bf6-4388-aa31-9dac63f8e315"));
		assertThat(conditions.get(1).getUuid(), equalTo("9757313d-92ef-4f51-a002-72a0493c5078"));
	}

	/**
	 * @see ConditionService#voidCondition(Condition, String) 
	 */
	@Test
	public void voidCondition_shouldVoidConditionSuccessfully(){
		Integer conditionId = 2;
		String voidReason = "Test Reason";
		Condition nonVoidedCondition = conditionService.getCondition(conditionId);
		assertFalse(nonVoidedCondition.getVoided());
		assertNull(nonVoidedCondition.getVoidReason());
		assertNull(nonVoidedCondition.getDateVoided());
		assertNull(nonVoidedCondition.getVoidedBy());
		
		conditionService.voidCondition(nonVoidedCondition, voidReason);
		
		Condition voidedCondition = conditionService.getCondition(conditionId);
		assertEquals(ConditionVerificationStatus.CONFIRMED, voidedCondition.getVerificationStatus());
		assertEquals(ConditionClinicalStatus.ACTIVE, voidedCondition.getClinicalStatus());
		assertEquals("2cc6880e-2c46-11e4-9138-a6c5e4d20fb7", voidedCondition.getUuid());
		assertTrue(voidedCondition.getVoided());
		assertEquals(voidReason, voidedCondition.getVoidReason());
		assertNotNull(voidedCondition.getDateVoided());
		assertEquals(Context.getAuthenticatedUser(), voidedCondition.getVoidedBy());
	}

	/**
	 * @see ConditionService#unvoidCondition(Condition) 
	 */
	@Test
	public void unvoidCondition_shouldUnvoidConditionSuccessfully(){
		Condition voidedCondition = conditionService.voidCondition(conditionService.getCondition(4), "Test Reason");
		assertTrue(voidedCondition.getVoided());
		assertNotNull(voidedCondition.getVoidReason());
		assertNotNull(voidedCondition.getDateVoided());
		assertEquals(1, voidedCondition.getVoidedBy().getUserId());
		
		Condition unVoidedCondition = conditionService.unvoidCondition(voidedCondition);
		
		assertEquals(ConditionVerificationStatus.CONFIRMED, unVoidedCondition.getVerificationStatus());
		assertEquals(ConditionClinicalStatus.ACTIVE, unVoidedCondition.getClinicalStatus());
		assertEquals("2cb6880e-2cd6-11e4-9138-a6c5e4d20fb7", unVoidedCondition.getUuid());
		assertFalse(unVoidedCondition.getVoided());
		assertNull(unVoidedCondition.getVoidReason());
		assertNull(unVoidedCondition.getDateVoided());
		assertNull(unVoidedCondition.getVoidedBy());
	}

	/**
	 * @see ConditionService#purgeCondition(Condition) 
	 */
	@Test
	public void purgeCondition_shouldPurgeCondition() {
		Integer conditionId = 1;
		Condition existingCondition = conditionService.getCondition(conditionId);
		assertNotNull(existingCondition);
		
		conditionService.purgeCondition(conditionService.getCondition(conditionId));
		
		Condition purgedCondition = conditionService.getCondition(conditionId);
		assertNull(purgedCondition);
	}
}

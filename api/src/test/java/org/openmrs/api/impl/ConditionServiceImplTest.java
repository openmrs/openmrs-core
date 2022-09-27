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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.ConditionService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

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
		Condition condition = new Condition();
		condition.setCondition(new CodedOrFreeText());
		condition.setClinicalStatus(ConditionClinicalStatus.INACTIVE);
		condition.setVerificationStatus(ConditionVerificationStatus.CONFIRMED);
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setPatient(new Patient(2));
		
		// perform
		Condition newCondition = conditionService.saveCondition(condition);
		
		// verify
		Condition oldCondition = conditionService.getConditionByUuid(EXISTING_CONDITION_UUID);
		assertTrue(oldCondition.getVoided());
		assertEquals(newCondition.getPreviousVersion(), oldCondition);
		assertEquals(newCondition.getClinicalStatus(), ConditionClinicalStatus.INACTIVE);
		
		// asserting previous behaviour using onset and end date no longer has any effect
		assertNull(newCondition.getOnsetDate());
		assertNull(oldCondition.getEndDate());
	}
	
	@Test
	public void saveCondition_shouldVoidExistingCondition() {
		// setup
		Condition condition = new Condition();
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setVoided(true);
		condition.setVoidReason("Voided by a test");
		
		// perform
		Condition voidedCondition = conditionService.saveCondition(condition);
		
		// verify
		assertTrue(voidedCondition.getVoided());
		assertEquals("Voided by a test", voidedCondition.getVoidReason());
		
		Condition oldCondition = conditionService.getConditionByUuid(EXISTING_CONDITION_UUID);
		assertEquals(voidedCondition.getId(), oldCondition.getId());
		assertTrue(oldCondition.getVoided());
		assertEquals(voidedCondition.getVoidReason(), oldCondition.getVoidReason());
	}

	@Test
	public void saveCondition_shouldNotChangeAnyOtherFieldsWhenVoidingCondition() {
		// setup
		Condition condition = new Condition();
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setVoided(true);
		condition.setVoidReason("Voided by a test");
		condition.setPatient(new Patient(8));

		// perform
		Condition voidedCondition = conditionService.saveCondition(condition);

		// verify
		assertTrue(voidedCondition.getVoided());
		assertEquals("Voided by a test", voidedCondition.getVoidReason());
		assertEquals(voidedCondition.getPatient().getId(), 2);
	}

	@Test
	public void saveCondition_shouldUnvoidExistingVoidedCondition() {
		// setup
		Context.getConditionService().voidCondition(conditionService.getConditionByUuid(EXISTING_CONDITION_UUID), "Voided for test");
		assertTrue(conditionService.getConditionByUuid(EXISTING_CONDITION_UUID).getVoided());
		
		Condition condition = new Condition();
		condition.setVoided(false);
		condition.setUuid(EXISTING_CONDITION_UUID);

		// perform
		Condition unvoidedCondition = conditionService.saveCondition(condition);

		// verify
		assertFalse(unvoidedCondition.getVoided());
		assertNull(unvoidedCondition.getVoidReason());

		Condition oldCondition = conditionService.getConditionByUuid(EXISTING_CONDITION_UUID);
		assertEquals(unvoidedCondition.getId(), oldCondition.getId());
		assertFalse(oldCondition.getVoided());
		assertEquals(unvoidedCondition.getVoidReason(), oldCondition.getVoidReason());
	}

	@Test
	public void saveCondition_shouldNotChangeAnyOtherFieldsWhenUnvoidingCondition() {
		// setup
		Context.getConditionService().voidCondition(conditionService.getConditionByUuid(EXISTING_CONDITION_UUID), "Voided for test");
		assertTrue(conditionService.getConditionByUuid(EXISTING_CONDITION_UUID).getVoided());

		Condition condition = new Condition();
		condition.setVoided(false);
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setPatient(new Patient(8));

		// perform
		Condition unvoidedCondition = conditionService.saveCondition(condition);

		// verify
		assertFalse(unvoidedCondition.getVoided());
		assertEquals(unvoidedCondition.getPatient().getId(), 2);
	}

	@Test
	public void saveCondition_shouldNotUpdateAVoidedCondition() {
		// setup
		Context.getConditionService().voidCondition(conditionService.getConditionByUuid(EXISTING_CONDITION_UUID), "Voided for test");
		assertTrue(conditionService.getConditionByUuid(EXISTING_CONDITION_UUID).getVoided());

		Condition condition = new Condition();
		condition.setVoided(true);
		condition.setUuid(EXISTING_CONDITION_UUID);
		condition.setPatient(new Patient(8));

		// perform
		Condition voidedCondition = conditionService.saveCondition(condition);

		// verify
		assertTrue(voidedCondition.getVoided());
		assertEquals(voidedCondition.getPatient().getId(), 2);
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
		condition.setEncounter(new Encounter(2039));
		conditionService.saveCondition(condition);
		
		// verify
		Condition savedCondition = conditionService.getConditionByUuid(uuid);
		assertEquals(Integer.valueOf(2039), savedCondition.getEncounter().getId());
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
	public void getAllConditions_shouldGetAllConditions() {
		List<Condition> conditions = conditionService.getAllConditions(patientService.getPatient(2));
		
		assertThat(conditions, hasSize(3));
		
		assertThat(conditions.get(0).getUuid(), equalTo("2cb6880e-2cd6-11e4-9138-a6c5e4d20fb7"));
		assertThat(conditions.get(1).getUuid(), equalTo("2cc6880e-2c46-15e4-9038-a6c5e4d22fb7"));
		assertThat(conditions.get(2).getUuid(), equalTo("2cc6880e-2c46-11e4-9138-a6c5e4d20fb7"));
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
		assertEquals(new Integer(1), voidedCondition.getVoidedBy().getUserId());
		
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

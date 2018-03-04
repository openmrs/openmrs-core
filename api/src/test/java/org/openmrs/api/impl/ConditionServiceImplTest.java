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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Patient;
import org.openmrs.api.ConditionService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for methods that are specific to the {@link ConditionServiceImpl}. General tests that
 * would span implementations should go on the {@link ConditionService}.
 */
public class ConditionServiceImplTest extends BaseContextSensitiveTest {
	
	protected static final String CONDITION_XML = "org/openmrs/api/include/ConditionServiceImplTest-SetupCondition.xml";
	
	private ConditionService conditionService;
	
	private PatientService patientService;
	
	
	@Before
	public void setup (){
		if(conditionService == null){
			conditionService = Context.getConditionService();
		}
		if(patientService == null){
			patientService = Context.getPatientService();
		}
		executeDataSet(CONDITION_XML);
	}

	/**
	 * @see ConditionService#saveCondition(Condition) 
	 */
	@Test
	public void saveCondition_shouldSaveNewCondition(){
		Integer patientId = 2;
		String uuid = "08002000-4469-12q3-551f-0339000c9a76";
		CodedOrFreeText codedOrFreeText = new CodedOrFreeText();
		Condition condition = new Condition();
		condition.setCondition(codedOrFreeText);
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		condition.setUuid(uuid);
		condition.setPatient(new Patient(patientId));
		conditionService.saveCondition(condition);
		Condition savedCondition = conditionService.getConditionByUuid(uuid);
		Assert.assertEquals(patientId, savedCondition.getPatient().getPatientId());
		Assert.assertEquals(uuid, savedCondition.getUuid());
		Assert.assertEquals(codedOrFreeText, savedCondition.getCondition());
		Assert.assertEquals(ConditionClinicalStatus.ACTIVE, savedCondition.getClinicalStatus());
		Assert.assertNotNull(savedCondition.getConditionId());
	}

	/**
	 * @see ConditionService#getConditionByUuid(String)
	 */
	@Test
	public void getConditionByUuid_shouldFindConditionGivenValidUuid() {
		String uuid="2cc6880e-2c46-15e4-9038-a6c5e4d22fb7";
		Condition condition = conditionService.getConditionByUuid(uuid);
		Assert.assertEquals(uuid, condition.getUuid());
	}

	/**
	 * @see ConditionService#getConditionByUuid(String)
	 */
	@Test
	public void getConditionByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(conditionService.getConditionByUuid("invalid uuid"));
	}

	/**
	 * @see ConditionService#getCondition(Integer) 
	 */
	@Test
	public void getCondition_shouldFindConditionGivenValidId() {
		Condition condition = conditionService.getCondition(1);
		Assert.assertEquals(ConditionClinicalStatus.INACTIVE, condition.getClinicalStatus());
		Assert.assertEquals(ConditionVerificationStatus.PROVISIONAL, condition.getVerificationStatus());
		Assert.assertEquals("2cc6880e-2c46-15e4-9038-a6c5e4d22fb7", condition.getUuid());
	}

	/**
	 * @see ConditionService#getActiveConditions(Patient)
	 */
	@Test
	public void getActiveConditions_shouldGetActiveConditions() {
		List<Condition> activeConditions = conditionService.getActiveConditions(patientService.getPatient(2));
		Assert.assertTrue(activeConditions.size() == 2);
		Assert.assertEquals("2cc6880e-2c46-11e4-9138-a6c5e4d20fb7",activeConditions.get(0).getUuid());
		Assert.assertEquals("2cc6880e-2c46-15e4-9038-a6c5e4d22fb7", activeConditions.get(1).getUuid());
	}
	
	

	/**
	 * @see ConditionService#voidCondition(Condition, String) 
	 */
	@Test
	public void voidCondition_shouldVoidConditionSuccessfully(){
		Integer conditionId = 2;
		String voidReason = "Test Reason";
		Condition nonVoidedCondition = conditionService.getCondition(conditionId);
		Assert.assertFalse(nonVoidedCondition.getVoided());
		Assert.assertNull(nonVoidedCondition.getVoidReason());
		Assert.assertNull(nonVoidedCondition.getDateVoided());
		Assert.assertNull(nonVoidedCondition.getVoidedBy());
		
		conditionService.voidCondition(nonVoidedCondition, voidReason);
		Condition voidedCondition = conditionService.getCondition(conditionId);
		Assert.assertEquals(ConditionVerificationStatus.CONFIRMED, voidedCondition.getVerificationStatus());
		Assert.assertEquals(ConditionClinicalStatus.ACTIVE, voidedCondition.getClinicalStatus());
		Assert.assertEquals("2cc6880e-2c46-11e4-9138-a6c5e4d20fb7", voidedCondition.getUuid());
		Assert.assertTrue(voidedCondition.getVoided());
		Assert.assertEquals(voidReason, voidedCondition.getVoidReason());
		Assert.assertNotNull(voidedCondition.getDateVoided());
		Assert.assertEquals(Context.getAuthenticatedUser(), voidedCondition.getVoidedBy());
	}

	/**
	 * @see ConditionService#unvoidCondition(Condition) 
	 */
	@Test
	public void unvoidCondition_shouldUnvoidConditionSuccessfully(){
		Condition voidedCondition = conditionService.voidCondition(conditionService.getCondition(4), "Test Reason");
		Assert.assertTrue(voidedCondition.getVoided());
		Assert.assertNotNull(voidedCondition.getVoidReason());
		Assert.assertNotNull(voidedCondition.getDateVoided());
		Assert.assertEquals(new Integer(1), voidedCondition.getVoidedBy().getUserId());
		
		Condition unVoidedCondition = conditionService.unvoidCondition(voidedCondition);
		Assert.assertEquals(ConditionVerificationStatus.CONFIRMED, unVoidedCondition.getVerificationStatus());
		Assert.assertEquals(ConditionClinicalStatus.ACTIVE, unVoidedCondition.getClinicalStatus());
		Assert.assertEquals("2cb6880e-2cd6-11e4-9138-a6c5e4d20fb7", unVoidedCondition.getUuid());
		Assert.assertFalse(unVoidedCondition.getVoided());
		Assert.assertNull(unVoidedCondition.getVoidReason());
		Assert.assertNull(unVoidedCondition.getDateVoided());
		Assert.assertNull(unVoidedCondition.getVoidedBy());
	}

	/**
	 * @see ConditionService#purgeCondition(Condition) 
	 */
	@Test
	public void purgeCondition_shouldPurgeCondition() {
		Integer conditionId = 1;
		Condition existingCondition = conditionService.getCondition(conditionId);
		Assert.assertNotNull(existingCondition);
		conditionService.purgeCondition(conditionService.getCondition(conditionId));
		Condition purgedCondition = conditionService.getCondition(conditionId);
		Assert.assertNull(purgedCondition);
	}
}

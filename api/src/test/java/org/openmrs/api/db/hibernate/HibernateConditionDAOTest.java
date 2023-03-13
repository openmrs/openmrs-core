/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.api.db.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Patient;
import org.openmrs.api.db.ConditionDAO;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateConditionDAOTest extends BaseContextSensitiveTest {
	
	private static final String CONDITIONS_XML = "org/openmrs/api/db/hibernate/include/HibernateConditionDAOTestDataSet.xml";
	
	
	@Autowired
	ConditionDAO dao;
	
	
	@BeforeEach
	public void setUp() {
		executeDataSet(CONDITIONS_XML);
		
		updateSearchIndex();
	}
	
	@Test
	public void shouldSaveCondition() {
		CodedOrFreeText codedOrFreeText = new CodedOrFreeText(new Concept(4),
			new ConceptName(5089), "non coded");
		ConditionClinicalStatus clinicalStatus = ConditionClinicalStatus.ACTIVE;
		ConditionVerificationStatus verificationStatus = ConditionVerificationStatus.CONFIRMED;
		Patient patient = new Patient(2);
		Date onsetDate = new Date();
		Date endDate = new Date();
		Condition previousVersion = dao.getConditionByUuid("2cc6880e-2c46-15e4-9038-a6c5e4d22fb7");
		String additionalDetail = "additionalDetail";
		int conditionId = 20;
		Condition condition = new Condition();
		condition.setConditionId(conditionId);
		condition.setCondition(codedOrFreeText);
		condition.setClinicalStatus(clinicalStatus);
		condition.setVerificationStatus(verificationStatus);
		condition.setPreviousVersion(previousVersion);
		condition.setAdditionalDetail(additionalDetail);
		condition.setOnsetDate(onsetDate);
		condition.setEndDate(endDate);
		condition.setPatient(patient);
		
		dao.saveCondition(condition);
		
		Condition savedCondition = dao.getCondition(conditionId);

		assertEquals(additionalDetail, savedCondition.getAdditionalDetail());
		assertEquals(conditionId, (int) savedCondition.getConditionId());
		assertEquals(onsetDate, savedCondition.getOnsetDate());
		assertEquals(endDate, savedCondition.getEndDate());
		assertEquals(clinicalStatus, savedCondition.getClinicalStatus());
		assertEquals(verificationStatus, savedCondition.getVerificationStatus());
		assertEquals(previousVersion, savedCondition.getPreviousVersion());
		assertEquals(patient, savedCondition.getPatient());
	}
	
	@Test
	public void shouldGetConditionByUuid() {
		String uuid = "2cc6880e-2c46-15e4-9038-a6c5e4d22fb7";
		ConditionClinicalStatus expectedClinicalStatus = ConditionClinicalStatus.INACTIVE;
		ConditionVerificationStatus expectedVerificationStatus = ConditionVerificationStatus.PROVISIONAL;
		
		Condition condition = dao.getConditionByUuid(uuid);
		assertEquals(condition.getClinicalStatus(), expectedClinicalStatus);
		assertEquals(condition.getVerificationStatus(), expectedVerificationStatus);
		assertEquals(1, (int) condition.getId());
		assertEquals("2017-01-15 00:00:00.0", condition.getEndDate().toString());
		assertEquals(1, (int) condition.getCreator().getId());
	}
	
	@Test
	public void shouldGetCondition() {
		int id = 1;
		ConditionClinicalStatus expectedClinicalStatus = ConditionClinicalStatus.INACTIVE;
		ConditionVerificationStatus expectedVerificationStatus = ConditionVerificationStatus.PROVISIONAL;
		
		Condition condition = dao.getCondition(id);
		
		assertEquals(expectedClinicalStatus, condition.getClinicalStatus());
		assertEquals(expectedVerificationStatus, condition.getVerificationStatus());
		assertEquals(1, (int) condition.getId());
		assertEquals("2017-01-15 00:00:00.0", condition.getEndDate().toString());
		assertEquals(1, (int) condition.getCreator().getId());
	}
	
	@Test
	public void shouldGetConditionHistory() {
		Patient patient = new Patient(2);
		List<Condition> history = dao.getConditionHistory(patient);
		
		assertEquals(3, history.size());
	}
	
	@Test
	public void shouldGetActiveConditions() {
		Patient patient = new Patient(2);
		List<Condition> active = dao.getActiveConditions(patient);
		assertEquals(1, active.size());
	}
	
	@Test
	public void shouldDeleteCondition() {
		String uuid = "2cc6880e-2c46-15e4-9038-a6c5e4d22fb7";
		Condition condition = dao.getConditionByUuid(uuid);

		assertNotNull(condition);

		dao.deleteCondition(condition);

		assertNull(dao.getConditionByUuid(uuid));
	}
}

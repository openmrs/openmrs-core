/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of methods within the Condition class
 * @see Condition
 */
public class ConditionTest {
	
	Condition baseCondition = new Condition();
	Concept concept1 = new Concept(1);
	Concept concept2 = new Concept(2);
	ConceptName conceptName1 = new ConceptName(1);
	ConceptName conceptName2 = new ConceptName(2);
	String text1 = "Text 1";
	String text2 = "Text 2";
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	@BeforeEach
	public void before() throws Exception {
		baseCondition.setConditionId(1234);
		baseCondition.setUuid(UUID.randomUUID().toString());
		baseCondition.setCondition(new CodedOrFreeText(concept1, conceptName1, text1));
		baseCondition.setPreviousVersion(null);
		baseCondition.setPatient(null);
		baseCondition.setEncounter(null);
		baseCondition.setAdditionalDetail(text1);
		baseCondition.setFormNamespaceAndPath(text1);
		baseCondition.setOnsetDate(df.parse("2022-03-22"));
		baseCondition.setEndDate(df.parse("2023-01-19"));
		baseCondition.setEndReason(text1);
		baseCondition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		baseCondition.setVerificationStatus(ConditionVerificationStatus.PROVISIONAL);
		baseCondition.setVoided(false);
		baseCondition.setVoidReason(null);
		baseCondition.setDateVoided(null);
		baseCondition.setVoidedBy(null);
	}
	
	@Test
	public void matches_shouldReturnFalseIfNoFieldsHaveChanged() {
		Condition condition = Condition.newInstance(baseCondition);
		assertTrue(condition.matches(baseCondition));
		assertTrue(baseCondition.matches(condition));
	}

	@Test
	public void matches_shouldReturnFalseIfOnlyIdentityFieldsHaveChanged() {
		Condition condition = Condition.newInstance(baseCondition);
		condition.setId(5678);
		assertTrue(condition.matches(baseCondition));
		assertTrue(baseCondition.matches(condition));
		condition.setUuid(UUID.randomUUID().toString());
		assertTrue(condition.matches(baseCondition));
		assertTrue(baseCondition.matches(condition));
	}

	@Test
	public void matches_shouldReturnTrueIfNonIdentityFieldsHaveChanged() {
		Condition condition = Condition.newInstance(baseCondition);
		assertTrue(condition.matches(baseCondition));
		
		// Condition Coded
		condition.setCondition(new CodedOrFreeText(concept2, conceptName1, text1));
		assertFalse(condition.matches(baseCondition));
		condition.setCondition(baseCondition.getCondition());
		assertTrue(condition.matches(baseCondition));

		// Condition Concept Name
		condition.setCondition(new CodedOrFreeText(concept1, conceptName2, text1));
		assertFalse(condition.matches(baseCondition));
		condition.setCondition(baseCondition.getCondition());
		assertTrue(condition.matches(baseCondition));

		// Condition Non-Coded
		condition.setCondition(new CodedOrFreeText(concept1, conceptName1, text2));
		assertFalse(condition.matches(baseCondition));
		condition.setCondition(baseCondition.getCondition());
		assertTrue(condition.matches(baseCondition));

		// Previous version
		condition.setPreviousVersion(new Condition());
		assertFalse(condition.matches(baseCondition));
		condition.setPreviousVersion(baseCondition.getPreviousVersion());
		assertTrue(condition.matches(baseCondition));

		// Patient
		condition.setPatient(new Patient());
		assertFalse(condition.matches(baseCondition));
		condition.setPatient(baseCondition.getPatient());
		assertTrue(condition.matches(baseCondition));

		// Encounter
		condition.setEncounter(new Encounter());
		assertFalse(condition.matches(baseCondition));
		condition.setEncounter(baseCondition.getEncounter());
		assertTrue(condition.matches(baseCondition));

		// Additional details
		condition.setAdditionalDetail(text2);
		assertFalse(condition.matches(baseCondition));
		condition.setAdditionalDetail(baseCondition.getAdditionalDetail());
		assertTrue(condition.matches(baseCondition));

		// Form namespace and path
		condition.setFormNamespaceAndPath(text2);
		assertFalse(condition.matches(baseCondition));
		condition.setFormNamespaceAndPath(baseCondition.getFormNamespaceAndPath());
		assertTrue(condition.matches(baseCondition));

		// Onset date
		condition.setOnsetDate(new Date());
		assertFalse(condition.matches(baseCondition));
		condition.setOnsetDate(baseCondition.getOnsetDate());
		assertTrue(condition.matches(baseCondition));

		// Onset date
		condition.setEndDate(new Date());
		assertFalse(condition.matches(baseCondition));
		condition.setEndDate(baseCondition.getEndDate());
		assertTrue(condition.matches(baseCondition));

		// End reason
		condition.setEndReason(text2);
		assertFalse(condition.matches(baseCondition));
		condition.setEndReason(baseCondition.getEndReason());
		assertTrue(condition.matches(baseCondition));

		// Clinical Status
		condition.setClinicalStatus(ConditionClinicalStatus.INACTIVE);
		assertFalse(condition.matches(baseCondition));
		condition.setClinicalStatus(baseCondition.getClinicalStatus());
		assertTrue(condition.matches(baseCondition));

		// Verification Status
		condition.setVerificationStatus(ConditionVerificationStatus.CONFIRMED);
		assertFalse(condition.matches(baseCondition));
		condition.setVerificationStatus(baseCondition.getVerificationStatus());
		assertTrue(condition.matches(baseCondition));

		// Voided
		condition.setVoided(true);
		assertFalse(condition.matches(baseCondition));
		condition.setVoided(baseCondition.getVoided());
		assertTrue(condition.matches(baseCondition));

		// Void reason
		condition.setVoidReason(text2);
		assertFalse(condition.matches(baseCondition));
		condition.setVoidReason(baseCondition.getVoidReason());
		assertTrue(condition.matches(baseCondition));

		// Date voided
		condition.setDateVoided(new Date());
		assertFalse(condition.matches(baseCondition));
		condition.setDateVoided(baseCondition.getDateVoided());
		assertTrue(condition.matches(baseCondition));

		// Voided by
		condition.setVoidedBy(new User());
		assertFalse(condition.matches(baseCondition));
		condition.setVoidedBy(baseCondition.getVoidedBy());
		assertTrue(condition.matches(baseCondition));
	}
}

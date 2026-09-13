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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the persistence of {@link ConceptInterpretationRule} and the way it cascades from its
 * owning {@link Concept}.
 */
public class ConceptInterpretationRuleTest extends BaseContextSensitiveTest {

	/**
	 * A coded concept carrying two interpretation rules in the standard test dataset.
	 */
	private static final int CODED_CONCEPT_ID = 21;

	private static final String EXISTING_RULE_UUID = "6b7ac1e8-1f6a-4f52-9b8f-6e0f0b0a1d01";

	private ConceptService conceptService;

	@BeforeEach
	public void before() {
		conceptService = Context.getConceptService();
	}

	@Test
	public void shouldLoadTheRulesOfAConcept() {
		Concept concept = conceptService.getConcept(CODED_CONCEPT_ID);

		assertEquals(2, concept.getInterpretationRules().size());

		// the collection is ordered by primary key, so the first rule is the one loaded above
		Iterator<ConceptInterpretationRule> rules = concept.getInterpretationRules().iterator();
		ConceptInterpretationRule first = rules.next();

		assertEquals(EXISTING_RULE_UUID, first.getUuid());
		assertEquals("$patient.getAge() < 5", first.getCriteria());
		assertEquals(Obs.Interpretation.CRITICALLY_ABNORMAL, first.getInterpretation());
		assertEquals(1, first.getPriority());
		assertEquals(concept, first.getConcept());
	}

	@Test
	public void saveConcept_shouldPersistTheInterpretationAndPriorityOfANewRule() {
		Concept concept = conceptService.getConcept(CODED_CONCEPT_ID);

		ConceptInterpretationRule rule = new ConceptInterpretationRule();
		rule.setCriteria("$patient.getGender().equals('F')");
		rule.setInterpretation(Obs.Interpretation.OFF_SCALE_HIGH);
		rule.setPriority(7);
		concept.addInterpretationRule(rule);

		conceptService.saveConcept(concept);

		Context.flushSession();
		// force a reload from the database rather than a hit on the session cache
		Context.clearSession();

		ConceptInterpretationRule reloaded = getRule(conceptService.getConcept(CODED_CONCEPT_ID), rule.getUuid());

		assertNotNull(reloaded);
		assertEquals("$patient.getGender().equals('F')", reloaded.getCriteria());
		assertEquals(Obs.Interpretation.OFF_SCALE_HIGH, reloaded.getInterpretation());
		assertEquals(7, reloaded.getPriority());
	}

	@Test
	public void saveConcept_shouldCascadeSaveANewRule() {
		Concept concept = conceptService.getConcept(CODED_CONCEPT_ID);

		ConceptInterpretationRule rule = new ConceptInterpretationRule();
		rule.setCriteria("$patient.getAge() > 65");
		rule.setInterpretation(Obs.Interpretation.ABNORMAL);
		rule.setPriority(9);
		concept.addInterpretationRule(rule);

		assertNull(rule.getConceptInterpretationRuleId());

		conceptService.saveConcept(concept);
		Context.flushSession();

		assertNotNull(rule.getConceptInterpretationRuleId());
		assertEquals(3, conceptService.getConcept(CODED_CONCEPT_ID).getInterpretationRules().size());
	}

	@Test
	public void saveConcept_shouldDeleteARuleRemovedFromTheConcept() throws Exception {
		Concept concept = conceptService.getConcept(CODED_CONCEPT_ID);
		ConceptInterpretationRule rule = getRule(concept, EXISTING_RULE_UUID);
		assertNotNull(rule);

		assertTrue(concept.removeInterpretationRule(rule));
		conceptService.saveConcept(concept);

		Context.flushSession();
		Context.clearSession();

		Concept reloaded = conceptService.getConcept(CODED_CONCEPT_ID);
		assertEquals(1, reloaded.getInterpretationRules().size());
		assertNull(getRule(reloaded, EXISTING_RULE_UUID));
		assertFalse(rowExists(EXISTING_RULE_UUID));
	}

	private ConceptInterpretationRule getRule(Concept concept, String uuid) {
		return concept.getInterpretationRules().stream().filter(rule -> uuid.equals(rule.getUuid())).findFirst()
		        .orElse(null);
	}

	private boolean rowExists(String uuid) throws Exception {
		try (PreparedStatement statement = getConnection()
		        .prepareStatement("select count(*) from concept_interpretation_rule where uuid = ?")) {
			statement.setString(1, uuid);
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getInt(1) > 0;
			}
		}
	}
}

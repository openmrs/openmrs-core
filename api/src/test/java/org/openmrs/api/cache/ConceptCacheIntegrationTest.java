/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.cache;

import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.openmrs.util.DatabaseUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConceptCacheIntegrationTest extends BaseContextSensitiveNonTransactionalTest {

	private ConceptService conceptService;

	private SessionFactory sessionFactory;

	@BeforeEach
	public void setup() {
		conceptService = Context.getConceptService();
		sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
	}

	@Test
	public void fullyCachedConceptGraph_shouldLoadWithoutIssuingSql() {
		// 5089 has names, descriptions and mappings; 21 has answers; 23 has set members.
		loadGraph(5089);
		loadGraph(21);
		loadGraph(23);

		// Drop the first-level cache so the next reads can only be served by the second-level cache.
		Context.clearSession();
		assertTrue(sessionFactory.getStatistics().isStatisticsEnabled(),
		    "Hibernate statistics must be enabled for this test to count statements");
		sessionFactory.getStatistics().clear();

		loadGraph(5089);
		loadGraph(21);
		loadGraph(23);

		assertEquals(0, sessionFactory.getStatistics().getPrepareStatementCount(),
		    "a fully cached concept graph should be served without issuing SQL");
	}

	@Test
	public void savingAnUnchangedConcept_shouldNotOverwriteAnExternallyFixedCachedName() {
		int maxNameId = ((Number) DatabaseUtil
		        .executeSQL(getConnection(), "select max(concept_name_id) from concept_name", true).get(0).get(0))
		        .intValue();
		try {
			Concept concept = conceptService.getConcept(5089);
			assertEquals("WEIGHT (KG)", concept.getName().getName());
			Context.clearSession();

			DatabaseUtil.executeSQL(getConnection(),
			    "update concept_name set name = 'WEIGHT (KG) corrected' where concept_name_id = 1439", false);

			concept = conceptService.getConcept(5089);
			assertEquals("WEIGHT (KG)", concept.getName().getName());

			conceptService.saveConcept(concept);
			assertEquals("WEIGHT (KG) corrected", concept.getName().getName());

			List<List<Object>> rows = DatabaseUtil.executeSQL(getConnection(),
			    "select name, voided from concept_name where concept_name_id = 1439", true);
			assertEquals("WEIGHT (KG) corrected", rows.get(0).get(0));
			assertFalse((Boolean) rows.get(0).get(1));
		} finally {
			DatabaseUtil.executeSQL(getConnection(),
			    "delete from concept_name where concept_id = 5089 and concept_name_id > " + maxNameId, false);
			DatabaseUtil.executeSQL(getConnection(),
			    "update concept_name set name = 'WEIGHT (KG)', voided = false, void_reason = null, "
			            + "date_voided = null, voided_by = null where concept_name_id = 1439",
			    false);
			Context.clearEntireCache();
		}
	}

	private void loadGraph(Integer conceptId) {
		Concept concept = conceptService.getConcept(conceptId);
		assertNotNull(concept);

		for (ConceptName name : concept.getNames()) {
			name.getName();
			name.getLocale();
			if (name.getTags() != null) {
				name.getTags().size();
			}
		}

		for (ConceptDescription description : concept.getDescriptions()) {
			description.getDescription();
		}

		for (ConceptAnswer answer : concept.getAnswers()) {
			answer.getAnswerConcept().getName();
		}

		for (ConceptSet set : concept.getConceptSets()) {
			set.getConcept().getName();
			set.getConceptSet().getName();
		}

		for (ConceptMap map : concept.getConceptMappings()) {
			map.getConceptReferenceTerm().getCode();
			map.getConceptReferenceTerm().getConceptSource().getName();
			map.getConceptMapType().getName();
		}

		concept.getAttributes().size();
	}
}

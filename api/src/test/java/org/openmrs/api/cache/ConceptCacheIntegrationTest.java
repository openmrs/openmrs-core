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

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
		// 5089 has names, descriptions, mappings and reference ranges; 21 has answers; 23 has set
		// members.
		loadGraph(5089);
		loadGraph(21);
		loadGraph(23);

		// Drop the first-level cache so the next reads can only be served by the second-level cache.
		Context.clearSession();
		sessionFactory.getStatistics().clear();

		loadGraph(5089);
		loadGraph(21);
		loadGraph(23);

		assertEquals(0, sessionFactory.getStatistics().getPrepareStatementCount(),
		    "a fully cached concept graph should be served without issuing SQL");
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
			map.getConceptMapType().getName();
		}

		concept.getAttributes().size();

		if (concept instanceof ConceptNumeric) {
			((ConceptNumeric) concept).getReferenceRanges().forEach(range -> range.getCriteria());
		}
	}
}

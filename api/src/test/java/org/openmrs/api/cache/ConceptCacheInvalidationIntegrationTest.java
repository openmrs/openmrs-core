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

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConceptCacheInvalidationIntegrationTest extends BaseContextSensitiveNonTransactionalTest {

	@Test
	public void editingNameAndAddingMapping_shouldNotServeStaleCachedData() {
		ConceptService conceptService = Context.getConceptService();
		ConceptReferenceTerm termA = conceptService.getConceptReferenceTerm(1);
		ConceptMapType mapType = conceptService.getConceptMapType(2);

		Concept concept = new Concept();
		concept.addName(new ConceptName("Cache invalidation name", Locale.ENGLISH));
		concept.addDescription(new ConceptDescription("A concept used to verify cache invalidation", Locale.ENGLISH));
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));
		concept.addConceptMapping(new ConceptMap(termA, mapType));

		Integer conceptId = conceptService.saveConcept(concept).getConceptId();
		assertNotNull(conceptId);

		try {
			Context.clearSession();
			Concept cached = conceptService.getConcept(conceptId);
			assertEquals("Cache invalidation name", cached.getName(Locale.ENGLISH).getName());
			assertEquals(1, cached.getConceptMappings().size());

			Concept toEdit = conceptService.getConcept(conceptId);
			toEdit.getName(Locale.ENGLISH).setName("Cache invalidation name updated");
			ConceptReferenceTerm termB = conceptService.getConceptReferenceTerm(2);
			toEdit.addConceptMapping(new ConceptMap(termB, mapType));
			conceptService.saveConcept(toEdit);

			Context.clearSession();
			Concept reloaded = conceptService.getConcept(conceptId);
			assertEquals("Cache invalidation name updated", reloaded.getName(Locale.ENGLISH).getName());
			assertEquals(2, reloaded.getConceptMappings().size());
			assertTrue(reloaded.getConceptMappings().stream()
			        .anyMatch(map -> map.getConceptReferenceTerm().getId().equals(termB.getId())));
		} finally {
			Context.clearSession();
			Concept toPurge = conceptService.getConcept(conceptId);
			if (toPurge != null) {
				conceptService.purgeConcept(toPurge);
			}
			Context.clearSession();
		}
	}
}

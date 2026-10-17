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

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptSearchResult;
import org.openmrs.security.DomainObjectAuthorizationRule;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the one shape in which a {@code @PostFilter} can be present and still reach no rule: the
 * methods returning {@link ConceptSearchResult}, which is a search wrapper rather than the
 * {@link Concept} a rule is registered for. A rule is selected from the element's own runtime type,
 * and {@code Concept.class} is not assignable from {@code ConceptSearchResult}, so filtering on the
 * element itself collapses to the privilege check {@code @PreAuthorize} already made - silently,
 * since nothing throws and every other read path does filter.
 * <p>
 * Each denial is paired with the unarmed call, since a search returning nothing would pass the
 * filtering assertion on its own.
 * <p>
 * {@link DenyingConceptRule} is registered for the whole suite (picked up by the same
 * component-scan as any other bean) but denies nothing until a test names a concept, so it does not
 * affect unrelated tests.
 */
public class ConceptServiceHasPermissionTest extends BaseContextSensitiveTest {

	private static final int COUGH_SYRUP = 3;

	/** An answer of {@link #CIVIL_STATUS_ANSWERS_TO}, so it is reached through findConceptAnswers. */
	private static final int YES = 7;

	private static final int CIVIL_STATUS_ANSWERS_TO = 21;

	@Autowired
	private ConceptService conceptService;

	@Autowired
	private DenyingConceptRule rule;

	@AfterEach
	public void resetRule() {
		rule.setDeniedConceptId(null);
	}

	@Test
	public void getConcepts_shouldFilterASearchResultWhoseConceptTheRegisteredRuleDenies() {
		assertTrue(conceptsFound().contains(COUGH_SYRUP), "the premise: this search finds the concept undenied");

		rule.setDeniedConceptId(COUGH_SYRUP);

		assertFalse(conceptsFound().contains(COUGH_SYRUP),
		    "the rule must be consulted on the concept a search result wraps, not on the wrapper");
	}

	@Test
	public void findConceptAnswers_shouldFilterASearchResultWhoseConceptTheRegisteredRuleDenies() {
		assertTrue(answersFound().contains(YES), "the premise: this search finds the answer undenied");

		// the answer, not the concept being answered: denying that one stops the @PreAuthorize instead
		rule.setDeniedConceptId(YES);

		assertFalse(answersFound().contains(YES),
		    "the rule must be consulted on the concept a search result wraps, not on the wrapper");
	}

	private List<Integer> conceptsFound() {
		return conceptIds(conceptService.getConcepts("COUGH SYRUP", Locale.ENGLISH, false));
	}

	private List<Integer> answersFound() {
		return conceptIds(
		    conceptService.findConceptAnswers("YES", Locale.ENGLISH, conceptService.getConcept(CIVIL_STATUS_ANSWERS_TO)));
	}

	private List<Integer> conceptIds(List<ConceptSearchResult> results) {
		return results.stream().map(result -> result.getConcept().getConceptId()).toList();
	}

	/**
	 * Denies exactly one, test-selected concept, authorizing every other target. Off by default so
	 * registering it does not change behavior for tests that do not use it.
	 */
	@Component
	public static class DenyingConceptRule implements DomainObjectAuthorizationRule {

		private volatile Integer deniedConceptId;

		public void setDeniedConceptId(Integer deniedConceptId) {
			this.deniedConceptId = deniedConceptId;
		}

		@Override
		public Class<?> getTargetType() {
			return Concept.class;
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Object targetDomainObject, Object permission) {
			if (deniedConceptId == null || !(targetDomainObject instanceof Concept concept)) {
				return true;
			}
			return !deniedConceptId.equals(concept.getConceptId());
		}

		@Override
		public boolean isAuthorized(Authentication authentication, Serializable targetId, Class<?> targetType,
		        Object permission) {
			if (deniedConceptId == null) {
				return true;
			}
			// an id form this rule cannot interpret denies, as DomainObjectAuthorizationRule requires
			return targetId instanceof Integer id && !deniedConceptId.equals(id);
		}
	}
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttributeMatcherPredicateTest {

	private VisitAttributeType attributeType;

	@BeforeEach
	public void setUp() {
		attributeType = new VisitAttributeType();
	}

	@Test
	public void test_shouldReturnTrueIfAnActiveAttributeHasTheGivenValue() {
		Visit visit = visitWithAttribute("abc", false);

		assertTrue(matcher("abc").test(visit));
	}

	@Test
	public void test_shouldReturnFalseIfNoActiveAttributeHasTheGivenValue() {
		Visit visit = visitWithAttribute("abc", false);

		assertFalse(matcher("xyz").test(visit));
	}

	@Test
	public void test_shouldIgnoreVoidedAttributes() {
		Visit visit = visitWithAttribute("abc", true);

		assertFalse(matcher("abc").test(visit));
	}

	@Test
	public void test_shouldReturnFalseIfTheObjectHasNoAttributes() {
		assertFalse(matcher("abc").test(new Visit()));
	}

	@Test
	public void negate_shouldRemoveNonMatchingObjectsWhenUsedWithRemoveIf() {
		Visit matching = visitWithAttribute("abc", false);
		Visit nonMatching = visitWithAttribute("xyz", false);
		List<Visit> visits = new ArrayList<>(Arrays.asList(matching, nonMatching));

		visits.removeIf(matcher("abc").negate());

		assertEquals(Collections.singletonList(matching), visits);
	}

	private AttributeMatcherPredicate<Visit, VisitAttributeType> matcher(String serializedValue) {
		Map<VisitAttributeType, String> values = Collections.singletonMap(attributeType, serializedValue);
		return new AttributeMatcherPredicate<>(values);
	}

	private Visit visitWithAttribute(String serializedValue, boolean voided) {
		VisitAttribute attribute = new VisitAttribute();
		attribute.setAttributeType(attributeType);
		attribute.setValueReferenceInternal(serializedValue);
		attribute.setVoided(voided);
		Visit visit = new Visit();
		visit.addAttribute(attribute);
		return visit;
	}
}

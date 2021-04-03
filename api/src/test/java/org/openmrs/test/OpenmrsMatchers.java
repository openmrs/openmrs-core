/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;

import static org.hamcrest.Matchers.is;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.openmrs.Concept;
import org.openmrs.ConceptSearchResult;
import org.openmrs.OpenmrsObject;

/**
 * Useful OpenMRS specific matchers.
 * 
 * @since 1.9.4
 */
public class OpenmrsMatchers {
	
	/**
	 * Matches by id.
	 * 
	 * @param id
	 * @return the matcher
	 */
	public static Matcher<OpenmrsObject> hasId(final Integer id) {
		return new FeatureMatcher<OpenmrsObject, Integer>(
		                                                  is(id), "id", "id") {
			
			@Override
			protected Integer featureValueOf(final OpenmrsObject actual) {
				return actual.getId();
			}
		};
	}
	
	/**
	 * Matches by uuid.
	 * 
	 * @param uuid
	 * @return the uuid
	 */
	public static Matcher<OpenmrsObject> hasUuid(final String uuid) {
		return new FeatureMatcher<OpenmrsObject, String>(
		                                                 is(uuid), "uuid", "uuid") {
			
			@Override
			protected String featureValueOf(final OpenmrsObject actual) {
				return actual.getUuid();
			}
		};
	}
	
	/**
	 * Matches by concept.
	 * 
	 * @param concept
	 * @return the concept
	 */
	public static Matcher<ConceptSearchResult> hasConcept(final Matcher<Concept> concept) {
		return new FeatureMatcher<ConceptSearchResult, Concept>(
		                                                        concept, "concept", "concept") {
			
			@Override
			protected Concept featureValueOf(ConceptSearchResult actual) {
				return actual.getConcept();
			}
		};
	}
}

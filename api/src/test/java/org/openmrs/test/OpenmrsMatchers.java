/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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

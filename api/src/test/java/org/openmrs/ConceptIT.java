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
package org.openmrs;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Contains integration tests of the Concept class.
 */
public class ConceptIT extends BaseContextSensitiveTest {
	
	/**
	 * @verifies return a name in the matching locale if exact is set to false
	 * @see Concept#getName(java.util.Locale, boolean)
	 */
	@Test
	public void getName_shouldReturnANameInTheMatchingLocaleIfExactIsSetToFalse() throws Exception {
		Concept concept = new Concept();
		ConceptName frenchConceptName = new ConceptName("frenchName", Locale.FRENCH);
		ConceptName englishConceptName = new ConceptName("enqlishName", Locale.ENGLISH);
		
		concept.addName(englishConceptName);
		concept.addName(frenchConceptName);
		
		assertEquals(frenchConceptName, concept.getName(Locale.FRENCH));
	}
}

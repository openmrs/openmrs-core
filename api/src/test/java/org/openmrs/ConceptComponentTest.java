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

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Contains integration tests of the Concept class.
 */
public class ConceptComponentTest extends BaseContextSensitiveTest {
	
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

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

import java.util.Locale;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the ConceptWord object
 */
public class ConceptWordTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptWord#makeConceptWords(Concept)
	 */
	@Test
	@Verifies(value = "should return separate ConceptWord objects for the same word in different ConceptNames", method = "makeConceptWords(Concept)")
	public void makeConceptWords_shouldReturnSeparateConceptWordObjectsForTheSameWordInDifferentConceptNames()
	        throws Exception {
		Concept concept = new Concept(1);
		concept.addName(new ConceptName("name number one", Locale.ENGLISH));
		concept.addName(new ConceptName("name number two", Locale.ENGLISH));
		
		Set<ConceptWord> words = ConceptWord.makeConceptWords(concept);
		Assert.assertEquals(6, words.size());
	}
	
	/**
	 * @see {@link ConceptWord#makeConceptWords(Concept)}
	 */
	@Test
	@Verifies(value = "should not include voided names", method = "makeConceptWords(Concept)")
	public void makeConceptWords_shouldNotIncludeVoidedNames() throws Exception {
		Concept concept = new Concept(1);
		concept.addName(new ConceptName("name number one", Locale.ENGLISH));
		
		ConceptName name2 = new ConceptName("name number two", Locale.ENGLISH);
		name2.setVoided(true);
		concept.addName(name2);
		
		Set<ConceptWord> words = ConceptWord.makeConceptWords(concept);
		Assert.assertEquals(3, words.size());
	}
}

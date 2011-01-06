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

import java.util.Locale;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the ConceptWord object
 */
public class ConceptWordTest {
	
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

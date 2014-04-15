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
package org.openmrs.web.dwr;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;

public class ConceptListItemTest {
	
	/**
	 * Tests that the constructor sets the preferred name if any even if the matched name is marked
	 * as fully specified
	 * 
	 * @see {@link ConceptListItem#ConceptListItem(Concept, ConceptName, Locale)}
	 */
	@Test
	public void shouldSetThePreferredNameIfFoundEvenWhenTheMatchedNameIsMarkedAsFullySpecified() throws Exception {
		Concept concept = new Concept();
		concept.setDatatype(new ConceptDatatype());
		concept.setConceptClass(new ConceptClass());
		Locale locale = Locale.ENGLISH;
		ConceptName hit = new ConceptName("name1", locale);
		final String expectedPrefName = "name2";
		ConceptName name2 = new ConceptName(expectedPrefName, locale);
		concept.addName(hit);
		concept.addName(name2);
		concept.setPreferredName(name2);
		assertEquals(expectedPrefName, new ConceptListItem(concept, hit, locale).getPreferredName());
	}
	
	/**
	 * @see {@link ConceptListItem#ConceptListItem(Concept, ConceptName, Locale)}
	 * @throws Exception
	 */
	@Test
	public void shouldSetFullySpecifiedNameAsPreferredIfThereIsNoPreferredNameInTheLocale() throws Exception {
		Concept concept = new Concept();
		concept.setDatatype(new ConceptDatatype());
		concept.setConceptClass(new ConceptClass());
		Locale locale = Locale.ENGLISH;
		ConceptName hit = new ConceptName("name1", locale);
		final String expectedPrefName = "name2";
		ConceptName name2 = new ConceptName(expectedPrefName, locale);
		concept.addName(hit);
		concept.addName(name2);
		concept.setFullySpecifiedName(name2);
		assertEquals(expectedPrefName, new ConceptListItem(concept, hit, locale).getPreferredName());
	}
	
	/**
	 * @see {@link ConceptListItem#ConceptListItem(Concept, ConceptName, Locale)}
	 * @throws Exception
	 */
	@Test
	public void shouldSetPreferredNameFromBroaderLocaleIfNoneExistsInALocaleWithACountryOrVariation() throws Exception {
		Concept concept = new Concept();
		concept.setDatatype(new ConceptDatatype());
		concept.setConceptClass(new ConceptClass());
		Locale locale = Locale.US;
		ConceptName hit = new ConceptName("name1", locale);
		final String expectedPrefName = "name2";
		ConceptName name2 = new ConceptName(expectedPrefName, Locale.ENGLISH);
		ConceptName name3 = new ConceptName("name3", locale);
		concept.addName(name2);
		concept.addName(hit);
		concept.addName(name3);
		concept.setPreferredName(name2);
		assertEquals(expectedPrefName, new ConceptListItem(concept, hit, locale).getPreferredName());
	}
	
	/**
	 * @see {@link ConceptListItem#ConceptListItem(Concept, ConceptName, Locale)}
	 * @throws Exception
	 */
	@Test
	public void shouldSetFullySpecifiedNameFromBroaderLocaleIfNoneExistsInALocaleWithACountryOrVariation() throws Exception {
		Concept concept = new Concept();
		concept.setDatatype(new ConceptDatatype());
		concept.setConceptClass(new ConceptClass());
		Locale locale = Locale.US;
		ConceptName hit = new ConceptName("name1", locale);
		final String expectedPrefName = "name2";
		ConceptName name2 = new ConceptName(expectedPrefName, Locale.ENGLISH);
		ConceptName name3 = new ConceptName("name3", locale);
		concept.addName(name2);
		concept.addName(hit);
		concept.addName(name3);
		concept.setFullySpecifiedName(name2);
		assertEquals(expectedPrefName, new ConceptListItem(concept, hit, locale).getPreferredName());
	}
}

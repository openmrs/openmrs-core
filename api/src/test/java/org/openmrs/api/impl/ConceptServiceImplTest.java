/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Locale;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Unit tests for methods that are specific to the {@link ConceptServiceImpl}. General tests that
 * would span implementations should go on the {@link ConceptService}.
 */
public class ConceptServiceImplTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 * @verifies return the concept with new conceptID if creating new concept
	 */
	@Test
	public void saveConcept_shouldReturnTheConceptWithNewConceptIDIfCreatingNewConcept() throws Exception {
		Concept c = new Concept();
		ConceptName fullySpecifiedName = new ConceptName("requires one name min", new Locale("fr", "CA"));
		c.addName(fullySpecifiedName);
		Concept savedC = Context.getConceptService().saveConcept(c);
		assertNotNull(savedC);
		assertTrue(savedC.getConceptId() > 0);
	}
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 * @verifies return the concept with same conceptID if updating existing concept
	 */
	
	@Test
	public void saveConcept_shouldReturnTheConceptWithSameConceptIDIfUpdatingExistingConcept() throws Exception {
		Concept c = new Concept();
		ConceptName fullySpecifiedName = new ConceptName("requires one name min", new Locale("fr", "CA"));
		c.addName(fullySpecifiedName);
		Concept savedC = Context.getConceptService().saveConcept(c);
		assertNotNull(savedC);
		Concept updatedC = Context.getConceptService().saveConcept(c);
		assertNotNull(updatedC);
		assertEquals(updatedC.getConceptId(), savedC.getConceptId());
	}
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 * @verifies leave preferred name preferred if set
	 */
	@Test
	public void saveConcept_shouldLeavePreferredNamePreferredIfSet() throws Exception {
		Locale loc = new Locale("fr", "CA");
		ConceptName fullySpecifiedName = new ConceptName("fully specified", loc);
		fullySpecifiedName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED); //be explicit for test case
		ConceptName shortName = new ConceptName("short name", loc);
		shortName.setConceptNameType(ConceptNameType.SHORT); //be explicit for test case
		ConceptName synonym = new ConceptName("synonym", loc);
		synonym.setConceptNameType(null); //synonyms are id'd by a null type
		ConceptName indexTerm = new ConceptName("indexTerm", loc);
		indexTerm.setConceptNameType(ConceptNameType.INDEX_TERM); //synonyms are id'd by a null type
		
		//saveConcept never picks an index term for default, so we'll use it for the test
		indexTerm.setLocalePreferred(true);
		
		Concept c = new Concept();
		c.addName(fullySpecifiedName);
		c.addName(synonym);
		c.addName(indexTerm);
		c.addName(shortName);
		
		//ignore it so we can test the set default preferred name  functionality
		try {
			Context.getConceptService().saveConcept(c);
		}
		catch (org.openmrs.api.APIException e) {
			//ignore it
		}
		assertNotNull("there's a preferred name", c.getPreferredName(loc));
		assertTrue("name was explicitly marked preferred", c.getPreferredName(loc).isPreferred());
		assertEquals("name matches", c.getPreferredName(loc).getName(), indexTerm.getName());
	}
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 * @verifies not set default preferred name to short or index terms
	 */
	@Test
	public void saveConcept_shouldNotSetDefaultPreferredNameToShortOrIndexTerms() throws Exception {
		Locale loc = new Locale("fr", "CA");
		ConceptName shortName = new ConceptName("short name", loc);
		shortName.setConceptNameType(ConceptNameType.SHORT); //be explicit for test case
		ConceptName indexTerm = new ConceptName("indexTerm", loc);
		indexTerm.setConceptNameType(ConceptNameType.INDEX_TERM); //synonyms are id'd by a null type
		
		Concept c = new Concept();
		HashSet<ConceptName> allNames = new HashSet<ConceptName>(4);
		allNames.add(indexTerm);
		allNames.add(shortName);
		c.setNames(allNames);
		
		//The API will throw a validation error because preferred name is an index term
		//ignore it so we can test the set default preferred name  functionality
		try {
			Context.getConceptService().saveConcept(c);
		}
		catch (org.openmrs.api.APIException e) {
			//ignore it
		}
		assertNull("there's a preferred name", c.getPreferredName(loc));
		assertFalse("name was explicitly marked preferred", shortName.isPreferred());
		assertFalse("name was explicitly marked preferred", indexTerm.isPreferred());
	}
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 * @verifies set default preferred name to fully specified first
	 * If Concept.getPreferredName(locale) returns null, saveConcept chooses one.
	 * The default first choice is the fully specified name in the locale.
	 * The default second choice is a synonym in the locale.
	 */
	@Test
	public void saveConcept_shouldSetDefaultPreferredNameToFullySpecifiedFirst() throws Exception {
		Locale loc = new Locale("fr", "CA");
		ConceptName fullySpecifiedName = new ConceptName("fully specified", loc);
		fullySpecifiedName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED); //be explicit for test case
		ConceptName shortName = new ConceptName("short name", loc);
		shortName.setConceptNameType(ConceptNameType.SHORT); //be explicit for test case
		ConceptName synonym = new ConceptName("synonym", loc);
		synonym.setConceptNameType(null); //synonyms are id'd by a null type
		ConceptName indexTerm = new ConceptName("indexTerm", loc);
		indexTerm.setConceptNameType(ConceptNameType.INDEX_TERM); //synonyms are id'd by a null type
		
		Concept c = new Concept();
		c.addName(fullySpecifiedName);
		c.addName(synonym);
		c.addName(indexTerm);
		c.addName(shortName);
		assertFalse("check test assumption - the API didn't automatically set preferred vlag", c.getFullySpecifiedName(loc)
		        .isPreferred());
		
		assertNotNull("Concept is legit, save succeeds", Context.getConceptService().saveConcept(c));
		
		Context.getConceptService().saveConcept(c);
		assertNotNull("there's a preferred name", c.getPreferredName(loc));
		assertTrue("name was explicitly marked preferred", c.getPreferredName(loc).isPreferred());
		assertEquals("name matches", c.getPreferredName(loc).getName(), fullySpecifiedName.getName());
	}
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 * @verifies set default preferred name to a synonym second
	 * If Concept.getPreferredName(locale) returns null, saveConcept chooses one.
	 * The default first choice is the fully specified name in the locale.
	 * The default second choice is a synonym in the locale.
	 */
	@Test
	public void saveConcept_shouldSetDefaultPreferredNameToASynonymSecond() throws Exception {
		Locale loc = new Locale("fr", "CA");
		Locale otherLocale = new Locale("en", "US");
		//Create a fully specified name, but for another locale
		//so the Concept passes validation
		ConceptName fullySpecifiedName = new ConceptName("fully specified", otherLocale);
		fullySpecifiedName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED); //be explicit for test case
		ConceptName shortName = new ConceptName("short name", loc);
		shortName.setConceptNameType(ConceptNameType.SHORT); //be explicit for test case
		ConceptName synonym = new ConceptName("synonym", loc);
		synonym.setConceptNameType(null); //synonyms are id'd by a null type
		ConceptName indexTerm = new ConceptName("indexTerm", loc);
		indexTerm.setConceptNameType(ConceptNameType.INDEX_TERM); //synonyms are id'd by a null type
		
		Concept c = new Concept();
		HashSet<ConceptName> allNames = new HashSet<ConceptName>(4);
		allNames.add(indexTerm);
		allNames.add(fullySpecifiedName);
		allNames.add(synonym);
		c.setNames(allNames);
		
		assertNull("check test assumption - the API hasn't promoted a name to a fully specified name", c
		        .getFullySpecifiedName(loc));
		
		Context.getConceptService().saveConcept(c);
		assertNotNull("there's a preferred name", c.getPreferredName(loc));
		assertTrue("name was explicitly marked preferred", c.getPreferredName(loc).isPreferred());
		assertEquals("name matches", c.getPreferredName(loc).getName(), synonym.getName());
		assertEquals("fully specified name unchanged", c.getPreferredName(otherLocale).getName(), fullySpecifiedName
		        .getName());
		
	}
	
	@Test
	public void saveConcept_shouldTrimWhitespacesInConceptName() throws Exception {
		//Given
		Concept concept = new Concept();
		String nameWithSpaces = "  jwm  ";
		concept.addName(new ConceptName(nameWithSpaces, new Locale("en", "US")));
		//When
		Context.getConceptService().saveConcept(concept);
		//Then
		assertNotEquals(concept.getName().getName(), nameWithSpaces);
		assertEquals(concept.getName().getName(), "jwm");
	}
}

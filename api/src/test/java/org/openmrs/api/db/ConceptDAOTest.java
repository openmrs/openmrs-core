/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests the concept word weighing logic, tests in this class assume that other factors remain
 * constant e.g the lengths of the words and the concept name they are associated to.
 */
public class ConceptDAOTest extends BaseContextSensitiveTest {
	
	private ConceptDAO dao = null;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeEachTest() {
		
		if (dao == null)
			// fetch the dao from the spring application context
			dao = (ConceptDAO) applicationContext.getBean("conceptDAO");
	}
	
	/**
	 * @see ConceptDAO#allConceptSources(boolean)
	 */
	@Test
	public void AllConceptSources_shouldReturnAllConceptSources() {
		assertEquals(dao.getAllConceptSources(true).size(), 5);
	}
	
	/**
	 * @see ConceptDAO#allConceptSources(boolean)
	 */
	@Test
	public void AllConceptSources_shouldReturnAllUnretiredConceptSources() {
		assertEquals(dao.getAllConceptSources(false).size(), 3);
	}
	
	/**
	 * @see ConceptDAO#isConceptMapTypeInUse(ConceptMapType)
	 */
	@Test
	public void isConceptMapTypeInUse_shouldReturnTrueIfAMapTypeHasAConceptMapOrMoreUsingIt() {
		assertTrue(dao.isConceptMapTypeInUse(Context.getConceptService().getConceptMapType(6)));
	}
	
	/**
	 * @see ConceptDAO#isConceptMapTypeInUse(ConceptMapType)
	 */
	@Test
	public void isConceptMapTypeInUse_shouldReturnTrueIfAMapTypeHasAConceptReferenceTermMapOrMoreUsingIt() {
		assertTrue(dao.isConceptMapTypeInUse(Context.getConceptService().getConceptMapType(4)));
	}
	
	/**
	 * @see ConceptDAO#isConceptReferenceTermInUse(ConceptReferenceTerm)
	 */
	@Test
	public void isConceptReferenceTermInUse_shouldReturnTrueIfATermHasAConceptMapOrMoreUsingIt() {
		assertTrue(dao.isConceptReferenceTermInUse(Context.getConceptService().getConceptReferenceTerm(10)));
	}
	
	/**
	 * @see ConceptDAO#isConceptReferenceTermInUse(ConceptReferenceTerm)
	 */
	@Test
	public void isConceptReferenceTermInUse_shouldReturnTrueIfATermHasAConceptReferenceTermMapOrMoreUsingIt()
	{
		assertTrue(dao.isConceptReferenceTermInUse(Context.getConceptService().getConceptReferenceTerm(2)));
	}
	
	/**
	 * @see ConceptDAO#isConceptMapTypeInUse(ConceptMapType)
	 */
	@Test
	public void isConceptMapTypeInUse_shouldReturnFalseIfAMapTypeHasNoMapsUsingIt() {
		assertFalse(dao.isConceptMapTypeInUse(Context.getConceptService().getConceptMapType(3)));
	}
	
	/**
	 * @see ConceptDAO#isConceptReferenceTermInUse(ConceptReferenceTerm)
	 */
	@Test
	public void isConceptReferenceTermInUse_shouldReturnFalseIfATermHasNoMapsUsingIt() {
		assertFalse(dao.isConceptReferenceTermInUse(Context.getConceptService().getConceptReferenceTerm(11)));
	}
	
	@Test
	public void purgeConcept_shouldPurgeConcept() {
		Concept concept = dao.getConcept(11);
		dao.purgeConcept(concept);
		
		assertNull(dao.getConcept(11));
	}
	
	/**
	 * @see {@link
	 *      ConceptDAO#getConcepts(String,Locale,null,List<QConceptClass;>,List<QConceptDatatype;>)}
	 */
	@Test
	public void getConcepts_shouldNotReturnConceptsWithMatchingNamesThatAreVoided() {
		Concept concept = dao.getConcept(7);
		updateSearchIndex();
		List<Concept> concepts = dao.getConcepts("VOIDED", null, false, new ArrayList<>(),
				new ArrayList<>());
		assertEquals(0, concepts.size());
	}
	
	/**
	 * @see ConceptDAO#getConceptsByAnswer(Concept)
	 */
	@Test
	public void getConceptsByAnswer_shouldReturnConceptsForTheGivenAnswerConcept() {
		Concept concept = dao.getConcept(22);
		List<Concept> conceptsByAnswer = dao.getConceptsByAnswer(concept);
		assertNotNull(conceptsByAnswer);
		assertEquals(1, conceptsByAnswer.size());
		Concept conceptByAnswer = conceptsByAnswer.get(0);
		assertEquals(21, conceptByAnswer.getConceptId().intValue());
	}
	
	/**
	 * @see {@link
	 *      ConceptDAO#getConcepts(String,List<Locale>,null,List<ConceptClass>,List<ConceptClass
	 *      >,List<ConceptDatatype>,List<ConceptDatatype>,Concept,Integer,Integer)}
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Disabled
	public void getConcepts_shouldReturnCorrectResultsForConceptWithNamesThatContainsWordsWithMoreWeight() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-words.xml");
		Concept conceptWithMultipleMatchingNames = dao.getConcept(3000);
		//recalculate the weights just in case the logic for calculating the weights is changed
		ConceptService cs = Context.getConceptService();
		cs.updateConceptIndex(conceptWithMultipleMatchingNames);
		cs.updateConceptIndex(dao.getConcept(4000));
		List<ConceptSearchResult> searchResults = dao
		        .getConcepts("trust", Collections.singletonList(Locale.ENGLISH), false, Collections.EMPTY_LIST,
		            Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, null, null, null);
		
		assertEquals(2, searchResults.size());
		//the first concept is the one with a word with the highest weight
		assertEquals(conceptWithMultipleMatchingNames, searchResults.get(0).getConcept());
		//For conceptId=3000, its search result should ALWAYS match on 'TRUST ME' because it is shorter THAN 'TRUST ALWAYS'
		assertEquals(9998, searchResults.get(0).getConceptName().getConceptNameId().intValue());
	}
	
	/**
	 * @see {@link
	 *      ConceptDAO#getConcepts(String,List<Locale>,null,List<ConceptClass>,List<ConceptClass>,
	 *      List<ConceptDatatype>,List<ConceptDatatype>,Concept,Integer,Integer)}
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Disabled
	public void getConcepts_shouldReturnCorrectResultsIfAConceptNameContainsSameWordMoreThanOnce() {
		ConceptService cs = Context.getConceptService();
		ConceptClass cc = cs.getConceptClass(1);
		Locale locale = Locale.ENGLISH;
		ConceptDatatype dt = cs.getConceptDatatype(4);
		Concept c1 = new Concept();
		ConceptName cn1a = new ConceptName("ONE TERM", locale);
		c1.addName(cn1a);
		c1.setConceptClass(cc);
		c1.setDatatype(dt);
		cs.saveConcept(c1);
		
		ConceptName cn1b = new ConceptName("ONE TO ONE", locale);
		cn1b.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		cn1b.setLocalePreferred(true);
		c1.addName(cn1b);
		cs.saveConcept(c1);
		assertTrue(cn1a.isSynonym());
		assertTrue(cn1b.getConceptNameId() > cn1a.getConceptNameId());
		
		Concept c2 = new Concept();
		ConceptName cn2a = new ConceptName("ONE TO MANY", locale);
		c2.addName(cn2a);
		c2.setConceptClass(cc);
		c2.setDatatype(dt);
		cs.saveConcept(c2);
		
		updateSearchIndex();
		
		List<ConceptSearchResult> searchResults1 = dao
		        .getConcepts("one", Collections.singletonList(locale), false, Collections.EMPTY_LIST,
		            Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, null, null, null);
		
		assertEquals(2, searchResults1.size());
		assertEquals(c1, searchResults1.get(0).getConcept());
		assertEquals(cn1b, searchResults1.get(0).getConceptName());
	}
	
}

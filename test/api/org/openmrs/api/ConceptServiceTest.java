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
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.TestUtil;

/**
 * This test class (should) contain tests for all of the ConcepService methods
 * 
 * TODO clean up and finish this test class
 * 
 * @see org.openmrs.api.ConceptService
 */
public class ConceptServiceTest extends BaseContextSensitiveTest {
	
	protected ConceptService conceptService = null;
	protected static final String INITIAL_CONCEPTS_XML = "org/openmrs/api/include/ConceptServiceTest-initialConcepts.xml";
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * The "@Before" method in {@link BaseContextSensitiveTest} is run
	 * right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeAllTests() throws Exception {
		conceptService = Context.getConceptService();
	}
	
	/**
	 * Test getting a concept by name and by partial name. 
	 * 
	 * NOTE: This test assumes that there are at least a few concepts 
	 * in the database system
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetConceptByName() throws Exception {
		
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// get the first concept in the dictionary
		Concept firstConcept = conceptService.getConcept(1);
		assertNotNull("There should be a concept with id of 1", firstConcept);
		
		// get the concept by its name
		String name = firstConcept.getName().getName();
		Concept firstConceptByName = conceptService.getConceptByName(name);
		assertNotNull("You should be able to get this concept by name", firstConceptByName);
		
		// substring the name and try to get it again
		List<Concept> firstConceptsByPartialNameList = conceptService.getConceptsByName(name.substring(1));
		assertTrue("You should be able to get the concept by partial name", firstConceptsByPartialNameList.contains(firstConcept));
		
	}
	
	@Test
	@SkipBaseSetup
	public void shouldSaveConceptNumeric() throws Exception {
		TestUtil.printOutTableContents(getConnection(), "concept");
		
		initializeInMemoryDatabase();
		executeDataSet(INITIAL_CONCEPTS_XML);
		authenticate();
		
		ConceptService conceptService = Context.getConceptService();
		
		// this tests saving a current concept as a newly changed conceptnumeric
		// assumes there is already a concept in the database  
		// with a concept id of #1
		ConceptNumeric cn = new ConceptNumeric(1);
		cn.setDatatype(new ConceptDatatype(1));
		cn.addName(new ConceptName("a new conceptnumeric", Locale.US));
		cn.setHiAbsolute(20.0);
		conceptService.saveConcept(cn);
		
		// this tests saving a previously conceptnumeric as just a concept
		Concept c2 = new Concept(2);
		c2.addName(new ConceptName("not a numeric anymore", Locale.US));
		c2.setDatatype(new ConceptDatatype(3));
		conceptService.saveConcept(c2);
		
		// this tests saving a never before in the database conceptnumeric
		ConceptNumeric cn3 = new ConceptNumeric();
		cn3.setDatatype(new ConceptDatatype(1));
		cn3.addName(new ConceptName("a brand new conceptnumeric", Locale.US));
		cn3.setHiAbsolute(50.0);
		conceptService.saveConcept(cn3);
		
		// commit these so we can refetch from the database cleanly
		// commenting this out because junit4 doesn't seem to care
		//commitTransaction(true);
		
		// check the first concept
		Concept firstConcept = conceptService.getConcept(1);
		assertEquals("a new conceptnumeric", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptNumeric);
		ConceptNumeric firstConceptNumeric = (ConceptNumeric)firstConcept;
		assertEquals(20.0, firstConceptNumeric.getHiAbsolute().doubleValue(), 0);
		
		// check the second concept
		Concept secondConcept = conceptService.getConcept(2);
		// this will probably still be a ConceptNumeric object.  what to do about that?
		// revisit this problem when discriminators are in place
		//assertFalse(secondConcept instanceof ConceptNumeric);
		// this shouldn't think its a conceptnumeric object though
		assertFalse(secondConcept.isNumeric());
		assertEquals("not a numeric anymore", secondConcept.getName(Locale.US).getName());
		
		// check the third concept
		Concept thirdConcept = conceptService.getConcept(cn3.getConceptId());
		assertTrue(thirdConcept instanceof ConceptNumeric);
		ConceptNumeric thirdConceptNumeric = (ConceptNumeric)thirdConcept;
		assertEquals("a brand new conceptnumeric", thirdConceptNumeric.getName(Locale.US).getName());
		assertEquals(50.0, thirdConceptNumeric.getHiAbsolute().doubleValue(), 0);
		
	}

	/**
     * @see {@link ConceptService#getConceptComplex(Integer)}
     */
    @Test
    //@Verifies(value = "should return a concept complex object", method = "getConceptComplex(Integer)")
    public void getConceptComplex_shouldReturnAConceptComplexObject() throws Exception {
    	executeDataSet("org/openmrs/api/include/ObsServiceTest-complex.xml");
	    ConceptComplex concept = Context.getConceptService().getConceptComplex(8473);
	    Assert.assertNotNull(concept);
    }

}

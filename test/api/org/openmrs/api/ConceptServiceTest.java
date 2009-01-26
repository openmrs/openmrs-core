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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.TestUtil;
import org.openmrs.test.Verifies;

/**
 * This test class (should) contain tests for all of the ConcepService methods TODO clean up and
 * finish this test class
 * 
 * @see org.openmrs.api.ConceptService
 */
public class ConceptServiceTest extends BaseContextSensitiveTest {
	
	protected ConceptService conceptService = null;
	
	protected static final String INITIAL_CONCEPTS_XML = "org/openmrs/api/include/ConceptServiceTest-initialConcepts.xml";
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeAllTests() throws Exception {
		conceptService = Context.getConceptService();
	}
	
	/**
	 * Test getting a concept by name and by partial name. NOTE: This test assumes that there are at
	 * least a few concepts in the database system
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
		assertTrue("You should be able to get the concept by partial name", firstConceptsByPartialNameList
		        .contains(firstConcept));
		
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
		ConceptNumeric firstConceptNumeric = (ConceptNumeric) firstConcept;
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
		ConceptNumeric thirdConceptNumeric = (ConceptNumeric) thirdConcept;
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
	
	/**
	 * @verifies {@link ConceptService#saveConcept(Concept)} test = should generate id for new
	 *           concept if none is specified
	 */
	@Test
	public void saveConcept_shouldGenerateIdForNewConceptIfNoneIsSpecified() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("Weight", Locale.US));
		concept.setConceptId(null);
		concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Numeric"));
		concept.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		concept = Context.getConceptService().saveConcept(concept);
		assertFalse(concept.getConceptId().equals(5089));
	}
	
	/**
	 * @verifies {@link ConceptService#saveConcept(Concept)} test = should keep id for new concept
	 *           if one is specified
	 */
	@Test
	public void saveConcept_shouldKeepIdForNewConceptIfOneIsSpecified() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("Weight", Locale.US));
		concept.setConceptId(5089);
		concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Numeric"));
		concept.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		concept = Context.getConceptService().saveConcept(concept);
		assertTrue(concept.getConceptId().equals(5089));
	}
	
	/**
	 * @see {@link ConceptService#conceptIterator()}
	 */
	@Test
	@Verifies(value = "should iterate over all concepts", method = "conceptIterator()")
	public void conceptIterator_shouldIterateOverAllConcepts() throws Exception {
		Iterator<Concept> iterator = Context.getConceptService().conceptIterator();
		
		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals(3, iterator.next().getConceptId().intValue());
	}
	
	/**
	 * This test will fail if it takes more than 15 seconds to run. (Checks for an error with the
	 * iterator looping forever) The @Timed annotation is used as an alternative to
	 * @Test(timeout=15000) so that the Spring transactions work correctly. Junit has a "feature"
	 * where it executes the befores/afters in a thread separate from the one that the actual test
	 * ends up running in when timed.
	 * 
	 * @see {@link ConceptService#conceptIterator()}
	 */
	@Test()
	@Verifies(value = "should start with the smallest concept id", method = "conceptIterator()")
	public void conceptIterator_shouldStartWithTheSmallestConceptId() throws Exception {
		List<Concept> allConcepts = Context.getConceptService().getAllConcepts();
		int numberofconcepts = allConcepts.size();
		
		// sanity check
		Assert.assertTrue(numberofconcepts > 0);
		
		// now count up the number of concepts the iterator returns
		int iteratorCount = 0;
		Iterator<Concept> iterator = Context.getConceptService().conceptIterator();
		while (iterator.hasNext() && iteratorCount < numberofconcepts + 5) { // the lt check is in case of infinite loops 
			iterator.next();
			iteratorCount++;
		}
		Assert.assertEquals(numberofconcepts, iteratorCount);
	}
	
	/**
     * @see {@link ConceptService#saveConcept(Concept)}
     */
    @Test
    @Verifies(value = "should reuse concept name tags that already exist in the database", method = "saveConcept(Concept)")
    public void saveConcept_shouldReuseConceptNameTagsThatAlreadyExistInTheDatabase() throws Exception {
    	executeDataSet("org/openmrs/api/include/ConceptServiceTest-tags.xml");
    	
    	ConceptService cs = Context.getConceptService();
    	
    	// make sure the name tag exists already
    	ConceptNameTag cnt = cs.getConceptNameTagByName("preferred_en");
    	Assert.assertNotNull(cnt);
    	
    	ConceptName cn = new ConceptName("Some name", Locale.ENGLISH);
    	
    	Concept concept = new Concept();
    	concept.setPreferredName(Locale.ENGLISH, cn);
    	concept.setDatatype(new ConceptDatatype(1));
    	concept.setConceptClass(new ConceptClass(1));
    	
    	cs.saveConcept(concept);
    	
    	Collection<ConceptNameTag> savedConceptNameTags = concept.getBestName(Locale.ENGLISH).getTags();
    	ConceptNameTag savedConceptNameTag = (ConceptNameTag)savedConceptNameTags.toArray()[0];
    	Assert.assertEquals(cnt.getConceptNameTagId(), savedConceptNameTag.getConceptNameTagId());
    }

}

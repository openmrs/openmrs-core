/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openmrs.test.OpenmrsMatchers.hasConcept;
import static org.openmrs.test.OpenmrsMatchers.hasId;
import static org.openmrs.test.TestUtil.containsId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.dbunit.dataset.IDataSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptStopWord;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.ConceptMapTypeComparator;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.Errors;

/**
 * This test class (should) contain tests for all of the ConcepService methods TODO clean up and
 * finish this test class
 * 
 * @see org.openmrs.api.ConceptService
 */
public class ConceptServiceTest extends BaseContextSensitiveTest {
	
	protected ConceptService conceptService = null;
	
	protected static final String INITIAL_CONCEPTS_XML = "org/openmrs/api/include/ConceptServiceTest-initialConcepts.xml";
	
	protected static final String GET_CONCEPTS_BY_SET_XML = "org/openmrs/api/include/ConceptServiceTest-getConceptsBySet.xml";
	
	protected static final String GET_DRUG_MAPPINGS = "org/openmrs/api/include/ConceptServiceTest-getDrugMappings.xml";
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
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
	
	@After
	public void revertToDefaultLocale() throws Exception {
		Context.setLocale(Locale.US);
	}
	
	/**
	 * Updates the search index to clean up after each test.
	 * 
	 * @see org.openmrs.test.BaseContextSensitiveTest#updateSearchIndex()
	 */
	@Before
	@Override
	public void updateSearchIndex() {
		super.updateSearchIndex();
	}
	
	/**
	 * Updates the search index after executing each dataset.
	 * 
	 * @see org.openmrs.test.BaseContextSensitiveTest#executeDataSet(org.dbunit.dataset.IDataSet)
	 */
	@Override
	public void executeDataSet(IDataSet dataset) throws Exception {
		super.executeDataSet(dataset);
		
		updateSearchIndex();
	}
	
	/**
	 * Test getting a concept by name and by partial name.
	 * 
	 * @see {@link ConceptService#getConceptByName(String)}
	 */
	@Test
	@Verifies(value = "should get concept by name", method = "getConceptByName(String)")
	public void getConceptByName_shouldGetConceptByName() throws Exception {
		
		String nameToFetch = "Some non numeric concept name";
		
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Concept conceptByName = conceptService.getConceptByName(nameToFetch);
		assertEquals("Unable to fetch concept by name", 1, conceptByName.getId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getConceptByName(String)}
	 */
	@Test
	@Verifies(value = "should get concept by partial name", method = "getConceptByName(String)")
	public void getConceptByName_shouldGetConceptByPartialName() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// substring of the name
		String partialNameToFetch = "Some";
		
		List<Concept> firstConceptsByPartialNameList = conceptService.getConceptsByName(partialNameToFetch);
		assertThat(firstConceptsByPartialNameList, containsInAnyOrder(hasId(1), hasId(2)));
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should save a ConceptNumeric as a concept", method = "saveConcept(Concept)")
	public void saveConcept_shouldSaveAConceptNumericAsAConcept() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		//This will automatically add the given locale to the list of allowed locales
		Context.setLocale(Locale.US);
		// this tests saving a previously conceptnumeric as just a concept
		Concept c2 = new Concept(2);
		ConceptName cn = new ConceptName("not a numeric anymore", Locale.US);
		c2.addName(cn);
		
		c2.setDatatype(new ConceptDatatype(3));
		conceptService.saveConcept(c2);
		
		Concept secondConcept = conceptService.getConcept(2);
		// this will probably still be a ConceptNumeric object.  what to do about that?
		// revisit this problem when discriminators are in place
		//assertFalse(secondConcept instanceof ConceptNumeric);
		// this shouldn't think its a conceptnumeric object though
		assertFalse(secondConcept.isNumeric());
		assertEquals("not a numeric anymore", secondConcept.getName(Locale.US).getName());
		
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should save a new ConceptNumeric", method = "saveConcept(Concept)")
	public void saveConcept_shouldSaveANewConceptNumeric() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		Context.setLocale(Locale.US);
		// this tests saving a never before in the database conceptnumeric
		ConceptNumeric cn3 = new ConceptNumeric();
		cn3.setDatatype(new ConceptDatatype(1));
		
		ConceptName cn = new ConceptName("a brand new conceptnumeric", Locale.US);
		cn3.addName(cn);
		cn3.setHiAbsolute(50.0);
		conceptService.saveConcept(cn3);
		
		Concept thirdConcept = conceptService.getConcept(cn3.getConceptId());
		assertTrue(thirdConcept instanceof ConceptNumeric);
		ConceptNumeric thirdConceptNumeric = (ConceptNumeric) thirdConcept;
		assertEquals("a brand new conceptnumeric", thirdConceptNumeric.getName(Locale.US).getName());
		assertEquals(50.0, thirdConceptNumeric.getHiAbsolute(), 0);
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should save non ConceptNumeric object as conceptNumeric", method = "saveConcept(Concept)")
	public void saveConcept_shouldSaveNonConceptNumericObjectAsConceptNumeric() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// this tests saving a current concept as a newly changed conceptnumeric
		// assumes there is already a concept in the database
		// with a concept id of #1
		ConceptNumeric cn = new ConceptNumeric(1);
		cn.setDatatype(new ConceptDatatype(1));
		cn.addName(new ConceptName("a new conceptnumeric", Locale.US));
		cn.setHiAbsolute(20.0);
		conceptService.saveConcept(cn);
		
		Concept firstConcept = conceptService.getConceptNumeric(1);
		assertEquals("a new conceptnumeric", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptNumeric);
		ConceptNumeric firstConceptNumeric = (ConceptNumeric) firstConcept;
		assertEquals(20.0, firstConceptNumeric.getHiAbsolute(), 0);
		
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should save non ConceptComplex object as conceptComplex", method = "saveConcept(Concept)")
	public void saveConcept_shouldSaveNonConceptComplexObjectAsConceptComplex() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// this tests saving a current concept as a newly changed conceptComplex
		// assumes there is already a concept in the database
		// with a concept id of #1
		ConceptComplex cn = new ConceptComplex(1);
		cn.setDatatype(new ConceptDatatype(13));
		cn.addName(new ConceptName("a new conceptComplex", Locale.US));
		cn.setHandler("SomeHandler");
		conceptService.saveConcept(cn);
		
		Concept firstConcept = conceptService.getConceptComplex(1);
		assertEquals("a new conceptComplex", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptComplex);
		ConceptComplex firstConceptComplex = (ConceptComplex) firstConcept;
		assertEquals("SomeHandler", firstConceptComplex.getHandler());
		
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "save changes between concept numeric and complex", method = "saveConcept(Concept)")
	public void saveConcept_shouldSaveChangesBetweenConceptNumericAndComplex() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		//save a concept numeric
		ConceptNumeric cn = new ConceptNumeric(1);
		cn.setDatatype(new ConceptDatatype(1));
		cn.addName(new ConceptName("a new conceptnumeric", Locale.US));
		cn.setHiAbsolute(20.0);
		conceptService.saveConcept(cn);
		
		//confirm that we saved a concept numeric
		Concept firstConcept = conceptService.getConceptNumeric(1);
		assertEquals("a new conceptnumeric", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptNumeric);
		ConceptNumeric firstConceptNumeric = (ConceptNumeric) firstConcept;
		assertEquals(20.0, firstConceptNumeric.getHiAbsolute(), 0);
		
		//change to concept complex
		ConceptComplex cn2 = new ConceptComplex(1);
		cn2.setDatatype(new ConceptDatatype(13));
		cn2.addName(new ConceptName("a new conceptComplex", Locale.US));
		cn2.setHandler("SomeHandler");
		conceptService.saveConcept(cn2);
		
		//confirm that we saved a concept complex
		firstConcept = conceptService.getConceptComplex(1);
		assertEquals("a new conceptComplex", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptComplex);
		ConceptComplex firstConceptComplex = (ConceptComplex) firstConcept;
		assertEquals("SomeHandler", firstConceptComplex.getHandler());
		
		//change to concept numeric
		cn = new ConceptNumeric(1);
		ConceptDatatype dt = new ConceptDatatype(1);
		dt.setName("Numeric");
		cn.setDatatype(dt);
		cn.addName(new ConceptName("a new conceptnumeric", Locale.US));
		cn.setHiAbsolute(20.0);
		conceptService.saveConcept(cn);
		
		//confirm that we saved a concept numeric
		firstConcept = conceptService.getConceptNumeric(1);
		assertEquals("a new conceptnumeric", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptNumeric);
		firstConceptNumeric = (ConceptNumeric) firstConcept;
		assertEquals(20.0, firstConceptNumeric.getHiAbsolute(), 0);
		
		//change to concept complex
		cn2 = new ConceptComplex(1);
		cn2.setDatatype(new ConceptDatatype(13));
		cn2.addName(new ConceptName("a new conceptComplex", Locale.US));
		cn2.setHandler("SomeHandler");
		conceptService.saveConcept(cn2);
		
		//confirm we saved a concept complex
		firstConcept = conceptService.getConceptComplex(1);
		assertEquals("a new conceptComplex", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptComplex);
		firstConceptComplex = (ConceptComplex) firstConcept;
		assertEquals("SomeHandler", firstConceptComplex.getHandler());
	}
	
	/**
	 * @see {@link ConceptService#getConceptComplex(Integer)}
	 */
	@Test
	@Verifies(value = "should return a concept complex object", method = "getConceptComplex(Integer)")
	public void getConceptComplex_shouldReturnAConceptComplexObject() throws Exception {
		executeDataSet("org/openmrs/api/include/ObsServiceTest-complex.xml");
		ConceptComplex concept = Context.getConceptService().getConceptComplex(8473);
		Assert.assertNotNull(concept);
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should generate id for new concept if none is specified", method = "saveConcept(Concept)")
	public void saveConcept_shouldGenerateIdForNewConceptIfNoneIsSpecified() throws Exception {
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("Weight", Context.getLocale());
		concept.addName(cn);
		
		concept.setConceptId(null);
		concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Numeric"));
		concept.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		concept = Context.getConceptService().saveConcept(concept);
		assertFalse(concept.getConceptId().equals(5089));
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should keep id for new concept if one is specified", method = "saveConcept(Concept)")
	public void saveConcept_shouldKeepIdForNewConceptIfOneIsSpecified() throws Exception {
		Integer conceptId = 343434; // a nonexistent concept id;
		Assert.assertNull(conceptService.getConcept(conceptId)); // sanity check
		
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("Weight", Context.getLocale());
		concept.addName(cn);
		concept.setConceptId(conceptId);
		concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Numeric"));
		concept.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		concept = Context.getConceptService().saveConcept(concept);
		assertTrue(concept.getConceptId().equals(conceptId));
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
	 * "@Test(timeout=15000)" so that the Spring transactions work correctly. Junit has a "feature"
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
		cn.addTag(new ConceptNameTag("preferred_en", "preferred name in a language"));
		Concept concept = new Concept();
		concept.addName(cn);
		
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));
		
		cs.saveConcept(concept);
		
		Collection<ConceptNameTag> savedConceptNameTags = concept.getName(Locale.ENGLISH, false).getTags();
		ConceptNameTag savedConceptNameTag = (ConceptNameTag) savedConceptNameTags.toArray()[0];
		Assert.assertEquals(cnt.getConceptNameTagId(), savedConceptNameTag.getConceptNameTagId());
	}
	
	/**
	 * @see {@link ConceptService#saveConceptSource(ConceptSource)}
	 */
	@Test
	@Verifies(value = "should not set creator if one is supplied already", method = "saveConceptSource(ConceptSource)")
	public void saveConceptSource_shouldNotSetCreatorIfOneIsSuppliedAlready() throws Exception {
		User expectedCreator = new User(501); // a user that isn't logged in now
		
		ConceptSource newConceptSource = new ConceptSource();
		newConceptSource.setName("name");
		newConceptSource.setDescription("desc");
		newConceptSource.setHl7Code("hl7Code");
		newConceptSource.setCreator(expectedCreator);
		Context.getConceptService().saveConceptSource(newConceptSource);
		
		Assert.assertEquals(newConceptSource.getCreator(), expectedCreator);
	}
	
	/**
	 * @see {@link ConceptService#saveConceptSource(ConceptSource)}
	 */
	@Test
	@Verifies(value = "should not set date created if one is supplied already", method = "saveConceptSource(ConceptSource)")
	public void saveConceptSource_shouldNotSetDateCreatedIfOneIsSuppliedAlready() throws Exception {
		Date expectedDate = new Date(new Date().getTime() - 10000);
		
		ConceptSource newConceptSource = new ConceptSource();
		newConceptSource.setName("name");
		newConceptSource.setDescription("desc");
		newConceptSource.setHl7Code("hl7Code");
		
		newConceptSource.setDateCreated(expectedDate);
		Context.getConceptService().saveConceptSource(newConceptSource);
		
		Assert.assertEquals(newConceptSource.getDateCreated(), expectedDate);
	}
	
	/**
	 * @see {@link ConceptService#getConcept(String)}
	 */
	@Test
	@Verifies(value = "should return null given null parameter", method = "getConcept(String)")
	public void getConcept_shouldReturnNullGivenNullParameter() throws Exception {
		Assert.assertNull(Context.getConceptService().getConcept((String) null));
	}
	
	/**
	 * @see {@link ConceptService#getConceptByName(String)}
	 */
	@Test
	@Verifies(value = "should return null given null parameter", method = "getConceptByName(String)")
	public void getConceptByName_shouldReturnNullGivenNullParameter() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptByName(null));
	}
	
	/**
	 * This test verifies that {@link ConceptName}s are fetched correctly from the hibernate cache.
	 * (Or really, not fetched from the cache but instead are mapped with lazy=false. For some
	 * reason Hibernate isn't able to find objects in the cache if a parent object was the one that
	 * loaded them)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFetchNamesForConceptsThatWereFirstFetchedAsNumerics() throws Exception {
		Concept concept = Context.getConceptService().getConcept(5089);
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(5089);
		
		conceptNumeric.getNames().size();
		concept.getNames().size();
	}
	
	/**
	 * This test verifies that {@link ConceptDescription}s are fetched correctly from the hibernate
	 * cache. (Or really, not fetched from the cache but instead are mapped with lazy=false. For
	 * some reason Hibernate isn't able to find objects in the cache if a parent object was the one
	 * that loaded them)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFetchDescriptionsForConceptsThatWereFirstFetchedAsNumerics() throws Exception {
		Concept concept = Context.getConceptService().getConcept(5089);
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(5089);
		
		conceptNumeric.getDescriptions().size();
		concept.getDescriptions().size();
	}
	
	/**
	 * @see {@link ConceptService#getConceptByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should get concept with given code and source hl7 code", method = "getConceptByMapping(String,String)")
	public void getConceptByMapping_shouldGetConceptWithGivenCodeAndSourceHl7Code() throws Exception {
		Concept concept = conceptService.getConceptByMapping("WGT234", "SSTRM");
		Assert.assertEquals(5089, concept.getId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getConceptByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should get concept with given code and source hl7 name", method = "getConceptByMapping(String,String)")
	public void getConceptByMapping_shouldGetConceptWithGivenCodeAndSourceName() throws Exception {
		Concept concept = conceptService.getConceptByMapping("WGT234", "Some Standardized Terminology");
		Assert.assertEquals(5089, concept.getId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getConceptByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should return null if source code does not exist", method = "getConceptByMapping(String,String)")
	public void getConceptByMapping_shouldReturnNullIfSourceCodeDoesNotExist() throws Exception {
		Concept concept = conceptService.getConceptByMapping("A random concept code", "A random source code");
		Assert.assertNull(concept);
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should return null if no mapping exists", method = "getConceptByMapping(String,String)")
	public void getConceptByMapping_shouldReturnNullIfNoMappingExists() throws Exception {
		Concept concept = conceptService.getConceptByMapping("A random concept code", "SSTRM");
		Assert.assertNull(concept);
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should return retired concept by default if that is the only match", method = "getConceptByMapping(String,String)")
	public void getConceptByMapping_shouldReturnRetiredConceptByDefaultIfOnlyMatch() throws Exception {
		Concept concept = conceptService.getConceptByMapping("454545", "SSTRM");
		Assert.assertEquals(24, concept.getId().intValue());
		
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String,Boolean)}
	 */
	@Test
	@Verifies(value = "should return retired concept if that is the only match", method = "getConceptByMapping(String,String,Boolean)")
	public void getConceptByMapping_shouldReturnRetiredConceptIfOnlyMatch() throws Exception {
		Concept concept = conceptService.getConceptByMapping("454545", "SSTRM", true);
		Assert.assertEquals(24, concept.getId().intValue());
		
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String,Boolean)}
	 */
	@Test
	@Verifies(value = "should not return retired concept", method = "getConceptByMapping(String,String,Boolean)")
	public void getConceptByMapping_shouldNotReturnRetiredConcept() throws Exception {
		Concept concept = conceptService.getConceptByMapping("454545", "SSTRM", false);
		Assert.assertNull(concept);
		
	}
	
	@Test(expected = APIException.class)
	@Verifies(value = "should throw exception if two non-retired concepts have the name mapping", method = "getConceptsByMapping(String,String,Boolean)")
	public void getConceptByMapping_shouldThrowExceptionIfTwoConceptsHaveSameMapping() throws Exception {
		conceptService.getConceptByMapping("127689", "Some Standardized Terminology");
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should get distinct concepts with given code and and source hl7 code", method = "getConceptsByMapping(String,String)")
	public void getConceptsByMapping_shouldGetConceptsWithGivenCodeAndSourceH17Code() throws Exception {
		List<Concept> concepts = conceptService.getConceptsByMapping("127689", "Some Standardized Terminology");
		Assert.assertEquals(2, concepts.size());
		Assert.assertTrue(containsId(concepts, 16));
		Assert.assertTrue(containsId(concepts, 6));
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should get distinct concepts with given code and source name", method = "getConceptsByMapping(String,String)")
	public void getConceptsByMapping_shouldGetConceptsWithGivenCodeAndSourceName() throws Exception {
		List<Concept> concepts = conceptService.getConceptsByMapping("127689", "SSTRM");
		Assert.assertEquals(2, concepts.size());
		Assert.assertTrue(containsId(concepts, 16));
		Assert.assertTrue(containsId(concepts, 6));
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should return empty list if code does not exist", method = "getConceptByMapping(String,String)")
	public void getConceptsByMapping_shouldReturnEmptyListIfSourceCodeDoesNotExist() throws Exception {
		List<Concept> concept = conceptService.getConceptsByMapping("A random concept code", "A random source code");
		Assert.assertTrue(concept.isEmpty());
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should return empty list if no mappings exist", method = "getConceptsByMapping(String,String)")
	public void getConceptsByMapping_shouldReturnEmptyListIfNoMappingsExist() throws Exception {
		List<Concept> concept = conceptService.getConceptsByMapping("A random concept code", "SSTRM");
		Assert.assertTrue(concept.isEmpty());
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String,Boolean)}
	 */
	@Test
	@Verifies(value = "should return retired and non-retired concepts", method = "getConceptsByMapping(String,String)")
	public void getConceptsByMapping_shouldReturnRetiredAndNonRetiredConceptsByDefault() throws Exception {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM");
		Assert.assertEquals(2, concepts.size());
		Assert.assertTrue(containsId(concepts, 16));
		Assert.assertTrue(containsId(concepts, 24));
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String,Boolean)}
	 */
	@Test
	@Verifies(value = "should only return non-retired concepts", method = "getConceptsByMapping(String,String,Boolean)")
	public void getConceptsByMapping_shouldOnlyReturnNonRetiredConcepts() throws Exception {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM", false);
		Assert.assertEquals(1, concepts.size());
		Assert.assertTrue(containsId(concepts, 16));
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String,Boolean)}
	 */
	@Test
	@Verifies(value = "should return retired and non-retired concepts", method = "getConceptsByMapping(String,String,Boolean)")
	public void getConceptsByMapping_shouldReturnRetiredAndNonRetiredConcepts() throws Exception {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM", true);
		Assert.assertEquals(2, concepts.size());
		Assert.assertTrue(containsId(concepts, 16));
		Assert.assertTrue(containsId(concepts, 24));
	}
	
	/**
	 * @see {@link ConceptService#getConceptsByMapping(String,String,Boolean)}
	 */
	@Test
	@Verifies(value = "should sort non-retired concepts first", method = "getConceptsByMapping(String,String,Boolean)")
	public void getConceptsByMapping_shouldSortNonRetiredConceptsFirst() throws Exception {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM", true);
		Assert.assertEquals(16, concepts.get(0).getId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getConceptByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should ignore case while returning results", method = "getConceptByMapping(String,String)")
	public void getConceptByMapping_shouldIgnoreCase() throws Exception {
		Concept concept = conceptService.getConceptByMapping("wgt234", "sstrm");
		Assert.assertEquals(5089, concept.getId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getConceptAnswerByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptAnswerByUuid(String)")
	public void getConceptAnswerByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "b1230431-2fe5-49fc-b535-ae42bc849747";
		ConceptAnswer conceptAnswer = Context.getConceptService().getConceptAnswerByUuid(uuid);
		Assert.assertEquals(1, (int) conceptAnswer.getConceptAnswerId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptAnswerByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptAnswerByUuid(String)")
	public void getConceptAnswerByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptAnswerByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptByUuid(String)")
	public void getConceptByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab";
		Concept concept = Context.getConceptService().getConceptByUuid(uuid);
		Assert.assertEquals(3, (int) concept.getConceptId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptByUuid(String)")
	public void getConceptByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptClassByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptClassByUuid(String)")
	public void getConceptClassByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "97097dd9-b092-4b68-a2dc-e5e5be961d42";
		ConceptClass conceptClass = Context.getConceptService().getConceptClassByUuid(uuid);
		Assert.assertEquals(1, (int) conceptClass.getConceptClassId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptClassByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptClassByUuid(String)")
	public void getConceptClassByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptClassByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptDatatypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptDatatypeByUuid(String)")
	public void getConceptDatatypeByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "8d4a4488-c2cc-11de-8d13-0010c6dffd0f";
		ConceptDatatype conceptDatatype = Context.getConceptService().getConceptDatatypeByUuid(uuid);
		Assert.assertEquals(1, (int) conceptDatatype.getConceptDatatypeId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptDatatypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptDatatypeByUuid(String)")
	public void getConceptDatatypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptDatatypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptDescriptionByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptDescriptionByUuid(String)")
	public void getConceptDescriptionByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "5f4d710b-d333-40b7-b449-6e0e739d15d0";
		ConceptDescription conceptDescription = Context.getConceptService().getConceptDescriptionByUuid(uuid);
		Assert.assertEquals(1, (int) conceptDescription.getConceptDescriptionId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptDescriptionByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptDescriptionByUuid(String)")
	public void getConceptDescriptionByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptDescriptionByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptNameByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptNameByUuid(String)")
	public void getConceptNameByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "9bc5693a-f558-40c9-8177-145a4b119ca7";
		ConceptName conceptName = Context.getConceptService().getConceptNameByUuid(uuid);
		Assert.assertEquals(1439, (int) conceptName.getConceptNameId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptNameByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptNameByUuid(String)")
	public void getConceptNameByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptNameByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptNameTagByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptNameTagByUuid(String)")
	public void getConceptNameTagByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "9e9df183-2328-4117-acd8-fb9bf400911d";
		ConceptNameTag conceptNameTag = Context.getConceptService().getConceptNameTagByUuid(uuid);
		Assert.assertEquals(1, (int) conceptNameTag.getConceptNameTagId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptNameTagByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptNameTagByUuid(String)")
	public void getConceptNameTagByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptNameTagByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptNumericByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptNumericByUuid(String)")
	public void getConceptNumericByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumericByUuid(uuid);
		Assert.assertEquals(5089, (int) conceptNumeric.getConceptId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptNumericByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptNumericByUuid(String)")
	public void getConceptNumericByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptNumericByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptProposalByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptProposalByUuid(String)")
	public void getConceptProposalByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "57a68666-5067-11de-80cb-001e378eb67e";
		ConceptProposal conceptProposal = Context.getConceptService().getConceptProposalByUuid(uuid);
		Assert.assertEquals(1, (int) conceptProposal.getConceptProposalId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptProposalByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptProposalByUuid(String)")
	public void getConceptProposalByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptProposalByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptSetByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptSetByUuid(String)")
	public void getConceptSetByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "1a111827-639f-4cb4-961f-1e025bf88d90";
		ConceptSet conceptSet = Context.getConceptService().getConceptSetByUuid(uuid);
		Assert.assertEquals(1, (int) conceptSet.getConceptSetId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptSetByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptSetByUuid(String)")
	public void getConceptSetByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptSetByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptSourceByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptSourceByUuid(String)")
	public void getConceptSourceByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "75f5b378-5065-11de-80cb-001e378eb67e";
		ConceptSource conceptSource = Context.getConceptService().getConceptSourceByUuid(uuid);
		Assert.assertEquals(3, (int) conceptSource.getConceptSourceId());
	}
	
	/**
	 * @see {@link ConceptService#getConceptSourceByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptSourceByUuid(String)")
	public void getConceptSourceByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getConceptSourceByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getDrugByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getDrugByUuid(String)")
	public void getDrugByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "3cfcf118-931c-46f7-8ff6-7b876f0d4202";
		Drug drug = Context.getConceptService().getDrugByUuid(uuid);
		Assert.assertEquals(2, (int) drug.getDrugId());
	}
	
	/**
	 * @see {@link ConceptService#getDrugByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getDrugByUuid(String)")
	public void getDrugByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getConceptService().getDrugByUuid("some invalid uuid"));
	}
	
	@Test
	@Verifies(value = "should return drugs that are not retired", method = "getDrugs(String)")
	public void getDrugs_shouldReturnDrugsThatAreNotRetired() throws Exception {
		List<Drug> drugs = Context.getConceptService().getDrugs("ASPIRIN" /* is not retired */);
		Assert.assertFalse(drugs.get(0).isRetired());
	}
	
	/**
	 * @see {@link ConceptService#getDrugs(String)}
	 */
	@Test
	@Verifies(value = "should not return drugs that are retired", method = "getDrugs(String)")
	public void getDrugs_shouldNotReturnDrugsThatAreRetired() throws Exception {
		List<Drug> drugs = Context.getConceptService().getDrugs("TEST_DRUG_NAME_RETIRED" /* is retired */);
		Assert.assertEquals(0, drugs.size());
	}
	
	/**
	 * @see {@link ConceptService#getDrugs(String)}
	 */
	@Test
	@Verifies(value = "return drugs by drug id", method = "getDrugs(String)")
	public void getDrugs_shouldReturnDrugsByDrugId() throws Exception {
		Integer drugId = 2;
		Drug drug = Context.getConceptService().getDrug(drugId);
		List<Drug> drugs = Context.getConceptService().getDrugs(String.valueOf(drugId));
		Assert.assertTrue(drugs.contains(drug));
	}
	
	/**
	 * @see {@link ConceptService#getDrugs(String)}
	 */
	@Test
	@Verifies(value = "not fail if there is no drug by given id", method = "getDrugs(String)")
	public void getDrugs_shouldNotFailIfThereisNoDrugByGivenDrugId() throws Exception {
		List<Drug> drugs = Context.getConceptService().getDrugs("123456");
		Assert.assertNotNull(drugs);
	}
	
	/**
	 * @see {@link ConceptService#getDrugs(String)}
	 */
	@Test
	@Verifies(value = "return drugs by drug concept id", method = "getDrugs(String)")
	public void getDrugs_shouldReturnDrugsByDrugConceptId() throws Exception {
		Integer conceptId = 792;
		Drug drug = Context.getConceptService().getDrug(2);
		
		// assert that given drug has concept with tested id
		Assert.assertNotNull(drug.getConcept());
		Assert.assertEquals(drug.getConcept().getConceptId(), conceptId);
		
		List<Drug> drugs = Context.getConceptService().getDrugs(String.valueOf(conceptId));
		Assert.assertTrue(drugs.contains(drug));
	}
	
	/**
	 * This tests for being able to find concepts with names in en_GB locale when the user is in the
	 * en locale.
	 * 
	 * @see {@link ConceptService#getConceptByName(String)}
	 */
	@Test
	@Verifies(value = "should find concepts with names in more specific locales", method = "getConceptByName(String)")
	public void getConceptByName_shouldFindConceptsWithNamesInMoreSpecificLocales() throws Exception {
		Locale origLocale = Context.getLocale();
		
		executeDataSet(INITIAL_CONCEPTS_XML);
		Context.setLocale(Locale.ENGLISH);
		
		// make sure that concepts are found that have a specific locale on them
		Assert.assertNotNull(Context.getConceptService().getConceptByName("Numeric name with en_GB locale"));
		
		// find concepts with same generic locale
		Assert.assertNotNull(Context.getConceptService().getConceptByName("Some numeric concept name"));
		
		// reset the locale for the next test
		Context.setLocale(origLocale);
	}
	
	/**
	 * This tests for being able to find concepts with names in the en locale when the user is in
	 * the en_GB locale
	 * 
	 * @see {@link ConceptService#getConceptByName(String)}
	 */
	@Test
	@Verifies(value = "should find concepts with names in more generic locales", method = "getConceptByName(String)")
	public void getConceptByName_shouldFindConceptsWithNamesInMoreGenericLocales() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		//prior tests have changed the locale to 'en_US', so we need to set it back
		Context.setLocale(Locale.UK);
		// make sure that concepts are found that have a specific locale on them
		Assert.assertNotNull(Context.getConceptService().getConceptByName("Some numeric concept name"));
	}
	
	/**
	 * This tests for being able to find concepts with names in en_GB locale when the user is in the
	 * en_GB locale.
	 * 
	 * @see {@link ConceptService#getConceptByName(String)}
	 */
	@Test
	@Verifies(value = "should find concepts with names in same specific locale", method = "getConceptByName(String)")
	public void getConceptByName_shouldFindConceptsWithNamesInSameSpecificLocale() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Context.setLocale(Locale.UK);
		
		// make sure that concepts are found that have a specific locale on them
		Assert.assertNotNull(Context.getConceptService().getConceptByName("Numeric name with en_GB locale"));
	}
	
	/**
	 * @see {@link ConceptService#retireConceptSource(ConceptSource,String)}
	 */
	@Test
	@Verifies(value = "should retire concept source", method = "retireConceptSource(ConceptSource,String)")
	public void retireConceptSource_shouldRetireConceptSource() throws Exception {
		ConceptSource cs = conceptService.getConceptSource(3);
		conceptService.retireConceptSource(cs, "dummy reason for retirement");
		
		cs = conceptService.getConceptSource(3);
		Assert.assertTrue(cs.isRetired());
		Assert.assertEquals("dummy reason for retirement", cs.getRetireReason());
	}
	
	/**
	 * @verifies {@link ConceptService#saveConcept(Concept)} test = should create new concept in
	 *           database
	 */
	@Test
	@Verifies(value = "should create new concept in database", method = "saveConcept(Concept)")
	public void saveConcept_shouldCreateNewConceptInDatabase() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Concept conceptToAdd = new Concept();
		ConceptName cn = new ConceptName("new name", Context.getLocale());
		conceptToAdd.addName(cn);
		assertFalse(conceptService.getAllConcepts().contains(conceptToAdd));
		conceptService.saveConcept(conceptToAdd);
		assertTrue(conceptService.getAllConcepts().contains(conceptToAdd));
	}
	
	/**
	 * @verifies {@link ConceptService#saveConcept(Concept)} test = should update concept already
	 *           existing in database
	 */
	@Test
	@Verifies(value = "should update concept already existing in database", method = "saveConcept(Concept)")
	public void saveConcept_shouldUpdateConceptAlreadyExistingInDatabase() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// using isSet() as a value to check and change
		assertFalse(conceptService.getConcept(2).isSet());
		Concept concept = conceptService.getConcept(2);
		// change a value
		concept.setSet(true);
		
		// save the concept
		conceptService.saveConcept(concept);
		// see if the value was updated in the database
		assertTrue(conceptService.getConcept(2).isSet());
	}
	
	/**
	 * @verifies {@link ConceptService#getConceptSourceByName(String)} test = should get
	 *           ConceptSource with the given name
	 */
	@Test
	public void getConceptSourceByName_shouldGetConceptSourceWithTheGivenName() throws Exception {
		ConceptSource conceptSource = conceptService.getConceptSourceByName("SNOMED CT");
		assertEquals("Method did not retrieve ConceptSource by name", Integer.valueOf(2), conceptSource.getConceptSourceId());
	}
	
	/**
	 * @verifies {@link ConceptService#getConceptSourceByName(String)} test = should return null if
	 *           no ConceptSource with that name is found
	 */
	@Test
	public void getConceptSourceByName_shouldReturnNullIfNoConceptSourceWithThatNameIsFound() throws Exception {
		ConceptSource conceptSource = conceptService.getConceptSourceByName("Some invalid name");
		assertNull("Method did not return null when no ConceptSource with that name is found", conceptSource);
	}
	
	/**
	 * @verifies {@link ConceptService#getConceptsByConceptSource(ConceptSource)} test = should
	 *           return a List of ConceptMaps if concept mappings found
	 */
	@Test
	public void getConceptsByConceptSource_shouldReturnAListOfConceptMapsIfConceptMappingsFound() throws Exception {
		List<ConceptMap> list = conceptService
		        .getConceptsByConceptSource(conceptService.getConceptSourceByName("SNOMED CT"));
		assertEquals(2, list.size());
	}
	
	/**
	 * @verifies {@link ConceptService#getConceptsByConceptSource(ConceptSource)} test = should
	 *           return empty List of ConceptMaps if none found
	 */
	@Test
	public void getConceptsByConceptSource_shouldReturnEmptyListOfConceptMapsIfNoneFound() throws Exception {
		List<ConceptMap> list = conceptService.getConceptsByConceptSource(conceptService
		        .getConceptSourceByName("Some invalid name"));
		assertEquals(0, list.size());
	}
	
	/**
	 * @verifies {@link ConceptService#saveConceptSource(ConceptSource)} test = should save a
	 *           ConceptSource with a null hl7Code
	 */
	@Test
	public void saveConceptSource_shouldSaveAConceptSourceWithANullHl7Code() throws Exception {
		ConceptSource source = new ConceptSource();
		String aNullString = null;
		String sourceName = "A concept source with null HL7 code";
		source.setName(sourceName);
		source.setHl7Code(aNullString);
		conceptService.saveConceptSource(source);
		assertEquals("Did not save a ConceptSource with a null hl7Code", source, conceptService
		        .getConceptSourceByName(sourceName));
		
	}
	
	/**
	 * @verifies {@link ConceptService#saveConceptSource(ConceptSource)} test = should not save a
	 *           ConceptSource if voided is null
	 */
	@Test(expected = Exception.class)
	public void saveConceptSource_shouldNotSaveAConceptSourceIfVoidedIsNull() throws Exception {
		ConceptSource source = new ConceptSource();
		source.setVoided(null);
		assertNull(source.getVoided());
		
		conceptService.saveConceptSource(source);
		
	}
	
	/**
	 * @verifies {@link ConceptService#saveConceptNameTag(ConceptNameTag)} test = should save a
	 *           concept name tag if tag does not exist
	 */
	@Test
	public void saveConceptNameTag_shouldSaveAConceptNameTagIfATagDoesNotExist() throws Exception {
		ConceptNameTag nameTag = new ConceptNameTag();
		nameTag.setTag("a new tag");
		
		ConceptNameTag savedNameTag = conceptService.saveConceptNameTag(nameTag);
		
		assertNotNull(nameTag.getId());
		assertEquals(savedNameTag.getId(), nameTag.getId());
	}
	
	/**
	 * @verifies {@link ConceptService#saveConceptNameTag(ConceptNameTag)} test = should not save a
	 *           concept name tag if tag exists
	 */
	@Test(expected = Exception.class)
	public void saveConceptNameTag_shouldNotSaveAConceptNameTagIfTagExists() throws Exception {
		String tag = "a new tag";
		
		ConceptNameTag nameTag = new ConceptNameTag();
		nameTag.setTag(tag);
		
		conceptService.saveConceptNameTag(nameTag);
		
		ConceptNameTag secondNameTag = new ConceptNameTag();
		secondNameTag.setTag(tag);
		
		ConceptNameTag existingConceptNameTag = conceptService.saveConceptNameTag(secondNameTag);
		
		assertNull(secondNameTag.getId());
		assertEquals(existingConceptNameTag.getId(), nameTag.getId());
	}
	
	/**
	 * @throws Exception to be asserted on
	 * @verifies {@link ConceptService#saveConcept(Concept)} test = should not update a Concept
	 *           datatype if it is attached to an observation
	 */
	@Test(expected = ConceptInUseException.class)
	public void saveConcept_shouldNotUpdateConceptDataTypeIfConceptIsAttachedToAnObservation() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Concept concept = conceptService.getConcept(2);
		assertNotNull(concept);
		
		ObsService obsService = Context.getObsService();
		Obs obs = new Obs(Context.getPersonService().getPerson(1), concept, new Date(), Context.getLocationService()
		        .getLocation(1));
		obs.setValueCoded(Context.getConceptService().getConcept(7));
		obsService.saveObs(obs, "Creating a new observation with a concept");
		
		ConceptDatatype newDatatype = conceptService.getConceptDatatypeByName("Text");
		concept.setDatatype(newDatatype);
		conceptService.saveConcept(concept);
	}
	
	/**
	 * @throws Exception to be asserted on
	 * @verifies {@link ConceptService#saveConcept(Concept)} test = should update a Concept if
	 *           anything else other than the datatype is changed and it is attached to an
	 *           observation
	 */
	@Test
	public void saveConcept_shouldUpdateConceptIfConceptIsAttachedToAnObservationAndItIsANonDatatypeChange()
	        throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Concept concept = conceptService.getConcept(1);
		assertNotNull(concept);
		
		ObsService obsService = Context.getObsService();
		Obs obs = new Obs(new Person(1), concept, new Date(), new Location(1));
		obs.setValueCoded(Context.getConceptService().getConcept(7));
		obsService.saveObs(obs, "Creating a new observation with a concept");
		
		conceptService.saveConcept(concept);
	}
	
	/**
	 * @see {@link ConceptService#getFalseConcept()}
	 */
	@Test
	@Verifies(value = "should return the false concept", method = "getFalseConcept()")
	public void getFalse_shouldReturnTheFalseConcept() throws Exception {
		createTrueFalseGlobalProperties();
		Assert.assertNotNull(conceptService.getFalseConcept());
		Assert.assertEquals(8, conceptService.getFalseConcept().getId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getTrueConcept()}
	 */
	@Test
	@Verifies(value = "should return the true concept", method = "getTrueConcept()")
	public void getTrue_shouldReturnTheTrueConcept() throws Exception {
		createTrueFalseGlobalProperties();
		Assert.assertNotNull(conceptService.getTrueConcept());
		Assert.assertEquals(7, conceptService.getTrueConcept().getId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getUnknownConcept()}
	 */
	@Test
	@Verifies(value = "should return the unknown concept", method = "getUnknownConcept()")
	public void getUnknownConcept_shouldReturnTheUnknownConcept() throws Exception {
		createUnknownConceptGlobalProperty();
		Assert.assertNotNull(conceptService.getUnknownConcept());
		Assert.assertEquals(9, conceptService.getUnknownConcept().getId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getConceptDatatypeByName(String)}
	 */
	@Test
	@Verifies(value = "should convert the datatype of a boolean concept to coded", method = "changeConceptFromBooleanToCoded(Concept)")
	public void changeConceptFromBooleanToCoded_shouldConvertTheDatatypeOfABooleanConceptToCoded() throws Exception {
		Concept concept = conceptService.getConcept(18);
		Assert.assertEquals(conceptService.getConceptDatatypeByName("Boolean").getConceptDatatypeId(), concept.getDatatype()
		        .getConceptDatatypeId());
		conceptService.convertBooleanConceptToCoded(concept);
		Assert.assertEquals(conceptService.getConceptDatatypeByName("Coded").getConceptDatatypeId(), concept.getDatatype()
		        .getConceptDatatypeId());
	}
	
	/**
	 * @see {@link ConceptService#getConcept(Integer)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if the datatype of the concept is not boolean", method = "changeConceptFromBooleanToCoded(Concept)")
	public void changeConceptFromBooleanToCoded_shouldFailIfTheDatatypeOfTheConceptIsNotBoolean() throws Exception {
		Concept concept = conceptService.getConcept(5497);
		conceptService.convertBooleanConceptToCoded(concept);
	}
	
	/**
	 * @see {@link ConceptService#convertBooleanConceptToCoded(Concept)}
	 */
	@Test
	@Verifies(value = "should explicitly add false concept as a value_Coded answer", method = "changeConceptFromBooleanToCoded(Concept)")
	public void changeConceptFromBooleanToCoded_shouldExplicitlyAddFalseConceptAsAValue_CodedAnswer() throws Exception {
		Concept concept = conceptService.getConcept(18);
		Collection<ConceptAnswer> answers = concept.getAnswers(false);
		boolean falseConceptFound = false;
		//initially the concept shouldn't present
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getFalseConcept()))
				falseConceptFound = true;
		}
		Assert.assertEquals(false, falseConceptFound);
		conceptService.convertBooleanConceptToCoded(concept);
		answers = concept.getAnswers(false);
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getFalseConcept()))
				falseConceptFound = true;
		}
		Assert.assertEquals(true, falseConceptFound);
	}
	
	/**
	 * @see {@link ConceptService#convertBooleanConceptToCoded(Concept)}
	 */
	@Test
	@Verifies(value = "should explicitly add true concept as a value_Coded answer", method = "changeConceptFromBooleanToCoded(Concept)")
	public void changeConceptFromBooleanToCoded_shouldExplicitlyAddTrueConceptAsAValue_CodedAnswer() throws Exception {
		Concept concept = conceptService.getConcept(18);
		Collection<ConceptAnswer> answers = concept.getAnswers(false);
		boolean trueConceptFound = false;
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getTrueConcept()))
				trueConceptFound = true;
		}
		Assert.assertEquals(false, trueConceptFound);
		conceptService.convertBooleanConceptToCoded(concept);
		answers = concept.getAnswers(false);
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getTrueConcept()))
				trueConceptFound = true;
		}
		Assert.assertEquals(true, trueConceptFound);
	}
	
	/**
	 * @see {@link ConceptService#getFalseConcept()}
	 */
	@Test
	@Verifies(value = "should return the false concept", method = "getFalseConcept()")
	public void getFalseConcept_shouldReturnTheFalseConcept() throws Exception {
		createTrueFalseGlobalProperties();
		Assert.assertEquals(8, conceptService.getFalseConcept().getConceptId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getTrueConcept()}
	 */
	@Test
	@Verifies(value = "should return the true concept", method = "getTrueConcept()")
	public void getTrueConcept_shouldReturnTheTrueConcept() throws Exception {
		createTrueFalseGlobalProperties();
		Assert.assertEquals(7, conceptService.getTrueConcept().getConceptId().intValue());
	}
	
	/**
	 * Utility method that creates the global properties 'concept.true' and 'concept.false'
	 */
	private static void createTrueFalseGlobalProperties() {
		GlobalProperty trueConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT, "7",
		        "Concept id of the concept defining the TRUE boolean concept");
		GlobalProperty falseConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT, "8",
		        "Concept id of the concept defining the TRUE boolean concept");
		Context.getAdministrationService().saveGlobalProperty(trueConceptGlobalProperty);
		Context.getAdministrationService().saveGlobalProperty(falseConceptGlobalProperty);
	}
	
	/**
	 * Utility method that creates the global property concept.unknown'
	 */
	private static void createUnknownConceptGlobalProperty() {
		GlobalProperty unknownConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_UNKNOWN_CONCEPT,
		        "9", "Concept id of the concept defining the UNKNOWN concept");
		Context.getAdministrationService().saveGlobalProperty(unknownConceptGlobalProperty);
	}
	
	/**
	 * @see {@link ConceptService#getConceptDatatypeByName(String)}
	 */
	@Test
	@Verifies(value = "should not return a fuzzy match on name", method = "getConceptDatatypeByName(String)")
	public void getConceptDatatypeByName_shouldNotReturnAFuzzyMatchOnName() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		ConceptDatatype result = conceptService.getConceptDatatypeByName("Tex");
		Assert.assertNull(result);
	}
	
	/**
	 * @see {@link ConceptService#getConceptDatatypeByName(String)}
	 */
	@Test
	@Verifies(value = "should return an exact match on name", method = "getConceptDatatypeByName(String)")
	public void getConceptDatatypeByName_shouldReturnAnExactMatchOnName() throws Exception {
		// given
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// when
		ConceptDatatype result = conceptService.getConceptDatatypeByName("Text");
		
		// then
		assertEquals("Text", result.getName());
	}
	
	/**
	 * @see {@link ConceptService#purgeConcept(Concept)}
	 */
	@Test(expected = ConceptNameInUseException.class)
	@Verifies(value = "should fail if any of the conceptNames of the concept is being used by an obs", method = "purgeConcept(Concept)")
	public void purgeConcept_shouldFailIfAnyOfTheConceptNamesOfTheConceptIsBeingUsedByAnObs() throws Exception {
		Obs o = new Obs();
		o.setConcept(Context.getConceptService().getConcept(3));
		o.setPerson(new Patient(2));
		o.setEncounter(new Encounter(3));
		o.setObsDatetime(new Date());
		o.setLocation(new Location(1));
		ConceptName conceptName = new ConceptName(1847);
		o.setValueCodedName(conceptName);
		Context.getObsService().saveObs(o, null);
		//ensure that the association between the conceptName and the obs has been established
		Assert.assertEquals(true, conceptService.hasAnyObservation(conceptName));
		
		Concept concept = conceptService.getConceptByName("cd4 count");
		//make sure the name concept name exists
		Assert.assertNotNull(concept);
		conceptService.purgeConcept(concept);
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should create a new conceptName when the old name is changed", method = "saveConcept(Concept)")
	public void saveConcept_shouldCreateANewConceptNameWhenTheOldNameIsChanged() throws Exception {
		Concept concept = conceptService.getConceptByName("cd4 count");
		Assert.assertEquals(3, concept.getNames(true).size());
		ConceptName oldName = null;
		for (ConceptName cn : concept.getNames()) {
			if (cn.getConceptNameId().equals(1847)) {
				oldName = cn;
				cn.setName("new name");
			}
		}
		
		conceptService.saveConcept(concept);
		
		//force Hibernate interceptor to set dateCreated
		Context.flushSession();
		
		Assert.assertEquals(4, concept.getNames(true).size());
		
		for (ConceptName cn : concept.getNames()) {
			if (cn.getName().equals("new name")) {
				Assert.assertTrue(oldName.getDateCreated().before(cn.getDateCreated()));
			}
		}
		
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should void the conceptName if the text of the name has changed", method = "saveConcept(Concept)")
	public void saveConcept_shouldVoidTheConceptNameIfTheTextOfTheNameHasChanged() throws Exception {
		Concept concept = conceptService.getConceptByName("cd4 count");
		Assert.assertEquals(false, conceptService.getConceptName(1847).isVoided().booleanValue());
		for (ConceptName cn : concept.getNames()) {
			if (cn.getConceptNameId().equals(1847))
				cn.setName("new name");
		}
		//ensure that the conceptName has actually been found and replaced
		Assert.assertEquals(true, concept.hasName("new name", new Locale("en", "GB")));
		conceptService.saveConcept(concept);
		Assert.assertEquals(true, conceptService.getConceptName(1847).isVoided().booleanValue());
	}
	
	/**
	 * Test getting a concept by name and by partial name.
	 * 
	 * @see {@link ConceptService#getConceptByName(String)}
	 */
	@Test
	@Verifies(value = "should return all concepts in set and subsets", method = "getConceptsByConceptSet(Concept)")
	public void getConceptsByConceptSet_shouldReturnAllConceptsInSet() throws Exception {
		
		executeDataSet(GET_CONCEPTS_BY_SET_XML);
		
		Concept concept = conceptService.getConcept(1);
		
		List<Concept> conceptSet = conceptService.getConceptsByConceptSet(concept);
		
		assertThat(conceptSet, containsInAnyOrder(hasId(2), hasId(3), hasId(4), hasId(5), hasId(6)));
	}
	
	/**
	 * @see {@link ConceptService#saveConceptStopWord(org.openmrs.ConceptStopWord)}
	 */
	@Test
	@Verifies(value = "should save concept stop word into database", method = "saveConceptStopWord(ConceptStopWord)")
	public void saveConceptStopWord_shouldSaveConceptStopWordIntoDatabase() throws Exception {
		ConceptStopWord conceptStopWord = new ConceptStopWord("AND", Locale.FRANCE);
		conceptService.saveConceptStopWord(conceptStopWord);
		
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.FRANCE);
		assertEquals(1, conceptStopWords.size());
		assertEquals("AND", conceptStopWords.get(0));
	}
	
	/**
	 * @see {@link ConceptService#saveConceptStopWord(ConceptStopWord)}
	 */
	@Test
	@Verifies(value = "should assign default Locale ", method = "saveConceptStopWord(ConceptStopWord)")
	public void saveConceptStopWord_shouldSaveConceptStopWordAssignDefaultLocaleIsItNull() throws Exception {
		ConceptStopWord conceptStopWord = new ConceptStopWord("The");
		conceptService.saveConceptStopWord(conceptStopWord);
		
		List<String> conceptStopWords = conceptService.getConceptStopWords(Context.getLocale());
		assertThat(conceptStopWords, hasItem("THE"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptStopWords(Locale)}
	 */
	@Test
	@Verifies(value = "should return default Locale ConceptStopWords if Locale is null", method = "getConceptStopWords(Locale)")
	public void getConceptStopWords_shouldReturnDefaultLocaleConceptStopWordsIfLocaleIsNull() throws Exception {
		List<String> conceptStopWords = conceptService.getConceptStopWords(null);
		assertEquals(1, conceptStopWords.size());
	}
	
	/**
	 * @see {@link ConceptService#saveConceptStopWord(ConceptStopWord)}
	 */
	@Test
	@Verifies(value = "should put generated concept stop word id onto returned concept stop word", method = "saveConceptStopWord(ConceptStopWord)")
	public void saveConceptStopWord_shouldSaveReturnConceptStopWordWithId() throws Exception {
		ConceptStopWord conceptStopWord = new ConceptStopWord("A", Locale.UK);
		ConceptStopWord savedConceptStopWord = conceptService.saveConceptStopWord(conceptStopWord);
		
		assertNotNull(savedConceptStopWord.getId());
	}
	
	/**
	 * @see {@link ConceptService#saveConceptStopWord(ConceptStopWord)}
	 */
	@Test(expected = ConceptStopWordException.class)
	@Verifies(value = "should fail if a duplicate conceptStopWord in a locale is added", method = "saveConceptStopWord(ConceptStopWord)")
	public void saveConceptStopWord_shouldFailIfADuplicateConceptStopWordInALocaleIsAdded() throws Exception {
		ConceptStopWord conceptStopWord = new ConceptStopWord("A");
		try {
			conceptService.saveConceptStopWord(conceptStopWord);
			conceptService.saveConceptStopWord(conceptStopWord);
		}
		catch (ConceptStopWordException e) {
			assertEquals("ConceptStopWord.duplicated", e.getMessage());
			throw e;
		}
	}
	
	/**
	 * @see {@link ConceptService#saveConceptStopWord(ConceptStopWord)}
	 */
	@Test
	@Verifies(value = "should save concept stop word in uppercase", method = "saveConceptStopWord(ConceptStopWord)")
	public void saveConceptStopWord_shouldSaveConceptStopWordInUppercase() throws Exception {
		ConceptStopWord conceptStopWord = new ConceptStopWord("lowertoupper");
		ConceptStopWord savedConceptStopWord = conceptService.saveConceptStopWord(conceptStopWord);
		
		assertEquals("LOWERTOUPPER", savedConceptStopWord.getValue());
	}
	
	/**
	 * @see {@link ConceptService#getConceptStopWords(Locale)}
	 */
	@Test
	@Verifies(value = "should return list of concept stop word for given locale", method = "getConceptStopWords(Locale)")
	public void getConceptStopWords_shouldReturnListOfConceptStopWordsForGivenLocale() throws Exception {
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.ENGLISH);
		
		assertThat(conceptStopWords, containsInAnyOrder("A", "AN"));
	}
	
	/**
	 * @see {@link ConceptService#getAllConceptStopWords()}
	 */
	@Test
	@Verifies(value = "should return all the concept stop words", method = "getAllConceptStopWords()")
	public void getAllConceptStopWords_shouldReturnAllConceptStopWords() throws Exception {
		List<ConceptStopWord> conceptStopWords = conceptService.getAllConceptStopWords();
		assertEquals(4, conceptStopWords.size());
	}
	
	/**
	 * @see {@link ConceptService#getAllConceptStopWords()}
	 */
	@Test
	@Verifies(value = "should return empty list if nothing found", method = "getAllConceptStopWords()")
	public void getAllConceptStopWords_shouldReturnEmptyListIfNoRecordFound() throws Exception {
		conceptService.deleteConceptStopWord(1);
		conceptService.deleteConceptStopWord(2);
		conceptService.deleteConceptStopWord(3);
		conceptService.deleteConceptStopWord(4);
		
		List<ConceptStopWord> conceptStopWords = conceptService.getAllConceptStopWords();
		assertEquals(0, conceptStopWords.size());
	}
	
	/**
	 * @see {@link ConceptService#getConceptStopWords(Locale)}
	 */
	@Test
	@Verifies(value = "should return empty list if no stop words are found for the given locale", method = "getConceptStopWords(Locale)")
	public void getConceptStopWords_shouldReturnEmptyListIfNoConceptStopWordsForGivenLocale() throws Exception {
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.GERMANY);
		assertEquals(0, conceptStopWords.size());
	}
	
	/**
	 * @see {@link ConceptService#deleteConceptStopWord(Integer)}
	 */
	@Test
	@Verifies(value = "should delete the given concept stop word from the database", method = "deleteConceptStopWord(ConceptStopWordId)")
	public void deleteConceptStopWord_shouldDeleteTheGivenConceptStopWord() throws Exception {
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.US);
		
		assertEquals(1, conceptStopWords.size());
		
		conceptService.deleteConceptStopWord(4);
		
		conceptStopWords = conceptService.getConceptStopWords(Locale.US);
		assertEquals(0, conceptStopWords.size());
	}
	
	/**
	 * This test fetches all concepts in the xml test dataset and ensures that every locale for a
	 * concept name is among those listed in the global property 'locale.allowed.list' and default
	 * locale. NOTE that it doesn't test a particular API method directly.
	 */
	@Test
	@Verifies(value = "should not accept a locale that is neither among the localeAllowedList nor a default locale", method = "saveConcept(Concept)")
	public void saveConcept_shouldNotAcceptALocaleThatIsNeitherAmongTheLocaleAllowedListNorADefaultLocale() throws Exception {
		List<Concept> concepts = Context.getConceptService().getAllConcepts();
		Set<Locale> allowedLocales = LocaleUtility.getLocalesInOrder();
		for (Concept concept : concepts) {
			if (!CollectionUtils.isEmpty(concept.getNames())) {
				for (ConceptName cn : concept.getNames()) {
					Assert.assertTrue("The locale '" + cn.getLocale() + "' of conceptName with id: " + cn.getConceptNameId()
					        + " is not among those listed in the global property 'locale.allowed.list'", allowedLocales
					        .contains(cn.getLocale()));
				}
			}
		}
	}
	
	/**
	 * This test fetches all concepts in the xml test dataset and ensures that every locale that has
	 * atleast one conceptName has a name marked as preferred. NOTE that it doesn't test a
	 * particular API method directly.
	 */
	@Test
	@Verifies(value = "should always return a preferred name for every locale that has atleast one unvoided name", method = "")
	public void saveConcept_shouldAlwaysReturnAPreferredNameForEveryLocaleThatHasAtleastOneUnvoidedName() throws Exception {
		List<Concept> concepts = Context.getConceptService().getAllConcepts();
		Set<Locale> allowedLocales = LocaleUtility.getLocalesInOrder();
		for (Concept concept : concepts) {
			for (Locale locale : allowedLocales) {
				if (!CollectionUtils.isEmpty(concept.getNames(locale))) {
					Assert.assertNotNull("Concept with Id: " + concept.getConceptId() + " has no preferred name in locale:"
					        + locale, concept.getPreferredName(locale));
					Assert.assertEquals(true, concept.getPreferredName(locale).isLocalePreferred().booleanValue());
				}
			}
		}
	}
	
	/**
	 * This test is run against the xml test dataset for all concepts to ensure that in every locale
	 * with one or more names, there isn't more than one name explicitly marked as locale preferred.
	 * NOTE that it doesn't test a particular API method directly
	 */
	@Test
	@Verifies(value = "should ensure that every concepName locale has exactly one preferred name", method = "")
	public void saveConcept_shouldEnsureThatEveryConcepNameLocaleHasExactlyOnePreferredName() throws Exception {
		List<Concept> concepts = Context.getConceptService().getAllConcepts();
		Set<Locale> allowedLocales = LocaleUtility.getLocalesInOrder();
		for (Concept concept : concepts) {
			for (Locale locale : allowedLocales) {
				Collection<ConceptName> namesInLocale = concept.getNames(locale);
				if (!CollectionUtils.isEmpty(namesInLocale)) {
					int preferredNamesFound = 0;
					for (ConceptName conceptName : namesInLocale) {
						if (conceptName.isLocalePreferred()) {
							preferredNamesFound++;
							Assert.assertTrue("Found multiple preferred names for conceptId: " + concept.getConceptId()
							        + " in the locale '" + locale + "'", preferredNamesFound < 2);
						}
					}
				}
			}
		}
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should set a preferred name for each locale if none is marked", method = "saveConcept(Concept)")
	public void saveConcept_shouldSetAPreferredNameForEachLocaleIfNoneIsMarked() throws Exception {
		//add some other locales to locale.allowed.list for testing purposes
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(
		    OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST);
		gp.setPropertyValue(gp.getPropertyValue().concat(",fr,ja,en_GB"));
		Context.getAdministrationService().saveGlobalProperty(gp);
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("name1", Locale.ENGLISH));
		concept.addName(new ConceptName("name2", Locale.ENGLISH));
		concept.addName(new ConceptName("name3", Locale.FRENCH));
		concept.addName(new ConceptName("name4", Locale.FRENCH));
		concept.addName(new ConceptName("name5", Locale.JAPANESE));
		concept.addName(new ConceptName("name6", Locale.JAPANESE));
		
		concept = Context.getConceptService().saveConcept(concept);
		Assert.assertNotNull(concept.getPreferredName(Locale.ENGLISH));
		Assert.assertNotNull(concept.getPreferredName(Locale.FRENCH));
		Assert.assertNotNull(concept.getPreferredName(Locale.JAPANESE));
	}
	
	/**
	 * @see ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept)
	 * @verifies not require mapped concept on reject action
	 */
	@Test
	public void mapConceptProposalToConcept_shouldNotRequireMappedConceptOnRejectAction() throws Exception {
		String uuid = "af4ae460-0e2b-11e0-a94b-469c3c5a0c2f";
		ConceptProposal proposal = Context.getConceptService().getConceptProposalByUuid(uuid);
		Assert.assertNotNull("could not find proposal " + uuid, proposal);
		proposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		try {
			Context.getConceptService().mapConceptProposalToConcept(proposal, null);
		}
		catch (APIException ex) {
			Assert.fail("cought APIException when rejecting a proposal with null mapped concept");
		}
	}
	
	/**
	 * @see ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept)
	 * @verifies allow rejecting proposals
	 */
	@Test
	public void mapConceptProposalToConcept_shouldAllowRejectingProposals() throws Exception {
		String uuid = "af4ae460-0e2b-11e0-a94b-469c3c5a0c2f";
		ConceptProposal proposal = Context.getConceptService().getConceptProposalByUuid(uuid);
		Assert.assertNotNull("could not find proposal " + uuid, proposal);
		//because there is a  different unit test for the case when mapped proposal is null, we use a non-null concept here for our testing
		Concept concept = conceptService.getConcept(3);
		Assert.assertNotNull("could not find target concept to use for the test", concept);
		proposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		Context.getConceptService().mapConceptProposalToConcept(proposal, concept);
		//retrieve the proposal from the model and check its new state
		ConceptProposal persisted = Context.getConceptService().getConceptProposalByUuid(uuid);
		Assert.assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT, persisted.getState());
	}
	
	/**
	 * @see {@link ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Test
	@Verifies(value = "should return concept search results that match unique concepts", method = "getConcepts(String,List<Locale>,null,List<ConceptClass>,List<ConceptClass>,List<ConceptDatatype>,List<ConceptDatatype>,Concept,Integer,Integer)")
	public void getConcepts_shouldReturnConceptSearchResultsThatMatchUniqueConcepts() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("trust", Collections
		        .singletonList(Locale.ENGLISH), false, null, null, null, null, null, null, null);
		//trust is included in 2 names for conceptid=3000 and in one name for conceptid=4000.
		//So we should see 2 results only
		Assert.assertEquals(2, searchResults.size());
	}
	
	/**
	 * @see {@link ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Test
	@Verifies(value = "should return concept search results that contain all search words as first", method = "getConcepts(String,List<Locale>,null,List<ConceptClass>,List<ConceptClass>,List<ConceptDatatype>,List<ConceptDatatype>,Concept,Integer,Integer)")
	public void getConcepts_shouldReturnConceptSearchResultsThatContainAllSearchWordsAsFirst() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("trust now", Collections
		        .singletonList(Locale.ENGLISH), false, null, null, null, null, null, null, null);
		//"trust now" must be first hit
		assertThat(searchResults.get(0).getWord(), is("trust now"));
	}
	
	/**
	 * @see {@link ConceptService#getConceptReferenceTermByName(String,ConceptSource)}
	 */
	@Test
	@Verifies(value = "should return a concept reference term that matches the given name from the given source", method = "getConceptReferenceTermByName(String,ConceptSource)")
	public void getConceptReferenceTermByName_shouldReturnAConceptReferenceTermThatMatchesTheGivenNameFromTheGivenSource()
	        throws Exception {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTermByName("weight term",
		    new ConceptSource(1));
		Assert.assertNotNull(term);
		Assert.assertEquals("weight term", term.getName());
	}
	
	/**
	 * @see {@link ConceptService#getConceptReferenceTermByCode(String,ConceptSource)}
	 */
	@Test
	@Verifies(value = "should return a concept reference term that matches the given code from the given source", method = "getConceptReferenceTermByCode(String,ConceptSource)")
	public void getConceptReferenceTermByCode_shouldReturnAConceptReferenceTermThatMatchesTheGivenCodeFromTheGivenSource()
	        throws Exception {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTermByCode("2332523",
		    new ConceptSource(2));
		Assert.assertNotNull(term);
		Assert.assertEquals("2332523", term.getCode());
	}
	
	/**
	 * @see {@link ConceptService#getConceptMapTypes(null,null)}
	 */
	@Test
	@Verifies(value = "should not include hidden concept map types if includeHidden is set to false", method = "getConceptMapTypes(null,null)")
	public void getConceptMapTypes_shouldNotIncludeHiddenConceptMapTypesIfIncludeHiddenIsSetToFalse() throws Exception {
		Assert.assertEquals(6, Context.getConceptService().getConceptMapTypes(true, false).size());
	}
	
	/**
	 * @see {@link ConceptService#getConceptMapTypes(null,null)}
	 */
	@Test
	@Verifies(value = "should return a sorted list ordered as follows: regular, retired, hidden, retired and hidden", method = "getConceptMapTypes(null,null)")
	public void getConceptMapTypes_shouldReturnSortedList() throws Exception {
		List<ConceptMapType> conceptMapTypes = Context.getConceptService().getConceptMapTypes(true, true);
		
		for (int i = 0; i < conceptMapTypes.size() - 1; i++) {
			ConceptMapType current = conceptMapTypes.get(i);
			ConceptMapType next = conceptMapTypes.get(i + 1);
			int currentWeight = ConceptMapTypeComparator.getConceptMapTypeSortWeight(current);
			int nextWeight = ConceptMapTypeComparator.getConceptMapTypeSortWeight(next);
			
			assertTrue(currentWeight <= nextWeight);
		}
	}
	
	/**
	 * @see {@link ConceptService#getConceptMapTypes(null,null)}
	 */
	@Test
	@Verifies(value = "should return all the concept map types if includeRetired and hidden are set to true", method = "getConceptMapTypes(null,null)")
	public void getConceptMapTypes_shouldReturnAllTheConceptMapTypesIfIncludeRetiredAndHiddenAreSetToTrue() throws Exception {
		Assert.assertEquals(8, Context.getConceptService().getConceptMapTypes(true, true).size());
	}
	
	/**
	 * @see {@link ConceptService#getConceptMapTypes(null,null)}
	 */
	@Test
	@Verifies(value = "should return only un retired concept map types if includeRetired is set to false", method = "getConceptMapTypes(null,null)")
	public void getConceptMapTypes_shouldReturnOnlyUnRetiredConceptMapTypesIfIncludeRetiredIsSetToFalse() throws Exception {
		Assert.assertEquals(6, Context.getConceptService().getConceptMapTypes(false, true).size());
	}
	
	/**
	 * @see {@link ConceptService#getAllConceptMapTypes()}
	 */
	@Test
	@Verifies(value = "should return all the concept map types excluding hidden ones", method = "getAllConceptMapTypes()")
	public void getActiveConceptMapTypes_shouldReturnAllTheConceptMapTypesExcludingHiddenOnes() throws Exception {
		Assert.assertEquals(6, Context.getConceptService().getActiveConceptMapTypes().size());
	}
	
	/**
	 * @see {@link ConceptService#getConceptMapTypeByName(String)}
	 */
	@Test
	@Verifies(value = "should return a conceptMapType matching the specified name", method = "getConceptMapTypeByName(String)")
	public void getConceptMapTypeByName_shouldReturnAConceptMapTypeMatchingTheSpecifiedName() throws Exception {
		Assert.assertEquals("same-as", Context.getConceptService().getConceptMapTypeByName("same-as").getName());
	}
	
	/**
	 * @see {@link ConceptService#getConceptMapTypeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return a conceptMapType matching the specified uuid", method = "getConceptMapTypeByUuid(String)")
	public void getConceptMapTypeByUuid_shouldReturnAConceptMapTypeMatchingTheSpecifiedUuid() throws Exception {
		Assert.assertEquals("is-parent-to", Context.getConceptService().getConceptMapTypeByUuid(
		    "0e7a8536-49d6-11e0-8fed-18a905e044dc").getName());
	}
	
	/**
	 * @see {@link ConceptService#purgeConceptMapType(ConceptMapType)}
	 */
	@Test
	@Verifies(value = "should delete the specified conceptMapType from the database", method = "purgeConceptMapType(ConceptMapType)")
	public void purgeConceptMapType_shouldDeleteTheSpecifiedConceptMapTypeFromTheDatabase() throws Exception {
		//sanity check
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		Assert.assertNotNull(mapType);
		Context.getConceptService().purgeConceptMapType(mapType);
		Assert.assertNull(Context.getConceptService().getConceptMapType(1));
	}
	
	/**
	 * @see {@link ConceptService#purgeConceptNameTag(ConceptNameTag)}
	 */
	@Test
	@Verifies(value = "should delete the specified ConceptNameTag from the database", method = "purgeConceptNameTag(ConceptNameTag)")
	public void purgeConceptNameTag_shouldDeleteTheSpecifiedConceptNameTagFromTheDatabase() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-tags.xml");
		//sanity check
		ConceptNameTag nameTag = Context.getConceptService().getConceptNameTagByName("preferred_en");
		Assert.assertNotNull(nameTag);
		Context.getConceptService().purgeConceptNameTag(nameTag);
		Assert.assertNull(Context.getConceptService().getConceptNameTagByName("preferred_en"));
	}
	
	/**
	 * @see {@link ConceptService#saveConceptMapType(ConceptMapType)}
	 */
	@Test
	@Verifies(value = "should add the specified concept map type to the database and assign to it an id", method = "saveConceptMapType(ConceptMapType)")
	public void saveConceptMapType_shouldAddTheSpecifiedConceptMapTypeToTheDatabaseAndAssignToItAnId() throws Exception {
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("test type");
		mapType = Context.getConceptService().saveConceptMapType(mapType);
		Assert.assertNotNull(mapType.getId());
		Assert.assertNotNull(Context.getConceptService().getConceptMapTypeByName("test type"));
	}
	
	/**
	 * @see {@link ConceptService#saveConceptMapType(ConceptMapType)}
	 */
	@Test
	@Verifies(value = "should update an existing concept map type", method = "saveConceptMapType(ConceptMapType)")
	public void saveConceptMapType_shouldUpdateAnExistingConceptMapType() throws Exception {
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		//sanity checks
		Assert.assertNull(mapType.getDateChanged());
		Assert.assertNull(mapType.getChangedBy());
		mapType.setName("random name");
		mapType.setDescription("random description");
		ConceptMapType editedMapType = Context.getConceptService().saveConceptMapType(mapType);
		Context.flushSession();
		Assert.assertEquals("random name", editedMapType.getName());
		Assert.assertEquals("random description", editedMapType.getDescription());
		//date changed and changed by should have been updated
		Assert.assertNotNull(editedMapType.getDateChanged());
		Assert.assertNotNull(editedMapType.getChangedBy());
	}
	
	/**
	 * @see {@link ConceptService#retireConceptMapType(ConceptMapType,String)}
	 */
	@Test
	@Verifies(value = "should retire the specified conceptMapType with the given retire reason", method = "retireConceptMapType(ConceptMapType,String)")
	public void retireConceptMapType_shouldRetireTheSpecifiedConceptMapTypeWithTheGivenRetireReason() throws Exception {
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		Assert.assertFalse(mapType.isRetired());
		Assert.assertNull(mapType.getRetiredBy());
		Assert.assertNull(mapType.getDateRetired());
		Assert.assertNull(mapType.getRetireReason());
		ConceptMapType retiredMapType = Context.getConceptService().retireConceptMapType(mapType, "test retire reason");
		Assert.assertTrue(retiredMapType.isRetired());
		Assert.assertEquals(retiredMapType.getRetireReason(), "test retire reason");
		Assert.assertNotNull(retiredMapType.getRetiredBy());
		Assert.assertNotNull(retiredMapType.getDateRetired());
	}
	
	/**
	 * @see {@link ConceptService#retireConceptMapType(ConceptMapType,String)}
	 */
	@Test
	@Verifies(value = "should should set the default retire reason if none is given", method = "retireConceptMapType(ConceptMapType,String)")
	public void retireConceptMapType_shouldShouldSetTheDefaultRetireReasonIfNoneIsGiven() throws Exception {
		//sanity check
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		Assert.assertNull(mapType.getRetireReason());
		ConceptMapType retiredMapType = Context.getConceptService().retireConceptMapType(mapType, null);
		Assert.assertNotNull(retiredMapType.getRetireReason());
	}
	
	/**
	 * @see {@link ConceptService#getAllConceptReferenceTerms(null)}
	 */
	@Test
	@Verifies(value = "should return all the concept reference terms if includeRetired is set to true", method = "getConceptReferenceTerms(null)")
	public void getConceptReferenceTerms_shouldReturnAllTheConceptReferenceTermsIfIncludeRetiredIsSetToTrue()
	        throws Exception {
		Assert.assertEquals(11, Context.getConceptService().getConceptReferenceTerms(true).size());
	}
	
	/**
	 * @see {@link ConceptService#getAllConceptReferenceTerms(null)}
	 */
	@Test
	@Verifies(value = "should return only un retired concept reference terms if includeRetired is set to false", method = "getConceptReferenceTerms(null)")
	public void getConceptReferenceTerms_shouldReturnOnlyUnRetiredConceptReferenceTermsIfIncludeRetiredIsSetToFalse()
	        throws Exception {
		Assert.assertEquals(10, Context.getConceptService().getConceptReferenceTerms(false).size());
	}
	
	/**
	 * @see {@link ConceptService#getConceptReferenceTermByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return the concept reference term that matches the given uuid", method = "getConceptReferenceTermByUuid(String)")
	public void getConceptReferenceTermByUuid_shouldReturnTheConceptReferenceTermThatMatchesTheGivenUuid() throws Exception {
		Assert.assertEquals("weight term2", Context.getConceptService().getConceptReferenceTermByUuid("SNOMED CT-2332523")
		        .getName());
	}
	
	/**
	 * @see {@link ConceptService#getConceptReferenceTermsBySource(ConceptSource)}
	 */
	@Test
	@Verifies(value = "should return only the concept reference terms from the given concept source", method = "getConceptReferenceTermsBySource(ConceptSource)")
	public void getConceptReferenceTerms_shouldReturnOnlyTheConceptReferenceTermsFromTheGivenConceptSource()
	        throws Exception {
		Assert.assertEquals(9, conceptService.getConceptReferenceTerms(null, conceptService.getConceptSource(1), 0, null,
		    true).size());
	}
	
	/**
	 * @see {@link ConceptService#retireConceptReferenceTerm(ConceptReferenceTerm,String)}
	 */
	@Test
	@Verifies(value = "should retire the specified concept reference term with the given retire reason", method = "retireConceptReferenceTerm(ConceptReferenceTerm,String)")
	public void retireConceptReferenceTerm_shouldRetireTheSpecifiedConceptReferenceTermWithTheGivenRetireReason()
	        throws Exception {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		Assert.assertFalse(term.isRetired());
		Assert.assertNull(term.getRetireReason());
		Assert.assertNull(term.getRetiredBy());
		Assert.assertNull(term.getDateRetired());
		ConceptReferenceTerm retiredTerm = Context.getConceptService()
		        .retireConceptReferenceTerm(term, "test retire reason");
		Assert.assertTrue(retiredTerm.isRetired());
		Assert.assertEquals("test retire reason", retiredTerm.getRetireReason());
		Assert.assertNotNull(retiredTerm.getRetiredBy());
		Assert.assertNotNull(retiredTerm.getDateRetired());
	}
	
	/**
	 * @see {@link ConceptService#retireConceptReferenceTerm(ConceptReferenceTerm,String)}
	 */
	@Test
	@Verifies(value = "should should set the default retire reason if none is given", method = "retireConceptReferenceTerm(ConceptReferenceTerm,String)")
	public void retireConceptReferenceTerm_shouldShouldSetTheDefaultRetireReasonIfNoneIsGiven() throws Exception {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		term = Context.getConceptService().retireConceptReferenceTerm(term, null);
		Assert.assertNotNull(term.getRetireReason());
	}
	
	/**
	 * @see {@link ConceptService#saveConceptReferenceTerm(ConceptReferenceTerm)}
	 */
	@Test
	@Verifies(value = "should add a concept reference term to the database and assign an id to it", method = "saveConceptReferenceTerm(ConceptReferenceTerm)")
	public void saveConceptReferenceTerm_shouldAddAConceptReferenceTermToTheDatabaseAndAssignAnIdToIt() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("test term");
		term.setCode("test code");
		ConceptSource source = Context.getConceptService().getConceptSource(1);
		term.setConceptSource(source);
		ConceptReferenceTerm savedTerm = Context.getConceptService().saveConceptReferenceTerm(term);
		Assert.assertNotNull(savedTerm.getId());
		Assert.assertNotNull(Context.getConceptService().getConceptReferenceTermByName("test term", source));
	}
	
	/**
	 * @see {@link ConceptService#saveConceptReferenceTerm(ConceptReferenceTerm)}
	 */
	@Test
	@Verifies(value = "should update changes to the concept reference term in the database", method = "saveConceptReferenceTerm(ConceptReferenceTerm)")
	public void saveConceptReferenceTerm_shouldUpdateChangesToTheConceptReferenceTermInTheDatabase() throws Exception {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		//sanity checks
		Assert.assertEquals(Context.getConceptService().getConceptSource(1), term.getConceptSource());
		Assert.assertNull(term.getChangedBy());
		Assert.assertNull(term.getDateChanged());
		term.setName("new name");
		term.setCode("new code");
		term.setDescription("new descr");
		ConceptSource conceptSource2 = Context.getConceptService().getConceptSource(2);
		term.setConceptSource(conceptSource2);
		
		ConceptReferenceTerm editedTerm = Context.getConceptService().saveConceptReferenceTerm(term);
		Context.flushSession();
		Assert.assertEquals("new name", editedTerm.getName());
		Assert.assertEquals("new code", editedTerm.getCode());
		Assert.assertEquals("new descr", editedTerm.getDescription());
		Assert.assertEquals(conceptSource2, editedTerm.getConceptSource());
		//The auditable fields should have been set
		Assert.assertNotNull(term.getChangedBy());
		Assert.assertNotNull(term.getDateChanged());
	}
	
	/**
	 * @see {@link ConceptService#unretireConceptMapType(ConceptMapType)}
	 */
	@Test
	@Verifies(value = "should unretire the specified concept map type and drop all retire related fields", method = "unretireConceptMapType(ConceptMapType)")
	public void unretireConceptMapType_shouldUnretireTheSpecifiedConceptMapTypeAndDropAllRetireRelatedFields()
	        throws Exception {
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(6);
		Assert.assertTrue(mapType.isRetired());
		Assert.assertNotNull(mapType.getRetiredBy());
		Assert.assertNotNull(mapType.getDateRetired());
		Assert.assertNotNull(mapType.getRetireReason());
		ConceptMapType unRetiredMapType = Context.getConceptService().unretireConceptMapType(mapType);
		Assert.assertFalse(unRetiredMapType.isRetired());
		Assert.assertNull(unRetiredMapType.getRetireReason());
		Assert.assertNull(unRetiredMapType.getRetiredBy());
		Assert.assertNull(unRetiredMapType.getDateRetired());
	}
	
	/**
	 * @see {@link ConceptService#unretireConceptReferenceTerm(ConceptReferenceTerm)}
	 */
	@Test
	@Verifies(value = "should unretire the specified concept reference term and drop all retire related fields", method = "unretireConceptReferenceTerm(ConceptReferenceTerm)")
	public void unretireConceptReferenceTerm_shouldUnretireTheSpecifiedConceptReferenceTermAndDropAllRetireRelatedFields()
	        throws Exception {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(11);
		Assert.assertTrue(term.isRetired());
		Assert.assertNotNull(term.getRetireReason());
		Assert.assertNotNull(term.getRetiredBy());
		Assert.assertNotNull(term.getDateRetired());
		ConceptReferenceTerm retiredTerm = Context.getConceptService().unretireConceptReferenceTerm(term);
		Assert.assertFalse(retiredTerm.isRetired());
		Assert.assertNull(retiredTerm.getRetireReason());
		Assert.assertNull(retiredTerm.getRetiredBy());
		Assert.assertNull(retiredTerm.getDateRetired());
	}
	
	/**
	 * @see {@link ConceptService#getAllConceptReferenceTerms()}
	 */
	@Test
	@Verifies(value = "should return all concept reference terms in the database", method = "getAllConceptReferenceTerms()")
	public void getAllConceptReferenceTerms_shouldReturnAllConceptReferenceTermsInTheDatabase() throws Exception {
		Assert.assertEquals(11, Context.getConceptService().getAllConceptReferenceTerms().size());
	}
	
	/**
	 * @see {@link ConceptService#getConceptMappingsToSource(ConceptSource)}
	 */
	@Test
	@Verifies(value = "should return a List of ConceptMaps from the given source", method = "getConceptMappingsToSource(ConceptSource)")
	public void getConceptMappingsToSource_shouldReturnAListOfConceptMapsFromTheGivenSource() throws Exception {
		Assert.assertEquals(8, Context.getConceptService().getConceptMappingsToSource(
		    Context.getConceptService().getConceptSource(1)).size());
	}
	
	/**
	 * @see {@link ConceptService#getReferenceTermMappingsTo(ConceptReferenceTerm)}
	 */
	@Test
	@Verifies(value = "should return all concept reference term maps where the specified term is the termB", method = "getReferenceTermMappingsTo(ConceptReferenceTerm)")
	public void getReferenceTermMappingsTo_shouldReturnAllConceptReferenceTermMapsWhereTheSpecifiedTermIsTheTermB()
	        throws Exception {
		Assert.assertEquals(2, Context.getConceptService().getReferenceTermMappingsTo(
		    Context.getConceptService().getConceptReferenceTerm(4)).size());
	}
	
	/**
	 * @see {@link ConceptService#getCountOfConcepts(String, List, boolean, List, List, List, List, Concept)}
	 */
	@Test
	@Verifies(value = "should return a count of unique concepts", method = "getCountOfConcepts(String,List<QLocale;>,null,List<QConceptClass;>,List<QConceptClass;>,List<QConceptDatatype;>,List<QConceptDatatype;>,Concept)")
	public void getCountOfConcepts_shouldReturnACountOfUniqueConcepts() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		Assert.assertEquals(2, conceptService.getCountOfConcepts("trust", Collections.singletonList(Locale.ENGLISH), false,
		    null, null, null, null, null).intValue());
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should not fail when a duplicate name is edited to a unique value", method = "saveConcept(Concept)")
	public void saveConcept_shouldNotFailWhenADuplicateNameIsEditedToAUniqueValue() throws Exception {
		//Insert a row to simulate an existing duplicate fully specified/preferred name that needs to be edited
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-conceptWithDuplicateName.xml");
		Concept conceptToEdit = conceptService.getConcept(10000);
		Locale locale = new Locale("en", "GB");
		ConceptName duplicateNameToEdit = conceptToEdit.getFullySpecifiedName(locale);
		//Ensure the name is a duplicate in it's locale
		Concept otherConcept = conceptService.getConcept(5497);
		Assert.assertTrue(duplicateNameToEdit.getName().equalsIgnoreCase(
		    otherConcept.getFullySpecifiedName(locale).getName()));
		
		duplicateNameToEdit.setName("new unique name");
		conceptService.saveConcept(conceptToEdit);
	}
	
	/**
	 * @see {@link ConceptService#getConceptReferenceTerms(String,ConceptSource,Integer,Integer,null)}
	 */
	@Test
	@Verifies(value = "should return unique terms with a code or name containing the search phrase", method = "getConceptReferenceTerms(String,ConceptSource,Integer,Integer,null)")
	public void getConceptReferenceTerms_shouldReturnUniqueTermsWithACodeOrNameContainingTheSearchPhrase() throws Exception {
		List<ConceptReferenceTerm> matches = Context.getConceptService().getConceptReferenceTerms("cd4", null, null, null,
		    true);
		Assert.assertEquals(3,

		matches.size());
		Set<ConceptReferenceTerm> uniqueTerms = new HashSet<ConceptReferenceTerm>();
		//check that we have only unique terms
		for (ConceptReferenceTerm conceptReferenceTerm : matches)
			Assert.assertTrue(uniqueTerms.add(conceptReferenceTerm));
	}
	
	/**
	 * @see ConceptService#getConceptsByAnswer(ConceptClass)
	 */
	@Test
	public void getConceptsByAnswer_shouldFindAnswersForConcept() throws Exception {
		Concept concept = conceptService.getConcept(7);
		Assert.assertNotNull(concept);
		List<Concept> concepts = conceptService.getConceptsByAnswer(concept);
		Assert.assertEquals(1, concepts.size());
		Assert.assertEquals(21, concepts.get(0).getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptsByClass(ConceptClass)
	 * @verifies not fail due to no name in search
	 */
	@Test
	public void getConceptsByClass_shouldNotFailDueToNoNameInSearch() throws Exception {
		ConceptService cs = Context.getConceptService();
		cs.getConceptsByClass(cs.getConceptClass(1));
	}
	
	/**
	 * @see {@link ConceptService#getCountOfConceptReferenceTerms(String,ConceptSource,null)}
	 */
	@Test
	@Verifies(value = "should include retired terms if includeRetired is set to true", method = "getCountOfConceptReferenceTerms(String,ConceptSource,null)")
	public void getCountOfConceptReferenceTerms_shouldIncludeRetiredTermsIfIncludeRetiredIsSetToTrue() throws Exception {
		Assert.assertEquals(11, conceptService.getCountOfConceptReferenceTerms("", null, true).intValue());
	}
	
	/**
	 * @see {@link ConceptService#getCountOfConceptReferenceTerms(String,ConceptSource,null)}
	 */
	@Test
	@Verifies(value = "should not include retired terms if includeRetired is set to false", method = "getCountOfConceptReferenceTerms(String,ConceptSource,null)")
	public void getCountOfConceptReferenceTerms_shouldNotIncludeRetiredTermsIfIncludeRetiredIsSetToFalse() throws Exception {
		Assert.assertEquals(10, conceptService.getCountOfConceptReferenceTerms("", null, false).intValue());
	}
	
	/**
	 * @see ConceptService#getConceptsByName(String,Locale)
	 * @verifies return concepts for all countries and global language given language only locale
	 */
	@Test
	public void getConceptsByName_shouldReturnConceptsForAllCountriesAndGlobalLanguageGivenLanguageOnlyLocale()
	        throws Exception {
		//given
		String name = "Concept";
		Concept concept1 = new Concept();
		concept1.addName(new ConceptName(name, new Locale("en", "US")));
		Context.getConceptService().saveConcept(concept1);
		
		Concept concept2 = new Concept();
		concept2.addName(new ConceptName(name, new Locale("en", "GB")));
		Context.getConceptService().saveConcept(concept2);
		
		Concept concept3 = new Concept();
		concept3.addName(new ConceptName(name, new Locale("en")));
		Context.getConceptService().saveConcept(concept3);
		
		updateSearchIndex();
		
		//when
		List<Concept> concepts = Context.getConceptService().getConceptsByName(name, new Locale("en"), false);
		
		//then
		Assert.assertEquals(3, concepts.size());
		Assert.assertTrue(concepts.containsAll(Arrays.asList(concept1, concept2, concept3)));
	}
	
	/**
	 * @see ConceptService#getConceptsByName(String,Locale)
	 * @verifies return concepts for specific country and global language given language and country
	 *           locale
	 */
	@Test
	public void getConceptsByName_shouldReturnConceptsForSpecificCountryAndGlobalLanguageGivenLanguageAndCountryLocale()
	        throws Exception {
		//given
		String name = "Concept";
		Concept concept1 = new Concept();
		concept1.addName(new ConceptName(name, new Locale("en", "US")));
		Context.getConceptService().saveConcept(concept1);
		
		Concept concept2 = new Concept();
		concept2.addName(new ConceptName(name, new Locale("en", "GB")));
		Context.getConceptService().saveConcept(concept2);
		
		Concept concept3 = new Concept();
		concept3.addName(new ConceptName(name, new Locale("en")));
		Context.getConceptService().saveConcept(concept3);
		
		updateSearchIndex();
		
		//when
		List<Concept> concepts = Context.getConceptService().getConceptsByName(name, new Locale("en", "US"), false);
		
		//then
		assertThat(concepts.get(0), is(concept1));
		assertThat(concepts, containsInAnyOrder(concept1, concept2, concept3));
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@SuppressWarnings("deprecation")
	@Test
	@Verifies(value = "should create a reference term for a concept mapping on the fly when creating a concept", method = "saveConcept(Concept)")
	public void saveConcept_shouldCreateAReferenceTermForAConceptMappingOnTheFlyWhenCreatingAConcept() throws Exception {
		int initialTermCount = conceptService.getAllConceptReferenceTerms().size();
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("test name", Context.getLocale()));
		ConceptMap map = new ConceptMap();
		map.setSourceCode("unique code");
		map.setSource(conceptService.getConceptSource(1));
		concept.addConceptMapping(map);
		conceptService.saveConcept(concept);
		Assert.assertNotNull(concept.getId());
		Assert.assertEquals(initialTermCount + 1, conceptService.getAllConceptReferenceTerms().size());
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@SuppressWarnings("deprecation")
	@Test
	@Verifies(value = "should create a reference term for a concept mapping on the fly when editing a concept", method = "saveConcept(Concept)")
	public void saveConcept_shouldCreateAReferenceTermForAConceptMappingOnTheFlyWhenEditingAConcept() throws Exception {
		int initialTermCount = conceptService.getAllConceptReferenceTerms().size();
		Concept concept = conceptService.getConcept(5497);
		ConceptMap map = new ConceptMap();
		map.setSourceCode("unique code");
		map.setSource(conceptService.getConceptSource(1));
		concept.addConceptMapping(map);
		conceptService.saveConcept(concept);
		Assert.assertEquals(initialTermCount + 1, conceptService.getAllConceptReferenceTerms().size());
	}
	
	/**
	 * @see ConceptService#getDefaultConceptMapType()
	 * @verifies return same as by default
	 */
	@Test
	public void getDefaultConceptMapType_shouldReturnSameAsByDefault() throws Exception {
		ConceptMapType conceptMapType = conceptService.getDefaultConceptMapType();
		Assert.assertNotNull(conceptMapType);
		Assert.assertEquals("same-as", conceptMapType.getName());
	}
	
	/**
	 * @see ConceptService#getDefaultConceptMapType()
	 * @verifies return type as set in gp
	 */
	@Test
	public void getDefaultConceptMapType_shouldReturnTypeAsSetInGp() throws Exception {
		final String testName = "is a";
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty("concept.defaultConceptMapType", testName));
		
		ConceptMapType conceptMapType = conceptService.getDefaultConceptMapType();
		Assert.assertNotNull(conceptMapType);
		Assert.assertEquals(testName, conceptMapType.getName());
	}
	
	/**
	 * @see {@link ConceptService#getConceptMapTypeByName(String)}
	 */
	@Test
	@Verifies(value = "should be case insensitive", method = "getConceptMapTypeByName(String)")
	public void getConceptMapTypeByName_shouldBeCaseInsensitive() throws Exception {
		String name = "SAME-as";
		ConceptMapType mt = Context.getConceptService().getConceptMapTypeByName(name);
		Assert.assertNotNull(mt);
		//sanity check in case the test dataset is edited
		Assert.assertNotSame(name, mt.getName());
		Assert.assertEquals(2, mt.getId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getConceptReferenceTermByName(String,ConceptSource)}
	 */
	@Test
	@Verifies(value = "should be case insensitive", method = "getConceptReferenceTermByName(String,ConceptSource)")
	public void getConceptReferenceTermByName_shouldBeCaseInsensitive() throws Exception {
		String name = "WEIGHT term";
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTermByName(name, new ConceptSource(1));
		Assert.assertNotNull(term);
		Assert.assertNotSame(name, term.getName());
		Assert.assertEquals(1, term.getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptByName(String)
	 * @verifies return null given blank string
	 */
	@Test
	public void getConceptByName_shouldReturnNullGivenBlankString() throws Exception {
		Concept concept = conceptService.getConceptByName("");
		assertNull(concept);
		concept = conceptService.getConceptByName("  ");
		assertNull(concept);
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 * @verifies add new concept name
	 */
	@Test
	public void saveConcept_shouldAddNewConceptName() throws Exception {
		Concept concept = conceptService.getConcept(3);
		
		ConceptName name = new ConceptName("new name", Locale.US);
		
		concept.addName(name);
		
		conceptService.saveConcept(concept);
		assertNotNull(name.getConceptNameId());
	}
	
	/**
	 * @see ConceptService@getTrueConcept()
	 * @verifies should return proper true concept
	 */
	@Test
	public void getTrueConcept_shouldReturnProperTrueConcept() throws Exception {
		Concept trueConceptLoadedManually = Context.getConceptService().getConcept(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT));
		Concept trueConceptLoadedByServiceMethod = Context.getConceptService().getTrueConcept();
		Assert.assertTrue(trueConceptLoadedManually.equals(trueConceptLoadedByServiceMethod));
	}
	
	/**
	 * @see ConceptService@getFalseConcept()
	 * @verifies should return proper false concept
	 */
	@Test
	public void getFalseConcept_shouldReturnProperFalseConcept() throws Exception {
		Concept falseConceptLoadedManually = Context.getConceptService().getConcept(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT));
		Concept falseConceptLoadedByServiceMethod = Context.getConceptService().getFalseConcept();
		Assert.assertTrue(falseConceptLoadedManually.equals(falseConceptLoadedByServiceMethod));
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should not set audit info if the concept is not edited", method = "saveConcept(Concept)")
	public void saveConcept_shouldNotSetAuditInfoIfTheConceptIsNotEdited() throws Exception {
		Concept concept = conceptService.getConcept(3);
		Assert.assertNull(concept.getDateChanged());
		Assert.assertNull(concept.getChangedBy());
		conceptService.saveConcept(concept);
		
		Assert.assertNull(concept.getDateChanged());
		Assert.assertNull(concept.getChangedBy());
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should set audit info if an item is removed from any of its child collections", method = "saveConcept(Concept)")
	public void saveConcept_shouldSetAuditInfoIfAnItemIsRemovedFromAnyOfItsChildCollections() throws Exception {
		Concept concept = conceptService.getConcept(3);
		Assert.assertNull(concept.getDateChanged());
		Assert.assertNull(concept.getChangedBy());
		
		ConceptDescription description = concept.getDescription();
		Assert.assertNotNull(description);
		Assert.assertTrue(concept.removeDescription(description));
		conceptService.saveConcept(concept);
		
		Assert.assertNotNull(concept.getDateChanged());
		Assert.assertNotNull(concept.getChangedBy());
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should set audit info if any item in the child collections is edited", method = "saveConcept(Concept)")
	public void saveConcept_shouldSetAuditInfoIfAnyItemInTheChildCollectionsIsEdited() throws Exception {
		Concept concept = conceptService.getConcept(3);
		Assert.assertNull(concept.getDateChanged());
		Assert.assertNull(concept.getChangedBy());
		
		ConceptDescription description = concept.getDescription();
		Assert.assertNotNull(description);
		description.setDescription("changed to something else");
		conceptService.saveConcept(concept);
		
		Assert.assertNotNull(concept.getDateChanged());
		Assert.assertNotNull(concept.getChangedBy());
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should set audit info if an item is added to any of its child collections", method = "saveConcept(Concept)")
	public void saveConcept_shouldSetAuditInfoIfAnItemIsAddedToAnyOfItsChildCollections() throws Exception {
		Concept concept = conceptService.getConcept(3);
		Assert.assertNull(concept.getDateChanged());
		Assert.assertNull(concept.getChangedBy());
		
		ConceptDescription description = new ConceptDescription("new description", Context.getLocale());
		concept.addDescription(description);
		conceptService.saveConcept(concept);
		Assert.assertNotNull(description.getConceptDescriptionId());
		
		Assert.assertNotNull(concept.getDateChanged());
		Assert.assertNotNull(concept.getChangedBy());
	}
	
	/**
	 * @see ConceptService#getDrugsByIngredient(Concept)
	 * @verifies return drugs matched by drug concept
	 */
	@Test
	public void getDrugsByIngredient_shouldReturnDrugsMatchedByDrugConcept() throws Exception {
		List<Drug> drugs = conceptService.getDrugsByIngredient(new Concept(792));
		assertEquals(1, drugs.size());
	}
	
	/**
	 * @see ConceptService#getDrugsByIngredient(Concept)
	 * @verifies return drugs matched by intermediate concept
	 */
	@Test
	public void getDrugsByIngredient_shouldReturnDrugsMatchedByIntermediateConcept() throws Exception {
		List<Drug> drugs = conceptService.getDrugsByIngredient(new Concept(88));
		assertEquals(2, drugs.size());
	}
	
	/**
	 * @see ConceptService#getDrugsByIngredient(Concept)
	 * @verifies return empty list if nothing found
	 */
	@Test
	public void getDrugsByIngredient_shouldReturnEmptyListIfNothingFound() throws Exception {
		List<Drug> drugs = conceptService.getDrugsByIngredient(new Concept(18));
		assertEquals(0, drugs.size());
	}
	
	/**
	 * @see {@link ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Test
	@Verifies(value = "should return a search result whose concept name contains all word tokens as first", method = "getConcepts(String,List<QLocale;>,null,List<QConceptClass;>,List<QConceptClass;>,List<QConceptDatatype;>,List<QConceptDatatype;>,Concept,Integer,Integer)")
	public void getConcepts_shouldReturnASearchResultWhoseConceptNameContainsAllWordTokensAsFirst() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("SALBUTAMOL INHALER", Collections
		        .singletonList(new Locale("en", "US")), false, null, null, null, null, null, null, null);
		
		assertThat(searchResults.get(0).getWord(), is("SALBUTAMOL INHALER"));
	}
	
	/**
	 * @see {@link ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Test
	@Verifies(value = "should return a search result for phrase with stop words", method = "getConcepts(String,List<QLocale;>,null,List<QConceptClass;>,List<QConceptClass;>,List<QConceptDatatype;>,List<QConceptDatatype;>,Concept,Integer,Integer)")
	public void getConcepts_shouldReturnASearchResultForPhraseWithStopWords() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		conceptService.saveConceptStopWord(new ConceptStopWord("OF", Locale.US));
		
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("tuberculosis of knee", Collections
		        .singletonList(new Locale("en", "US")), false, null, null, null, null, null, null, null);
		
		Assert.assertEquals(1, searchResults.size());
		Assert.assertEquals("Tuberculosis of Knee", searchResults.get(0).getConceptName().getName());
	}
	
	/**
	 * @see {@link ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Test
	@Verifies(value = "should return concepts with specified classes", method = "getConcepts(String,List<QLocale;>,boolean,List<QConceptClass;>,List<QConceptClass;>,List<QConceptDatatype;>,List<QConceptDatatype;>,Concept,Integer,Integer)")
	public void getConcepts_shouldReturnConceptsWithSpecifiedClasses() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptClass> classes = new ArrayList<ConceptClass>();
		classes.add(Context.getConceptService().getConceptClassByName("Finding"));
		classes.add(Context.getConceptService().getConceptClassByName("LabSet"));
		List<ConceptSearchResult> searchResults = conceptService.getConcepts(null, null, false, classes, null, null, null,
		    null, null, null);
		Assert.assertEquals(2, searchResults.size());
	}
	
	/**
	 * @see {@link ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Test
	@Verifies(value = "should include retired concepts in the search results", method = "getConcepts(String,List<QLocale;>,boolean,List<QConceptClass;>,List<QConceptClass;>,List<QConceptDatatype;>,List<QConceptDatatype;>,Concept,Integer,Integer)")
	public void getConcepts_shouldIncludeRetiredConceptsInTheSearchResults() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptClass> classes = new ArrayList<ConceptClass>();
		classes.add(Context.getConceptService().getConceptClassByName("Finding"));
		List<ConceptSearchResult> searchResults = conceptService.getConcepts(null, null, true, classes, null, null, null,
		    null, null, null);
		Assert.assertEquals(2, searchResults.size());
	}
	
	/**
	 * @see {@link ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)}
	 */
	@Test
	@Verifies(value = "should exclude specified classes from the search results", method = "getConcepts(String,List<QLocale;>,boolean,List<QConceptClass;>,List<QConceptClass;>,List<QConceptDatatype;>,List<QConceptDatatype;>,Concept,Integer,Integer)")
	public void getConcepts_shouldExcludeSpecifiedClassesFromTheSearchResults() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptClass> classes = new ArrayList<ConceptClass>();
		classes.add(Context.getConceptService().getConceptClassByName("Finding"));
		classes.add(Context.getConceptService().getConceptClassByName("LabSet"));
		List<ConceptClass> excludeClasses = new ArrayList<ConceptClass>();
		excludeClasses.add(Context.getConceptService().getConceptClassByName("Finding"));
		List<ConceptSearchResult> searchResults = conceptService.getConcepts(null, null, false, classes, excludeClasses,
		    null, null, null, null, null);
		Assert.assertEquals(1, searchResults.size());
	}
	
	/**
	 * @see {@link ConceptService#getConcepts(String,List<Locale>,null,List<ConceptClass>,List<
	 *      ConceptClass>,List<ConceptDatatype>,List<ConceptDatatype>,Concept,Integer,Integer)}
	 */
	@Test
	@Verifies(value = "should not return concepts with matching names that are voided", method = "getConcepts(String,List<Locale>,null,List<ConceptClass>,List<ConceptClass>,List<ConceptDatatype>,List<ConceptDatatype>,Concept,Integer,Integer)")
	public void getConcepts_shouldNotReturnConceptsWithMatchingNamesThatAreVoided() throws Exception {
		Concept concept = conceptService.getConcept(7);
		
		List<ConceptSearchResult> results = conceptService.getConcepts("VOIDED", Collections.singletonList(Locale.ENGLISH),
		    false, null, null, null, null, null, null, null);
		
		for (ConceptSearchResult result : results) {
			assertThat(result.getConcept(), not(concept));
		}
	}
	
	/**
	 * @see ConceptServiceImpl#getConcepts(String phrase, List<Locale> locales, boolean
	 *      includeRetired,List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	 *      List<ConceptDatatype> requireDatatypes,List<ConceptDatatype> excludeDatatypes, Concept
	 *      answersToConcept, Integer start, Integer size)
	 */
	@Test
	@Verifies(value = "should not fail with null Classes and Datatypes", method = "getConcepts(String phrase, List<Locale> locales, boolean includeRetired,List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,List<ConceptDatatype> excludeDatatypes, Concept answersToConcept, Integer start, Integer size)")
	public void getConcepts_shouldNotFailWithNullClassesAndDatatypes() throws Exception {
		ConceptService conceptService = Context.getConceptService();
		Assert.assertNotNull(conceptService.getConcepts("VOIDED", Collections.singletonList(Locale.ENGLISH), false, null,
		    null, null, null, null, null, null));
	}
	
	/**
	 * @see ConceptServiceImpl# getCountOfConcepts(String phrase, List<Locale> locales, boolean
	 *      includeRetired,List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	 *      List<ConceptDatatype> requireDatatypes,List<ConceptDatatype> excludeDatatypes, Concept
	 *      answersToConcept)
	 */
	@Test
	@Verifies(value = "should not fail with null Classes and Datatypes", method = "getCountOfConcepts(String phrase, List<Locale> locales, boolean includeRetired,List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses, List<ConceptDatatype> requireDatatypes,List<ConceptDatatype> excludeDatatypes, Concept answersToConcept)")
	public void getCountOfConcepts_shouldNotFailWithNullClassesAndDatatypes() throws Exception {
		ConceptService conceptService = Context.getConceptService();
		Assert.assertNotNull(conceptService.getCountOfConcepts("VOIDED", Collections.singletonList(Locale.ENGLISH), false,
		    null, null, null, null, null));
	}
	
	/**
	 * @see {@link ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept,Locale)}
	 */
	@Test
	@Verifies(value = "should not set value coded name when add concept is selected", method = "mapConceptProposalToConcept(ConceptProposal,Concept,Locale)")
	public void mapConceptProposalToConcept_shouldNotSetValueCodedNameWhenAddConceptIsSelected() throws Exception {
		ConceptProposal cp = conceptService.getConceptProposal(2);
		Assert.assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED, cp.getState());
		final Concept civilStatusConcept = conceptService.getConcept(4);
		final int mappedConceptId = 6;
		Assert.assertTrue(Context.getObsService().getObservationsByPersonAndConcept(cp.getEncounter().getPatient(),
		    civilStatusConcept).isEmpty());
		Concept mappedConcept = conceptService.getConcept(mappedConceptId);
		
		cp.setObsConcept(civilStatusConcept);
		cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT);
		conceptService.mapConceptProposalToConcept(cp, mappedConcept, null);
		mappedConcept = conceptService.getConcept(mappedConceptId);
		List<Obs> observations = Context.getObsService().getObservationsByPersonAndConcept(cp.getEncounter().getPatient(),
		    civilStatusConcept);
		Assert.assertEquals(1, observations.size());
		Obs obs = observations.get(0);
		Assert.assertNull(obs.getValueCodedName());
	}
	
	/**
	 * @see {@link ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept,Locale)}
	 */
	@Test
	@Verifies(value = "should set value coded name when add synonym is selected", method = "mapConceptProposalToConcept(ConceptProposal,Concept,Locale)")
	public void mapConceptProposalToConcept_shouldSetValueCodedNameWhenAddSynonymIsSelected() throws Exception {
		ConceptProposal cp = conceptService.getConceptProposal(2);
		Assert.assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED, cp.getState());
		final Concept civilStatusConcept = conceptService.getConcept(4);
		final int mappedConceptId = 6;
		final String finalText = "Weight synonym";
		Assert.assertTrue(Context.getObsService().getObservationsByPersonAndConcept(cp.getEncounter().getPatient(),
		    civilStatusConcept).isEmpty());
		Concept mappedConcept = conceptService.getConcept(mappedConceptId);
		
		cp.setFinalText(finalText);
		cp.setObsConcept(civilStatusConcept);
		cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM);
		conceptService.mapConceptProposalToConcept(cp, mappedConcept, null);
		mappedConcept = conceptService.getConcept(mappedConceptId);
		List<Obs> observations = Context.getObsService().getObservationsByPersonAndConcept(cp.getEncounter().getPatient(),
		    civilStatusConcept);
		Assert.assertEquals(1, observations.size());
		Obs obs = observations.get(0);
		Assert.assertNotNull(obs.getValueCodedName());
		Assert.assertEquals(finalText, obs.getValueCodedName().getName());
	}
	
	/**
	 * @see {@link ConceptService#getAllConcepts(String,null,null)}
	 */
	@Test
	@Verifies(value = "should exclude retired concepts when set includeRetired to false", method = "getAllConcepts(String,null,null)")
	public void getAllConcepts_shouldExcludeRetiredConceptsWhenSetIncludeRetiredToFalse() throws Exception {
		final List<Concept> allConcepts = conceptService.getAllConcepts(null, true, false);
		
		assertEquals(34, allConcepts.size());
		assertEquals(3, allConcepts.get(0).getConceptId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getAllConcepts(String,null,null)}
	 */
	@Test
	@Verifies(value = "should order by a concept field", method = "getAllConcepts(String,null,null)")
	public void getAllConcepts_shouldOrderByAConceptField() throws Exception {
		List<Concept> allConcepts = conceptService.getAllConcepts("dateCreated", true, true);
		
		assertEquals(36, allConcepts.size());
		assertEquals(88, allConcepts.get(0).getConceptId().intValue());
		assertEquals(27, allConcepts.get(allConcepts.size() - 1).getConceptId().intValue());
		
		//check desc order
		allConcepts = conceptService.getAllConcepts("dateCreated", false, true);
		
		assertEquals(36, allConcepts.size());
		assertEquals(23, allConcepts.get(0).getConceptId().intValue());
		assertEquals(88, allConcepts.get(allConcepts.size() - 1).getConceptId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getAllConcepts(String,null,null)}
	 */
	@Test
	@Verifies(value = "should order by a concept name field", method = "getAllConcepts(String,null,null)")
	public void getAllConcepts_shouldOrderByAConceptNameField() throws Exception {
		List<Concept> allConcepts = conceptService.getAllConcepts("name", true, false);
		
		assertEquals(34, allConcepts.size());
		assertEquals("ANTIRETROVIRAL TREATMENT GROUP", allConcepts.get(0).getName().getName());
		assertEquals("tab (s)", allConcepts.get(allConcepts.size() - 1).getName().getName());
		
		//test the desc order
		allConcepts = conceptService.getAllConcepts("name", false, false);
		
		assertEquals(34, allConcepts.size());
		assertEquals("tab (s)", allConcepts.get(0).getName().getName());
		assertEquals("ANTIRETROVIRAL TREATMENT GROUP", allConcepts.get(allConcepts.size() - 1).getName().getName());
	}
	
	/**
	 * @see {@link ConceptService#getAllConcepts(String,null,null)}
	 */
	@Test
	@Verifies(value = "should order by concept id and include retired when given no parameters", method = "getAllConcepts(String,null,null)")
	public void getAllConcepts_shouldOrderByConceptIdAndIncludeRetiredWhenGivenNoParameters() throws Exception {
		final List<Concept> allConcepts = conceptService.getAllConcepts();
		
		assertEquals(36, allConcepts.size());
		assertEquals(3, allConcepts.get(0).getConceptId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#getAllConcepts(String,null,null)}
	 */
	@Test
	@Verifies(value = "should order by concept id descending when set asc parameter to false", method = "getAllConcepts(String,null,null)")
	public void getAllConcepts_shouldOrderByConceptIdDescendingWhenSetAscParameterToFalse() throws Exception {
		final List<Concept> allConcepts = conceptService.getAllConcepts(null, false, true);
		
		assertEquals(36, allConcepts.size());
		assertEquals(5497, allConcepts.get(0).getConceptId().intValue());
	}
	
	/**
	 * @see {@link ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept,Locale)}
	 */
	@Test(expected = DuplicateConceptNameException.class)
	@Verifies(value = "should fail when adding a duplicate syonymn", method = "mapConceptProposalToConcept(ConceptProposal,Concept,Locale)")
	public void mapConceptProposalToConcept_shouldFailWhenAddingADuplicateSyonymn() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-proposals.xml");
		ConceptService cs = Context.getConceptService();
		ConceptProposal cp = cs.getConceptProposal(10);
		cp.setFinalText(cp.getOriginalText());
		cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM);
		Concept mappedConcept = cs.getConcept(5);
		Locale locale = new Locale("en", "GB");
		Assert.assertTrue(mappedConcept.hasName(cp.getFinalText(), locale));
		
		cs.mapConceptProposalToConcept(cp, mappedConcept, locale);
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@Verifies(value = "should pass when saving a concept after removing a name", method = "saveConcept(Concept)")
	public void saveConcept_shouldPassWhenSavingAConceptAfterRemovingAName() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		Concept concept = conceptService.getConcept(3000);
		Assert.assertFalse(concept.getSynonyms().isEmpty());
		concept.removeName(concept.getSynonyms().iterator().next());
		conceptService.saveConcept(concept);
	}
	
	/**
	 * @see {@link ConceptService#saveConceptNameTag(Object,Errors)}
	 */
	@Test(expected = Exception.class)
	@Verifies(value = "not save a concept name tag if tag is null, empty or whitespace", method = "saveConceptNameTag(ConceptNameTag)")
	public void saveConceptNameTag_shouldNotSaveATagIfItIsInvalid() throws Exception {
		ConceptNameTag cnt = new ConceptNameTag();
		ConceptService cs = Context.getConceptService();
		
		ConceptNameTag faultyNameTag = cs.saveConceptNameTag(cnt);
	}
	
	/**
	 * @see {@link ConceptService#saveConceptNameTag(Object,Errors)}
	 */
	@Test
	@Verifies(value = "save a concept name tag if tag is supplied", method = "saveConceptNameTag(ConceptNameTag)")
	public void saveConceptNameTag_shouldSaveATagIfItIsSupplied() throws Exception {
		ConceptNameTag cnt = new ConceptNameTag();
		cnt.setTag("abcd");
		cnt.setDescription("test");
		ConceptService cs = Context.getConceptService();
		
		Integer id = cs.saveConceptNameTag(cnt).getId();
		Context.flushSession();
		Context.clearSession();
		
		ConceptNameTag savedNameTag = cs.getConceptNameTag(id);
		assertEquals(savedNameTag.getTag(), "abcd");
		assertEquals(savedNameTag.getDescription(), "test");
	}
	
	/**
	 * @see {@link ConceptService#saveConceptNameTag(Object,Errors)}
	 */
	@Test
	@Verifies(value = "save an edited concept name tag", method = "saveConceptNameTag(ConceptNameTag)")
	public void saveConceptNameTag_shouldSaveAnEditedNameTag() throws Exception {
		ConceptService cs = Context.getConceptService();
		ConceptNameTag cnt = cs.getConceptNameTag(1);
		cnt.setTag("dcba");
		
		Integer id = cs.saveConceptNameTag(cnt).getId();
		Context.flushSession();
		Context.clearSession();
		
		ConceptNameTag savedNameTag = cs.getConceptNameTag(id);
		assertEquals(savedNameTag.getTag(), "dcba");
	}
	
	/**
	 * @verifies get drugs with names matching the search phrase
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldGetDrugsWithNamesMatchingTheSearchPhrase() throws Exception {
		//Should be case insensitive
		List<Drug> drugs = conceptService.getDrugs("tri", null, false, false);
		assertThat(drugs, contains(conceptService.getDrug(2)));
	}
	
	/**
	 * @verifies include retired drugs if includeRetired is set to true
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldIncludeRetiredDrugsIfIncludeRetiredIsSetToTrue() throws Exception {
		//Should be case insensitive
		final String searchPhrase = "Nyq";
		List<Drug> drugs = conceptService.getDrugs(searchPhrase, null, false, false);
		assertEquals(0, drugs.size());
		
		drugs = conceptService.getDrugs(searchPhrase, null, false, true);
		assertEquals(1, drugs.size());
		assertEquals(11, drugs.get(0).getDrugId().intValue());
	}
	
	/**
	 * @verifies get drugs linked to concepts with names that match the phrase
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldGetDrugsLinkedToConceptsWithNamesThatMatchThePhrase() throws Exception {
		final Integer expectedDrugId = 2;
		List<Drug> drugs = conceptService.getDrugs("stav", null, false, false);
		assertEquals(1, drugs.size());
		assertEquals(expectedDrugId, drugs.get(0).getDrugId());
		
		//should match anywhere in the concept name
		drugs = conceptService.getDrugs("lamiv", null, false, false);
		assertEquals(1, drugs.size());
		assertEquals(expectedDrugId, drugs.get(0).getDrugId());
	}
	
	/**
	 * @verifies get drugs linked to concepts with names that match the phrase and locale
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldGetDrugsLinkedToConceptsWithNamesThatMatchThePhraseAndLocale() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-drugSearch.xml");
		final String searchPhrase = "some";
		List<Drug> drugs = conceptService.getDrugs(searchPhrase, Locale.FRENCH, true, false);
		assertEquals(0, drugs.size());
		
		drugs = conceptService.getDrugs(searchPhrase, Locale.CANADA_FRENCH, true, false);
		assertEquals(1, drugs.size());
		assertEquals(3, drugs.get(0).getDrugId().intValue());
	}
	
	/**
	 * @verifies get drugs linked to concepts with names that match the phrase and related locales
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldGetDrugsLinkedToConceptsWithNamesThatMatchThePhraseAndRelatedLocales() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-drugSearch.xml");
		
		final String searchPhrase = "another";
		//Should look only in the exact locale if exactLocale is set to true
		List<Drug> drugs = conceptService.getDrugs(searchPhrase, Locale.CANADA_FRENCH, true, false);
		assertThat(drugs, is(empty()));
		
		//Should look in broader locale if exactLocale is set to false
		drugs = conceptService.getDrugs(searchPhrase, Locale.CANADA_FRENCH, false, false);
		assertThat(drugs, contains(hasId(3)));
	}
	
	/**
	 * @verifies get drugs that have mappings with reference term codes that match the phrase
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldGetDrugsThatHaveMappingsWithReferenceTermCodesThatMatchThePhrase() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-drugSearch.xml");
		List<Drug> drugs = conceptService.getDrugs("XXX", null, true, true);
		assertThat(drugs, contains(hasId(11), hasId(444)));
	}
	
	/**
	 * Ensures that unique drugs are returned in situations where more than one searched fields
	 * match e.g drug name and linked concept name match the search phrase
	 * 
	 * @verifies return unique drugs
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldReturnUniqueDrugs() throws Exception {
		//sanity check that drug.name and drug.concept.name will both match the search phrase
		Drug drug = conceptService.getDrugByNameOrId("ASPIRIN");
		assertEquals(drug.getName().toLowerCase(), drug.getConcept().getName().getName().toLowerCase());
		
		List<Drug> drugs = conceptService.getDrugs("Asp", null, false, false);
		assertEquals(1, drugs.size());
		assertEquals(3, drugs.get(0).getDrugId().intValue());
	}
	
	/**
	 * @verifies return all drugs with a matching term code or drug name or concept name
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldReturnAllDrugsWithAMatchingTermCodeOrDrugNameOrConceptName() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-drugSearch.xml");
		List<Drug> drugs = conceptService.getDrugs("XXX", null, false, true);
		assertThat(drugs, containsInAnyOrder(conceptService.getDrug(3), conceptService.getDrug(11), conceptService
		        .getDrug(444)));
	}
	
	/**
	 * @verifies reject a null search phrase
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getDrugs_shouldRejectANullSearchPhrase() throws Exception {
		conceptService.getDrugs(null, null, false, false);
	}
	
	/**
	 * @verifies get a list of all drugs that match on all the parameter values
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldGetAListOfAllDrugsThatMatchOnAllTheParameterValues() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<ConceptMapType> conceptMapTypeList = new ArrayList<ConceptMapType>();
		conceptMapTypeList.add(conceptService.getConceptMapType(1));
		ConceptSource source = conceptService.getConceptSource(1);
		List<Drug> drugs = conceptService.getDrugsByMapping("WGT234", source, conceptMapTypeList, false);
		assertEquals(1, drugs.size());
		assertTrue(containsId(drugs, 2));
	}
	
	/**
	 * @verifies exclude duplicate matches
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldExcludeDuplicateMatches() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<ConceptMapType> conceptMapTypeList = conceptService.getConceptMapTypes(false, true);
		//the expected matching drug has two mappings to different concept sources but same code
		//so this test also ensure that we can never get back duplicates
		ConceptSource source = conceptService.getConceptSource(1);
		List<Drug> drugs = conceptService.getDrugsByMapping("WGT234", source, conceptMapTypeList, false);
		assertEquals(1, drugs.size());
		assertTrue(containsId(drugs, 2));
	}
	
	/**
	 * @verifies return retired and non-retired drugs if includeRetired is set to true
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldReturnRetiredAndNonretiredDrugsIfIncludeRetiredIsSetToTrue() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<ConceptMapType> conceptMapTypeList = conceptService.getConceptMapTypes(false, true);
		List<Drug> drugs = conceptService.getDrugsByMapping("WGT234", conceptService.getConceptSource(1),
		    conceptMapTypeList, true);
		assertEquals(2, drugs.size());
		assertTrue(containsId(drugs, 2));
		assertTrue(containsId(drugs, 11));
	}
	
	/**
	 * @verifies return empty list if no matches are found
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldReturnEmptyListIfNoMatchesAreFound() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<ConceptMapType> conceptMapTypeList = conceptService.getConceptMapTypes(false, true);
		List<Drug> drugs = conceptService.getDrugsByMapping("some radom code", conceptService.getConceptSource(2),
		    conceptMapTypeList, false);
		assertTrue(drugs.isEmpty());
	}
	
	/**
	 * @verifies match on the code
	 * @see ConceptService#getDrugsByMapping(String, ConceptSource, Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldMatchOnTheCode() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		ConceptSource source = conceptService.getConceptSource(1);
		List<Drug> drugs = conceptService.getDrugsByMapping("WGT234", source, null, false);
		assertEquals(1, drugs.size());
		assertTrue(containsId(drugs, 2));
	}
	
	/**
	 * @verifies match on the concept source
	 * @see ConceptService#getDrugsByMapping(String, ConceptSource, Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldMatchOnTheConceptSource() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<Drug> drugs = conceptService.getDrugsByMapping(null, conceptService.getConceptSource(2), null, false);
		assertEquals(1, drugs.size());
		assertTrue(containsId(drugs, 2));
	}
	
	/**
	 * @verifies match on the map types
	 * @see ConceptService#getDrugsByMapping(String, ConceptSource, Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldMatchOnTheMapTypes() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<ConceptMapType> conceptMapTypeList = conceptService.getConceptMapTypes(false, true);
		ConceptSource source = conceptService.getConceptSource(1);
		List<Drug> drugs = conceptService.getDrugsByMapping(null, source, conceptMapTypeList, false);
		assertEquals(2, drugs.size());
		assertTrue(containsId(drugs, 2));
		assertTrue(containsId(drugs, 3));
		
		drugs = conceptService.getDrugsByMapping(null, source, conceptMapTypeList, true);
		assertEquals(3, drugs.size());
		assertTrue(containsId(drugs, 2));
		assertTrue(containsId(drugs, 3));
		assertTrue(containsId(drugs, 11));
	}
	
	/**
	 * @verifies fail if no code and concept source and withAnyOfTheseTypes are provided
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test(expected = APIException.class)
	public void getDrugsByMapping_shouldFailIfNoCodeAndConceptSourceAndWithAnyOfTheseTypesAreProvided() throws Exception {
		conceptService.getDrugByMapping(null, null, null);
	}
	
	/**
	 * @verifies fail if source is null
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldFailIfSourceIsNull() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage("ConceptSource.is.required");
		conceptService.getDrugsByMapping("random", null, null, false);
	}
	
	/**
	 * @verifies return a drug that matches the code and source and the best map type
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test
	public void getDrugByMapping_shouldReturnADrugThatMatchesTheCodeAndSourceAndTheBestMapType() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		final Integer expectedDrugId = 2;
		final ConceptSource source = conceptService.getConceptSource(2);
		final ConceptMapType mapTypeWithMatch = conceptService.getConceptMapType(1);
		final ConceptMapType mapTypeWithNoMatch = conceptService.getConceptMapType(2);
		List<ConceptMapType> conceptMapTypeList = new ArrayList<ConceptMapType>();
		conceptMapTypeList.add(mapTypeWithMatch);
		conceptMapTypeList.add(mapTypeWithNoMatch);
		Drug drug = conceptService.getDrugByMapping("WGT234", source, conceptMapTypeList);
		assertEquals(expectedDrugId, drug.getDrugId());
		
		//Lets switch the order is the map types in the list to make sure that
		//if there is no match on the first map type, the logic matches on the second
		//sanity check that actually there will be no match on the first map type in the list
		conceptMapTypeList.clear();
		conceptMapTypeList.add(mapTypeWithNoMatch);
		assertNull(conceptService.getDrugByMapping("WGT234", source, conceptMapTypeList));
		
		conceptMapTypeList.add(mapTypeWithMatch);
		drug = conceptService.getDrugByMapping("WGT234", source, conceptMapTypeList);
		assertEquals(expectedDrugId, drug.getDrugId());
	}
	
	/**
	 * @verifies fail if multiple drugs are found matching the best map type
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test(expected = DAOException.class)
	public void getDrugByMapping_shouldFailIfMultipleDrugsAreFoundMatchingTheBestMapType() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		ConceptSource source = conceptService.getConceptSource(1);
		conceptService.getDrugByMapping("CD41003", source, Collections.singleton(conceptService.getConceptMapType(2)));
	}
	
	/**
	 * @verifies return null if no match found
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test
	public void getDrugByMapping_shouldReturnNullIfNoMatchFound() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<ConceptMapType> conceptMapTypeList = conceptService.getConceptMapTypes(false, true);
		Drug drug = conceptService.getDrugByMapping("random code", conceptService.getConceptSource(1), conceptMapTypeList);
		assertNull(drug);
	}
	
	/**
	 * @verifies fail if no code and concept source and withAnyOfTheseTypes are provided
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test(expected = APIException.class)
	public void getDrugByMapping_shouldFailIfNoCodeAndConceptSourceAndWithAnyOfTheseTypesAreProvided() throws Exception {
		conceptService.getDrugByMapping(null, null, Collections.EMPTY_LIST);
	}
	
	/**
	 * @verifies return a drug that matches the code and source
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test
	public void getDrugByMapping_shouldReturnADrugThatMatchesTheCodeAndSource() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
		final Integer expectedDrugId = 2;
		Drug drug = conceptService.getDrugByMapping("WGT234", conceptService.getConceptSource(2), null);
		assertEquals(expectedDrugId, drug.getDrugId());
	}
	
	/**
	 * @verifies fail if source is null
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test
	public void getDrugByMapping_shouldFailIfSourceIsNull() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage("ConceptSource.is.required");
		conceptService.getDrugByMapping("random", null, null);
	}
	
	/**
	 * @verifies get orderable concepts
	 * @see ConceptService#getOrderableConcepts(String, java.util.List, boolean, Integer, Integer)
	 */
	@Test
	public void getOrderableConcepts_shouldGetOrderableConcepts() throws Exception {
		//In current data set order_type_map table contains conceptClass 1 and 3.
		// Using that adding two concepts to test the functionality
		ConceptService cs = Context.getConceptService();
		ConceptClass cc1 = cs.getConceptClass(1);
		ConceptClass cc3 = cs.getConceptClass(3);
		Locale locale = Locale.ENGLISH;
		ConceptDatatype dt = cs.getConceptDatatype(4);
		Concept c1 = new Concept();
		ConceptName cn1a = new ConceptName("ONE TERM", locale);
		c1.addName(cn1a);
		c1.setConceptClass(cc1);
		c1.setDatatype(dt);
		cs.saveConcept(c1);
		
		Concept c2 = new Concept();
		ConceptName cn2a = new ConceptName("ONE TO MANY", locale);
		c2.addName(cn2a);
		c2.setConceptClass(cc3);
		c2.setDatatype(dt);
		cs.saveConcept(c2);
		
		updateSearchIndex();
		
		List<ConceptSearchResult> conceptSearchResultList = Context.getConceptService().getOrderableConcepts("one",
		    Collections.singletonList(locale), true, null, null);
		assertEquals(2, conceptSearchResultList.size());
	}
	
	/**
	 * @see ConceptService#getConcepts(String,List,boolean,List,List,List,List,Concept,Integer,Integer)
	 * @verifies return preferred names higher
	 */
	@Test
	public void getConcepts_shouldReturnPreferredNamesHigher() throws Exception {
		Concept hivProgram = conceptService.getConceptByName("hiv program");
		ConceptName synonym = new ConceptName("synonym", Context.getLocale());
		hivProgram.addName(synonym);
		conceptService.saveConcept(hivProgram);
		
		Concept mdrTbProgram = conceptService.getConceptByName("mdr-tb program");
		synonym = new ConceptName("synonym", Context.getLocale());
		synonym.setLocalePreferred(true);
		mdrTbProgram.addName(synonym);
		conceptService.saveConcept(mdrTbProgram);
		
		updateSearchIndex();
		
		List<ConceptSearchResult> concepts = conceptService.getConcepts("synonym", null, false, null, null, null, null,
		    null, null, null);
		
		assertThat(concepts, contains(hasConcept(is(mdrTbProgram)), hasConcept(is(hivProgram))));
	}
	
	/**
	 * @see ConceptService#getConcepts(String,List,boolean,List,List,List,List,Concept,Integer,Integer)
	 * @verifies find concept by full code
	 */
	@Test
	public void getConcepts_shouldFindConceptByFullCode() throws Exception {
		//given
		String code1 = "CD41003";
		String code2 = "7345693";
		Concept concept = conceptService.getConceptByMapping(code2, "SNOMED CT");
		
		//when
		List<ConceptSearchResult> concepts1 = conceptService.getConcepts(code1, Arrays.asList(Context.getLocale()), false,
		    null, null, null, null, null, null, null);
		List<ConceptSearchResult> concepts2 = conceptService.getConcepts(code2, Arrays.asList(Context.getLocale()), false,
		    null, null, null, null, null, null, null);
		
		//then
		assertThat(concepts1, contains(hasConcept(is(concept))));
		assertThat(concepts2, contains(hasConcept(is(concept))));
	}
}

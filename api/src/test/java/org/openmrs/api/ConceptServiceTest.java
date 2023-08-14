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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.openmrs.api.context.Context.getObsService;
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

import net.sf.ehcache.Ehcache;
import org.apache.commons.collections.CollectionUtils;
import org.dbunit.dataset.IDataSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptAttributeType;
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
import org.openmrs.DrugIngredient;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.ConceptMapTypeComparator;
import org.openmrs.util.DateUtil;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.validation.Errors;

/**
 * This test class (should) contain tests for all of the ConceptService methods TODO clean up and
 * finish this test class
 * 
 * @see org.openmrs.api.ConceptService
 */
public class ConceptServiceTest extends BaseContextSensitiveTest {
	
	protected ConceptService conceptService = null;

	protected static final String INITIAL_CONCEPTS_XML = "org/openmrs/api/include/ConceptServiceTest-initialConcepts.xml";
	
	protected static final String GET_CONCEPTS_BY_SET_XML = "org/openmrs/api/include/ConceptServiceTest-getConceptsBySet.xml";
	
	protected static final String GET_DRUG_MAPPINGS = "org/openmrs/api/include/ConceptServiceTest-getDrugMappings.xml";

	protected static final String CONCEPT_ATTRIBUTE_TYPE_XML = "org/openmrs/api/include/ConceptServiceTest-conceptAttributeType.xml";

	@Autowired
	CacheManager cacheManager;

	// For testing concept lookups by static constant
	private static final String TEST_CONCEPT_CONSTANT_ID = "3";
 
	private static final String TEST_CONCEPT_CONSTANT_UUID = "35d3346a-6769-4d52-823f-b4b234bac3e3";
	
	private static final String TEST_CONCEPT_CONSTANT_NAME = "COUGH SYRUP";
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeAllTests() {
		conceptService = Context.getConceptService();
	}
	
	@AfterEach
	public void revertToDefaultLocale() {
		Context.setLocale(Locale.US);
	}
	
	/**
	 * Updates the search index to clean up after each test.
	 * 
	 * @see org.openmrs.test.BaseContextSensitiveTest#updateSearchIndex()
	 */
	@BeforeEach
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
	public void executeDataSet(IDataSet dataset) {
		super.executeDataSet(dataset);
		
		updateSearchIndex();
	}
	
	/**
	 * @see ConceptService#getConceptByName(String)
	 */
	@Test
	public void getConceptByName_shouldGetConceptByName() {
		
		String nameToFetch = "Some non numeric concept name";
		
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Concept conceptByName = conceptService.getConceptByName(nameToFetch);
		assertEquals(1, conceptByName.getId().intValue(), "Unable to fetch concept by name");
	}
	
	/**
	 * @see ConceptService#getConceptByName(String)
	 */
	@Test
	public void getConceptByName_shouldGetConceptByPartialName() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// substring of the name
		String partialNameToFetch = "Some";
		
		List<Concept> firstConceptsByPartialNameList = conceptService.getConceptsByName(partialNameToFetch);
		assertThat(firstConceptsByPartialNameList, containsInAnyOrder(hasId(1), hasId(2)));
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldSaveAConceptNumericAsAConcept() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		//This will automatically add the given locale to the list of allowed locales
		Context.setLocale(Locale.US);
		// this tests saving a previously conceptnumeric as just a concept
		Concept c2 = new Concept(2);
		ConceptName cn = new ConceptName("not a numeric anymore", Locale.US);
		c2.addName(cn);
		c2.addDescription(new ConceptDescription("some description",null));
		c2.setDatatype(new ConceptDatatype(3));
		c2.setConceptClass(new ConceptClass(1));
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
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldSaveANewConceptNumeric() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		Context.setLocale(Locale.US);
		// this tests saving a never before in the database conceptnumeric
		ConceptNumeric cn3 = new ConceptNumeric();
		cn3.setDatatype(new ConceptDatatype(1));
		cn3.setConceptClass(new ConceptClass(1));

		ConceptName cn = new ConceptName("a brand new conceptnumeric", Locale.US);
		cn3.addName(cn);
		cn3.addDescription(new ConceptDescription("some description",null));
		cn3.setHiAbsolute(50.0);
		conceptService.saveConcept(cn3);
		
		Concept thirdConcept = conceptService.getConcept(cn3.getConceptId());
		assertTrue(thirdConcept instanceof ConceptNumeric);
		ConceptNumeric thirdConceptNumeric = (ConceptNumeric) thirdConcept;
		assertEquals("a brand new conceptnumeric", thirdConceptNumeric.getName(Locale.US).getName());
		assertEquals(50.0, thirdConceptNumeric.getHiAbsolute(), 0);
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldSaveNonConceptNumericObjectAsConceptNumeric() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// this tests saving a current concept as a newly changed conceptnumeric
		// assumes there is already a concept in the database
		// with a concept id of #1
		ConceptNumeric cn = new ConceptNumeric(1);
		cn.setDatatype(new ConceptDatatype(1));
		cn.setConceptClass(new ConceptClass(1));
		cn.addName(new ConceptName("a new conceptnumeric", Locale.US));
		cn.addDescription(new ConceptDescription("some description",null));
		cn.setHiAbsolute(20.0);
		conceptService.saveConcept(cn);
		
		Concept firstConcept = conceptService.getConceptNumeric(1);
		firstConcept.addDescription(new ConceptDescription("some description",null));
		assertEquals("a new conceptnumeric", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptNumeric);
		ConceptNumeric firstConceptNumeric = (ConceptNumeric) firstConcept;
		assertEquals(20.0, firstConceptNumeric.getHiAbsolute(), 0);
		
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldSaveNonConceptComplexObjectAsConceptComplex() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// this tests saving a current concept as a newly changed conceptComplex
		// assumes there is already a concept in the database
		// with a concept id of #1
		ConceptComplex cn = new ConceptComplex(1);
		cn.setDatatype(new ConceptDatatype(13));
		cn.setConceptClass(new ConceptClass(1));
		cn.addName(new ConceptName("a new conceptComplex", Locale.US));
		cn.addDescription(new ConceptDescription("some description",null));
		cn.setHandler("SomeHandler");
		conceptService.saveConcept(cn);
		
		Concept firstConcept = conceptService.getConceptComplex(1);
		assertEquals("a new conceptComplex", firstConcept.getName(Locale.US).getName());
		assertTrue(firstConcept instanceof ConceptComplex);
		ConceptComplex firstConceptComplex = (ConceptComplex) firstConcept;
		assertEquals("SomeHandler", firstConceptComplex.getHandler());
		
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldSaveChangesBetweenConceptNumericAndComplex() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		//save a concept numeric
		ConceptNumeric cn = new ConceptNumeric(1);
		cn.setDatatype(new ConceptDatatype(1));
		cn.setConceptClass(new ConceptClass(1));
		cn.addName(new ConceptName("a new conceptnumeric", Locale.US));
		cn.addDescription(new ConceptDescription("some description",null));
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
		cn2.setConceptClass(new ConceptClass(1));
		cn2.addName(new ConceptName("a new conceptComplex", Locale.US));
		cn2.addDescription(new ConceptDescription("some description",null));
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
		cn.setConceptClass(new ConceptClass(1));
		cn.addName(new ConceptName("a new conceptnumeric", Locale.US));
		cn.addDescription(new ConceptDescription("some description",null));
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
		cn2.setConceptClass(new ConceptClass(1));
		cn2.addName(new ConceptName("a new conceptComplex", Locale.US));
		cn2.addDescription(new ConceptDescription("some description",null));
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
	 * @see ConceptService#getConceptComplex(Integer)
	 */
	@Test
	public void getConceptComplex_shouldReturnAConceptComplexObject() {
		executeDataSet("org/openmrs/api/include/ObsServiceTest-complex.xml");
		ConceptComplex concept = Context.getConceptService().getConceptComplex(8473);
		assertNotNull(concept);
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldGenerateIdForNewConceptIfNoneIsSpecified() {
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("Weight", Context.getLocale());
		concept.addName(cn);
		concept.addDescription(new ConceptDescription("some description",null));
		
		concept.setConceptId(null);
		concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Numeric"));
		concept.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		concept = Context.getConceptService().saveConcept(concept);
		assertFalse(concept.getConceptId().equals(5089));
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldKeepIdForNewConceptIfOneIsSpecified() {
		Integer conceptId = 343434; // a nonexistent concept id;
		assertNull(conceptService.getConcept(conceptId)); // sanity check
		
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("Weight", Context.getLocale());
		concept.addName(cn);
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setConceptId(conceptId);
		concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Numeric"));
		concept.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		concept = Context.getConceptService().saveConcept(concept);
		assertTrue(concept.getConceptId().equals(conceptId));
	}
	
	/**
	 * @see ConceptService#conceptIterator()
	 */
	@Test
	public void conceptIterator_shouldIterateOverAllConcepts() {
		Iterator<Concept> iterator = Context.getConceptService().conceptIterator();
		
		assertTrue(iterator.hasNext());
		assertEquals(3, iterator.next().getConceptId().intValue());
	}
	
	/**
	 * This test will fail if it takes more than 15 seconds to run. (Checks for an error with the
	 * iterator looping forever) The @Timed annotation is used as an alternative to
	 * "@Test(timeout=15000)" so that the Spring transactions work correctly. Junit has a "feature"
	 * where it executes the befores/afters in a thread separate from the one that the actual test
	 * ends up running in when timed.
	 * 
	 * @see ConceptService#conceptIterator()
	 */
	@Test()
	public void conceptIterator_shouldStartWithTheSmallestConceptId() {
		List<Concept> allConcepts = Context.getConceptService().getAllConcepts();
		int numberofconcepts = allConcepts.size();
		
		// sanity check
		assertTrue(numberofconcepts > 0);
		
		// now count up the number of concepts the iterator returns
		int iteratorCount = 0;
		Iterator<Concept> iterator = Context.getConceptService().conceptIterator();
		while (iterator.hasNext() && iteratorCount < numberofconcepts + 5) { // the lt check is in case of infinite loops
			iterator.next();
			iteratorCount++;
		}
		assertEquals(numberofconcepts, iteratorCount);
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldReuseConceptNameTagsThatAlreadyExistInTheDatabase() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-tags.xml");
		
		ConceptService cs = Context.getConceptService();
		
		// make sure the name tag exists already
		ConceptNameTag cnt = cs.getConceptNameTagByName("preferred_en");
		assertNotNull(cnt);
		
		ConceptName cn = new ConceptName("Some name", Locale.ENGLISH);
		cn.addTag(new ConceptNameTag("preferred_en", "preferred name in a language"));
		Concept concept = new Concept();
		concept.addName(cn);
		concept.addDescription(new ConceptDescription("some description",null));
		
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));
		
		cs.saveConcept(concept);
		
		Collection<ConceptNameTag> savedConceptNameTags = concept.getName(Locale.ENGLISH, false).getTags();
		ConceptNameTag savedConceptNameTag = (ConceptNameTag) savedConceptNameTags.toArray()[0];
		assertEquals(cnt.getConceptNameTagId(), savedConceptNameTag.getConceptNameTagId());
	}
	
	/**
	 * @see ConceptService#saveConceptSource(ConceptSource)
	 */
	@Test
	public void saveConceptSource_shouldNotSetCreatorIfOneIsSuppliedAlready() {
		User expectedCreator = new User(501); // a user that isn't logged in now
		
		ConceptSource newConceptSource = new ConceptSource();
		newConceptSource.setName("name");
		newConceptSource.setDescription("desc");
		newConceptSource.setHl7Code("hl7Code");
		newConceptSource.setCreator(expectedCreator);
		Context.getConceptService().saveConceptSource(newConceptSource);
		
		assertEquals(newConceptSource.getCreator(), expectedCreator);
	}
	
	/**
	 * @see ConceptService#saveConceptSource(ConceptSource)
	 */
	@Test
	public void saveConceptSource_shouldNotSetDateCreatedIfOneIsSuppliedAlready() {
		Date expectedDate = new Date(new Date().getTime() - 10000);
		
		ConceptSource newConceptSource = new ConceptSource();
		newConceptSource.setName("name");
		newConceptSource.setDescription("desc");
		newConceptSource.setHl7Code("hl7Code");
		
		newConceptSource.setDateCreated(expectedDate);
		Context.getConceptService().saveConceptSource(newConceptSource);
		
		assertEquals(DateUtil.truncateToSeconds(expectedDate), newConceptSource.getDateCreated());
	}
	
	/**
	 * @see ConceptService#getConcept(String)
	 */
	@Test
	public void getConcept_shouldReturnNullGivenNullParameter() {
		assertNull(Context.getConceptService().getConcept((String) null));
	}
	
	/**
	 * @see ConceptService#getConceptByName(String)
	 */
	@Test
	public void getConceptByName_shouldReturnNullGivenNullParameter() {
		assertNull(Context.getConceptService().getConceptByName(null));
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
	public void shouldFetchNamesForConceptsThatWereFirstFetchedAsNumerics() {
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
	public void shouldFetchDescriptionsForConceptsThatWereFirstFetchedAsNumerics() {
		Concept concept = Context.getConceptService().getConcept(5089);
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(5089);
		
		conceptNumeric.getDescriptions().size();
		concept.getDescriptions().size();
	}
	
	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	@Test
	public void getConceptByMapping_shouldGetConceptWithGivenCodeAndSourceHl7Code() {
		Concept concept = conceptService.getConceptByMapping("WGT234", "SSTRM");
		assertEquals(5089, concept.getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	@Test
	public void getConceptByMapping_shouldGetConceptWithGivenCodeAndSourceName() {
		Concept concept = conceptService.getConceptByMapping("WGT234", "Some Standardized Terminology");
		assertEquals(5089, concept.getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	@Test
	public void getConceptByMapping_shouldReturnNullIfSourceCodeDoesNotExist() {
		Concept concept = conceptService.getConceptByMapping("A random concept code", "A random source code");
		assertNull(concept);
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	@Test
	public void getConceptByMapping_shouldReturnNullIfNoMappingExists() {
		Concept concept = conceptService.getConceptByMapping("A random concept code", "SSTRM");
		assertNull(concept);
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	@Test
	public void getConceptByMapping_shouldReturnRetiredConceptByDefaultIfOnlyMatch() {
		Concept concept = conceptService.getConceptByMapping("454545", "SSTRM");
		assertEquals(24, concept.getId().intValue());
		
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	@Test
	public void getConceptByMapping_shouldReturnRetiredConceptIfOnlyMatch() {
		Concept concept = conceptService.getConceptByMapping("454545", "SSTRM", true);
		assertEquals(24, concept.getId().intValue());
		
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	@Test
	public void getConceptByMapping_shouldNotReturnRetiredConcept() {
		Concept concept = conceptService.getConceptByMapping("454545", "SSTRM", false);
		assertNull(concept);
		
	}
	
	@Test
	public void getConceptByMapping_shouldThrowExceptionIfTwoConceptsHaveSameMapping() {
		assertThrows(APIException.class, () -> conceptService.getConceptByMapping("127689", "Some Standardized Terminology"));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	@Test
	public void getConceptsByMapping_shouldGetConceptsWithGivenCodeAndSourceH17Code() {
		List<Concept> concepts = conceptService.getConceptsByMapping("127689", "Some Standardized Terminology");
		assertEquals(2, concepts.size());
		assertTrue(containsId(concepts, 16));
		assertTrue(containsId(concepts, 6));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	@Test
	public void getConceptsByMapping_shouldGetConceptsWithGivenCodeAndSourceName() {
		List<Concept> concepts = conceptService.getConceptsByMapping("127689", "SSTRM");
		assertEquals(2, concepts.size());
		assertTrue(containsId(concepts, 16));
		assertTrue(containsId(concepts, 6));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	@Test
	public void getConceptsByMapping_shouldReturnEmptyListIfSourceCodeDoesNotExist() {
		List<Concept> concept = conceptService.getConceptsByMapping("A random concept code", "A random source code");
		assertThat(concept, is(empty()));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String)
	 */
	@Test
	public void getConceptsByMapping_shouldReturnEmptyListIfNoMappingsExist() {
		List<Concept> concept = conceptService.getConceptsByMapping("A random concept code", "SSTRM");
		assertThat(concept, is(empty()));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	@Test
	public void getConceptsByMapping_shouldReturnRetiredAndNonRetiredConceptsByDefault() {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM");
		assertEquals(2, concepts.size());
		assertTrue(containsId(concepts, 16));
		assertTrue(containsId(concepts, 24));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	@Test
	public void getConceptsByMapping_shouldOnlyReturnNonRetiredConcepts() {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM", false);
		assertEquals(1, concepts.size());
		assertTrue(containsId(concepts, 16));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	@Test
	public void getConceptsByMapping_shouldReturnRetiredAndNonRetiredConcepts() {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM", true);
		assertEquals(2, concepts.size());
		assertTrue(containsId(concepts, 16));
		assertTrue(containsId(concepts, 24));
	}
	
	/**
	 * @see ConceptService#getConceptsByMapping(String,String,Boolean)
	 */
	@Test
	public void getConceptsByMapping_shouldSortNonRetiredConceptsFirst() {
		List<Concept> concepts = conceptService.getConceptsByMapping("766554", "SSTRM", true);
		assertEquals(16, concepts.get(0).getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	@Test
	public void getConceptByMapping_shouldIgnoreCase() {
		Concept concept = conceptService.getConceptByMapping("wgt234", "sstrm");
		assertEquals(5089, concept.getId().intValue());
	}

	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	@Test
	public void getConceptIdsByMapping_shouldPopulateCache() {
		Cache cache = cacheManager.getCache("conceptIdsByMapping");
		Ehcache ehcache = ((EhCacheCache) cache).getNativeCache();
		cache.clear();
		assertThat(ehcache.getSize(), is(0));
		conceptService.getConceptIdsByMapping("wgt234", "sstrm", true);
		assertThat(ehcache.getSize(), is(1));
		List<SimpleKey> keys = ehcache.getKeys();
		assertThat(keys.size(), is(1));
		SimpleKey foundKey = keys.get(0);
		SimpleKey expectedKey = new SimpleKey("wgt234", "sstrm", true);
		assertThat(foundKey.toString(), equalTo(expectedKey.toString()));;
	}

	/**
	 * @see ConceptService#getConceptByMapping(String,String)
	 */
	@Test
	public void shouldEvictConceptIdsIfSourceOrTermsAreUpdated() {
		Cache cache = cacheManager.getCache("conceptIdsByMapping");
		Ehcache ehcache = ((EhCacheCache) cache).getNativeCache();
		ConceptSource cs = conceptService.getConceptSourceByHL7Code("SSTRM");
		ConceptReferenceTerm crt = conceptService.getConceptReferenceTermByCode("WGT234", cs);
		ConceptReferenceTerm dummyTerm = new ConceptReferenceTerm(cs, "DUMMY", "DummyTerm");
		conceptService.saveConceptReferenceTerm(dummyTerm);
		assertThat(ehcache.getSize(), is(0));

		// Update Concept Source
		conceptService.getConceptIdsByMapping(crt.getCode(), cs.getHl7Code(), true);
		assertThat(ehcache.getSize(), is(1));
		cs.setDateChanged(new Date());
		conceptService.saveConceptSource(cs);
		assertThat(ehcache.getSize(), is(0));

		// Save Concept Reference Term
		conceptService.getConceptIdsByMapping(crt.getCode(), cs.getHl7Code(), true);
		assertThat(ehcache.getSize(), is(1));
		crt.setDateChanged(new Date());
		conceptService.saveConceptReferenceTerm(crt);
		assertThat(ehcache.getSize(), is(0));

		// purgeConceptReferenceTerm
		conceptService.getConceptIdsByMapping(crt.getCode(), cs.getHl7Code(), true);
		assertThat(ehcache.getSize(), is(1));
		conceptService.purgeConceptReferenceTerm(dummyTerm);
		assertThat(ehcache.getSize(), is(0));
	}
	
	/**
	 * @see ConceptService#getConceptAnswerByUuid(String)
	 */
	@Test
	public void getConceptAnswerByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "b1230431-2fe5-49fc-b535-ae42bc849747";
		ConceptAnswer conceptAnswer = Context.getConceptService().getConceptAnswerByUuid(uuid);
		assertEquals(1, (int) conceptAnswer.getConceptAnswerId());
	}
	
	/**
	 * @see ConceptService#getConceptAnswerByUuid(String)
	 */
	@Test
	public void getConceptAnswerByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptAnswerByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptByUuid(String)
	 */
	@Test
	public void getConceptByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab";
		Concept concept = Context.getConceptService().getConceptByUuid(uuid);
		assertEquals(3, (int) concept.getConceptId());
	}
	
	/**
	 * @see ConceptService#getConceptByUuid(String)
	 */
	@Test
	public void getConceptByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptClassByUuid(String)
	 */
	@Test
	public void getConceptClassByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "97097dd9-b092-4b68-a2dc-e5e5be961d42";
		ConceptClass conceptClass = Context.getConceptService().getConceptClassByUuid(uuid);
		assertEquals(1, (int) conceptClass.getConceptClassId());
	}
	
	/**
	 * @see ConceptService#getConceptClassByUuid(String)
	 */
	@Test
	public void getConceptClassByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptClassByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptDatatypeByUuid(String)
	 */
	@Test
	public void getConceptDatatypeByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "8d4a4488-c2cc-11de-8d13-0010c6dffd0f";
		ConceptDatatype conceptDatatype = Context.getConceptService().getConceptDatatypeByUuid(uuid);
		assertEquals(1, (int) conceptDatatype.getConceptDatatypeId());
	}
	
	/**
	 * @see ConceptService#getConceptDatatypeByUuid(String)
	 */
	@Test
	public void getConceptDatatypeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptDatatypeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptDescriptionByUuid(String)
	 */
	@Test
	public void getConceptDescriptionByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "5f4d710b-d333-40b7-b449-6e0e739d15d0";
		ConceptDescription conceptDescription = Context.getConceptService().getConceptDescriptionByUuid(uuid);
		assertEquals(1, (int) conceptDescription.getConceptDescriptionId());
	}
	
	/**
	 * @see ConceptService#getConceptDescriptionByUuid(String)
	 */
	@Test
	public void getConceptDescriptionByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptDescriptionByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptNameByUuid(String)
	 */
	@Test
	public void getConceptNameByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "9bc5693a-f558-40c9-8177-145a4b119ca7";
		ConceptName conceptName = Context.getConceptService().getConceptNameByUuid(uuid);
		assertEquals(1439, (int) conceptName.getConceptNameId());
	}
	
	/**
	 * @see ConceptService#getConceptNameByUuid(String)
	 */
	@Test
	public void getConceptNameByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptNameByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptNameTagByUuid(String)
	 */
	@Test
	public void getConceptNameTagByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "9e9df183-2328-4117-acd8-fb9bf400911d";
		ConceptNameTag conceptNameTag = Context.getConceptService().getConceptNameTagByUuid(uuid);
		assertEquals(1, (int) conceptNameTag.getConceptNameTagId());
	}
	
	/**
	 * @see ConceptService#getConceptNameTagByUuid(String)
	 */
	@Test
	public void getConceptNameTagByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptNameTagByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptNumericByUuid(String)
	 */
	@Test
	public void getConceptNumericByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumericByUuid(uuid);
		assertEquals(5089, (int) conceptNumeric.getConceptId());
	}
	
	/**
	 * @see ConceptService#getConceptNumericByUuid(String)
	 */
	@Test
	public void getConceptNumericByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptNumericByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptProposalByUuid(String)
	 */
	@Test
	public void getConceptProposalByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "57a68666-5067-11de-80cb-001e378eb67e";
		ConceptProposal conceptProposal = Context.getConceptService().getConceptProposalByUuid(uuid);
		assertEquals(1, (int) conceptProposal.getConceptProposalId());
	}
	
	/**
	 * @see ConceptService#getConceptProposalByUuid(String)
	 */
	@Test
	public void getConceptProposalByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptProposalByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptSetByUuid(String)
	 */
	@Test
	public void getConceptSetByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "1a111827-639f-4cb4-961f-1e025bf88d90";
		ConceptSet conceptSet = Context.getConceptService().getConceptSetByUuid(uuid);
		assertEquals(1, (int) conceptSet.getConceptSetId());
	}
	
	/**
	 * @see ConceptService#getConceptSetByUuid(String)
	 */
	@Test
	public void getConceptSetByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptSetByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getConceptSourceByUuid(String)
	 */
	@Test
	public void getConceptSourceByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "75f5b378-5065-11de-80cb-001e378eb67e";
		ConceptSource conceptSource = Context.getConceptService().getConceptSourceByUuid(uuid);
		assertEquals(3, (int) conceptSource.getConceptSourceId());
	}
	
	/**
	 * @see ConceptService#getConceptSourceByUuid(String)
	 */
	@Test
	public void getConceptSourceByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getConceptSourceByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ConceptService#getDrugByUuid(String)
	 */
	@Test
	public void getDrugByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "3cfcf118-931c-46f7-8ff6-7b876f0d4202";
		Drug drug = Context.getConceptService().getDrugByUuid(uuid);
		assertEquals(2, (int) drug.getDrugId());
	}
	
	/**
	 * @see ConceptService#getDrugByUuid(String)
	 */
	@Test
	public void getDrugByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getDrugByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link ConceptService#getDrugIngredientByUuid(String)}
	 */
	@Test
	public void getDrugIngredientByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "6519d653-393d-4118-9c83-a3715b82d4dc";
		DrugIngredient ingredient = Context.getConceptService().getDrugIngredientByUuid(uuid);
		assertEquals(88, (int) ingredient.getIngredient().getConceptId());
	}
	
	/**
	 * @see {@link ConceptService#getDrugIngredientByUuid(String)}
	 */
	@Test
	public void getDrugIngredientByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getConceptService().getDrugIngredientByUuid("some invalid uuid"));
	}
	
	@Test
	public void getDrugs_shouldReturnDrugsThatAreNotRetired() {
		List<Drug> drugs = Context.getConceptService().getDrugs("ASPIRIN" /* is not retired */);
		assertFalse(drugs.get(0).getRetired());
	}
	
	/**
	 * @see ConceptService#getDrugs(String)
	 */
	@Test
	public void getDrugs_shouldNotReturnDrugsThatAreRetired() {
		List<Drug> drugs = Context.getConceptService().getDrugs("TEST_DRUG_NAME_RETIRED" /* is retired */);
		assertEquals(0, drugs.size());
	}
	
	/**
	 * @see ConceptService#getDrugs(String)
	 */
	@Test
	public void getDrugs_shouldReturnDrugsByDrugId() {
		Integer drugId = 2;
		Drug drug = Context.getConceptService().getDrug(drugId);
		List<Drug> drugs = Context.getConceptService().getDrugs(String.valueOf(drugId));
		assertTrue(drugs.contains(drug));
	}
	
	/**
	 * @see ConceptService#getDrugs(String)
	 */
	@Test
	public void getDrugs_shouldNotFailIfThereisNoDrugByGivenDrugId() {
		List<Drug> drugs = Context.getConceptService().getDrugs("123456");
		assertNotNull(drugs);
	}
	
	/**
	 * @see ConceptService#getDrugs(String)
	 */
	@Test
	public void getDrugs_shouldReturnDrugsByDrugConceptId() {
		Integer conceptId = 792;
		Drug drug = Context.getConceptService().getDrug(2);
		
		// assert that given drug has concept with tested id
		assertNotNull(drug.getConcept());
		assertEquals(drug.getConcept().getConceptId(), conceptId);
		
		List<Drug> drugs = Context.getConceptService().getDrugs(String.valueOf(conceptId));
		assertTrue(drugs.contains(drug));
	}
	
	/**
	 * This tests for being able to find concepts with names in en_GB locale when the user is in the
	 * en locale.
	 * 
	 * @see ConceptService#getConceptByName(String)
	 */
	@Test
	public void getConceptByName_shouldFindConceptsWithNamesInMoreSpecificLocales() {
		Locale origLocale = Context.getLocale();
		
		executeDataSet(INITIAL_CONCEPTS_XML);
		Context.setLocale(Locale.ENGLISH);
		
		// make sure that concepts are found that have a specific locale on them
		assertNotNull(Context.getConceptService().getConceptByName("Numeric name with en_GB locale"));
		
		// find concepts with same generic locale
		assertNotNull(Context.getConceptService().getConceptByName("Some numeric concept name"));
		
		// reset the locale for the next test
		Context.setLocale(origLocale);
	}
	
	/**
	 * This tests for being able to find concepts with names in the en locale when the user is in
	 * the en_GB locale
	 * 
	 * @see ConceptService#getConceptByName(String)
	 */
	@Test
	public void getConceptByName_shouldFindConceptsWithNamesInMoreGenericLocales() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		//prior tests have changed the locale to 'en_US', so we need to set it back
		Context.setLocale(Locale.UK);
		// make sure that concepts are found that have a specific locale on them
		assertNotNull(Context.getConceptService().getConceptByName("Some numeric concept name"));
	}
	
	/**
	 * This tests for being able to find concepts with names in en_GB locale when the user is in the
	 * en_GB locale.
	 * 
	 * @see ConceptService#getConceptByName(String)
	 */
	@Test
	public void getConceptByName_shouldFindConceptsWithNamesInSameSpecificLocale() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Context.setLocale(Locale.UK);
		
		// make sure that concepts are found that have a specific locale on them
		assertNotNull(Context.getConceptService().getConceptByName("Numeric name with en_GB locale"));
	}
	
	/**
	 * @see ConceptService#retireConceptSource(ConceptSource,String)
	 */
	@Test
	public void retireConceptSource_shouldRetireConceptSource() {
		ConceptSource cs = conceptService.getConceptSource(3);
		conceptService.retireConceptSource(cs, "dummy reason for retirement");
		
		cs = conceptService.getConceptSource(3);
		assertTrue(cs.getRetired());
		assertEquals("dummy reason for retirement", cs.getRetireReason());
	}
	
	@Test
	public void saveConcept_shouldCreateNewConceptInDatabase() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		Concept conceptToAdd = new Concept();
		ConceptName cn = new ConceptName("new name", Context.getLocale());
		conceptToAdd.addName(cn);
		conceptToAdd.addDescription(new ConceptDescription("some description",null));
		conceptToAdd.setDatatype(new ConceptDatatype(1));
		conceptToAdd.setConceptClass(new ConceptClass(1));
		assertFalse(conceptService.getAllConcepts().contains(conceptToAdd));
		conceptService.saveConcept(conceptToAdd);
		assertTrue(conceptService.getAllConcepts().contains(conceptToAdd));
	}
	
	@Test
	public void saveConcept_shouldUpdateConceptAlreadyExistingInDatabase() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// using isSet() as a value to check and change
		assertFalse(conceptService.getConcept(2).getSet());
		Concept concept = conceptService.getConcept(2);
		// change a value
		concept.setSet(true);
		
		// save the concept
		conceptService.saveConcept(concept);
		// see if the value was updated in the database
		assertTrue(conceptService.getConcept(2).getSet());
	}
	
	@Test
	public void getConceptSourceByName_shouldGetConceptSourceWithTheGivenName() {
		ConceptSource conceptSource = conceptService.getConceptSourceByName("SNOMED CT");
		assertEquals(Integer.valueOf(2), conceptSource.getConceptSourceId(), "Method did not retrieve ConceptSource by name");
	}
	
	@Test
	public void getConceptSourceByName_shouldReturnNullIfNoConceptSourceWithThatNameIsFound() {
		ConceptSource conceptSource = conceptService.getConceptSourceByName("Some invalid name");
		assertNull(conceptSource, "Method did not return null when no ConceptSource with that name is found");
	}

	/**
	 * @see ConceptService#getConceptSourceByUniqueId(String)
	 */
	@Test
	public void getConceptSourceByUniqueId_shouldGetConceptSourceWithTheGivenUniqueId() {

		String existingUniqueId = "2.16.840.1.113883.6.96";
		ConceptSource conceptSource = conceptService.getConceptSourceByUniqueId(existingUniqueId);
		assertThat(conceptSource, is(not(nullValue())));
		assertThat(conceptSource.getUniqueId(), is(existingUniqueId));
	}

	/**
	 * @see ConceptService#getConceptSourceByUniqueId(String)
	 */
	@Test public void getConceptSourceByUniqueId_shouldReturnNullIfNoConceptSourceWithGivenUniqueIdIsFound()
	{

		assertThat(conceptService.getConceptSourceByUniqueId("9.99999.999.9999.999.999.999.9999.99"), is(nullValue()));
	}

	/**
	 * @see ConceptService#getConceptSourceByUniqueId(String)
	 */
	@Test
	public void getConceptSourceByUniqueId_shouldReturnNullIfGivenAnEmptyString() {

		assertThat(conceptService.getConceptSourceByUniqueId(""), is(nullValue()));
		assertThat(conceptService.getConceptSourceByUniqueId("    "), is(nullValue()));
	}

	/**
	 * @see ConceptService#getConceptSourceByUniqueId(String)
	 */
	@Test
	public void getConceptSourceByUniqueId_shouldFailIfGivenNull() {

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> conceptService.getConceptSourceByUniqueId(null));
		assertThat(exception.getMessage(), is("uniqueId is required"));
	}
	
	/**
	 * @see ConceptService#getConceptSourceByHL7Code(String)
	 */
	@Test
	public void getConceptSourceByHL7Code_shouldGetConceptSourceWithTheGivenUniqueId() {

		String existinghl7Code = "SCT";
		ConceptSource conceptSource = conceptService.getConceptSourceByHL7Code(existinghl7Code);
		assertThat(conceptSource, is(not(nullValue())));
		assertThat(conceptSource.getHl7Code(), is(existinghl7Code));
	}	
	
	/**
	 * @see ConceptService#getConceptSourceByHL7Code(String)
	 */
	@Test public void getConceptSourceByHL7Code_shouldReturnNullIfNoConceptSourceWithGivenUniqueIdIsFound()
	{

		assertThat(conceptService.getConceptSourceByHL7Code("XXXXX"), is(nullValue()));
	}
	
	/**
	 * @see ConceptService#getConceptSourceByHL7Code(String)
	 */
	@Test
	public void getConceptSourceByHL7Code_shouldReturnNullIfGivenAnEmptyString() {

		assertThat(conceptService.getConceptSourceByHL7Code(""), is(nullValue()));
		assertThat(conceptService.getConceptSourceByHL7Code("    "), is(nullValue()));
	}

	/**
	 * @see ConceptService#getConceptSourceByHL7Code(String)
	 */
	@Test
	public void getConceptSourceByHL7Code_shouldFailIfGivenNull() {

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> conceptService.getConceptSourceByHL7Code(null));
		assertThat(exception.getMessage(), is("hl7Code is required"));
	}
	
	@Test
	public void getConceptsByConceptSource_shouldReturnAListOfConceptMapsIfConceptMappingsFound() {
		List<ConceptMap> list = conceptService
		        .getConceptMappingsToSource(conceptService.getConceptSourceByName("SNOMED CT"));
		assertEquals(2, list.size());
	}
	
	@Test
	public void getConceptsByConceptSource_shouldReturnEmptyListOfConceptMapsIfNoneFound() {
		List<ConceptMap> list = conceptService.getConceptMappingsToSource(conceptService
		        .getConceptSourceByName("Some invalid name"));
		assertEquals(0, list.size());
	}
	
	@Test
	public void saveConceptSource_shouldSaveAConceptSourceWithANullHl7Code() {
		ConceptSource source = new ConceptSource();
		String aNullString = null;
		String sourceName = "A concept source with null HL7 code";
		source.setName(sourceName);
		source.setDescription("A concept source description");
		source.setHl7Code(aNullString);
		conceptService.saveConceptSource(source);
		assertEquals(source, conceptService.getConceptSourceByName(sourceName), "Did not save a ConceptSource with a null hl7Code");
		
	}
	
	@Test
	public void saveConceptSource_shouldNotSaveAConceptSourceIfVoidedIsNull() {
		ConceptSource source = new ConceptSource();
		source.setRetired(null);
		assertNull(source.getRetired());
		
		assertThrows(Exception.class, () -> conceptService.saveConceptSource(source));
		
	}
	
	@Test
	public void saveConceptNameTag_shouldSaveAConceptNameTagIfATagDoesNotExist() {
		ConceptNameTag nameTag = new ConceptNameTag();
		nameTag.setTag("a new tag");
		
		ConceptNameTag savedNameTag = conceptService.saveConceptNameTag(nameTag);
		
		assertNotNull(nameTag.getId());
		assertEquals(savedNameTag.getId(), nameTag.getId());
	}
	
	@Test
	public void saveConceptNameTag_shouldNotSaveAConceptNameTagIfTagExists() {
		String tag = "a new tag";
		
		ConceptNameTag nameTag = new ConceptNameTag();
		nameTag.setTag(tag);
		
		conceptService.saveConceptNameTag(nameTag);
		
		ConceptNameTag secondNameTag = new ConceptNameTag();
		secondNameTag.setTag(tag);
		
		assertThrows(Exception.class, () -> conceptService.saveConceptNameTag(secondNameTag));
		assertNull(secondNameTag.getId());
		
	}
	
	@Test
	public void saveConcept_shouldNotUpdateConceptDataTypeIfConceptIsAttachedToAnObservation() {
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
		assertThrows(ConceptInUseException.class, () -> conceptService.saveConcept(concept));
	}
	
	@Test
	public void saveConcept_shouldUpdateConceptIfConceptIsAttachedToAnObservationAndItIsANonDatatypeChange()
	{
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
	 * @see ConceptService#getFalseConcept()
	 */
	@Test
	public void getFalse_shouldReturnTheFalseConcept() {
		createTrueFalseGlobalProperties();
		assertNotNull(conceptService.getFalseConcept());
		assertEquals(8, conceptService.getFalseConcept().getId().intValue());
	}
	
	/**
	 * @see ConceptService#getTrueConcept()
	 */
	@Test
	public void getTrue_shouldReturnTheTrueConcept() {
		createTrueFalseGlobalProperties();
		assertNotNull(conceptService.getTrueConcept());
		assertEquals(7, conceptService.getTrueConcept().getId().intValue());
	}
	
	/**
	 * @see ConceptService#getUnknownConcept()
	 */
	@Test
	public void getUnknownConcept_shouldReturnTheUnknownConcept() {
		createUnknownConceptGlobalProperty();
		assertNotNull(conceptService.getUnknownConcept());
		assertEquals(9, conceptService.getUnknownConcept().getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptDatatypeByName(String)
	 */
	@Test
	public void changeConceptFromBooleanToCoded_shouldConvertTheDatatypeOfABooleanConceptToCoded() {
		Concept concept = conceptService.getConcept(18);
		assertEquals(conceptService.getConceptDatatypeByName("Boolean").getConceptDatatypeId(), concept.getDatatype()
		        .getConceptDatatypeId());
		conceptService.convertBooleanConceptToCoded(concept);
		assertEquals(conceptService.getConceptDatatypeByName("Coded").getConceptDatatypeId(), concept.getDatatype()
		        .getConceptDatatypeId());
	}
	
	/**
	 * @see ConceptService#getConcept(Integer)
	 */
	@Test
	public void changeConceptFromBooleanToCoded_shouldFailIfTheDatatypeOfTheConceptIsNotBoolean() {
		Concept concept = conceptService.getConcept(5497);
		assertThrows(APIException.class, () -> conceptService.convertBooleanConceptToCoded(concept));
	}
	
	/**
	 * @see ConceptService#convertBooleanConceptToCoded(Concept)
	 */
	@Test
	public void changeConceptFromBooleanToCoded_shouldExplicitlyAddFalseConceptAsAValue_CodedAnswer() {
		Concept concept = conceptService.getConcept(18);
		Collection<ConceptAnswer> answers = concept.getAnswers(false);
		boolean falseConceptFound = false;
		//initially the concept shouldn't present
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getFalseConcept())) {
				falseConceptFound = true;
			}
		}
		assertFalse(falseConceptFound);
		conceptService.convertBooleanConceptToCoded(concept);
		answers = concept.getAnswers(false);
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getFalseConcept())) {
				falseConceptFound = true;
			}
		}
		assertTrue(falseConceptFound);
	}
	
	/**
	 * @see ConceptService#convertBooleanConceptToCoded(Concept)
	 */
	@Test
	public void changeConceptFromBooleanToCoded_shouldExplicitlyAddTrueConceptAsAValue_CodedAnswer() {
		Concept concept = conceptService.getConcept(18);
		Collection<ConceptAnswer> answers = concept.getAnswers(false);
		boolean trueConceptFound = false;
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getTrueConcept())) {
				trueConceptFound = true;
			}
		}
		assertFalse(trueConceptFound);
		conceptService.convertBooleanConceptToCoded(concept);
		answers = concept.getAnswers(false);
		for (ConceptAnswer conceptAnswer : answers) {
			if (conceptAnswer.getAnswerConcept().equals(conceptService.getTrueConcept())) {
				trueConceptFound = true;
			}
		}
		assertTrue(trueConceptFound);
	}
	
	/**
	 * @see ConceptService#getFalseConcept()
	 */
	@Test
	public void getFalseConcept_shouldReturnTheFalseConcept() {
		createTrueFalseGlobalProperties();
		assertEquals(8, conceptService.getFalseConcept().getConceptId().intValue());
	}
	
	/**
	 * @see ConceptService#getTrueConcept()
	 */
	@Test
	public void getTrueConcept_shouldReturnTheTrueConcept() {
		createTrueFalseGlobalProperties();
		assertEquals(7, conceptService.getTrueConcept().getConceptId().intValue());
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
	 * @see ConceptService#getConceptDatatypeByName(String)
	 */
	@Test
	public void getConceptDatatypeByName_shouldNotReturnAFuzzyMatchOnName() {
		executeDataSet(INITIAL_CONCEPTS_XML);
		ConceptDatatype result = conceptService.getConceptDatatypeByName("Tex");
		assertNull(result);
	}
	
	/**
	 * @see ConceptService#getConceptDatatypeByName(String)
	 */
	@Test
	public void getConceptDatatypeByName_shouldReturnAnExactMatchOnName() {
		// given
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// when
		ConceptDatatype result = conceptService.getConceptDatatypeByName("Text");
		
		// then
		assertEquals("Text", result.getName());
	}
	
	/**
	 * @see ConceptService#purgeConcept(Concept)
	 */
	@Test
	public void purgeConcept_shouldFailIfAnyOfTheConceptNamesOfTheConceptIsBeingUsedByAnObs() {
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
		assertTrue(conceptService.hasAnyObservation(conceptName));
		
		Concept concept = conceptService.getConceptByName("cd4 count");
		//make sure the name concept name exists
		assertNotNull(concept);
		assertThrows(ConceptNameInUseException.class, () -> conceptService.purgeConcept(concept));
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldCreateANewConceptNameWhenTheOldNameIsChanged() {
		Concept concept = conceptService.getConceptByName("cd4 count");
		assertEquals(3, concept.getNames(true).size());
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
		
		assertEquals(4, concept.getNames(true).size());
		
		for (ConceptName cn : concept.getNames()) {
			if (cn.getName().equals("new name")) {
				assertTrue(oldName.getDateCreated().before(cn.getDateCreated()));
			}
		}
		
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldVoidTheConceptNameIfTheTextOfTheNameHasChanged() {
		Concept concept = conceptService.getConceptByName("cd4 count");
		assertFalse(conceptService.getConceptName(1847).getVoided());
		for (ConceptName cn : concept.getNames()) {
			if (cn.getConceptNameId().equals(1847)) {
				cn.setName("new name");
			}
		}
		//ensure that the conceptName has actually been found and replaced
		assertTrue(concept.hasName("new name", new Locale("en", "GB")));
		conceptService.saveConcept(concept);
		assertTrue(conceptService.getConceptName(1847).getVoided());
	}
	
	@Test
	public void getConceptsByConceptSet_shouldReturnAllConceptsInSet() {
		
		executeDataSet(GET_CONCEPTS_BY_SET_XML);
		
		Concept concept = conceptService.getConcept(1);
		
		List<Concept> conceptSet = conceptService.getConceptsByConceptSet(concept);
		
		assertThat(conceptSet, containsInAnyOrder(hasId(2), hasId(3), hasId(4), hasId(5), hasId(6)));
	}
	
	/**
	 * @see ConceptService#saveConceptStopWord(org.openmrs.ConceptStopWord)
	 */
	@Test
	public void saveConceptStopWord_shouldSaveConceptStopWordIntoDatabase() {
		ConceptStopWord conceptStopWord = new ConceptStopWord("AND", Locale.FRANCE);
		conceptService.saveConceptStopWord(conceptStopWord);
		
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.FRANCE);
		assertEquals(1, conceptStopWords.size());
		assertEquals("AND", conceptStopWords.get(0));
	}
	
	/**
	 * @see ConceptService#saveConceptStopWord(ConceptStopWord)
	 */
	@Test
	public void saveConceptStopWord_shouldSaveConceptStopWordAssignDefaultLocaleIsItNull() {
		ConceptStopWord conceptStopWord = new ConceptStopWord("The");
		conceptService.saveConceptStopWord(conceptStopWord);
		
		List<String> conceptStopWords = conceptService.getConceptStopWords(Context.getLocale());
		assertThat(conceptStopWords, hasItem("THE"));
	}
	
	/**
	 * @see ConceptService#getConceptStopWords(Locale)
	 */
	@Test
	public void getConceptStopWords_shouldReturnDefaultLocaleConceptStopWordsIfLocaleIsNull() {
		List<String> conceptStopWords = conceptService.getConceptStopWords(null);
		assertEquals(1, conceptStopWords.size());
	}
	
	/**
	 * @see ConceptService#saveConceptStopWord(ConceptStopWord)
	 */
	@Test
	public void saveConceptStopWord_shouldSaveReturnConceptStopWordWithId() {
		ConceptStopWord conceptStopWord = new ConceptStopWord("A", Locale.UK);
		ConceptStopWord savedConceptStopWord = conceptService.saveConceptStopWord(conceptStopWord);
		
		assertNotNull(savedConceptStopWord.getId());
	}
	
	/**
	 * @see ConceptService#saveConceptStopWord(ConceptStopWord)
	 */
	@Test
	public void saveConceptStopWord_shouldFailIfADuplicateConceptStopWordInALocaleIsAdded() {
		ConceptStopWord conceptStopWord = new ConceptStopWord("A");
		try {
			conceptService.saveConceptStopWord(conceptStopWord);
			assertThrows(ConceptStopWordException.class, () -> conceptService.saveConceptStopWord(conceptStopWord));
		}
		catch (ConceptStopWordException e) {
			assertEquals("ConceptStopWord.duplicated", e.getMessage());
			throw e;
		}
	}
	
	/**
	 * @see ConceptService#saveConceptStopWord(ConceptStopWord)
	 */
	@Test
	public void saveConceptStopWord_shouldSaveConceptStopWordInUppercase() {
		ConceptStopWord conceptStopWord = new ConceptStopWord("lowertoupper");
		ConceptStopWord savedConceptStopWord = conceptService.saveConceptStopWord(conceptStopWord);
		
		assertEquals("LOWERTOUPPER", savedConceptStopWord.getValue());
	}
	
	/**
	 * @see ConceptService#getConceptStopWords(Locale)
	 */
	@Test
	public void getConceptStopWords_shouldReturnListOfConceptStopWordsForGivenLocale() {
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.ENGLISH);
		
		assertThat(conceptStopWords, containsInAnyOrder("A", "AN"));
	}
	
	/**
	 * @see ConceptService#getAllConceptStopWords()
	 */
	@Test
	public void getAllConceptStopWords_shouldReturnAllConceptStopWords() {
		List<ConceptStopWord> conceptStopWords = conceptService.getAllConceptStopWords();
		assertEquals(4, conceptStopWords.size());
	}
	
	/**
	 * @see ConceptService#getAllConceptStopWords()
	 */
	@Test
	public void getAllConceptStopWords_shouldReturnEmptyListIfNoRecordFound() {
		conceptService.deleteConceptStopWord(1);
		conceptService.deleteConceptStopWord(2);
		conceptService.deleteConceptStopWord(3);
		conceptService.deleteConceptStopWord(4);
		
		List<ConceptStopWord> conceptStopWords = conceptService.getAllConceptStopWords();
		assertEquals(0, conceptStopWords.size());
	}
	
	/**
	 * @see ConceptService#getConceptStopWords(Locale)
	 */
	@Test
	public void getConceptStopWords_shouldReturnEmptyListIfNoConceptStopWordsForGivenLocale() {
		List<String> conceptStopWords = conceptService.getConceptStopWords(Locale.GERMANY);
		assertEquals(0, conceptStopWords.size());
	}
	
	/**
	 * @see ConceptService#deleteConceptStopWord(Integer)
	 */
	@Test
	public void deleteConceptStopWord_shouldDeleteTheGivenConceptStopWord() {
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
	public void saveConcept_shouldNotAcceptALocaleThatIsNeitherAmongTheLocaleAllowedListNorADefaultLocale() {
		List<Concept> concepts = Context.getConceptService().getAllConcepts();
		Set<Locale> allowedLocales = LocaleUtility.getLocalesInOrder();
		for (Concept concept : concepts) {
			if (!CollectionUtils.isEmpty(concept.getNames())) {
				for (ConceptName cn : concept.getNames()) {
					assertTrue(allowedLocales.contains(cn.getLocale()), "The locale '" + cn.getLocale() + "' of conceptName with id: " + cn.getConceptNameId() + " is not among those listed in the global property 'locale.allowed.list'");
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
	public void saveConcept_shouldAlwaysReturnAPreferredNameForEveryLocaleThatHasAtleastOneUnvoidedName() {
		List<Concept> concepts = Context.getConceptService().getAllConcepts();
		Set<Locale> allowedLocales = LocaleUtility.getLocalesInOrder();
		for (Concept concept : concepts) {
			for (Locale locale : allowedLocales) {
				if (!CollectionUtils.isEmpty(concept.getNames(locale))) {
					assertNotNull(concept.getPreferredName(locale), "Concept with Id: " + concept.getConceptId() + " has no preferred name in locale:" + locale);
					assertTrue(concept.getPreferredName(locale).getLocalePreferred());
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
	public void saveConcept_shouldEnsureThatEveryConcepNameLocaleHasExactlyOnePreferredName() {
		List<Concept> concepts = Context.getConceptService().getAllConcepts();
		Set<Locale> allowedLocales = LocaleUtility.getLocalesInOrder();
		for (Concept concept : concepts) {
			for (Locale locale : allowedLocales) {
				Collection<ConceptName> namesInLocale = concept.getNames(locale);
				if (!CollectionUtils.isEmpty(namesInLocale)) {
					int preferredNamesFound = 0;
					for (ConceptName conceptName : namesInLocale) {
						if (conceptName.getLocalePreferred()) {
							preferredNamesFound++;
							assertTrue(preferredNamesFound < 2, "Found multiple preferred names for conceptId: " + concept.getConceptId() + " in the locale '" + locale + "'");
						}
					}
				}
			}
		}
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldSetAPreferredNameForEachLocaleIfNoneIsMarked() {
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
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));

		concept = Context.getConceptService().saveConcept(concept);
		assertNotNull(concept.getPreferredName(Locale.ENGLISH));
		assertNotNull(concept.getPreferredName(Locale.FRENCH));
		assertNotNull(concept.getPreferredName(Locale.JAPANESE));
	}
	
	/**
	 * @see ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept)
	 */
	@Test
	public void mapConceptProposalToConcept_shouldNotRequireMappedConceptOnRejectAction() {
		String uuid = "af4ae460-0e2b-11e0-a94b-469c3c5a0c2f";
		ConceptProposal proposal = Context.getConceptService().getConceptProposalByUuid(uuid);
		assertNotNull(proposal, "could not find proposal " + uuid);
		proposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		try {
			Context.getConceptService().mapConceptProposalToConcept(proposal, null);
		}
		catch (APIException ex) {
			fail("cought APIException when rejecting a proposal with null mapped concept");
		}
	}
	
	/**
	 * @see ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept)
	 */
	@Test
	public void mapConceptProposalToConcept_shouldAllowRejectingProposals() {
		String uuid = "af4ae460-0e2b-11e0-a94b-469c3c5a0c2f";
		ConceptProposal proposal = Context.getConceptService().getConceptProposalByUuid(uuid);
		assertNotNull(proposal, "could not find proposal " + uuid);
		//because there is a  different unit test for the case when mapped proposal is null, we use a non-null concept here for our testing
		Concept concept = conceptService.getConcept(3);
		assertNotNull(concept, "could not find target concept to use for the test");
		proposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		Context.getConceptService().mapConceptProposalToConcept(proposal, concept);
		//retrieve the proposal from the model and check its new state
		ConceptProposal persisted = Context.getConceptService().getConceptProposalByUuid(uuid);
		assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT, persisted.getState());
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	@Test
	public void getConcepts_shouldReturnConceptSearchResultsThatMatchUniqueConcepts() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("trust", Collections
		        .singletonList(Locale.ENGLISH), false, null, null, null, null, null, null, null);
		//trust is included in 2 names for conceptid=3000 and in one name for conceptid=4000.
		//So we should see 2 results only
		assertEquals(2, searchResults.size());
	}

    /**
     * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
     */
    @Test
	public void getConcepts_shouldReturnConceptSearchResultsThatMatchUniqueConceptsEvenIfDifferentMatchingWords() {
        executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
        List<ConceptSearchResult> searchResults = conceptService.getConcepts("now", Collections
                .singletonList(Locale.ENGLISH), false, null, null, null, null, null, null, null);
        // "now matches both concept names "TRUST NOW" and "TRUST NOWHERE", but these are for the same concept (4000), so there should only be one item in the result set
        assertEquals(1, searchResults.size());
        assertEquals(new Integer(4000), searchResults.get(0).getConcept().getId());
	}

	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	@Test
	public void getConcepts_shouldReturnConceptSearchResultsThatContainAllSearchWordsAsFirst() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("trust now", Collections
		        .singletonList(Locale.ENGLISH), false, null, null, null, null, null, null, null);
		//"trust now" must be first hit
		assertThat(searchResults.get(0).getWord(), is("trust now"));
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTermByName(String,ConceptSource)
	 */
	@Test
	public void getConceptReferenceTermByName_shouldReturnAConceptReferenceTermThatMatchesTheGivenNameFromTheGivenSource()
	{
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTermByName("weight term",
		    new ConceptSource(1));
		assertNotNull(term);
		assertEquals("weight term", term.getName());
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTermByCode(String,ConceptSource)
	 */
	@Test
	public void getConceptReferenceTermByCode_shouldReturnAConceptReferenceTermThatMatchesTheGivenCodeFromTheGivenSource()
	{
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTermByCode("2332523",
		    new ConceptSource(2));
		assertNotNull(term);
		assertEquals("2332523", term.getCode());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypes(null,null)
	 */
	@Test
	public void getConceptMapTypes_shouldNotIncludeHiddenConceptMapTypesIfIncludeHiddenIsSetToFalse() {
		assertEquals(6, Context.getConceptService().getConceptMapTypes(true, false).size());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypes(null,null)
	 */
	@Test
	public void getConceptMapTypes_shouldReturnSortedList() {
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
	 * @see ConceptService#getConceptMapTypes(null,null)
	 */
	@Test
	public void getConceptMapTypes_shouldReturnAllTheConceptMapTypesIfIncludeRetiredAndHiddenAreSetToTrue() {
		assertEquals(8, Context.getConceptService().getConceptMapTypes(true, true).size());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypes(null,null)
	 */
	@Test
	public void getConceptMapTypes_shouldReturnOnlyUnRetiredConceptMapTypesIfIncludeRetiredIsSetToFalse() {
		assertEquals(6, Context.getConceptService().getConceptMapTypes(false, true).size());
	}
	
	/**
	 * @see ConceptService#getAllConceptMapTypes()
	 */
	@Test
	public void getActiveConceptMapTypes_shouldReturnAllTheConceptMapTypesExcludingHiddenOnes() {
		assertEquals(6, Context.getConceptService().getActiveConceptMapTypes().size());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypeByName(String)
	 */
	@Test
	public void getConceptMapTypeByName_shouldReturnAConceptMapTypeMatchingTheSpecifiedName() {
		assertEquals("same-as", Context.getConceptService().getConceptMapTypeByName("same-as").getName());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypeByUuid(String)
	 */
	@Test
	public void getConceptMapTypeByUuid_shouldReturnAConceptMapTypeMatchingTheSpecifiedUuid() {
		assertEquals("is-parent-to", Context.getConceptService().getConceptMapTypeByUuid(
		    "0e7a8536-49d6-11e0-8fed-18a905e044dc").getName());
	}
	
	/**
	 * @see ConceptService#purgeConceptMapType(ConceptMapType)
	 */
	@Test
	public void purgeConceptMapType_shouldDeleteTheSpecifiedConceptMapTypeFromTheDatabase() {
		//sanity check
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		assertNotNull(mapType);
		Context.getConceptService().purgeConceptMapType(mapType);
		assertNull(Context.getConceptService().getConceptMapType(1));
	}
	
	/**
	 * @see ConceptService#purgeConceptNameTag(ConceptNameTag)
	 */
	@Test
	public void purgeConceptNameTag_shouldDeleteTheSpecifiedConceptNameTagFromTheDatabase() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-tags.xml");
		//sanity check
		ConceptNameTag nameTag = Context.getConceptService().getConceptNameTagByName("preferred_en");
		assertNotNull(nameTag);
		Context.getConceptService().purgeConceptNameTag(nameTag);
		assertNull(Context.getConceptService().getConceptNameTagByName("preferred_en"));
	}
	
	/**
	 * @see ConceptService#saveConceptMapType(ConceptMapType)
	 */
	@Test
	public void saveConceptMapType_shouldAddTheSpecifiedConceptMapTypeToTheDatabaseAndAssignToItAnId() {
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("test type");
		mapType = Context.getConceptService().saveConceptMapType(mapType);
		assertNotNull(mapType.getId());
		assertNotNull(Context.getConceptService().getConceptMapTypeByName("test type"));
	}
	
	/**
	 * @see ConceptService#saveConceptMapType(ConceptMapType)
	 */
	@Test
	public void saveConceptMapType_shouldUpdateAnExistingConceptMapType() {
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		//sanity checks
		assertNull(mapType.getDateChanged());
		assertNull(mapType.getChangedBy());
		mapType.setName("random name");
		mapType.setDescription("random description");
		ConceptMapType editedMapType = Context.getConceptService().saveConceptMapType(mapType);
		Context.flushSession();
		assertEquals("random name", editedMapType.getName());
		assertEquals("random description", editedMapType.getDescription());
		//date changed and changed by should have been updated
		assertNotNull(editedMapType.getDateChanged());
		assertNotNull(editedMapType.getChangedBy());
	}
	
	/**
	 * @see ConceptService#retireConceptMapType(ConceptMapType,String)
	 */
	@Test
	public void retireConceptMapType_shouldRetireTheSpecifiedConceptMapTypeWithTheGivenRetireReason() {
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		assertFalse(mapType.getRetired());
		assertNull(mapType.getRetiredBy());
		assertNull(mapType.getDateRetired());
		assertNull(mapType.getRetireReason());
		ConceptMapType retiredMapType = Context.getConceptService().retireConceptMapType(mapType, "test retire reason");
		assertTrue(retiredMapType.getRetired());
		assertEquals(retiredMapType.getRetireReason(), "test retire reason");
		assertNotNull(retiredMapType.getRetiredBy());
		assertNotNull(retiredMapType.getDateRetired());
	}
	
	/**
	 * @see ConceptService#retireConceptMapType(ConceptMapType,String)
	 */
	@Test
	public void retireConceptMapType_shouldShouldSetTheDefaultRetireReasonIfNoneIsGiven() {
		//sanity check
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(1);
		assertNull(mapType.getRetireReason());
		ConceptMapType retiredMapType = Context.getConceptService().retireConceptMapType(mapType, null);
		assertNotNull(retiredMapType.getRetireReason());
	}
	
	/**
	 * @see ConceptService#getAllConceptReferenceTerms(null)
	 */
	@Test
	public void getConceptReferenceTerms_shouldReturnAllTheConceptReferenceTermsIfIncludeRetiredIsSetToTrue()
	{
		assertEquals(11, Context.getConceptService().getConceptReferenceTerms(true).size());
	}
	
	/**
	 * @see ConceptService#getAllConceptReferenceTerms(null)
	 */
	@Test
	public void getConceptReferenceTerms_shouldReturnOnlyUnRetiredConceptReferenceTermsIfIncludeRetiredIsSetToFalse()
	{
		assertEquals(10, Context.getConceptService().getConceptReferenceTerms(false).size());
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTermByUuid(String)
	 */
	@Test
	public void getConceptReferenceTermByUuid_shouldReturnTheConceptReferenceTermThatMatchesTheGivenUuid() {
		assertEquals("weight term2", Context.getConceptService().getConceptReferenceTermByUuid("SNOMED CT-2332523")
		        .getName());
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTermsBySource(ConceptSource)
	 */
	@Test
	public void getConceptReferenceTerms_shouldReturnOnlyTheConceptReferenceTermsFromTheGivenConceptSource()
	{
		assertEquals(9, conceptService.getConceptReferenceTerms(null, conceptService.getConceptSource(1), 0, null,
		    true).size());
	}
	
	/**
	 * @see ConceptService#retireConceptReferenceTerm(ConceptReferenceTerm,String)
	 */
	@Test
	public void retireConceptReferenceTerm_shouldRetireTheSpecifiedConceptReferenceTermWithTheGivenRetireReason()
	{
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		assertFalse(term.getRetired());
		assertNull(term.getRetireReason());
		assertNull(term.getRetiredBy());
		assertNull(term.getDateRetired());
		ConceptReferenceTerm retiredTerm = Context.getConceptService()
		        .retireConceptReferenceTerm(term, "test retire reason");
		assertTrue(retiredTerm.getRetired());
		assertEquals("test retire reason", retiredTerm.getRetireReason());
		assertNotNull(retiredTerm.getRetiredBy());
		assertNotNull(retiredTerm.getDateRetired());
	}
	
	/**
	 * @see ConceptService#retireConceptReferenceTerm(ConceptReferenceTerm,String)
	 */
	@Test
	public void retireConceptReferenceTerm_shouldShouldSetTheDefaultRetireReasonIfNoneIsGiven() {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		term = Context.getConceptService().retireConceptReferenceTerm(term, null);
		assertNotNull(term.getRetireReason());
	}
	
	/**
	 * @see ConceptService#saveConceptReferenceTerm(ConceptReferenceTerm)
	 */
	@Test
	public void saveConceptReferenceTerm_shouldAddAConceptReferenceTermToTheDatabaseAndAssignAnIdToIt() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("test term");
		term.setCode("test code");
		ConceptSource source = Context.getConceptService().getConceptSource(1);
		term.setConceptSource(source);
		ConceptReferenceTerm savedTerm = Context.getConceptService().saveConceptReferenceTerm(term);
		assertNotNull(savedTerm.getId());
		assertNotNull(Context.getConceptService().getConceptReferenceTermByName("test term", source));
	}
	
	/**
	 * @see ConceptService#saveConceptReferenceTerm(ConceptReferenceTerm)
	 */
	@Test
	public void saveConceptReferenceTerm_shouldUpdateChangesToTheConceptReferenceTermInTheDatabase() {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		//sanity checks
		assertEquals(Context.getConceptService().getConceptSource(1), term.getConceptSource());
		assertNull(term.getChangedBy());
		assertNull(term.getDateChanged());
		term.setName("new name");
		term.setCode("new code");
		term.setDescription("new descr");
		ConceptSource conceptSource2 = Context.getConceptService().getConceptSource(2);
		term.setConceptSource(conceptSource2);
		
		ConceptReferenceTerm editedTerm = Context.getConceptService().saveConceptReferenceTerm(term);
		Context.flushSession();
		assertEquals("new name", editedTerm.getName());
		assertEquals("new code", editedTerm.getCode());
		assertEquals("new descr", editedTerm.getDescription());
		assertEquals(conceptSource2, editedTerm.getConceptSource());
		//The auditable fields should have been set
		assertNotNull(term.getChangedBy());
		assertNotNull(term.getDateChanged());
	}
	
	/**
	 * @see ConceptService#unretireConceptMapType(ConceptMapType)
	 */
	@Test
	public void unretireConceptMapType_shouldUnretireTheSpecifiedConceptMapTypeAndDropAllRetireRelatedFields()
	{
		ConceptMapType mapType = Context.getConceptService().getConceptMapType(6);
		assertTrue(mapType.getRetired());
		assertNotNull(mapType.getRetiredBy());
		assertNotNull(mapType.getDateRetired());
		assertNotNull(mapType.getRetireReason());
		ConceptMapType unRetiredMapType = Context.getConceptService().unretireConceptMapType(mapType);
		assertFalse(unRetiredMapType.getRetired());
		assertNull(unRetiredMapType.getRetireReason());
		assertNull(unRetiredMapType.getRetiredBy());
		assertNull(unRetiredMapType.getDateRetired());
	}
	
	/**
	 * @see ConceptService#unretireConceptReferenceTerm(ConceptReferenceTerm)
	 */
	@Test
	public void unretireConceptReferenceTerm_shouldUnretireTheSpecifiedConceptReferenceTermAndDropAllRetireRelatedFields()
	{
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(11);
		assertTrue(term.getRetired());
		assertNotNull(term.getRetireReason());
		assertNotNull(term.getRetiredBy());
		assertNotNull(term.getDateRetired());
		ConceptReferenceTerm retiredTerm = Context.getConceptService().unretireConceptReferenceTerm(term);
		assertFalse(retiredTerm.getRetired());
		assertNull(retiredTerm.getRetireReason());
		assertNull(retiredTerm.getRetiredBy());
		assertNull(retiredTerm.getDateRetired());
	}
	
	/**
	 * @see ConceptService#getAllConceptReferenceTerms()
	 */
	@Test
	public void getAllConceptReferenceTerms_shouldReturnAllConceptReferenceTermsInTheDatabase() {
		assertEquals(11, Context.getConceptService().getAllConceptReferenceTerms().size());
	}
	
	/**
	 * @see ConceptService#getConceptMappingsToSource(ConceptSource)
	 */
	@Test
	public void getConceptMappingsToSource_shouldReturnAListOfConceptMapsFromTheGivenSource() {
		assertEquals(8, Context.getConceptService().getConceptMappingsToSource(
		    Context.getConceptService().getConceptSource(1)).size());
	}
	
	/**
	 * @see ConceptService#getReferenceTermMappingsTo(ConceptReferenceTerm)
	 */
	@Test
	public void getReferenceTermMappingsTo_shouldReturnAllConceptReferenceTermMapsWhereTheSpecifiedTermIsTheTermB()
	{
		assertEquals(2, Context.getConceptService().getReferenceTermMappingsTo(
		    Context.getConceptService().getConceptReferenceTerm(4)).size());
	}
	
	/**
	 * @see ConceptService#getCountOfConcepts(String, List, boolean, List, List, List, List, Concept)
	 */
	@Test
	public void getCountOfConcepts_shouldReturnACountOfUniqueConcepts() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		assertEquals(2, conceptService.getCountOfConcepts("trust", Collections.singletonList(Locale.ENGLISH), false,
		    null, null, null, null, null).intValue());
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldNotFailWhenADuplicateNameIsEditedToAUniqueValue() {
		//Insert a row to simulate an existing duplicate fully specified/preferred name that needs to be edited
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-conceptWithDuplicateName.xml");
		Concept conceptToEdit = conceptService.getConcept(10000);
		Locale locale = new Locale("en", "GB");
		conceptToEdit.addDescription(new ConceptDescription("some description",locale));
		ConceptName duplicateNameToEdit = conceptToEdit.getFullySpecifiedName(locale);
		//Ensure the name is a duplicate in it's locale
		Concept otherConcept = conceptService.getConcept(5497);
		assertTrue(duplicateNameToEdit.getName().equalsIgnoreCase(
		    otherConcept.getFullySpecifiedName(locale).getName()));
		
		duplicateNameToEdit.setName("new unique name");
		conceptService.saveConcept(conceptToEdit);
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTerms(String,ConceptSource,Integer,Integer,null)
	 */
	@Test
	public void getConceptReferenceTerms_shouldReturnUniqueTermsWithACodeOrNameContainingTheSearchPhrase() {
		List<ConceptReferenceTerm> matches = Context.getConceptService().getConceptReferenceTerms("cd4", null, null, null,
		    true);
		assertEquals(3,

		matches.size());
		Set<ConceptReferenceTerm> uniqueTerms = new HashSet<>();
		//check that we have only unique terms
		for (ConceptReferenceTerm conceptReferenceTerm : matches) {
			assertTrue(uniqueTerms.add(conceptReferenceTerm));
		}
	}
	
	/**
	 * @see ConceptService#getConceptsByAnswer(ConceptClass)
	 */
	@Test
	public void getConceptsByAnswer_shouldFindAnswersForConcept() {
		Concept concept = conceptService.getConcept(7);
		assertNotNull(concept);
		List<Concept> concepts = conceptService.getConceptsByAnswer(concept);
		assertEquals(1, concepts.size());
		assertEquals(21, concepts.get(0).getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptsByClass(ConceptClass)
	 */
	@Test
	public void getConceptsByClass_shouldGetConceptsByClass() {				
		// replay
		List<Concept> actualConcepts = conceptService.getConceptsByClass(new ConceptClass(3));
		
		// verify
		assertThat(actualConcepts.size(), is(6));
		assertThat(actualConcepts,
		    containsInAnyOrder(conceptService.getConcept(3), conceptService.getConcept(60), conceptService.getConcept(64),
		        conceptService.getConcept(67), conceptService.getConcept(88), conceptService.getConcept(792)));
	}
	
	/**
	 * @see ConceptService#getConceptsByClass(ConceptClass)
	 */
	@Test
	public void getConceptsByClass_shouldReturnAnEmptyListIfNoneWasFound() {
		// setup
		ConceptClass cc = new ConceptClass(23);
		
		// replay
		List<Concept> concepts = conceptService.getConceptsByClass(cc);
		
		// verify
		assertThat(concepts, is(empty()));
	}
	
	/**
	 * @see ConceptService#getCountOfConceptReferenceTerms(String,ConceptSource,null)
	 */
	@Test
	public void getCountOfConceptReferenceTerms_shouldIncludeRetiredTermsIfIncludeRetiredIsSetToTrue() {
		assertEquals(11, conceptService.getCountOfConceptReferenceTerms("", null, true).intValue());
	}
	
	/**
	 * @see ConceptService#getCountOfConceptReferenceTerms(String,ConceptSource,null)
	 */
	@Test
	public void getCountOfConceptReferenceTerms_shouldNotIncludeRetiredTermsIfIncludeRetiredIsSetToFalse() {
		assertEquals(10, conceptService.getCountOfConceptReferenceTerms("", null, false).intValue());
	}
	
	/**
	 * @see ConceptService#getConceptsByName(String,Locale)
	 */
	@Test
	public void getConceptsByName_shouldReturnConceptsForAllCountriesAndGlobalLanguageGivenLanguageOnlyLocale()
	{
		//given
		String name = "Concept";
		Concept concept1 = new Concept();
		concept1.addName(new ConceptName(name, new Locale("en", "US")));
		concept1.addDescription(new ConceptDescription("some description",null));
		concept1.setDatatype(new ConceptDatatype(1));
		concept1.setConceptClass(new ConceptClass(1));
		Context.getConceptService().saveConcept(concept1);
		
		Concept concept2 = new Concept();
		concept2.addName(new ConceptName(name, new Locale("en", "GB")));
		concept2.addDescription(new ConceptDescription("some description",null));
		concept2.setDatatype(new ConceptDatatype(1));
		concept2.setConceptClass(new ConceptClass(1));
		Context.getConceptService().saveConcept(concept2);
		
		Concept concept3 = new Concept();
		concept3.addName(new ConceptName(name, new Locale("en")));
		concept3.addDescription(new ConceptDescription("some description",null));
		concept3.setDatatype(new ConceptDatatype(1));
		concept3.setConceptClass(new ConceptClass(1));
		Context.getConceptService().saveConcept(concept3);
		
		updateSearchIndex();
		
		//when
		List<Concept> concepts = Context.getConceptService().getConceptsByName(name, new Locale("en"), false);
		
		//then
		assertEquals(3, concepts.size());
		assertTrue(concepts.containsAll(Arrays.asList(concept1, concept2, concept3)));
	}
	
	/**
	 * @see ConceptService#getConceptsByName(String,Locale)
	 */
	@Test
	public void getConceptsByName_shouldReturnConceptsForSpecificCountryAndGlobalLanguageGivenLanguageAndCountryLocale()
	{
		//given
		String name = "Concept";
		Concept concept1 = new Concept();
		concept1.addName(new ConceptName(name, new Locale("en", "US")));
		concept1.addDescription(new ConceptDescription("some description",null));
		concept1.setDatatype(new ConceptDatatype(1));
		concept1.setConceptClass(new ConceptClass(1));
		Context.getConceptService().saveConcept(concept1);
		
		Concept concept2 = new Concept();
		concept2.addName(new ConceptName(name, new Locale("en", "GB")));
		concept2.addDescription(new ConceptDescription("some description",null));
		concept2.setDatatype(new ConceptDatatype(1));
		concept2.setConceptClass(new ConceptClass(1));
		Context.getConceptService().saveConcept(concept2);
		
		Concept concept3 = new Concept();
		concept3.addName(new ConceptName(name, new Locale("en")));
		concept3.addDescription(new ConceptDescription("some description",null));
		concept3.setDatatype(new ConceptDatatype(1));
		concept3.setConceptClass(new ConceptClass(1));
		Context.getConceptService().saveConcept(concept3);
		
		updateSearchIndex();
		
		//when
		List<Concept> concepts = Context.getConceptService().getConceptsByName(name, new Locale("en", "US"), false);
		
		//then
		assertThat(concepts.get(0), is(concept1));
		assertThat(concepts, containsInAnyOrder(concept1, concept2, concept3));
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldCreateAReferenceTermForAConceptMappingOnTheFlyWhenCreatingAConcept() {
		int initialTermCount = conceptService.getAllConceptReferenceTerms().size();
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("test name", Context.getLocale()));
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));
		ConceptMap map = new ConceptMap();
		map.getConceptReferenceTerm().setCode("unique code");
		map.getConceptReferenceTerm().setConceptSource(conceptService.getConceptSource(1));
		concept.addDescription(new ConceptDescription("some description",null));
		concept.addConceptMapping(map);
		conceptService.saveConcept(concept);
		assertNotNull(concept.getId());
		assertEquals(initialTermCount + 1, conceptService.getAllConceptReferenceTerms().size());
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldCreateAReferenceTermForAConceptMappingOnTheFlyWhenEditingAConcept() {
		int initialTermCount = conceptService.getAllConceptReferenceTerms().size();
		Concept concept = conceptService.getConcept(5497);
		ConceptMap map = new ConceptMap();
		map.getConceptReferenceTerm().setCode("unique code");
		map.getConceptReferenceTerm().setConceptSource(conceptService.getConceptSource(1));
		concept.addConceptMapping(map);
		conceptService.saveConcept(concept);
		assertEquals(initialTermCount + 1, conceptService.getAllConceptReferenceTerms().size());
	}
	
	/**
	 * @see ConceptService#getDefaultConceptMapType()
	 */
	@Test
	public void getDefaultConceptMapType_shouldReturnSameAsByDefault() {
		ConceptMapType conceptMapType = conceptService.getDefaultConceptMapType();
		assertNotNull(conceptMapType);
		assertEquals("same-as", conceptMapType.getName());
	}
	
	/**
	 * @see ConceptService#getDefaultConceptMapType()
	 */
	@Test
	public void getDefaultConceptMapType_shouldReturnTypeAsSetInGp() {
		final String testName = "is a";
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty("concept.defaultConceptMapType", testName));
		
		ConceptMapType conceptMapType = conceptService.getDefaultConceptMapType();
		assertNotNull(conceptMapType);
		assertEquals(testName, conceptMapType.getName());
	}
	
	/**
	 * @see ConceptService#getConceptMapTypeByName(String)
	 */
	@Test
	public void getConceptMapTypeByName_shouldBeCaseInsensitive() {
		String name = "SAME-as";
		ConceptMapType mt = Context.getConceptService().getConceptMapTypeByName(name);
		assertNotNull(mt);
		//sanity check in case the test dataset is edited
		assertNotSame(name, mt.getName());
		assertEquals(2, mt.getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptReferenceTermByName(String,ConceptSource)
	 */
	@Test
	public void getConceptReferenceTermByName_shouldBeCaseInsensitive() {
		String name = "WEIGHT term";
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTermByName(name, new ConceptSource(1));
		assertNotNull(term);
		assertNotSame(name, term.getName());
		assertEquals(1, term.getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptByName(String)
	 */
	@Test
	public void getConceptByName_shouldReturnNullGivenBlankString() {
		Concept concept = conceptService.getConceptByName("");
		assertNull(concept);
		concept = conceptService.getConceptByName("  ");
		assertNull(concept);
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldAddNewConceptName() {
		Concept concept = conceptService.getConcept(3);
		
		ConceptName name = new ConceptName("new name", Locale.US);
		
		concept.addName(name);
		
		conceptService.saveConcept(concept);
		assertNotNull(name.getConceptNameId());
	}
	
	/**
	 * @see ConceptService@getTrueConcept()
	 */
	@Test
	public void getTrueConcept_shouldReturnProperTrueConcept() {
		Concept trueConceptLoadedManually = Context.getConceptService().getConcept(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT));
		Concept trueConceptLoadedByServiceMethod = Context.getConceptService().getTrueConcept();
		assertTrue(trueConceptLoadedManually.equals(trueConceptLoadedByServiceMethod));
	}
	
	/**
	 * @see ConceptService@getFalseConcept()
	 */
	@Test
	public void getFalseConcept_shouldReturnProperFalseConcept() {
		Concept falseConceptLoadedManually = Context.getConceptService().getConcept(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT));
		Concept falseConceptLoadedByServiceMethod = Context.getConceptService().getFalseConcept();
		assertTrue(falseConceptLoadedManually.equals(falseConceptLoadedByServiceMethod));
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	@Disabled
	public void saveConcept_shouldNotSetAuditInfoIfTheConceptIsNotEdited() {
		Concept concept = conceptService.getConcept(3);
		assertNull(concept.getDateChanged());
		assertNull(concept.getChangedBy());
		conceptService.saveConcept(concept);
		
		assertNull(concept.getDateChanged());
		assertNull(concept.getChangedBy());
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldSetAuditInfoIfAnItemIsRemovedFromAnyOfItsChildCollections() {
		Concept concept = conceptService.getConcept(3);
		assertNull(concept.getDateChanged());
		assertNull(concept.getChangedBy());

		Concept concept1= conceptService.getConcept(5);
		ConceptAnswer conceptanswer = new ConceptAnswer(concept1);
		concept.addAnswer(conceptanswer);
		conceptService.saveConcept(concept);
		assertNotNull(concept.getDateChanged());
		Date date=concept.getDateChanged();

		assertTrue(concept.removeAnswer(conceptanswer));
		conceptService.saveConcept(concept);
		assertNotNull(concept.getDateChanged());
		Date date1=concept.getDateChanged();
		assertFalse(date.equals(date1));
		assertNotNull(concept.getChangedBy());
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldSetAuditInfoIfAnyItemInTheChildCollectionsIsEdited() {
		Concept concept = conceptService.getConcept(3);
		assertNull(concept.getDateChanged());
		assertNull(concept.getChangedBy());
		
		ConceptDescription description = concept.getDescription();
		assertNotNull(description);
		description.setDescription("changed to something else");
		conceptService.saveConcept(concept);
		
		assertNotNull(concept.getDateChanged());
		assertNotNull(concept.getChangedBy());
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldSetAuditInfoIfAnItemIsAddedToAnyOfItsChildCollections() {
		Concept concept = conceptService.getConcept(3);
		assertNull(concept.getDateChanged());
		assertNull(concept.getChangedBy());
		
		ConceptDescription description = new ConceptDescription("new description",Context.getLocale());
		concept.addDescription(description);
		conceptService.saveConcept(concept);
		assertNotNull(description.getConceptDescriptionId());
		
		assertNotNull(concept.getDateChanged());
		assertNotNull(concept.getChangedBy());
	}
	
	/**
	 * @see ConceptService#getDrugsByIngredient(Concept)
	 */
	@Test
	public void getDrugsByIngredient_shouldReturnDrugsMatchedByDrugConcept() {
		List<Drug> drugs = conceptService.getDrugsByIngredient(new Concept(792));
		assertEquals(1, drugs.size());
	}
	
	/**
	 * @see ConceptService#getDrugsByIngredient(Concept)
	 */
	@Test
	public void getDrugsByIngredient_shouldReturnDrugsMatchedByIntermediateConcept() {
		List<Drug> drugs = conceptService.getDrugsByIngredient(new Concept(88));
		assertEquals(2, drugs.size());
	}
	
	/**
	 * @see ConceptService#getDrugsByIngredient(Concept)
	 */
	@Test
	public void getDrugsByIngredient_shouldReturnEmptyListIfNothingFound() {
		List<Drug> drugs = conceptService.getDrugsByIngredient(new Concept(18));
		assertEquals(0, drugs.size());
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	@Test
	public void getConcepts_shouldReturnASearchResultWhoseConceptNameContainsAllWordTokensAsFirst() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("SALBUTAMOL INHALER", Collections
		        .singletonList(new Locale("en", "US")), false, null, null, null, null, null, null, null);
		
		assertThat(searchResults.get(0).getWord(), is("SALBUTAMOL INHALER"));
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	@Test
	public void getConcepts_shouldReturnASearchResultForPhraseWithStopWords() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		conceptService.saveConceptStopWord(new ConceptStopWord("OF", Locale.US));
		
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("tuberculosis of knee", Collections
		        .singletonList(new Locale("en", "US")), false, null, null, null, null, null, null, null);
		
		assertEquals(1, searchResults.size());
		assertEquals("Tuberculosis of Knee", searchResults.get(0).getConceptName().getName());
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	@Test
	public void getConcepts_shouldReturnConceptsWithSpecifiedClasses() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptClass> classes = new ArrayList<>();
		classes.add(Context.getConceptService().getConceptClassByName("Finding"));
		classes.add(Context.getConceptService().getConceptClassByName("LabSet"));
		List<ConceptSearchResult> searchResults = conceptService.getConcepts(null, null, false, classes, null, null, null,
		    null, null, null);
		assertEquals(2, searchResults.size());
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	@Test
	public void getConcepts_shouldReturnEmptyListIfNoConceptWithinSpecifiedClassesWasFound() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptClass> classes = new ArrayList<>();
		classes.add(Context.getConceptService().getConceptClassByName("Finding"));
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("SALBUTAMOL", null, false, classes, null, null, null,
		    null, null, null);
		assertEquals(0, searchResults.size());
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	@Test
	public void getConcepts_shouldIncludeRetiredConceptsInTheSearchResults() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptClass> classes = new ArrayList<>();
		classes.add(Context.getConceptService().getConceptClassByName("Finding"));
		List<ConceptSearchResult> searchResults = conceptService.getConcepts(null, null, true, classes, null, null, null,
		    null, null, null);
		assertEquals(2, searchResults.size());
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	@Test
	public void getConcepts_shouldExcludeSpecifiedClassesFromTheSearchResults() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		List<ConceptClass> classes = new ArrayList<>();
		classes.add(Context.getConceptService().getConceptClassByName("Finding"));
		classes.add(Context.getConceptService().getConceptClassByName("LabSet"));
		List<ConceptClass> excludeClasses = new ArrayList<>();
		excludeClasses.add(Context.getConceptService().getConceptClassByName("Finding"));
		List<ConceptSearchResult> searchResults = conceptService.getConcepts(null, null, false, classes, excludeClasses,
		    null, null, null, null, null);
		assertEquals(1, searchResults.size());
	}
	
	/**
	 * @see ConceptService#getConcepts(String,List<Locale>,null,List<ConceptClass>,List<
	 *      ConceptClass>,List<ConceptDatatype>,List<ConceptDatatype>,Concept,Integer,Integer)
	 */
	@Test
	public void getConcepts_shouldNotReturnConceptsWithMatchingNamesThatAreVoided() {
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
	public void getConcepts_shouldNotFailWithNullClassesAndDatatypes() {
		ConceptService conceptService = Context.getConceptService();
		assertNotNull(conceptService.getConcepts("VOIDED", Collections.singletonList(Locale.ENGLISH), false, null,
		    null, null, null, null, null, null));
	}
	
	/**
	 * @see ConceptServiceImpl# getCountOfConcepts(String phrase, List<Locale> locales, boolean
	 *      includeRetired,List<ConceptClass> requireClasses, List<ConceptClass> excludeClasses,
	 *      List<ConceptDatatype> requireDatatypes,List<ConceptDatatype> excludeDatatypes, Concept
	 *      answersToConcept)
	 */
	@Test
	public void getCountOfConcepts_shouldNotFailWithNullClassesAndDatatypes() {
		ConceptService conceptService = Context.getConceptService();
		assertNotNull(conceptService.getCountOfConcepts("VOIDED", Collections.singletonList(Locale.ENGLISH), false,
		    null, null, null, null, null));
	}
	
	/**
	 * @see ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept,Locale)
	 */
	@Test
	public void mapConceptProposalToConcept_shouldNotSetValueCodedNameWhenAddConceptIsSelected() {
		ConceptProposal cp = conceptService.getConceptProposal(2);
		assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED, cp.getState());
		final Concept civilStatusConcept = conceptService.getConcept(4);
		final int mappedConceptId = 6;
		assertThat(getObsService().getObservationsByPersonAndConcept(cp.getEncounter().getPatient(),
		    civilStatusConcept), is(empty()));
		Concept mappedConcept = conceptService.getConcept(mappedConceptId);
		
		cp.setObsConcept(civilStatusConcept);
		cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT);
		conceptService.mapConceptProposalToConcept(cp, mappedConcept, null);
		mappedConcept = conceptService.getConcept(mappedConceptId);
		List<Obs> observations = Context.getObsService().getObservationsByPersonAndConcept(cp.getEncounter().getPatient(),
		    civilStatusConcept);
		assertEquals(1, observations.size());
		Obs obs = observations.get(0);
		assertNull(obs.getValueCodedName());
	}
	
	/**
	 * @see ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept,Locale)
	 */
	@Test
	public void mapConceptProposalToConcept_shouldSetValueCodedNameWhenAddSynonymIsSelected() {
		ConceptProposal cp = conceptService.getConceptProposal(2);
		assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED, cp.getState());
		final Concept civilStatusConcept = conceptService.getConcept(4);
		final int mappedConceptId = 6;
		final String finalText = "Weight synonym";
		assertThat(getObsService().getObservationsByPersonAndConcept(cp.getEncounter().getPatient(),
		    civilStatusConcept), is(empty()));
		Concept mappedConcept = conceptService.getConcept(mappedConceptId);
		mappedConcept.addDescription(new ConceptDescription("some description",Context.getLocale()));
		
		cp.setFinalText(finalText);
		cp.setObsConcept(civilStatusConcept);
		cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM);
		conceptService.mapConceptProposalToConcept(cp, mappedConcept, null);
		mappedConcept = conceptService.getConcept(mappedConceptId);
		List<Obs> observations = Context.getObsService().getObservationsByPersonAndConcept(cp.getEncounter().getPatient(),
		    civilStatusConcept);
		assertEquals(1, observations.size());
		Obs obs = observations.get(0);
		assertNotNull(obs.getValueCodedName());
		assertEquals(finalText, obs.getValueCodedName().getName());
	}
	
	/**
	 * @see ConceptService#getAllConcepts(String,null,null)
	 */
	@Test
	public void getAllConcepts_shouldExcludeRetiredConceptsWhenSetIncludeRetiredToFalse() {
		final List<Concept> allConcepts = conceptService.getAllConcepts(null, true, false);
		
		assertEquals(36, allConcepts.size());
		assertEquals(3, allConcepts.get(0).getConceptId().intValue());
	}
	
	/**
	 * @see ConceptService#getAllConcepts(String,null,null)
	 */
	@Test
	public void getAllConcepts_shouldOrderByAConceptField() {
		List<Concept> allConcepts = conceptService.getAllConcepts("dateCreated", true, true);
		
		assertEquals(38, allConcepts.size());
		assertEquals(88, allConcepts.get(0).getConceptId().intValue());
		assertEquals(27, allConcepts.get(allConcepts.size() - 1).getConceptId().intValue());
		
		//check desc order
		allConcepts = conceptService.getAllConcepts("dateCreated", false, true);
		
		assertEquals(38, allConcepts.size());
		assertEquals(27, allConcepts.get(0).getConceptId().intValue());
		assertEquals(88, allConcepts.get(allConcepts.size() - 1).getConceptId().intValue());
	}
	
	/**
	 * @see ConceptService#getAllConcepts(String,null,null)
	 */
	@Test
	public void getAllConcepts_shouldOrderByAConceptNameField() {
		List<Concept> allConcepts = conceptService.getAllConcepts("name", true, false);
		
		assertEquals(34, allConcepts.size());
		assertEquals("ANTIRETROVIRAL TREATMENT GROUP", allConcepts.get(0).getName().getName());
		assertEquals("YES", allConcepts.get(allConcepts.size() - 1).getName().getName());
		
		//test the desc order
		allConcepts = conceptService.getAllConcepts("name", false, false);
		
		assertEquals(34, allConcepts.size());
		assertEquals("YES", allConcepts.get(0).getName().getName());
		assertEquals("ANTIRETROVIRAL TREATMENT GROUP", allConcepts.get(allConcepts.size() - 1).getName().getName());
	}
	
	/**
	 * @see ConceptService#getAllConcepts(String,null,null)
	 */
	@Test
	public void getAllConcepts_shouldOrderByConceptIdAndIncludeRetiredWhenGivenNoParameters() {
		final List<Concept> allConcepts = conceptService.getAllConcepts();
		
		assertEquals(38, allConcepts.size());
		assertEquals(3, allConcepts.get(0).getConceptId().intValue());
	}
	
	/**
	 * @see ConceptService#getAllConcepts(String,null,null)
	 */
	@Test
	public void getAllConcepts_shouldOrderByConceptIdDescendingWhenSetAscParameterToFalse() {
		final List<Concept> allConcepts = conceptService.getAllConcepts(null, false, true);
		
		assertEquals(38, allConcepts.size());
		assertEquals(5497, allConcepts.get(0).getConceptId().intValue());
	}
	
	/**
	 * @see ConceptService#mapConceptProposalToConcept(ConceptProposal,Concept,Locale)
	 */
	@Test
	public void mapConceptProposalToConcept_shouldFailWhenAddingADuplicateSyonymn() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-proposals.xml");
		ConceptService cs = Context.getConceptService();
		ConceptProposal cp = cs.getConceptProposal(10);
		cp.setFinalText(cp.getOriginalText());
		cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM);
		Concept mappedConcept = cs.getConcept(5);
		Locale locale = new Locale("en", "GB");
		mappedConcept.addDescription(new ConceptDescription("some description",locale));
		assertTrue(mappedConcept.hasName(cp.getFinalText(), locale));
		
		assertThrows(DuplicateConceptNameException.class, () -> cs.mapConceptProposalToConcept(cp, mappedConcept, locale));
	}
	
	/**
	 * @see ConceptService#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldPassWhenSavingAConceptAfterRemovingAName() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		Concept concept = conceptService.getConcept(3000);
		concept.addDescription(new ConceptDescription("some description",null));
		assertFalse(concept.getSynonyms().isEmpty());
		concept.removeName(concept.getSynonyms().iterator().next());
		conceptService.saveConcept(concept);
	}
	
	/**
	 * @see ConceptService#saveConceptNameTag(Object,Errors)
	 */
	@Test
	public void saveConceptNameTag_shouldNotSaveATagIfItIsInvalid() {
		ConceptNameTag cnt = new ConceptNameTag();
		ConceptService cs = Context.getConceptService();
		
		assertThrows(Exception.class, () -> cs.saveConceptNameTag(cnt));
	}
	
	/**
	 * @see ConceptService#saveConceptNameTag(Object,Errors)
	 */
	@Test
	public void saveConceptNameTag_shouldSaveATagIfItIsSupplied() {
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
	 * @see ConceptService#saveConceptNameTag(Object,Errors)
	 */
	@Test
	public void saveConceptNameTag_shouldSaveAnEditedNameTag() {
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
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldGetDrugsWithNamesMatchingTheSearchPhrase() {
		//Should be case insensitive
		List<Drug> drugs = conceptService.getDrugs("tri", null, false, false);
		assertThat(drugs, contains(conceptService.getDrug(2)));
	}
	
	/**
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldIncludeRetiredDrugsIfIncludeRetiredIsSetToTrue() {
		//Should be case insensitive
		final String searchPhrase = "Nyq";
		List<Drug> drugs = conceptService.getDrugs(searchPhrase, null, false, false);
		assertEquals(0, drugs.size());
		
		drugs = conceptService.getDrugs(searchPhrase, null, false, true);
		assertEquals(1, drugs.size());
		assertEquals(11, drugs.get(0).getDrugId().intValue());
	}
	
	/**
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldGetDrugsLinkedToConceptsWithNamesThatMatchThePhrase() {
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
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldGetDrugsLinkedToConceptsWithNamesThatMatchThePhraseAndLocale() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-drugSearch.xml");
		final String searchPhrase = "some";
		List<Drug> drugs = conceptService.getDrugs(searchPhrase, Locale.FRENCH, true, false);
		assertEquals(0, drugs.size());
		
		drugs = conceptService.getDrugs(searchPhrase, Locale.CANADA_FRENCH, true, false);
		assertEquals(1, drugs.size());
		assertEquals(3, drugs.get(0).getDrugId().intValue());
	}
	
	/**
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldGetDrugsLinkedToConceptsWithNamesThatMatchThePhraseAndRelatedLocales() {
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
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldGetDrugsThatHaveMappingsWithReferenceTermCodesThatMatchThePhrase() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-drugSearch.xml");
		List<Drug> drugs = conceptService.getDrugs("XXX", null, true, true);
		assertThat(drugs, contains(hasId(11), hasId(444)));
	}
	
	/**
	 * Ensures that unique drugs are returned in situations where more than one searched fields
	 * match e.g drug name and linked concept name match the search phrase
	 * 
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldReturnUniqueDrugs() {
		//sanity check that drug.name and drug.concept.name will both match the search phrase
		Drug drug = conceptService.getDrug("ASPIRIN");
		assertEquals(drug.getName().toLowerCase(), drug.getConcept().getName().getName().toLowerCase());
		
		List<Drug> drugs = conceptService.getDrugs("Asp", null, false, false);
		assertEquals(1, drugs.size());
		assertEquals(3, drugs.get(0).getDrugId().intValue());
	}
	
	/**
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldReturnAllDrugsWithAMatchingTermCodeOrDrugNameOrConceptName() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-drugSearch.xml");
		List<Drug> drugs = conceptService.getDrugs("XXX", null, false, true);
		assertThat(drugs, containsInAnyOrder(conceptService.getDrug(3), conceptService.getDrug(11), conceptService
		        .getDrug(444)));
	}
	
	/**
	 * @see ConceptService#getDrugs(String, java.util.Locale, boolean, boolean)
	 */
	@Test
	public void getDrugs_shouldRejectANullSearchPhrase() {
		assertThrows(IllegalArgumentException.class, () -> conceptService.getDrugs(null, null, false, false));
	}
	
	/**
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldGetAListOfAllDrugsThatMatchOnAllTheParameterValues() {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<ConceptMapType> conceptMapTypeList = new ArrayList<>();
		conceptMapTypeList.add(conceptService.getConceptMapType(1));
		ConceptSource source = conceptService.getConceptSource(1);
		List<Drug> drugs = conceptService.getDrugsByMapping("WGT234", source, conceptMapTypeList, false);
		assertEquals(1, drugs.size());
		assertTrue(containsId(drugs, 2));
	}
	
	/**
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldExcludeDuplicateMatches() {
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
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldReturnRetiredAndNonretiredDrugsIfIncludeRetiredIsSetToTrue() {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<ConceptMapType> conceptMapTypeList = conceptService.getConceptMapTypes(false, true);
		List<Drug> drugs = conceptService.getDrugsByMapping("WGT234", conceptService.getConceptSource(1),
		    conceptMapTypeList, true);
		assertEquals(2, drugs.size());
		assertTrue(containsId(drugs, 2));
		assertTrue(containsId(drugs, 11));
	}
	
	/**
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldReturnEmptyListIfNoMatchesAreFound() {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<ConceptMapType> conceptMapTypeList = conceptService.getConceptMapTypes(false, true);
		List<Drug> drugs = conceptService.getDrugsByMapping("some radom code", conceptService.getConceptSource(2),
		    conceptMapTypeList, false);
		assertThat(drugs, is(empty()));
	}
	
	/**
	 * @see ConceptService#getDrugsByMapping(String, ConceptSource, Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldMatchOnTheCode() {
		executeDataSet(GET_DRUG_MAPPINGS);
		ConceptSource source = conceptService.getConceptSource(1);
		List<Drug> drugs = conceptService.getDrugsByMapping("WGT234", source, null, false);
		assertEquals(1, drugs.size());
		assertTrue(containsId(drugs, 2));
	}
	
	/**
	 * @see ConceptService#getDrugsByMapping(String, ConceptSource, Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldMatchOnTheConceptSource() {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<Drug> drugs = conceptService.getDrugsByMapping(null, conceptService.getConceptSource(2), null, false);
		assertEquals(1, drugs.size());
		assertTrue(containsId(drugs, 2));
	}
	
	/**
	 * @see ConceptService#getDrugsByMapping(String, ConceptSource, Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldMatchOnTheMapTypes() {
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
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldFailIfNoCodeAndConceptSourceAndWithAnyOfTheseTypesAreProvided() {
		assertThrows(APIException.class, () -> conceptService.getDrugByMapping(null, null, null));
	}
	
	/**
	 * @see ConceptService#getDrugsByMapping(String, org.openmrs.ConceptSource,
	 *      java.util.Collection, boolean)
	 */
	@Test
	public void getDrugsByMapping_shouldFailIfSourceIsNull() { 
		APIException exception = assertThrows(APIException.class, () -> conceptService.getDrugsByMapping("random", null, null, false));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("ConceptSource.is.required")));
	}
	
	/**
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test
	public void getDrugByMapping_shouldReturnADrugThatMatchesTheCodeAndSourceAndTheBestMapType() {
		executeDataSet(GET_DRUG_MAPPINGS);
		final Integer expectedDrugId = 2;
		final ConceptSource source = conceptService.getConceptSource(2);
		final ConceptMapType mapTypeWithMatch = conceptService.getConceptMapType(1);
		final ConceptMapType mapTypeWithNoMatch = conceptService.getConceptMapType(2);
		List<ConceptMapType> conceptMapTypeList = new ArrayList<>();
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
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test
	public void getDrugByMapping_shouldFailIfMultipleDrugsAreFoundMatchingTheBestMapType() {
		executeDataSet(GET_DRUG_MAPPINGS);
		ConceptSource source = conceptService.getConceptSource(1);
		assertThrows(DAOException.class, () -> conceptService.getDrugByMapping("CD41003", source, Collections.singleton(conceptService.getConceptMapType(2))));
	}
	
	/**
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test
	public void getDrugByMapping_shouldReturnNullIfNoMatchFound() {
		executeDataSet(GET_DRUG_MAPPINGS);
		List<ConceptMapType> conceptMapTypeList = conceptService.getConceptMapTypes(false, true);
		Drug drug = conceptService.getDrugByMapping("random code", conceptService.getConceptSource(1), conceptMapTypeList);
		assertNull(drug);
	}
	
	/**
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test
	public void getDrugByMapping_shouldFailIfNoCodeAndConceptSourceAndWithAnyOfTheseTypesAreProvided() {
		assertThrows(APIException.class, () -> conceptService.getDrugByMapping(null, null, Collections.EMPTY_LIST));
	}
	
	/**
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test
	public void getDrugByMapping_shouldReturnADrugThatMatchesTheCodeAndSource() {
		executeDataSet(GET_DRUG_MAPPINGS);
		final Integer expectedDrugId = 2;
		Drug drug = conceptService.getDrugByMapping("WGT234", conceptService.getConceptSource(2), null);
		assertEquals(expectedDrugId, drug.getDrugId());
	}
	
	/**
	 * @see ConceptService#getDrugByMapping(String, org.openmrs.ConceptSource, java.util.Collection
	 */
	@Test
	public void getDrugByMapping_shouldFailIfSourceIsNull() {
		APIException exception = assertThrows(APIException.class, () -> conceptService.getDrugByMapping("random", null, null));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("ConceptSource.is.required")));
	}
	
	/**
	 * @see ConceptService#getOrderableConcepts(String, java.util.List, boolean, Integer, Integer)
	 */
	@Test
	public void getOrderableConcepts_shouldGetOrderableConcepts() {
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
		c1.addDescription(new ConceptDescription("some description",null));
		c1.setConceptClass(cc1);
		c1.setDatatype(dt);
		cs.saveConcept(c1);
		
		Concept c2 = new Concept();
		ConceptName cn2a = new ConceptName("ONE TO MANY", locale);
		c2.addName(cn2a);
		c2.addDescription(new ConceptDescription("some description",null));
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
	 */
	@Test
	public void getConcepts_shouldReturnPreferredNamesHigher() {
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
	 */
	@Test
	public void getConcepts_shouldFindConceptByFullCode() {
		//given
		String code1 = "CD41003";
		String code2 = "7345693";
		Concept concept = conceptService.getConceptByMapping(code2, "SNOMED CT");
		
		//when
		List<ConceptSearchResult> concepts1 = conceptService.getConcepts(code1,
				Collections.singletonList(Context.getLocale()), false,
		    null, null, null, null, null, null, null);
		List<ConceptSearchResult> concepts2 = conceptService.getConcepts(code2,
				Collections.singletonList(Context.getLocale()), false,
		    null, null, null, null, null, null, null);
		
		//then
		assertThat(concepts1, contains(hasConcept(is(concept))));
		assertThat(concepts2, contains(hasConcept(is(concept))));
	}

	/**
	 * @see ConceptService#getAllConceptAttributeTypes()
	 */
	@Test
	public void getAllConceptAttributeTypes_shouldReturnAllConceptAttributeTypesIncludingRetiredOnes() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		assertEquals(2, Context.getConceptService().getAllConceptAttributeTypes().size());
	}

	/**
	 * @see ConceptService#saveConceptAttributeType(org.openmrs.ConceptAttributeType)
	 */
	@Test
	public void saveConceptAttributeType_shouldCreateANewConceptAttributeType() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptService conceptService = Context.getConceptService();
		assertEquals(2, conceptService.getAllConceptAttributeTypes().size());
		ConceptAttributeType conceptAttributeType = new ConceptAttributeType();
		conceptAttributeType.setName("Another one");
		conceptAttributeType.setDatatypeClassname(FreeTextDatatype.class.getName());
		conceptService.saveConceptAttributeType(conceptAttributeType);
		assertNotNull(conceptAttributeType.getId());
		assertEquals(3, conceptService.getAllConceptAttributeTypes().size());
	}

	/**
	 * @see ConceptService#getConceptAttributeType(Integer)
	 */
	@Test
	public void getConceptAttributeType_shouldReturnTheConceptAttributeTypeWithTheGivenId() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		assertEquals("Audit Date", Context.getConceptService().getConceptAttributeType(1).getName());
	}

	/**
	 * @see ConceptService#getConceptAttributeType(Integer)
	 */
	@Test
	public void getConceptAttributeType_shouldReturnNullIfNoConceptAttributeTypeExistsWithTheGivenId() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		assertNull(Context.getConceptService().getConceptAttributeType(999));
	}

	/**
	 * @see ConceptService#getConceptAttributeTypeByUuid(String)
	 */
	@Test
	public void getConceptAttributeTypeByUuid_shouldReturnTheConceptAttributeTypeWithTheGivenUuid() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		assertEquals("Audit Date", Context.getConceptService().getConceptAttributeTypeByUuid(
				"9516cc50-6f9f-11e0-8414-001e378eb67e").getName());
	}

	/**
	 * @see ConceptService#getConceptAttributeTypeByUuid(String)
	 */
	@Test
	public void getConceptAttributeTypeByUuid_shouldReturnNullIfNoConceptAttributeTypeExistsWithTheGivenUuid()
	{
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		assertNull(Context.getConceptService().getConceptAttributeTypeByUuid("not-a-uuid"));
	}

	/**
	 * @see ConceptService#purgeConceptAttributeType(ConceptAttributeType)
	 */
	@Test
	public void purgeConceptAttributeType_shouldCompletelyRemoveAConceptAttributeType() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptService conceptService = Context.getConceptService();
		assertEquals(2, conceptService.getAllConceptAttributeTypes().size());
		conceptService.purgeConceptAttributeType(conceptService.getConceptAttributeType(2));
		assertEquals(1, conceptService.getAllConceptAttributeTypes().size());
	}

	/**
	 * @see ConceptService#getConceptAttributeTypes(String)
	 */
	@Test
	public void getConceptAttributeTypes_shouldSearchConceptAttributesByName() throws Exception{
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptService conceptService = Context.getConceptService();
		assertEquals(1, conceptService.getConceptAttributeTypes("we").size());
		assertEquals(0, conceptService.getConceptAttributeTypes("attr type").size());
	}

	/**
	 * @see ConceptService#getConceptAttributeTypeByName(String)
	 */
	@Test
	public void getConceptAttributeTypes_shouldGetConceptAttributeByExactName() throws Exception{
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptService conceptService = Context.getConceptService();
		assertNotNull(conceptService.getConceptAttributeTypeByName("Audit Date"));
		assertNull(conceptService.getConceptAttributeTypeByName("date"));
	}

	/**
	 * @see ConceptService#retireConceptAttributeType(ConceptAttributeType, String)
	 */
	@Test
	public void retireConceptAttributeType_shouldRetireAConceptAttributeType() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptAttributeType vat = Context.getConceptService().getConceptAttributeType(1);
		assertFalse(vat.getRetired());
		assertNull(vat.getRetiredBy());
		assertNull(vat.getDateRetired());
		assertNull(vat.getRetireReason());
		Context.getConceptService().retireConceptAttributeType(vat, "for testing");
		vat = Context.getConceptService().getConceptAttributeType(1);
		assertTrue(vat.getRetired());
		assertNotNull(vat.getRetiredBy());
		assertNotNull(vat.getDateRetired());
		assertEquals("for testing", vat.getRetireReason());
	}

	/**
	 * @see ConceptService#unretireConceptAttributeType(ConceptAttributeType)
	 */
	@Test
	public void unretireConceptAttributeType_shouldUnretireARetiredConceptAttributeType() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptService service = Context.getConceptService();
		ConceptAttributeType conceptAttributeType = service.getConceptAttributeType(2);
		assertTrue(conceptAttributeType.getRetired());
		assertNotNull(conceptAttributeType.getDateRetired());
		assertNotNull(conceptAttributeType.getRetiredBy());
		assertNotNull(conceptAttributeType.getRetireReason());
		service.unretireConceptAttributeType(conceptAttributeType);
		assertFalse(conceptAttributeType.getRetired());
		assertNull(conceptAttributeType.getDateRetired());
		assertNull(conceptAttributeType.getRetiredBy());
		assertNull(conceptAttributeType.getRetireReason());
	}

	/**
	 * @see ConceptService#getConceptAttributeByUuid(String)
	 */
	@Test
	public void getConceptAttributeByUuid_shouldGetTheConceptAttributeWithTheGivenUuid() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptService service = Context.getConceptService();
		assertEquals("2011-04-25", service.getConceptAttributeByUuid("3a2bdb18-6faa-11e0-8414-001e378eb67e")
				.getValueReference());
	}

	/**
	 * @see ConceptService#getConceptAttributeByUuid(String)
	 */
	@Test
	public void getConceptAttributeByUuid_shouldReturnNullIfNoConceptAttributeHasTheGivenUuid() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptService service = Context.getConceptService();
		assertNull(service.getConceptAttributeByUuid("not-a-uuid"));
	}

	/**
	 * @see ConceptService#hasAnyConceptAttribute(ConceptAttributeType)
	 */
	@Test
	public void hasAnyConceptAttribute_shouldReturnTrueIfAnyConceptAttributeIsPresentForGivenConceptAttributeType() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptService conceptService = Context.getConceptService();
		ConceptAttributeType conceptAttributeType = conceptService.getConceptAttributeType(1);
		assertTrue(conceptService.hasAnyConceptAttribute(conceptAttributeType));
	}

	/**
	 * @see ConceptService#hasAnyConceptAttribute(ConceptAttributeType)
	 */
	@Test
	public void hasAnyConceptAttribute_shouldReturnFalseIfNoConceptAttributeFoundForGivenConceptAttributeType() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		ConceptService conceptService = Context.getConceptService();
		ConceptAttributeType conceptAttributeType = conceptService.getConceptAttributeType(2);
		assertFalse(conceptService.hasAnyConceptAttribute(conceptAttributeType));
	}
	
	/**
	 * @see ConceptService#getConcepts(String, List, boolean, List, List, List, List, Concept, Integer, Integer)
	 */
	@Test
	public void getConcepts_shouldPassWithAndOrNotWords() {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-names.xml");
		
		//search phrase with AND
		List<ConceptSearchResult> searchResults = conceptService.getConcepts("AND SALBUTAMOL INHALER", Collections
		        .singletonList(new Locale("en", "US")), false, null, null, null, null, null, null, null);
		
		assertEquals(1, searchResults.size());
		assertThat(searchResults.get(0).getWord(), is("AND SALBUTAMOL INHALER"));
		
		//search phrase with OR
		searchResults = conceptService.getConcepts("SALBUTAMOL OR INHALER", Collections
	        .singletonList(new Locale("en", "US")), false, null, null, null, null, null, null, null);
	
		assertEquals(1, searchResults.size());
		assertThat(searchResults.get(0).getWord(), is("SALBUTAMOL OR INHALER"));
		
		//search phrase with NOT
		searchResults = conceptService.getConcepts("SALBUTAMOL INHALER NOT", Collections
	        .singletonList(new Locale("en", "US")), false, null, null, null, null, null, null, null);
	
		assertEquals(1, searchResults.size());
		assertThat(searchResults.get(0).getWord(), is("SALBUTAMOL INHALER NOT"));
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	@Test
	public void getConceptByReference_shouldFindAConceptByItsConceptName() {
		assertEquals(3, conceptService.getConceptByReference(TEST_CONCEPT_CONSTANT_NAME).getConceptId());
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	@Test
	public void getConceptByReference_shouldFindAConceptByItsConceptId() {
		assertEquals("COUGH SYRUP", conceptService.getConceptByReference(TEST_CONCEPT_CONSTANT_ID).getName().toString());
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	@Test
	public void getConceptByReference_shouldFindAConceptByItsMapping() {
		Concept concept = conceptService.getConceptByReference("SSTRM:454545");
		assertEquals(24, concept.getId().intValue());
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	@Test
	public void getConceptByReference_shouldFindAConceptByItsUuid() {
		assertEquals(60, conceptService.getConceptByReference(TEST_CONCEPT_CONSTANT_UUID).getConceptId());
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	@Test
	public void getConcept_shouldFindAConceptWithNonStandardUuid() throws Exception {
		String nonStandardUuid = "1000AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		assertEquals(64, conceptService.getConceptByReference(nonStandardUuid).getConceptId());
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String) tests static constant containing ids and
	 *      UUIDs
	 */
	@Test
	public void getConceptByReference_shouldFindAConceptWithStaticConstant() {
		assertNotNull(conceptService.getConceptByReference("org.openmrs.api.ConceptServiceTest.TEST_CONCEPT_CONSTANT_UUID"));
		assertNotNull(conceptService.getConceptByReference("org.openmrs.api.ConceptServiceTest.TEST_CONCEPT_CONSTANT_ID"));
		assertNotNull(conceptService.getConceptByReference("org.openmrs.api.ConceptServiceTest.TEST_CONCEPT_CONSTANT_NAME"));
	}
	
	/**
	 * @see ConceptService#getConceptByReference(String)
	 */
	@Test
	public void getConceptByReference_shouldReturnNullWhenEitherConceptRefIsInvalidOrDoesNotMatchAnyConcept() {
		assertNull(conceptService.getConceptByReference(null));  //given null 
		assertNull(conceptService.getConceptByReference(""));  //with empty string
		assertNull(conceptService.getConceptByReference("id, name or map which does not match to any concept"));
		assertNull(conceptService.getConceptByReference("1000")); //invalid uuid but exists in standardTestDataset
	}
}

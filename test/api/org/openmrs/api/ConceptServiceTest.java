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
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptWord;
import org.openmrs.Drug;
import org.openmrs.User;
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
		assertEquals("Unable to fetch concept by name", conceptByName, new Concept(1));
	}
	
	/**
	 * @see {@link ConceptService#getConceptByName(String)}
	 */
	@Test
	@Verifies(value = "should get concept by partial name", method = "getConceptByName(String)")
	public void getConceptByName_shouldGetConceptByPartialName() throws Exception {
		executeDataSet(INITIAL_CONCEPTS_XML);
		
		// substring of the name 
		String partialNameToFetch = "So";
		
		List<Concept> firstConceptsByPartialNameList = conceptService.getConceptsByName(partialNameToFetch);
		assertTrue("You should be able to get the concept by partial name", firstConceptsByPartialNameList
		        .contains(new Concept(1)));
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should save a ConceptNumeric as a concept", method = "saveConcept(Concept)")
	public void saveConcept_shouldSaveAConceptNumericAsAConcept() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(INITIAL_CONCEPTS_XML);
		authenticate();
		
		// this tests saving a previously conceptnumeric as just a concept
		Concept c2 = new Concept(2);
		c2.addName(new ConceptName("not a numeric anymore", Locale.US));
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
	@SkipBaseSetup
	@Verifies(value = "should save a new ConceptNumeric", method = "saveConcept(Concept)")
	public void saveConcept_shouldSaveANewConceptNumeric() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(INITIAL_CONCEPTS_XML);
		authenticate();
		
		// this tests saving a never before in the database conceptnumeric
		ConceptNumeric cn3 = new ConceptNumeric();
		cn3.setDatatype(new ConceptDatatype(1));
		cn3.addName(new ConceptName("a brand new conceptnumeric", Locale.US));
		cn3.setHiAbsolute(50.0);
		conceptService.saveConcept(cn3);
		
		Concept thirdConcept = conceptService.getConcept(cn3.getConceptId());
		assertTrue(thirdConcept instanceof ConceptNumeric);
		ConceptNumeric thirdConceptNumeric = (ConceptNumeric) thirdConcept;
		assertEquals("a brand new conceptnumeric", thirdConceptNumeric.getName(Locale.US).getName());
		assertEquals(50.0, thirdConceptNumeric.getHiAbsolute().doubleValue(), 0);
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept)}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should save non ConceptNumeric object as conceptNumeric", method = "saveConcept(Concept)")
	public void saveConcept_shouldSaveNonConceptNumericObjectAsConceptNumeric() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(INITIAL_CONCEPTS_XML);
		authenticate();
		
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
		assertEquals(20.0, firstConceptNumeric.getHiAbsolute().doubleValue(), 0);
		
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
		concept.addName(new ConceptName("Weight", Locale.US));
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
		concept.addName(new ConceptName("Weight", Locale.US));
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
		
		Concept concept = new Concept();
		concept.setPreferredName(Locale.ENGLISH, cn);
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));
		
		cs.saveConcept(concept);
		
		Collection<ConceptNameTag> savedConceptNameTags = concept.getBestName(Locale.ENGLISH).getTags();
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
	 * @see ConceptService#getConceptWords(String, List, boolean, List, List, List, List, Concept,
	 *      Integer, Integer)
	 */
	@Test
	@Verifies(value = "should return the best matched name", method = "getConceptWords(String,List<QLocale;>,null,List<QConceptClass;>,List<QConceptClass;>,List<QConceptDatatype;>,List<QConceptDatatype;>,Concept,Integer,Integer)")
	public void getConceptWords_shouldReturnTheBestMatchedName() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-words.xml");
		List<ConceptWord> words = Context.getConceptService().getConceptWords("cd4",
		    Collections.singletonList(Locale.ENGLISH), false, null, null, null, null, null, null, null);
		Assert.assertEquals(1847, words.get(0).getConceptName().getConceptNameId().intValue());
		
		TestUtil.printOutTableContents(getConnection(), "concept_word");
	}
	
	/**
	 * @see {@link ConceptService#getConceptByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should get concept with given code and source hl7 code", method = "getConceptByMapping(String,String)")
	public void getConceptByMapping_shouldGetConceptWithGivenCodeAndSourceHl7Code() throws Exception {
		Concept concept = conceptService.getConceptByMapping("WGT234", "SSTRM");
		Assert.assertEquals(new Concept(5089), concept);
	}
	
	/**
	 * @see {@link ConceptService#getConceptByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should get concept with given code and source hl7 name", method = "getConceptByMapping(String,String)")
	public void getConceptByMapping_shouldGetConceptWithGivenCodeAndSourceName() throws Exception {
		Concept concept = conceptService.getConceptByMapping("WGT234", "Some Standardized Terminology");
		Assert.assertEquals(new Concept(5089), concept);
	}
	
	/**
	 * @see {@link ConceptService#getConceptByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should return null if code does not exist", method = "getConceptByMapping(String,String)")
	public void getConceptByMapping_shouldReturnNullIfCodeDoesNotExist() throws Exception {
		Concept concept = conceptService.getConceptByMapping("A random concept code", "SSTRM");
		Assert.assertNull(concept);
	}
	
	/**
	 * @see {@link ConceptService#getConceptByMapping(String,String)}
	 */
	@Test
	@Verifies(value = "should return null if mapping does not exist", method = "getConceptByMapping(String,String)")
	public void getConceptByMapping_shouldReturnNullIfMappingDoesNotExist() throws Exception {
		Concept concept = conceptService.getConceptByMapping("WGT234", "A random mapping code");
		Assert.assertNull(concept);
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
		String uuid = "749b5078-8371-4849-aeab-181e3aed9415";
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
		// sanity check
		Assert.assertEquals(Context.getLocale(), Locale.UK);
		
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
		// sanity check
		Assert.assertEquals(Context.getLocale(), Locale.UK);
		
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
}

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
import org.junit.Rule;
import org.junit.Before;

import java.util.HashSet;
import java.util.Locale;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.Set;

import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.APIException;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Unit tests for methods that are specific to the {@link ConceptServiceImpl}. General tests that
 * would span implementations should go on the {@link ConceptService}.
 */
public class ConceptServiceImplTest extends BaseContextSensitiveTest {
	protected ConceptService conceptService = null;
	
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
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 * @verifies return the concept with new conceptID if creating new concept
	 */
	@Test
	public void saveConcept_shouldReturnTheConceptWithNewConceptIDIfCreatingNewConcept() throws Exception {
		Concept c = new Concept();
		ConceptName fullySpecifiedName = new ConceptName("requires one name min", new Locale("fr", "CA"));
		c.addName(fullySpecifiedName);
		c.addDescription(new ConceptDescription("some description",null));
		c.setDatatype(new ConceptDatatype(1));
		c.setConceptClass(new ConceptClass(1));
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
		c.addDescription(new ConceptDescription("some description",null));
		c.setDatatype(new ConceptDatatype(1));
		c.setConceptClass(new ConceptClass(1));
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
		c.addDescription(new ConceptDescription("some description",null));
		c.setDatatype(new ConceptDatatype(1));
		c.setConceptClass(new ConceptClass(1));
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
		c.addDescription(new ConceptDescription("some description",null));
		c.setDatatype(new ConceptDatatype(1));
		c.setConceptClass(new ConceptClass(1));

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
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));
		//When
		Context.getConceptService().saveConcept(concept);
		//Then
		assertNotEquals(concept.getName().getName(), nameWithSpaces);
		assertEquals(concept.getName().getName(), "jwm");
	}

	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 * @verifies force set flag if set members exist
	 */
	@Test
	public void saveConcept_shouldForceSetFlagIfSetMembersExist() throws Exception {
		//Given
		Concept concept = new Concept();
		concept.addName(new ConceptName("Concept", new Locale("en", "US")));
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));
		Concept conceptSetMember = new Concept();
		conceptSetMember.addName(new ConceptName("Set Member", new Locale("en", "US")));
		conceptSetMember.addDescription(new ConceptDescription("some description",null));
		conceptSetMember.setConceptClass(new ConceptClass(1));
		conceptSetMember.setDatatype(new ConceptDatatype(1));
		Context.getConceptService().saveConcept(conceptSetMember);
		concept.addSetMember(conceptSetMember);
		concept.setSet(false);
		//When
		Context.getConceptService().saveConcept(concept);
		//Then
		assertTrue(concept.getSet());
	}
	
	/**
	 * @see ConceptServiceImpl#retireConcept(Concept,String)
	 * @Verifies should fail to retire concept if no reason is given
	 */
	@Test
	public void retireConcept_shouldFailToRetireConceptIfReasonIsNotGiven() throws Exception {
		Concept concept = conceptService.getConcept(3);
		expectedException.expect(IllegalArgumentException.class);
		
		conceptService.retireConcept(concept,"");	
	}
	
	/**
	 * @see ConceptServiceImpl#retireConcept(Concept,String)
	 * @Verifies should retire concept
	 */
	@Test
	public void retireConcept_shouldRetireConcept() throws Exception {
		Concept concept = conceptService.getConcept(3);
		conceptService.retireConcept(concept, "dummy reason for retirement");
			
		assertTrue(concept.isRetired());
		assertEquals("dummy reason for retirement", concept.getRetireReason());
	}
	
	/**
	 * @see ConceptServiceImpl#getAllDrugs()
	 * @Verifies should return all the drugs 
	 */
	@Test
	public void getAllDrugs_shouldReturnAListOfAllDrugs() throws Exception {
		List<Drug> allDrugs = conceptService.getAllDrugs(true);
		//4 Drugs in the sample dataset
		
		assertEquals(4, allDrugs.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getDrugs(String, Concept, boolean, boolean, boolean, Integer, Integer)
	 * @Verifies return list of drugs
	 */
	@Test
	public void getDrugs_shouldReturnListOfDrugs() throws Exception {
		Concept concept = conceptService.getConceptByUuid("05ec820a-d297-44e3-be6e-698531d9dd3f");
		List<Drug> drugs = conceptService.getDrugs("ASPIRIN", concept, true, true, true, 0, 100 );
		assertEquals("ASPIRIN",drugs.get(0).getName());
	}
	
	/**
	 * @see ConceptServiceImpl#getDrug(String)
	 * @Verifies should return the given Drug taking drugNumber or ID as a param
	 */
	@Test
	public void getDrug_shouldReturnTheDrug() throws Exception {
		Drug drug = conceptService.getDrugByUuid("05ec820a-d297-44e3-be6e-698531d9dd3f");
		assertEquals(drug, conceptService.getDrug("ASPIRIN"));
	}
	
	/**
	 * @see ConceptServiceImpl#getDrug(String)
	 * @Verifies should return null if no drug is found 
	 */
	@Test
	public void getDrug_shouldReturnNullIfNoDrugIsFound() throws Exception {
		assertNull(conceptService.getDrug(123456));
				
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptClasses(boolean)
	 * @Verifies should return a list of all concept classes 
	 */
	@Test
	public void getAllConceptClasses_shouldReturnAllConceptClasses() throws Exception {
		List<ConceptClass> conceptClasses = conceptService.getAllConceptClasses(true);
		assertEquals(20, conceptClasses.size());
		
		//check for not retired classes
		conceptClasses = conceptService.getAllConceptClasses(false);
		assertEquals(20, conceptClasses.size());
		assertEquals(conceptService.getConceptClass(20), conceptClasses.get(19));
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConceptClass(ConceptClass)
	 * @Verifies should purge concept class
	 */
	@Test
	public void purgeConceptClass_shouldPurgeTheGivenConceptClass() throws Exception {
		ConceptClass cc = conceptService.getConceptClass(1);
		assertNotNull(cc);
		
		conceptService.purgeConceptClass(cc);
		assertNull(conceptService.getConceptClass(1));
		
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptDatatypes()
	 * @Verifies should return all concept datatypes
	 */
	@Test
	public void getAllConceptDatatypes_shouldReturnAllConceptDataypes() throws Exception {
		List<ConceptDatatype> conceptDatatypes = conceptService.getAllConceptDatatypes(true);
		assertEquals(12, conceptDatatypes.size());
		assertEquals(conceptService.getConceptDatatypeByUuid("8d4a4488-c2cc-11de-8d13-0010c6dffd0f"), conceptDatatypes.get(0));
	}
	
	/**
	 * @see ConceptServiceImpl#getSetsContainingConcept(Concept)
	 * @Verifies should return sets containing concept if given a valid concept
	 */
	@Test
	public void getSetsContainingConcept_shouldReturnSetsIfGivenValidConcept() throws Exception {
		Concept concept = conceptService.getConceptByUuid("0dde1358-7fcf-4341-a330-f119241a46e8");
		List<ConceptSet> conceptSets = conceptService.getSetsContainingConcept(concept);
		
		assertNotNull(conceptSets);
		assertEquals(conceptSets.get(0).getConcept(), concept);
	}
	
	/**
	 * @see ConceptServiceImpl#getSetsContainingConcept(Concept)
	 * @Verifies should return empty list if no set contains the given concept
	 */
	@Test
	public void getSetsContainingConcept_shouldReturnEmptyListIfGivenConceptWithNoSet() throws Exception {
		Concept concept = conceptService.getConceptByUuid("0cbe2ed3-cd5f-4f46-9459-26127c9265ab");
		List<ConceptSet> conceptSets = conceptService.getSetsContainingConcept(concept);
		
		assertEquals(conceptSets, java.util.Collections.emptyList());
	}
	
	/**
	 * @see ConceptServiceImpl#getConcepts(String, Locale, boolean)
	 * @Verifies return concepts in given locale
	 */
	@Test
	public void getConcepts_shouldReturnConceptsInGivenLocale() throws Exception {
		Locale locale = new Locale("en","GB");
		List<Locale> locales = new Vector<Locale>();
		locales.add(locale);
		List<ConceptSearchResult> res = conceptService.getConcepts("CD4 COUNT", locales, true, null, null, null, null, null, null, null);
		
		assertEquals("CD4 COUNT",res.get(0).getConceptName().getName());
		
	}
	
	/**
	 * @see ConceptServiceImpl#getDrigByIngredients(Concept)
	 * @Verifies should fail if no ingredient is given
	 */
	@Test
	public void getDrugsByIngredient_shouldFailIfGivenNoIngredient() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("ingredient is required");
		Concept nullConcept = null;
		
		conceptService.getDrugsByIngredient(nullConcept);
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptProposals(boolean)
	 * @Verifies should return a list of all concept proposals
	 */
	@Test
	public void getAllConceptProposals_shouldReturnAllConceptProposals() throws Exception {
		List<ConceptProposal> conceptProposals = conceptService.getAllConceptProposals(true);
		assertEquals(2, conceptProposals.size());
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConceptProposal(ConceptProposal)
	 * @Verifies should purge concept proposal
	 */
	@Test
	public void purgeConceptProposal_shouldPurgeTheGivenConceptProposal() throws Exception {
		conceptService.purgeConceptProposal(conceptService.getConceptProposal(2));
		assertNull(conceptService.getConceptProposal(2));
	}
	
	/**
	 * @see ConceptServiceImpl#getPrevConcept(Concept)
	 * @Verifies should return the concept previous to the given concept
	 */
	@Test
	public void getPrevConcept_shouldReturnThePrevConcept() throws Exception {
		Concept returnedConcept = conceptService.getPrevConcept(conceptService.getConcept(4));
		
		assertEquals(returnedConcept, conceptService.getConcept(3));
	}
	
	/**
	 * @see ConceptServiceImpl#getNextConcept(Concept)
	 * @Verifies should return the concept next to the given concept
	 */
	@Test
	public void getNextConcept_shouldReturnTheNextConcept() throws Exception {
		Concept returnedConcept = conceptService.getNextConcept(conceptService.getConcept(3));
		
		assertEquals(returnedConcept, conceptService.getConcept(4));
	}
	
	/**
	 * @see ConceptServiceImpl#getConceptsWithDrugsInFormulary()
	 * @Verifies should return the concepts with drugs in formula
	 */
	@Test
	public void getConceptsWithDrugsInFormulary_shouldReturnConcepts() throws Exception {
		List<Concept> concepts = conceptService.getConceptsWithDrugsInFormulary();
		//only 2 concepts in sample dataset which satisfy the concept
		assertEquals(2, concepts.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getMaxConceptId()
	 * @Verifies should give the max number of conceptId
	 */
	@Test
	public void getMaxConceptId_shouldGiveTheMaxConceptId() throws Exception {
		int num = conceptService.getMaxConceptId();
		assertEquals(5497, num);
	}
	
	/**
	 * @see ConceptServiceImpl#getLocalesOfConceptNames()
	 * @Verifies should give locales of concept names
	 */
	@Test
	public void getLocalesOfConceptNames_shouldReturnLocales() throws Exception {
		Set<Locale> locales = conceptService.getLocalesOfConceptNames();
		// all concept names in sample dataset have locale en-GB
		assertTrue(locales.contains(new Locale("en","GB")));
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptSources(bool) 
	 * @Verifies should return all concept sources
	 */
	@Test
	public void getAllConceptSources_shouldReturnConceptSources() throws Exception {
		List<ConceptSource> conceptSources = conceptService.getAllConceptSources(true);
		// 5 concept source in sample dataset
		assertEquals(5, conceptSources.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptNameTags
	 * @Verifies should return a lits of all concept name tags
	 */
	@Test
	public void getAllConceptNameTags_shouldReturnAllConceptNameTags() throws Exception {
		List<ConceptNameTag> conceptNameTags = conceptService.getAllConceptNameTags();
		// 15 concept name tags in sample dataset
		assertEquals(15, conceptNameTags.size());
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConceptSource(ConceptSource)
	 * @Verifies should purge the given concept source
	 */
	@Test
	public void purgeConceptSource_shouldPurgetTheConceptSource() throws Exception {
		ConceptSource conceptSource = conceptService.getConceptSource(1);
		conceptService.purgeConceptSource(conceptSource);
		
		assertNull(conceptService.getConceptSource(1));
	}
	
	/**
	 *@see ConceptServiceImpl#purgeConceptMapType(ConceptMapType)
	 *@Verifies should delete the conceptMapType
	 */
	@Test
	public void purgeConceptMapType_shouldPurgeConceptMapTypeIfInNotUse() throws Exception {
		ConceptMapType mapType = conceptService.getConceptMapType(8);
		assertNotNull(mapType);
		
		conceptService.purgeConceptMapType(mapType);
		assertNull(conceptService.getConceptMapType(8));
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConceptReferenceTerm(ConceptReferenceTerm)
	 * @Verifies should fail to purge concept reference term when given an in-use term
	 */
	@Test
	public void purgeConceptReferenceTerm_shouldFailIfGivenTermInUse() throws Exception {
		ConceptReferenceTerm refTerm = conceptService.getConceptReferenceTerm(1);
		assertNotNull(refTerm);
		expectedException.expect(APIException.class);
		expectedException.expectMessage("Reference term is in use");
		
		conceptService.purgeConceptReferenceTerm(refTerm);
		
	}
}

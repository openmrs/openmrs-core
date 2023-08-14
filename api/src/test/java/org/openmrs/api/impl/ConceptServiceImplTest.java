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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.openmrs.DrugReferenceMap;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Unit tests for methods that are specific to the {@link ConceptServiceImpl}. General tests that
 * would span implementations should go on the {@link ConceptService}.
 */
public class ConceptServiceImplTest extends BaseContextSensitiveTest {
	
	protected ConceptService conceptService = null;
	
	
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
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldReturnTheConceptWithNewConceptIDIfCreatingNewConcept() {
		Concept c = new Concept();
		ConceptName fullySpecifiedName = new ConceptName("requires one name min", new Locale("fr", "CA"));
		c.addName(fullySpecifiedName);
		c.addDescription(new ConceptDescription("some description", null));
		c.setDatatype(new ConceptDatatype(1));
		c.setConceptClass(new ConceptClass(1));
		Concept savedC = Context.getConceptService().saveConcept(c);
		assertNotNull(savedC);
		assertTrue(savedC.getConceptId() > 0);
	}
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 */
	
	@Test
	public void saveConcept_shouldReturnTheConceptWithSameConceptIDIfUpdatingExistingConcept() {
		Concept c = new Concept();
		ConceptName fullySpecifiedName = new ConceptName("requires one name min", new Locale("fr", "CA"));
		c.addName(fullySpecifiedName);
		c.addDescription(new ConceptDescription("some description", null));
		c.setDatatype(new ConceptDatatype(1));
		c.setConceptClass(new ConceptClass(1));
		Concept savedC = Context.getConceptService().saveConcept(c);
		assertNotNull(savedC);
		Context.flushSession(); //required for postgresql
		Concept updatedC = Context.getConceptService().saveConcept(c);
		assertNotNull(updatedC);
		assertEquals(updatedC.getConceptId(), savedC.getConceptId());
	}
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldLeavePreferredNamePreferredIfSet() {
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
		assertNotNull(c.getPreferredName(loc), "there's a preferred name");
		assertTrue(c.getPreferredName(loc).isPreferred(), "name was explicitly marked preferred");
		assertEquals(c.getPreferredName(loc).getName(), indexTerm.getName(), "name matches");
	}
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 */
	@Test
	public void saveConcept_shouldNotSetDefaultPreferredNameToShortOrIndexTerms() {
		Locale loc = new Locale("fr", "CA");
		ConceptName shortName = new ConceptName("short name", loc);
		shortName.setConceptNameType(ConceptNameType.SHORT); //be explicit for test case
		ConceptName indexTerm = new ConceptName("indexTerm", loc);
		indexTerm.setConceptNameType(ConceptNameType.INDEX_TERM); //synonyms are id'd by a null type
		
		Concept c = new Concept();
		HashSet<ConceptName> allNames = new HashSet<>(4);
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
		assertNull(c.getPreferredName(loc), "there's a preferred name");
		assertFalse(shortName.isPreferred(), "name was explicitly marked preferred");
		assertFalse(indexTerm.isPreferred(), "name was explicitly marked preferred");
	}
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 *           Concept.getPreferredName(locale) returns null, saveConcept chooses one. The default
	 *           first choice is the fully specified name in the locale. The default second choice
	 *           is a synonym in the locale.
	 */
	@Test
	public void saveConcept_shouldSetDefaultPreferredNameToFullySpecifiedFirst() {
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
		c.addDescription(new ConceptDescription("some description", null));
		c.setDatatype(new ConceptDatatype(1));
		c.setConceptClass(new ConceptClass(1));
		assertFalse(c.getFullySpecifiedName(loc).isPreferred(), "check test assumption - the API didn't automatically set preferred vlag");
			
		assertNotNull(Context.getConceptService().saveConcept(c), "Concept is legit, save succeeds");
		
		Context.flushSession(); //needed for postgresql
		Context.getConceptService().saveConcept(c);
		assertNotNull(c.getPreferredName(loc), "there's a preferred name");
		assertTrue(c.getPreferredName(loc).isPreferred(), "name was explicitly marked preferred");
		assertEquals(c.getPreferredName(loc).getName(), fullySpecifiedName.getName(), "name matches");
	}
	
	/**
	 * @see ConceptServiceImpl#saveConcept(Concept)
	 *           returns null, saveConcept chooses one. The default first choice is the fully
	 *           specified name in the locale. The default second choice is a synonym in the locale.
	 */
	@Test
	public void saveConcept_shouldSetDefaultPreferredNameToASynonymSecond() {
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
		HashSet<ConceptName> allNames = new HashSet<>(4);
		allNames.add(indexTerm);
		allNames.add(fullySpecifiedName);
		allNames.add(synonym);
		c.setNames(allNames);
		c.addDescription(new ConceptDescription("some description", null));
		c.setDatatype(new ConceptDatatype(1));
		c.setConceptClass(new ConceptClass(1));
		
		assertNull(c.getFullySpecifiedName(loc), "check test assumption - the API hasn't promoted a name to a fully specified name");
			
		Context.getConceptService().saveConcept(c);
		assertNotNull(c.getPreferredName(loc), "there's a preferred name");
		assertTrue(c.getPreferredName(loc).isPreferred(), "name was explicitly marked preferred");
		assertEquals(c.getPreferredName(loc).getName(), synonym.getName(), "name matches");
		assertEquals(c.getPreferredName(otherLocale).getName(), fullySpecifiedName.getName(), "fully specified name unchanged");
			
	}
	
	@Test
	public void saveConcept_shouldTrimWhitespacesInConceptName() {
		//Given
		Concept concept = new Concept();
		String nameWithSpaces = "  jwm  ";
		concept.addName(new ConceptName(nameWithSpaces, new Locale("en", "US")));
		concept.addDescription(new ConceptDescription("some description", null));
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
	 */
	@Test
	public void saveConcept_shouldForceSetFlagIfSetMembersExist() {
		//Given
		Concept concept = new Concept();
		concept.addName(new ConceptName("Concept", new Locale("en", "US")));
		concept.addDescription(new ConceptDescription("some description", null));
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));
		Concept conceptSetMember = new Concept();
		conceptSetMember.addName(new ConceptName("Set Member", new Locale("en", "US")));
		conceptSetMember.addDescription(new ConceptDescription("some description", null));
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
	 * @see ConceptServiceImpl#saveDrug(Drug) 
	 */
	@Test
	public void saveDrug_shouldPutGeneratedIdOntoReturnedDrug() {
		Drug drug = new Drug();
		Concept concept = new Concept();
		concept.addName(new ConceptName("Concept", new Locale("en", "US")));
		concept.addDescription(new ConceptDescription("Description", new Locale("en", "US")));
		concept.setConceptClass(new ConceptClass(1));
		concept.setDatatype(new ConceptDatatype(1));
		Concept savedConcept = conceptService.saveConcept(concept);
		drug.setConcept(savedConcept);
		assertNull(drug.getDrugId());
		Drug savedDrug = conceptService.saveDrug(drug);
		assertNotNull(savedDrug.getDrugId());
	}

	/**
	 * @see ConceptServiceImpl#saveDrug(Drug)
	 */
	@Test
	public void saveDrug_shouldCreateNewDrugInDatabase() {
		Drug drug = new Drug();
		Concept concept = new Concept();
		concept.addName(new ConceptName("Concept", new Locale("en", "US")));
		concept.addDescription(new ConceptDescription("Description", new Locale("en", "US")));
		concept.setConceptClass(new ConceptClass(1));
		concept.setDatatype(new ConceptDatatype(1));
		Concept savedConcept = conceptService.saveConcept(concept);
		drug.setConcept(savedConcept);
		drug.setName("Drug");
		assertNull(conceptService.getDrug("Drug"));
		Drug savedDrug = conceptService.saveDrug(drug);
		assertNotNull(conceptService.getDrug(savedDrug.getDrugId()));
	}

	/**
	 * @see ConceptServiceImpl#saveDrug(Drug)
	 */
	@Test
	public void saveDrug_shouldUpdateDrugAlreadyExistingInDatabase() {
		Drug drug = new Drug();
		Concept concept = new Concept();
		concept.addName(new ConceptName("Concept", new Locale("en", "US")));
		concept.addDescription(new ConceptDescription("Description", new Locale("en", "US")));
		concept.setConceptClass(new ConceptClass(1));
		concept.setDatatype(new ConceptDatatype(1));
		Concept savedConcept = conceptService.saveConcept(concept);
		drug.setConcept(savedConcept);
		drug.setCombination(false);
		Drug savedDrug = conceptService.saveDrug(drug);
		assertFalse(savedDrug.getCombination());
		savedDrug.setCombination(true);
		conceptService.saveDrug(savedDrug);
		assertTrue(conceptService.getDrug(savedDrug.getDrugId()).getCombination());
	}

	/**
	 * @see ConceptServiceImpl#saveDrug(Drug)
	 */
	@Test
	public void saveDrug_shouldSaveNewDrugReferenceMap() {
		Drug drug = new Drug();
		Concept concept = new Concept();
		concept.addName(new ConceptName("Concept", new Locale("en", "US")));
		concept.addDescription(new ConceptDescription("Description", new Locale("en", "US")));
		concept.setConceptClass(new ConceptClass(1));
		concept.setDatatype(new ConceptDatatype(1));
		Concept savedConcept = conceptService.saveConcept(concept);
		drug.setConcept(savedConcept);
		drug.setName("Example Drug");
		ConceptMapType sameAs = conceptService.getConceptMapTypeByUuid(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		ConceptSource snomedCt = conceptService.getConceptSourceByName("SNOMED CT");
		DrugReferenceMap map = new DrugReferenceMap();
		map.setDrug(drug);
		map.setConceptMapType(sameAs);
		map.setConceptReferenceTerm(new ConceptReferenceTerm(snomedCt, "example", ""));
		drug.addDrugReferenceMap(map);
		drug = conceptService.saveDrug(drug);
		assertEquals(1, conceptService.getDrug(drug.getDrugId()).getDrugReferenceMaps().size());
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConcept(Concept)
	 */
	@Test
	public void purgeConcept_shouldPurgeTheConceptIfNotBeingUsedByAnObs() {
		int conceptId = 88;
		conceptService.purgeConcept(conceptService.getConcept(conceptId));
		assertNull(conceptService.getConcept(conceptId));
	}
	
	/**
	 * @see ConceptServiceImpl#retireConcept(Concept,String)
	 */
	@Test
	public void retireConcept_shouldFailIfNoReasonIsGiven() {
		Concept concept = conceptService.getConcept(3);
		assertThrows(IllegalArgumentException.class, () -> conceptService.retireConcept(concept, ""));
	}
	
	/**
	 * @see ConceptServiceImpl#retireConcept(Concept,String)
	 */
	@Test
	public void retireConcept_shouldRetireTheGivenConcept() {
		String retireReason = "dummy reason";
		Concept concept = conceptService.getConcept(3);
		assertFalse(concept.getRetired());
		conceptService.retireConcept(concept, retireReason);
		assertTrue(concept.getRetired());
		assertEquals(retireReason, concept.getRetireReason());
	}
	
	/**
	 * @see ConceptServiceImpl#purgeDrug(Drug)
	 */
	@Test
	public void purgeDrug_shouldPurgeTheGivenDrug() {
		int drugId = 2;
		conceptService.purgeDrug(conceptService.getDrug(drugId));
		assertNull(conceptService.getDrug(drugId));
	}
	
	/**
	 * @see ConceptServiceImpl#getAllDrugs()
	 */
	@Test
	public void getAllDrugs_shouldReturnAListOfAllDrugs() {
		int resultWhenTrue = 4;
		List<Drug> allDrugs = conceptService.getAllDrugs();
		assertEquals(resultWhenTrue, allDrugs.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getAllDrugs(boolean)
	 */
	@Test
	public void getAllDrugs_shouldReturnAllDrugsIncludingRetiredOnesIfGivenTrue() {
		int resultWhenTrue = 4;
		List<Drug> allDrugs = conceptService.getAllDrugs(true);
		assertEquals(resultWhenTrue, allDrugs.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getAllDrugs(boolean)
	 */
	@Test
	public void getAllDrugs_shouldReturnAllDrugsExcludingRetiredOnesIfGivenFalse() {
		int resultWhenTrue = 2;
		List<Drug> allDrugs = conceptService.getAllDrugs(false);
		assertEquals(resultWhenTrue, allDrugs.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getDrugs(String, Concept, boolean, boolean, boolean, Integer,
	 *      Integer)
	 */
	@Test
	public void getDrugs_shouldReturnListOfMatchingDrugs() {
		String drugName = "ASPIRIN";
		String drugUuid = "05ec820a-d297-44e3-be6e-698531d9dd3f";
		Concept concept = conceptService.getConceptByUuid(drugUuid);
		List<Drug> drugs = conceptService.getDrugs(drugName, concept, true, true, true, 0, 100);
		assertTrue(drugs.contains(conceptService.getDrug(drugName)));
	}
	
	/**
	 * @see ConceptServiceImpl#getDrug(String)
	 */
	@Test
	public void getDrug_shouldReturnTheMatchingDrugObject() {
		String drugName = "ASPIRIN";
		String drugUuid = "05ec820a-d297-44e3-be6e-698531d9dd3f";
		Drug drug = conceptService.getDrugByUuid(drugUuid);
		assertEquals(drug, conceptService.getDrug(drugName));
	}
	
	/**
	 * @see ConceptServiceImpl#getDrug(String)
	 */
	@Test
	public void getDrug_shouldReturnNullIfNoMatchingDrugIsFound() {
		int drugIdNotPresent = 1234;
		assertNull(conceptService.getDrug(drugIdNotPresent));
	}
	
	/**
	 * @see ConceptServiceImpl#retireDrug(Drug, String)
	 */
	@Test
	public void retireDrug_shouldRetireTheGivenDrug() {
		String uuidOfDrugToCheck = "05ec820a-d297-44e3-be6e-698531d9dd3f";
		Drug drug = conceptService.getDrugByUuid(uuidOfDrugToCheck);
		conceptService.retireDrug(drug, "some dummy reason");
		assertTrue(drug.getRetired());
	}
	
	/**
	 * @see ConceptServiceImpl#unretireDrug(Drug)
	 */
	@Test
	public void unretireDrug_shouldMarkDrugAsNotRetired() {
		String uuidOfDrugToCheck = "7e2323fa-0fa0-461f-9b59-6765997d849e";
		Drug drug = conceptService.getDrugByUuid(uuidOfDrugToCheck);
		conceptService.unretireDrug(drug);
		assertFalse(drug.getRetired());
	}
	
	/**
	 * @see ConceptServiceImpl#unretireDrug(Drug)
	 */
	@Test
	public void unretireDrug_shouldNotChangeAttributesOfDrugThatIsAlreadyNotRetired() {
		String uuidOfDrugToCheck = "3cfcf118-931c-46f7-8ff6-7b876f0d4202";
		Drug drug = conceptService.getDrugByUuid(uuidOfDrugToCheck);
		assertFalse(drug.getRetired());
		conceptService.unretireDrug(drug);
		assertFalse(drug.getRetired());
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptClasses()
	 */
	@Test
	public void getAllConceptClasses_shouldReturnAListOfAllConceptClasses() {
		int resultSize = 20;
		List<ConceptClass> conceptClasses = conceptService.getAllConceptClasses();
		assertEquals(resultSize, conceptClasses.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptClasses(boolean)
	 */
	@Test
	public void getAllConceptClasses_shouldReturnAllConceptClassesIncludingRetiredOnesWhenGivenTrue() {
		int resultSizeWhenTrue = 20;
		List<ConceptClass> conceptClasses = conceptService.getAllConceptClasses(true);
		assertEquals(resultSizeWhenTrue, conceptClasses.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptClasses(boolean)
	 */
	@Test
	public void getAllConceptClasses_shouldReturnAllConceptClassesExcludingRetiredOnesWhenGivenFalse() {
		int resultSizeWhenFalse = 20;
		List<ConceptClass> conceptClasses = conceptService.getAllConceptClasses(false);
		assertEquals(resultSizeWhenFalse, conceptClasses.size());
	}
	
	/**
	 * @see ConceptServiceImpl#saveConceptClass(ConceptClass)
	 */
	@Test
	public void saveConceptClass_shouldSaveTheGivenConceptClass() {
		int unusedConceptClassId = 123;
		ConceptClass conceptClass = new ConceptClass(unusedConceptClassId);
		conceptClass.setName("name");
		conceptClass.setDescription("description");
		conceptService.saveConceptClass(conceptClass);
		assertEquals(conceptClass, conceptService.getConceptClass(unusedConceptClassId));
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConceptClass(ConceptClass)
	 */
	@Test
	public void purgeConceptClass_shouldDeleteTheGivenConceptClass() {
		int conceptClassId = 1;
		ConceptClass cc = conceptService.getConceptClass(conceptClassId);
		assertNotNull(cc);
		conceptService.purgeConceptClass(cc);
		assertNull(conceptService.getConceptClass(conceptClassId));
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptDatatypes()
	 */
	@Test
	public void getAllConceptDatatypes_shouldGiveAListOfAllConceptDataypes() {
		int resultSize = 12;
		String uuid = "8d4a4488-c2cc-11de-8d13-0010c6dffd0f";
		List<ConceptDatatype> conceptDatatypes = conceptService.getAllConceptDatatypes();
		assertEquals(resultSize, conceptDatatypes.size());
		assertTrue(conceptDatatypes.contains(conceptService.getConceptDatatypeByUuid(uuid)));
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptDatatypes(boolean)
	 */
	@Test
	public void getAllConceptDatatypes_shouldReturnAllConceptDataypesIncludingRetiredOnesWhenGivenTrue() {
		int resultSize = 12;
		String uuid = "8d4a4488-c2cc-11de-8d13-0010c6dffd0f";
		List<ConceptDatatype> conceptDatatypes = conceptService.getAllConceptDatatypes(true);
		assertEquals(resultSize, conceptDatatypes.size());
		assertTrue(conceptDatatypes.contains(conceptService.getConceptDatatypeByUuid(uuid)));
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptDatatypes(boolean)
	 */
	@Test
	public void getAllConceptDatatypes_shouldReturnAllConceptDataypesExcludingRetiredOnesWhenGivenFalse() {
		int resultSize = 12;
		String uuid = "8d4a4488-c2cc-11de-8d13-0010c6dffd0f";
		List<ConceptDatatype> conceptDatatypes = conceptService.getAllConceptDatatypes(false);
		assertEquals(resultSize, conceptDatatypes.size());
		assertTrue(conceptDatatypes.contains(conceptService.getConceptDatatypeByUuid(uuid)));
	}
	
	/**
	 * @see ConceptServiceImpl#getSetsContainingConcept(Concept)
	 */
	@Test
	public void getSetsContainingConcept_shouldGiveAListOfConceptSetContainingTheGivenConcept() {
		Concept concept = conceptService.getConcept(18);
		List<ConceptSet> conceptSets = conceptService.getSetsContainingConcept(concept);
		assertNotNull(conceptSets);
		assertEquals(conceptSets.get(0).getConcept(), concept);
	}
	
	/**
	 * @see ConceptServiceImpl#getSetsContainingConcept(Concept)
	 */
	@Test
	public void getSetsContainingConcept_shouldGiveAnEmptyListIfNoMatchingConceptSetIsFound() {
		String uuid = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab";
		Concept concept = conceptService.getConceptByUuid(uuid);
		List<ConceptSet> conceptSets = conceptService.getSetsContainingConcept(concept);
		assertEquals(conceptSets, Collections.emptyList());
	}
	
	/**
	 * @see ConceptServiceImpl#getSetsContainingConcept(Concept)
	 */
	@Test
	public void getSetsContainingConcept_shouldGiveAnEmptyListIfConceptIdIsNull() {
		List<ConceptSet> conceptSets = conceptService.getSetsContainingConcept(new Concept());
		assertEquals(conceptSets, Collections.emptyList());
	}
	
	/**
	 * @see ConceptServiceImpl#getConcepts(String, Locale, boolean)
	 */
	@Test
	public void getConcepts_shouldGiveAListOfConceptSearchResultForTheMatchingConcepts() {
		Locale locale = new Locale("en", "GB");
		String phrase = "CD4 COUNT";
		List<ConceptSearchResult> res = conceptService.getConcepts(phrase, locale, true);
		assertEquals(res.get(0).getConceptName().getName(), phrase);
	}
	
	/**
	 * @see ConceptServiceImpl#getDrugsByIngredient(Concept)
	 */
	@Test
	public void getDrugsByIngredient_shouldRaiseExceptionIfNoConceptIsGiven() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> conceptService.getDrugsByIngredient(null));
		assertThat(exception.getMessage(), is("ingredient is required"));
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptProposals(boolean)
	 */
	@Test
	public void getAllConceptProposals_shouldReturnAllConceptProposalsIncludingRetiredOnesWhenGivenTrue() {
		int matchedConceptProposals = 2;
		List<ConceptProposal> conceptProposals = conceptService.getAllConceptProposals(true);
		assertEquals(matchedConceptProposals, conceptProposals.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptProposals(boolean)
	 */
	@Test
	public void getAllConceptProposals_shouldReturnAllConceptProposalsExcludingRetiredOnesWhenGivenFalse() {
		int matchedConceptProposals = 1;
		List<ConceptProposal> conceptProposals = conceptService.getAllConceptProposals(false);
		assertEquals(matchedConceptProposals, conceptProposals.size());
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConceptProposal(ConceptProposal)
	 */
	@Test
	public void purgeConceptProposal_shouldPurgeTheGivenConceptProposal() {
		int conceptProposalId = 2;
		conceptService.purgeConceptProposal(conceptService.getConceptProposal(conceptProposalId));
		assertNull(conceptService.getConceptProposal(conceptProposalId));
	}
	
	/**
	 * @see ConceptServiceImpl#getPrevConcept(Concept)
	 */
	@Test
	public void getPrevConcept_shouldReturnTheConceptPreviousToTheGivenConcept() {
		Integer conceptId = 4;
		Integer prevConceptId = 3;
		Concept returnedConcept = conceptService.getPrevConcept(conceptService.getConcept(conceptId));
		assertEquals(returnedConcept, conceptService.getConcept(prevConceptId));
	}
	
	/**
	 * @see ConceptServiceImpl#getNextConcept(Concept)
	 */
	@Test
	public void getNextConcept_shouldReturnTheConceptNextToTheGivenConcept() {
		Integer conceptId = 3;
		Integer nextConceptId = 4;
		Concept returnedConcept = conceptService.getNextConcept(conceptService.getConcept(conceptId));
		assertEquals(returnedConcept, conceptService.getConcept(nextConceptId));
	}
	
	/**
	 * @see ConceptServiceImpl#getConceptsWithDrugsInFormulary()
	 */
	@Test
	public void getConceptsWithDrugsInFormulary_shouldGiveAListOfAllMatchingConcepts() {
		int matchingConcepts = 2;
		List<Concept> concepts = conceptService.getConceptsWithDrugsInFormulary();
		assertEquals(matchingConcepts, concepts.size());
	}
	
	/**
	 * @see ConceptService#getConceptsByAnswer(Concept)
	 */
	@Test
	public void getConceptsByAnswer_shouldReturnAnEmptyListIfConceptIdIsNull() {
		List<Concept> concepts = conceptService.getConceptsByAnswer(new Concept());
		assertEquals(concepts, Collections.emptyList());
	}
	
	/**
	 * @see ConceptServiceImpl#getMaxConceptId()
	 */
	@Test
	public void getMaxConceptId_shouldGiveTheMaximumConceptId() {
		int maxConceptId = 5497;
		assertEquals(new Integer(maxConceptId), conceptService.getMaxConceptId());
	}
	
	/**
	 * @see ConceptServiceImpl#getLocalesOfConceptNames()
	 */
	@Test
	public void getLocalesOfConceptNames_shouldReturnAListOfMatchingLocales() {
		Locale localeToSearch = new Locale("en", "GB");
		Set<Locale> locales = conceptService.getLocalesOfConceptNames();
		assertTrue(locales.contains(localeToSearch));
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptSources(boolean)
	 */
	@Test
	public void getAllConceptSources_shouldReturnAllConceptSourcesIncludingRetiredOnesWhenGivenTrue() {
		int conceptSourcesInDataset = 5;
		List<ConceptSource> conceptSources = conceptService.getAllConceptSources(true);
		assertEquals(conceptSourcesInDataset, conceptSources.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptSources(boolean)
	 */
	@Test
	public void getAllConceptSources_shouldReturnAllConceptSourcesExcludingRetiredOnesWhenGivenFalse() {
		int conceptSourcesInDataset = 3;
		List<ConceptSource> conceptSources = conceptService.getAllConceptSources(false);
		assertEquals(conceptSourcesInDataset, conceptSources.size());
	}
	
	/**
	 * @see ConceptServiceImpl#getAllConceptNameTags()
	 */
	@Test
	public void getAllConceptNameTags_shouldReturnAListOfAllConceptNameTags() {
		int conceptNameTagsInDataset = 15;
		List<ConceptNameTag> conceptNameTags = conceptService.getAllConceptNameTags();
		assertEquals(conceptNameTagsInDataset, conceptNameTags.size());
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConceptSource(ConceptSource)
	 */
	@Test
	public void purgeConceptSource_shouldPurgetTheGivenConceptSource() {
		Integer conceptSourceId = 1;
		ConceptSource conceptSource = conceptService.getConceptSource(conceptSourceId);
		conceptService.purgeConceptSource(conceptSource);
		assertNull(conceptService.getConceptSource(conceptSourceId));
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConceptMapType(ConceptMapType)
	 */
	@Test
	public void purgeConceptMapType_shouldDeleteTheSpecifiedConceptMapTypeFromTheDatabase() {
		Integer conceptMapTypeId = 8;
		ConceptMapType mapType = conceptService.getConceptMapType(conceptMapTypeId);
		assertNotNull(mapType);
		conceptService.purgeConceptMapType(mapType);
		assertNull(conceptService.getConceptMapType(conceptMapTypeId));
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConceptReferenceTerm(ConceptReferenceTerm)
	 */
	@Test
	public void purgeConceptReferenceTerm_shouldPurgeTheGivenConceptReferenceTerm() {
		Integer conceptReferenceTermId = 11;
		ConceptReferenceTerm refTerm = conceptService.getConceptReferenceTerm(conceptReferenceTermId);
		conceptService.purgeConceptReferenceTerm(refTerm);
		assertNull(conceptService.getConceptReferenceTerm(conceptReferenceTermId));
	}
	
	/**
	 * @see ConceptServiceImpl#getConceptReferenceTermByName(String, ConceptSource)
	 */
	@Test
	public void getConceptReferenceTermByName_shouldReturnNullIfNoConceptReferenceTermIsFound() {
		assertNull(conceptService.getConceptReferenceTermByName(null, new ConceptSource()));
	}
	
	/**
	 * @see ConceptServiceImpl#purgeConceptReferenceTerm(ConceptReferenceTerm)
	 */
	@Test
	public void purgeConceptReferenceTerm_shouldFailIfGivenConceptReferenceTermIsInUse() {
		ConceptReferenceTerm refTerm = conceptService.getConceptReferenceTerm(1);
		assertNotNull(refTerm);
		APIException exception = assertThrows(APIException.class, () -> conceptService.purgeConceptReferenceTerm(refTerm));
		assertThat(exception.getMessage(), is("Reference term is in use"));
	}
	
	/**
	 * @see ConceptServiceImpl#findConceptAnswers(String, Locale, Concept)
	 */
	@Test
	public void findConceptAnswers_shouldReturnAListOfAllMatchingConceptSearchResults() {
		Locale locale = new Locale("en", "GB");
		String phrase = "CD4 COUNT";
		int conceptId = 5497;
		List<ConceptSearchResult> concepts = conceptService.findConceptAnswers(phrase, locale,
		    conceptService.getConcept(conceptId));
		assertEquals(concepts.get(0).getConceptName().getName(), phrase);
	}
	
	/**
	 * @see ConceptServiceImpl#getOrderableConcepts(String, List, boolean, Integer, Integer)
	 */
	@Test
	public void getOrderableConcepts_shouldReturnAnEmptyListIfNoConceptSearchResultIsFound() {
		Integer someStartLength = 0;
		Integer someEndLength = 10;
		List<ConceptSearchResult> result = conceptService.getOrderableConcepts("some phrase", null, true, someStartLength,
		    someEndLength);
		assertEquals(result, Collections.emptyList());
	}
	
	/**
	 * @see ConceptServiceImpl#mapConceptProposalToConcept(ConceptProposal, Concept, Locale)
	 */
	@Test
	public void mapConceptProposalToConcept_shouldThrowAPIExceptionWhenMappingToNullConcept() {
		ConceptProposal cp = conceptService.getConceptProposal(2);
		Locale locale = new Locale("en", "GB");
		assertThrows(APIException.class, () -> conceptService.mapConceptProposalToConcept(cp, null, locale));
	}
	
	/**
	 * @see ConceptServiceImpl#getCountOfDrugs(String, Concept, boolean, boolean, boolean)
	 */
	@Test
	public void getCountOfDrugs_shouldReturnTheTotalNumberOfMatchingNumbers() {
		String phrase = "Triomune-30";
		int conceptId = 792;
		assertEquals(new Integer(1),
		    conceptService.getCountOfDrugs(phrase, conceptService.getConcept(conceptId), true, true, true));
	}

	/**
	 * @see ConceptServiceImpl#saveConceptProposal(ConceptProposal) 
	 */
	@Test
	public void saveConceptProposal_shouldReturnSavedConceptProposalObject() {
		final String ORIGINAL_TEXT = "OriginalText";
		ConceptProposal conceptProposal = new ConceptProposal();
		conceptProposal.setOriginalText(ORIGINAL_TEXT);
		List<ConceptProposal> existingConceptProposals = conceptService.getConceptProposals(ORIGINAL_TEXT);
		assertThat(existingConceptProposals, is(empty()));
		ConceptProposal savedConceptProposal = conceptService.saveConceptProposal(conceptProposal);
		assertEquals(ORIGINAL_TEXT, savedConceptProposal.getOriginalText());
		assertEquals(conceptProposal, savedConceptProposal);
	}

	/**
	 * @see ConceptServiceImpl#saveConceptProposal(ConceptProposal)
	 */
	@Test
	public void saveConceptProposal_shouldReturnUpdatedConceptProposalObject() {
		ConceptProposal conceptProposal = new ConceptProposal();
		conceptProposal.setOriginalText("OriginalText");
		ConceptProposal savedConceptProposal = conceptService.saveConceptProposal(conceptProposal);
		final String ANOTHER_ORIGINAL_TEXT = "AnotherOriginalText";
		savedConceptProposal.setOriginalText(ANOTHER_ORIGINAL_TEXT);
		ConceptProposal updatedConceptProposal = conceptService.saveConceptProposal(savedConceptProposal);
		assertEquals(ANOTHER_ORIGINAL_TEXT, updatedConceptProposal.getOriginalText());
	}

	/**
	 * @see ConceptServiceImpl#saveConceptProposal(ConceptProposal)
	 */
	@Test
	public void saveConceptProposal_shouldFailGivenNull() {
		assertThrows(IllegalArgumentException.class, () -> conceptService.saveConceptProposal(null));
	}

	/**
	 * @see ConceptServiceImpl#getConceptNameTagByName(String)
	 */
	@Test
	public void getConceptNameTagByName_shouldReturnNullIfNoConceptNameTagIsFound() {
		assertNull(conceptService.getConceptNameTagByName("random-tag"));
	}

	/**
	 * @see ConceptServiceImpl#getConceptNameTagByName(String)
	 */
	@Test
	public void getConceptNameTagByName_shouldReturnTheMatchingConceptNameTagObjectIfFound() {
		ConceptNameTag conceptNameTag = conceptService.getConceptNameTag(1);
		assertNotNull(conceptNameTag);
		assertEquals(conceptNameTag, conceptService.getConceptNameTagByName(conceptNameTag.getTag()));
	}
}

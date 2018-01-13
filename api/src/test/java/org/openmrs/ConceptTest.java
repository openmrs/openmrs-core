/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Behavior-driven tests of the Concept class.
 */
public class ConceptTest extends BaseContextSensitiveTest {
	
	/**
	 * When asked for a collection of compatible names, the returned collection should not include
	 * any incompatible names.
	 * 
	 * @see Concept#getCompatibleNames(Locale)
	 */
	@Test
	public void getCompatibleNames_shouldExcludeIncompatibleCountryLocales() {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createConcept(1, primaryLocale);
		
		// concept should only have US and generic english names.
		// add an incompatible name -- en_UK
		int initialNameCollectionSize = testConcept.getNames().size();
		ConceptName name_en_UK = createConceptName(initialNameCollectionSize + 1, "Labour", Locale.UK,
		    ConceptNameType.FULLY_SPECIFIED, false);
		testConcept.addName(name_en_UK);
		
		Collection<ConceptName> compatibleNames = testConcept.getCompatibleNames(primaryLocale);
		
		assertFalse(compatibleNames.contains(name_en_UK));
	}
	
	/**
	 * When asked for a collection of compatible names, the returned collection should not include
	 * any incompatible names.
	 * 
	 * @see Concept#getCompatibleNames(Locale)
	 */
	@Test
	public void getCompatibleNames_shouldExcludeIncompatibleLanguageLocales() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", new Locale("fr")));
		
		Assert.assertEquals(0, concept.getCompatibleNames(new Locale("en")).size());
	}
	
	/**
	 * The Concept should unmark the old conceptName as the locale preferred one to enforce the rule
	 * that a each locale should have only one preferred name per concept
	 * 
	 * @see Concept#setPreferredName(ConceptName)
	 */
	@Test
	public void setPreferredName_shouldOnlyAllowOnePreferredName() {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createConcept(1, primaryLocale);
		
		ConceptName initialPreferred = createConceptName(3, "Aspirin", primaryLocale, null, true);
		testConcept.addName(initialPreferred);
		Assert.assertEquals(true, initialPreferred.getLocalePreferred());
		ConceptName newPreferredName = createConceptName(4, "Doctor", primaryLocale, null, false);
		testConcept.setPreferredName(newPreferredName);
		
		assertEquals(false, initialPreferred.getLocalePreferred());
		assertEquals(true, newPreferredName.getLocalePreferred());
	}
	
	/**
	 * @see Concept#getDescription(Locale,null)
	 */
	@Test
	public void getDescription_shouldNotReturnLanguageOnlyMatchForExactMatches() {
		Concept mockConcept = new Concept();
		mockConcept.addDescription(new ConceptDescription("en desc", new Locale("en")));
		
		Assert.assertNull(mockConcept.getDescription(new Locale("en", "US"), true));
	}
	
	/**
	 * @see Concept#getDescription(Locale,null)
	 */
	@Test
	public void getDescription_shouldNotReturnMatchOnLanguageOnlyIfExactMatchExists() {
		Concept mockConcept = new Concept();
		mockConcept.addDescription(new ConceptDescription("en desc", new Locale("en")));
		mockConcept.addDescription(new ConceptDescription("en_US desc", new Locale("en", "US")));
		
		Concept mockConcept2 = new Concept();
		mockConcept2.addDescription(new ConceptDescription("en_US desc", new Locale("en", "US")));
		mockConcept2.addDescription(new ConceptDescription("en desc", new Locale("en")));
		
		Assert.assertEquals("en_US desc", mockConcept.getDescription(new Locale("en", "US"), false).getDescription());
		Assert.assertEquals("en_US desc", mockConcept2.getDescription(new Locale("en", "US"), false).getDescription());
	}
	
	/**
	 * @see Concept#getDescription(Locale,null)
	 */
	@Test
	public void getDescription_shouldReturnMatchOnLanguageOnly() {
		Concept mockConcept = new Concept();
		mockConcept.addDescription(new ConceptDescription("en desc", new Locale("en")));
		
		Assert.assertEquals("en desc", mockConcept.getDescription(new Locale("en", "US"), false).getDescription());
	}
	
	/**
	 * @see Concept#getDescription(Locale,null)
	 */
	@Test
	public void getDescription_shouldReturnMatchOnLocaleExactly() {
		Concept mockConcept = new Concept();
		mockConcept.addDescription(new ConceptDescription("en_US desc", new Locale("en", "US")));
		
		Assert.assertEquals("en_US desc", mockConcept.getDescription(new Locale("en", "US"), false).getDescription());
	}
	
	/**
	 * @see Concept#getName(Locale,null)
	 */
	@Test
	public void getName_shouldNotFailIfNoNamesAreDefined() {
		Concept concept = new Concept();
		Assert.assertNull(concept.getName(new Locale("en"), false));
		Assert.assertNull(concept.getName(new Locale("en"), true));
	}
	
	/**
	 * @see Concept#getName(Locale,null)
	 */
	@Test
	public void getName_shouldReturnExactNameLocaleMatchGivenExactEqualsTrue() {
		Locale definedNameLocale = new Locale("en", "US");
		Locale localeToSearch = new Locale("en", "US");
		
		Concept concept = new Concept();
		ConceptName fullySpecifiedName = new ConceptName("some name", definedNameLocale);
		fullySpecifiedName.setConceptNameId(1);
		fullySpecifiedName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		fullySpecifiedName.setLocalePreferred(false);
		concept.addName(fullySpecifiedName);
		Assert.assertNotNull(concept.getName(localeToSearch, true));
		Assert.assertEquals("some name", concept.getName(localeToSearch, true).getName());
	}
	
	/**
	 * @see Concept#getName(Locale,null)
	 */
	@Test
	public void getName_shouldReturnNullIfNoNamesAreFoundInLocaleGivenExactEqualsTrue() {
		Locale nonMatchingNameLocale = new Locale("en", "US");
		Locale localeToSearch = new Locale("en");
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", nonMatchingNameLocale));
		Assert.assertNull(concept.getName(localeToSearch, true));
	}
	
	/**
	 * @see Concept#getName(Locale,false)
	 */
	@Test
	public void getName_shouldReturnNameWithinSameLanguageIfExactEqualsFalse() {
		Locale localeToSearch = new Locale("en");
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("Test Concept", localeToSearch));
		Assert.assertEquals("Test Concept", (concept.getName(localeToSearch, false).toString()));
	}
	
	/**
	 * @see Concept#getNames(Boolean)
	 */
	@Test
	public void getNamesBoolean_shouldNotReturnVoidedConceptName() {
		Locale localeToSearch = new Locale("en");
		
		Concept concept = new Concept();
		ConceptName conceptName = new ConceptName("some name", localeToSearch);
		conceptName.setVoided(true);
		concept.addName(conceptName);
		Collection<ConceptName> cns = concept.getNames(false);
		Assert.assertNotNull(cns);
		Assert.assertEquals(cns.size(), 0);
		cns = concept.getNames(true);
		Assert.assertEquals(cns.size(), 1);
	}
	
	/**
	 * @see Concept#getNames()
	 */
	@Test
	public void getNames_shouldNotReturnVoidedConceptName() {
		Locale localeToSearch = new Locale("en");
		
		Concept concept = new Concept();
		ConceptName conceptName = new ConceptName("some name", localeToSearch);
		conceptName.setVoided(true);
		concept.addName(conceptName);
		Collection<ConceptName> cns = concept.getNames();
		Assert.assertNotNull(cns);
		Assert.assertEquals(cns.size(), 0);
	}
	
	/**
	 * @see Concept#getNames(Locale)
	 */
	@Test
	public void getNamesLocale_shouldReturnNonVoidedConceptName() {
		Locale localeToSearch = new Locale("en");
		Concept concept = new Concept();
		
		ConceptName conceptName = new ConceptName("some name", localeToSearch);
		conceptName.setVoided(true);
		concept.addName(conceptName);
		
		Collection<ConceptName> cns = concept.getNames(localeToSearch);
		Assert.assertEquals(cns.size(), 0);
	}
	
	/**
	 * @see Concept#getNames(Locale)
	 */
	@Test
	public void getNamesLocale_shouldReturnEmptyCollection() {
		Locale localeToSearch = new Locale("en");
		Concept concept = new Concept();
		
		Collection<ConceptName> cns = concept.getNames(localeToSearch);
		Assert.assertEquals(cns.size(), 0);
	}
	
	/**
	 * @see Concept#getBestName(Locale)
	 */
	@Test
	public void getBestNameLocale_shouldReturnNull() {
		Locale localeToSearch = new Locale("en");
		Concept concept = new Concept();
		ConceptName conceptName = concept.getName(localeToSearch);
		Assert.assertNull(conceptName);
	}
	
	/**
	 * @see Concept#getAnswers()
	 */
	@Test
	public void getAnswers_shouldNotReturnNullIfAnswersListIsNull() {
		Concept c = new Concept();
		c.setAnswers(null);
		Assert.assertNotNull(c.getAnswers());
		c.setAnswers(null);
		Assert.assertNotNull(c.getAnswers(true));
	}
	
	/**
	 * @see Concept#getAnswers()
	 */
	@Test
	public void getAnswers_shouldInitAnswersObject() {
		Concept c = new Concept();
		c.setAnswers(null); //make sure the list is null
		Assert.assertEquals(c.getAnswers(), c.getAnswers());
	}
	
	/**
	 * @see Concept#addAnswer(ConceptAnswer)
	 */
	@Test
	public void addAnswer_shouldNotFailIfAnswersListIsNull() {
		ConceptAnswer ca = new ConceptAnswer(123);
		Concept c = new Concept();
		c.setAnswers(null); // make sure the list is null
		c.addAnswer(ca);
	}
	
	/**
	 * @see Concept#getAnswers()
	 */
	@Test
	public void getAnswers_shouldReturnRetiredByDefault() {
		ConceptAnswer ca = new ConceptAnswer(new Concept(123));
		Concept c = new Concept();
		Assert.assertEquals(0, c.getAnswers().size());
		
		ca.getAnswerConcept().setRetired(false);//set test condition explicitly
		c.addAnswer(ca);
		
		ConceptAnswer ca2 = new ConceptAnswer(new Concept(456));
		ca2.getAnswerConcept().setRetired(true);
		c.addAnswer(ca2);
		Assert.assertEquals(2, c.getAnswers().size());
	}
	
	/**
	 * @see Concept#getAnswers()
	 */
	@Test
	public void getAnswers_shouldNotReturnRetiredIfFalse() {
		ConceptAnswer ca = new ConceptAnswer(new Concept(123));
		Concept c = new Concept();
		Assert.assertEquals(0, c.getAnswers(false).size());
		
		ca.getAnswerConcept().setRetired(false);//set test condition explicitly
		c.addAnswer(ca);
		
		ConceptAnswer ca2 = new ConceptAnswer(new Concept(456));
		ca2.getAnswerConcept().setRetired(true);
		c.addAnswer(ca2);
		Assert.assertEquals(1, c.getAnswers(false).size());
	}
	
	/**
	 * @see Concept#getAnswers()
	 */
	@Test
	public void getAnswers_shouldReturnRetiredIfTrue() {
		ConceptAnswer ca = new ConceptAnswer(new Concept(123));
		Concept c = new Concept();
		Assert.assertEquals(0, c.getAnswers(true).size());
		
		ca.getAnswerConcept().setRetired(false);//set test condition explicitly
		c.addAnswer(ca);
		
		ConceptAnswer ca2 = new ConceptAnswer(new Concept(456));
		ca2.getAnswerConcept().setRetired(true);
		c.addAnswer(ca2);
		Assert.assertEquals(2, c.getAnswers(true).size());
	}
	
	/**
	 * @see Concept#addAnswer(ConceptAnswer)
	 */
	@Test
	public void addAnswer_shouldSetTheSortWeightToTheMaxPlusOneIfNotProvided() {
		ConceptAnswer ca = new ConceptAnswer(123);
		Concept c = new Concept();
		c.setAnswers(null);//make sure null list
		c.addAnswer(ca);
		Assert.assertEquals(1d, ca.getSortWeight(), 0);
		
		ConceptAnswer ca2 = new ConceptAnswer(456);
		c.addAnswer(ca2);
		Assert.assertEquals(2d, ca2.getSortWeight(), 0);
	}
	
	/**
	 * @see Concept#setPreferredName(ConceptName)
	 */
	@Test
	public void setPreferredName_shouldAddTheNameToTheListOfNamesIfItNotAmongThemBefore() {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createConcept(1, primaryLocale);
		ConceptName newPreferredName = createConceptName(3, "Aspirin", primaryLocale, null, false);
		assertEquals(false, testConcept.getNames(primaryLocale).contains(newPreferredName));
		testConcept.setPreferredName(newPreferredName);
		assertEquals(true, testConcept.getNames(primaryLocale).contains(newPreferredName));
	}
	
	/**
	 * @see Concept#getFullySpecifiedName(Locale)
	 */
	@Test
	public void getFullySpecifiedName_shouldReturnTheNameMarkedAsFullySpecifiedForTheGivenLocale() {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createConcept(1, primaryLocale);
		ConceptName fullySpecifiedName_FR = createConceptName(3, "Docteur", new Locale("fr"),
		    ConceptNameType.FULLY_SPECIFIED, true);
		testConcept.addName(fullySpecifiedName_FR);
		Assert.assertEquals(primaryLocale, testConcept.getFullySpecifiedName(primaryLocale).getLocale());
		Assert.assertEquals(ConceptNameType.FULLY_SPECIFIED,
		    testConcept.getFullySpecifiedName(primaryLocale).getConceptNameType());
	}
	
	/**
	 * @see Concept#addSetMember(Concept,int)
	 */
	@Test
	public void addSetMember_shouldAddTheConceptToTheCurrentListOfConceptSet() {
		Concept concept = new Concept();
		Concept setMember = new Concept(1);
		
		Assert.assertEquals(0, concept.getConceptSets().size());
		
		concept.addSetMember(setMember);
		
		Assert.assertEquals(1, concept.getConceptSets().size());
		
	}
	
	/**
	 * @see Concept#addSetMember(Concept)
	 */
	@Test
	public void addSetMember_shouldAddConceptAsAConceptSet() {
		Concept concept = new Concept();
		Concept setMember = new Concept(1);
		concept.addSetMember(setMember);
		
		ConceptSet conceptSet = (ConceptSet) concept.getConceptSets().toArray()[0];
		
		Assert.assertEquals(setMember, conceptSet.getConcept());
	}
	
	/**
	 * @see Concept#addSetMember(Concept,int)
	 */
	@Test
	public void addSetMember_shouldAssignTheCallingComponentAsParentToTheConceptSet() {
		Concept concept = new Concept();
		Concept setMember = new Concept(11);
		concept.addSetMember(setMember);
		
		ConceptSet conceptSet = (ConceptSet) concept.getConceptSets().toArray()[0];
		
		Assert.assertEquals(concept, conceptSet.getConceptSet());
	}
	
	/**
	 * @see Concept#addSetMember(Concept)
	 */
	@Test
	public void addSetMember_shouldAppendConceptToExistingConceptSet() {
		Concept concept = new Concept();
		Concept setMember1 = new Concept(1);
		concept.addSetMember(setMember1);
		Concept setMember2 = new Concept(2);
		concept.addSetMember(setMember2);
		
		Assert.assertEquals(setMember2, concept.getSetMembers().get(1));
	}
	
	/**
	 * @see Concept#addSetMember(Concept)
	 */
	@Test
	public void addSetMember_shouldPlaceTheNewConceptLastInTheList() {
		Concept concept = new Concept();
		Concept setMember1 = new Concept(1);
		concept.addSetMember(setMember1, 3);
		Concept setMember2 = new Concept(2);
		concept.addSetMember(setMember2);
		
		Assert.assertEquals(setMember2, concept.getSetMembers().get(1));
	}
	
	/**
	 * @see Concept#getSetMembers()
	 */
	@Test
	public void getSetMembers_shouldReturnConceptSetMembersSortedAccordingToTheSortWeight() {
		Concept c = new Concept();
		ConceptSet set0 = new ConceptSet(new Concept(0), 3.0);
		ConceptSet set1 = new ConceptSet(new Concept(1), 2.0);
		ConceptSet set2 = new ConceptSet(new Concept(2), 1.0);
		ConceptSet set3 = new ConceptSet(new Concept(3), 0.0);
		
		List<ConceptSet> sets = new ArrayList<>();
		sets.add(set0);
		sets.add(set1);
		sets.add(set2);
		sets.add(set3);
		
		c.setConceptSets(sets);
		
		List<Concept> setMembers = c.getSetMembers();
		Assert.assertEquals(4, setMembers.size());
		Assert.assertEquals(set3.getConcept(), setMembers.get(0));
		Assert.assertEquals(set2.getConcept(), setMembers.get(1));
		Assert.assertEquals(set1.getConcept(), setMembers.get(2));
		Assert.assertEquals(set0.getConcept(), setMembers.get(3));
	}
	
	/**
	 * @see Concept#getSetMembers()
	 */
	@Test
	public void getSetMembers_shouldReturnConceptSetMembersSortedWithRetiredLast() {
		Concept c = new Concept();
		Concept retiredConcept = new Concept(3);
		retiredConcept.setRetired(true);
		Concept retiredConcept2 = new Concept(0);
		retiredConcept2.setRetired(true);
		Concept retiredConcept3 = new Concept(0);
		retiredConcept3.setRetired(true);
		ConceptSet set0 = new ConceptSet(retiredConcept, 3.0);
		ConceptSet set1 = new ConceptSet(new Concept(1), 2.0);
		ConceptSet set2 = new ConceptSet(new Concept(2), 1.0);
		ConceptSet set3 = new ConceptSet(retiredConcept2, 0.0);
		ConceptSet set4 = new ConceptSet();
		set4.setConcept(new Concept(3));
		ConceptSet set5 = new ConceptSet();
		set5.setConcept(retiredConcept3);
		
		List<ConceptSet> sets = new ArrayList<>();
		sets.add(set0);
		sets.add(set1);
		sets.add(set2);
		sets.add(set3);
		sets.add(set4);
		sets.add(set5);
		
		c.setConceptSets(sets);
		
		List<Concept> setMembers = c.getSetMembers();
		Assert.assertEquals(set4.getConcept(), setMembers.get(0));
		Assert.assertEquals(set2.getConcept(), setMembers.get(1));
		Assert.assertEquals(set1.getConcept(), setMembers.get(2));
		Assert.assertEquals(set5.getConcept(), setMembers.get(3));
		Assert.assertEquals(set3.getConcept(), setMembers.get(4));
		Assert.assertEquals(set0.getConcept(), setMembers.get(5));
	}
	
	/**
	 * @see Concept#getSetMembers()
	 */
	@Test
	public void getSetMembers_shouldReturnAllTheConceptMembersOfCurrentConcept() {
		Concept c = new Concept();
		
		Concept setMember1 = new Concept(12345);
		c.addSetMember(setMember1);
		
		Concept setMember2 = new Concept(67890);
		c.addSetMember(setMember2);
		
		List<Concept> setMembers = c.getSetMembers();
		
		Assert.assertEquals(2, setMembers.size());
		Assert.assertEquals(setMember1, setMembers.get(0));
		Assert.assertEquals(setMember2, setMembers.get(1));
	}
	
	/**
	 * @see Concept#getSetMembers()
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void getSetMembers_shouldReturnUnmodifiableListOfConceptMemberList() {
		Concept c = new Concept();
		c.addSetMember(new Concept(12345));
		List<Concept> setMembers = c.getSetMembers();
		
		Assert.assertEquals(1, setMembers.size());
		setMembers.add(new Concept());
	}
	
	/**
	 * @see Concept#addSetMember(Concept)
	 */
	@Test
	public void addSetMember_shouldAppendConceptToTheExistingListOfConceptSet() {
		Concept concept = new Concept();
		Concept firstSetMember = new Concept(2);
		concept.addSetMember(firstSetMember);
		
		Concept setMember = new Concept(3);
		concept.addSetMember(setMember);
		
		ConceptSet firstConceptSet = (ConceptSet) concept.getConceptSets().toArray()[0];
		ConceptSet secondConceptSet = (ConceptSet) concept.getConceptSets().toArray()[1];
		Assert.assertEquals(firstSetMember, firstConceptSet.getConcept());
		Assert.assertEquals(setMember, secondConceptSet.getConcept());
	}
	
	/**
	 * @see Concept#addSetMember(Concept,int)
	 */
	@Test
	public void addSetMember_shouldAssignTheGivenConceptAsAConceptSet() {
		Concept concept = new Concept();
		Concept setMember = new Concept(2);
		concept.addSetMember(setMember, 0);
		
		ConceptSet conceptSet = (ConceptSet) concept.getConceptSets().toArray()[0];
		Assert.assertEquals(setMember, conceptSet.getConcept());
	}
	
	/**
	 * @see Concept#addSetMember(Concept,int)
	 */
	@Test
	public void addSetMember_shouldInsertTheConceptBeforeTheFirstWithZeroIndex() {
		Concept concept = new Concept();
		Concept firstSetMember = new Concept(2);
		concept.addSetMember(firstSetMember);
		
		Concept setMember = new Concept(3);
		concept.addSetMember(setMember, 0);
		
		ConceptSet firstConceptSet = (ConceptSet) concept.getConceptSets().toArray()[0];
		ConceptSet secondConceptSet = (ConceptSet) concept.getConceptSets().toArray()[1];
		Assert.assertTrue(firstConceptSet.getSortWeight() < secondConceptSet.getSortWeight());
	}
	
	/**
	 * @see Concept#addSetMember(Concept,int)
	 */
	@Test
	public void addSetMember_shouldInsertTheConceptAtTheEndWithNegativeOneIndex() {
		Concept concept = new Concept();
		Concept firstSetMember = new Concept(2);
		concept.addSetMember(firstSetMember);
		
		Concept setMember = new Concept(3);
		concept.addSetMember(setMember, -1);
		
		ConceptSet secondConceptSet = (ConceptSet) concept.getConceptSets().toArray()[1];
		Assert.assertEquals(setMember, secondConceptSet.getConcept());
	}
	
	/**
	 * @see Concept#addSetMember(Concept,int)
	 */
	@Test
	public void addSetMember_shouldInsertTheConceptInTheThirdSlot() {
		Concept concept = new Concept();
		Concept firstSetMember = new Concept(2);
		concept.addSetMember(firstSetMember);
		
		Concept secondSetMember = new Concept(3);
		concept.addSetMember(secondSetMember);
		
		Concept thirdSetMember = new Concept(4);
		concept.addSetMember(thirdSetMember);
		
		Concept newThirdSetMember = new Concept(5);
		concept.addSetMember(newThirdSetMember, 2);
		
		ConceptSet thirdConceptSet = (ConceptSet) concept.getConceptSets().toArray()[2];
		Assert.assertEquals(newThirdSetMember, thirdConceptSet.getConcept());
	}
	
	/**
	 * @see Concept#getAllConceptNameLocales()
	 */
	@Test
	public void getAllConceptNameLocales_shouldReturnAllLocalesForConceptNamesForThisConceptWithoutDuplicates() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("name1", new Locale("en")));
		concept.addName(new ConceptName("name2", new Locale("en", "US")));
		concept.addName(new ConceptName("name3", new Locale("en", "UG")));
		concept.addName(new ConceptName("name4", new Locale("fr", "RW")));
		concept.addName(new ConceptName("name5", new Locale("en", "UK")));
		//add some names in duplicate locales
		concept.addName(new ConceptName("name6", new Locale("en", "US")));
		concept.addName(new ConceptName("name7", new Locale("en", "UG")));
		Set<Locale> localesForNames = concept.getAllConceptNameLocales();
		Assert.assertEquals(5, localesForNames.size());
	}
	
	/**
	 * @see Concept#getPreferredName(Locale)
	 */
	@Test
	public void getPreferredName_shouldReturnTheFullySpecifiedNameIfNoNameIsExplicitlyMarkedAsLocalePreferred() {
		Concept testConcept = createConcept(1, Locale.US);
		//preferred name in en_US
		ConceptName preferredNameEN_US = createConceptName(3, "Aspirin", Locale.US, null, false);
		testConcept.addName(preferredNameEN_US);
		String fullySpecName = testConcept.getFullySpecifiedName(Locale.US).getName();
		//preferred name in en
		ConceptName preferredNameEN = createConceptName(4, "Doctor", new Locale("en"), null, false);
		testConcept.addName(preferredNameEN);
		Assert.assertEquals(fullySpecName, testConcept.getPreferredName(Locale.US).getName());
	}
	
	/**
	 * @see Concept#getPreferredName(Locale)
	 */
	@Test
	public void getPreferredName_shouldReturnTheConceptNameExplicitlyMarkedAsLocalePreferred() {
		Concept testConcept = createConcept(1, Locale.US);
		//preferred name in en_US
		ConceptName preferredNameEN_US = createConceptName(3, "Aspirin", Locale.US, null, true);
		testConcept.addName(preferredNameEN_US);
		//preferred name in en
		ConceptName preferredNameEN = createConceptName(4, "Doctor", new Locale("en"), null, true);
		testConcept.addName(preferredNameEN);
		Assert.assertEquals(preferredNameEN_US, testConcept.getPreferredName(Locale.US));
		Assert.assertEquals(preferredNameEN, testConcept.getPreferredName(new Locale("en")));
	}
	
	/**
	 * @see Concept#getShortestName(Locale,Boolean)
	 */
	@Test
	public void getShortestName_shouldReturnTheShortestNameForTheConceptFromAnyLocaleIfExactIsFalse() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("shortName123", Context.getLocale()));
		concept.addName(new ConceptName("shortName12", Context.getLocale()));
		concept.addName(new ConceptName("shortName1", Locale.US));
		concept.addName(new ConceptName("shortName", Locale.FRANCE));
		Assert.assertEquals("shortName", concept.getShortestName(Context.getLocale(), false).getName());
	}
	
	/**
	 * @see Concept#getShortestName(Locale,Boolean)
	 */
	@Test
	public void getShortestName_shouldReturnTheShortestNameInAGivenLocaleForAConceptIfExactIsTrue() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("shortName123", Context.getLocale()));
		concept.addName(new ConceptName("shortName12", Context.getLocale()));
		concept.addName(new ConceptName("shortName1", Locale.US));
		concept.addName(new ConceptName("shortName", Locale.FRANCE));
		Assert.assertEquals("shortName12", concept.getShortestName(Context.getLocale(), true).getName());
	}
	
	/**
	 * @see Concept#setFullySpecifiedName(ConceptName)
	 */
	@Test
	public void setFullySpecifiedName_shouldAddTheNameToTheListOfNamesIfItNotAmongThemBefore() {
		Concept concept = createConcept(1, Context.getLocale());
		int expectedNumberOfNames = concept.getNames().size() + 1;
		concept.setFullySpecifiedName(new ConceptName("some name", Context.getLocale()));
		Assert.assertEquals(expectedNumberOfNames, concept.getNames().size());
	}
	
	/**
	 * @see Concept#setFullySpecifiedName(ConceptName)
	 */
	@Test
	public void setFullySpecifiedName_shouldConvertThePreviousFullySpecifiedNameIfAnyToASynonym() {
		Concept concept = createConcept(1, Context.getLocale());
		ConceptName oldFullySpecifiedName = concept.getFullySpecifiedName(Context.getLocale());
		//sanity check
		Assert.assertEquals(ConceptNameType.FULLY_SPECIFIED, oldFullySpecifiedName.getConceptNameType());
		
		concept.setFullySpecifiedName(new ConceptName("some name", Context.getLocale()));
		Assert.assertEquals(null, oldFullySpecifiedName.getConceptNameType());
	}
	
	/**
	 * @see Concept#setFullySpecifiedName(ConceptName)
	 */
	@Test
	public void setFullySpecifiedName_shouldSetTheConceptNameTypeOfTheSpecifiedNameToFullySpecified() {
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("some name", Context.getLocale());
		concept.setFullySpecifiedName(cn);
		Assert.assertEquals(ConceptNameType.FULLY_SPECIFIED, cn.getConceptNameType());
	}
	
	/**
	 * @see Concept#setShortName(ConceptName)
	 */
	@Test
	public void setShortName_shouldAddTheNameToTheListOfNamesIfItNotAmongThemBefore() {
		Concept concept = createConcept(1, Context.getLocale());
		int expectedNumberOfNames = concept.getNames().size() + 1;
		concept.setShortName(new ConceptName("some name", Context.getLocale()));
		Assert.assertEquals(expectedNumberOfNames, concept.getNames().size());
	}
	
	/**
	 * @see Concept#setShortName(ConceptName)
	 */
	@Test
	public void setShortName_shouldConvertThePreviousShortNameIfAnyToASynonym() {
		Concept concept = createConcept(1, Context.getLocale());
		ConceptName oldShortName = concept.getShortNameInLocale(Context.getLocale());
		//sanity check
		Assert.assertEquals(ConceptNameType.SHORT, oldShortName.getConceptNameType());
		
		concept.setShortName(new ConceptName("some name", Context.getLocale()));
		Assert.assertEquals(null, oldShortName.getConceptNameType());
	}
	
	/**
	 * @see Concept#setShortName(ConceptName)
	 */
	@Test
	public void setShortName_shouldSetTheConceptNameTypeOfTheSpecifiedNameToShort() {
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("some name", Context.getLocale());
		ConceptName FullySpecName = new ConceptName("fully spec name", Context.getLocale());
		concept.addName(FullySpecName);
		concept.setShortName(cn);
		Assert.assertEquals(ConceptNameType.SHORT, cn.getConceptNameType());
	}
	
	@Test
	public void setBlankShortName_shouldVoidTheOldOne() {
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("some name", Context.getLocale());
		ConceptName FullySpecName = new ConceptName("fully spec name", Context.getLocale());
		concept.addName(FullySpecName);
		concept.setShortName(cn);
		concept.setShortName(new ConceptName(" ", Context.getLocale()));
		assertThat(concept.getShortNameInLocale(Context.getLocale()), is(nullValue()));
	}
	
	/**
	 * @see Concept#getShortestName(Locale,Boolean)
	 */
	@Test
	public void getShortestName_shouldReturnTheNameMarkedAsTheShortNameForTheLocaleIfItIsPresent() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("shortName123", Context.getLocale()));
		concept.setShortName(new ConceptName("shortName12", Context.getLocale()));
		concept.setShortName(new ConceptName("shortName1", Locale.US));
		Assert.assertEquals("shortName1", concept.getShortestName(Locale.US, null).getName());
	}
	
	/**
	 * @see Concept#getShortestName(Locale,Boolean)
	 */
	@Test
	public void getShortestName_shouldReturnNullIfTheirAreNoNamesInTheSpecifiedLocaleAndExactIsTrue() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("shortName123", Context.getLocale()));
		concept.addName(new ConceptName("shortName12", Context.getLocale()));
		concept.addName(new ConceptName("shortName1", Locale.US));
		Assert.assertNull(concept.getShortestName(new Locale("fr"), true));
	}
	
	/**
	 * @see Concept#setPreferredName(ConceptName)
	 */
	@Test(expected = APIException.class)
	public void setPreferredName_shouldFailIfThePreferredNameToSetToIsAnIndexTerm() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", Context.getLocale()));
		ConceptName preferredName = new ConceptName("some pref name", Context.getLocale());
		preferredName.setLocalePreferred(true);
		preferredName.setConceptNameType(ConceptNameType.INDEX_TERM);
		concept.setPreferredName(preferredName);
	}
	
	/**
	 * @see Concept#addName(ConceptName)
	 */
	@Test
	public void addName_shouldMarkTheFirstNameAddedAsFullySpecified() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", Context.getLocale()));
		Assert.assertEquals("some name", concept.getFullySpecifiedName(Context.getLocale()).getName());
	}
	
	/**
	 * @see Concept#addName(ConceptName)
	 */
	@Test
	public void addName_shouldReplaceTheOldFullySpecifiedNameWithACurrentOne() {
		Concept concept = new Concept();
		ConceptName oldFullySpecName = new ConceptName("some name", Context.getLocale());
		concept.addName(oldFullySpecName);
		Assert.assertEquals(true, oldFullySpecName.isFullySpecifiedName());
		ConceptName newFullySpecName = new ConceptName("new name", Context.getLocale());
		newFullySpecName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		concept.addName(newFullySpecName);
		Assert.assertEquals(false, oldFullySpecName.isFullySpecifiedName());
		Assert.assertEquals("new name", concept.getFullySpecifiedName(Context.getLocale()).getName());
	}
	
	/**
	 * @see Concept#addName(ConceptName)
	 */
	@Test
	public void addName_shouldReplaceTheOldPreferredNameWithACurrentOne() {
		Concept concept = new Concept();
		ConceptName oldPreferredName = new ConceptName("some name", Context.getLocale());
		oldPreferredName.setLocalePreferred(true);
		concept.addName(oldPreferredName);
		ConceptName newPreferredName = new ConceptName("new name", Context.getLocale());
		newPreferredName.setLocalePreferred(true);
		concept.addName(newPreferredName);
		Assert.assertEquals(false, oldPreferredName.isPreferred());
		Assert.assertEquals("new name", concept.getPreferredName(Context.getLocale()).getName());
	}
	
	/**
	 * @see Concept#addName(ConceptName)
	 */
	@Test
	public void addName_shouldReplaceTheOldShortNameWithACurrentOne() {
		Concept concept = new Concept();
		ConceptName oldShortName = new ConceptName("some name", Context.getLocale());
		oldShortName.setConceptNameType(ConceptNameType.SHORT);
		concept.addName(oldShortName);
		ConceptName newShortName = new ConceptName("new name", Context.getLocale());
		newShortName.setConceptNameType(ConceptNameType.SHORT);
		concept.addName(newShortName);
		Assert.assertEquals(false, oldShortName.isShort());
		Assert.assertEquals("new name", concept.getShortNameInLocale(Context.getLocale()).getName());
	}
	
	@Test
	public void getSynonyms_shouldSortPreferredFirst() {
		Concept concept = new Concept();
		ConceptName conceptNameNotPreferred = new ConceptName("Non Preferred", Locale.ENGLISH);
		ConceptName conceptNameNotPreferred2 = new ConceptName("Non Preferred2", Locale.ENGLISH);
		ConceptName conceptNamePreferred = new ConceptName("Preferred", Locale.ENGLISH);
		conceptNamePreferred.setLocalePreferred(true);
		concept.addName(conceptNameNotPreferred);
		concept.addName(conceptNameNotPreferred2);
		concept.addName(conceptNamePreferred);
		
		conceptNameNotPreferred.setConceptNameType(null);
		conceptNameNotPreferred2.setConceptNameType(null);
		conceptNamePreferred.setConceptNameType(null);
		
		ConceptName conceptNameExpectedPreferred = concept.getSynonyms(Locale.ENGLISH).iterator().next();
		assertEquals("Preferred", conceptNameExpectedPreferred.getName());
	}
	
	@Test
	public void getShortNameInLocale_shouldReturnTheBestShortNameForAConcept() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("Giant cat", new Locale("en")));
		concept.addName(new ConceptName("Gato gigante", new Locale("es", "MX")));
		
		ConceptName shortName1 = new ConceptName("Cat", new Locale("en"));
		shortName1.setConceptNameType(ConceptNameType.SHORT);
		concept.addName(shortName1);
		
		ConceptName shortName2 = new ConceptName("Gato", new Locale("es"));
		shortName2.setConceptNameType(ConceptNameType.SHORT);
		concept.addName(shortName2);
		
		Assert.assertEquals("Gato", concept.getShortNameInLocale(new Locale("es", "ES")).getName());
	}
	
	@Test
	public void getPreferredName_shouldReturnTheBesLocalePreferred() {
		Concept testConcept = createConcept(1, Locale.US);
		ConceptName preferredName = createConceptName(4, "Doctor", new Locale("en"), null, true);
		testConcept.addName(preferredName);
		Assert.assertEquals(preferredName.getName(), testConcept.getPreferredName(Locale.US).getName());
	}
	
	/**
	 * Convenient factory method to create a populated Concept with a one fully specified name and
	 * one short name
	 * 
	 * @param id the id for the concept to create
	 * @param locale the locale of the of the conceptNames for the concept to create
	 * @return the created concept
	 */
	private Concept createConcept(int id, Locale locale) {
		Concept result = new Concept();
		result.setConceptId(id);
		Locale desiredLocale;
		if (locale == null) {
			desiredLocale = Context.getLocale();
		} else {
			desiredLocale = locale;
		}
		result.addName(createConceptName(2, "intravenous", desiredLocale, ConceptNameType.FULLY_SPECIFIED, false));
		result.addName(createConceptName(1, "IV", desiredLocale, ConceptNameType.SHORT, false));
		return result;
	}
	
	/**
	 * Convenient factory method to create a populated Concept name.
	 * 
	 * @param id id for the conceptName
	 * @param locale the locale or context locale if null
	 * @param conceptNameType the conceptNameType of the concept
	 * @param isLocalePreferred if this name should be marked as preferred in its locale
	 */
	private ConceptName createConceptName(int id, String name, Locale locale, ConceptNameType conceptNameType,
	        Boolean isLocalePreferred) {
		ConceptName result = new ConceptName();
		result.setConceptNameId(id);
		result.setName(name);
		if (locale == null) {
			result.setLocale(Context.getLocale());
		} else {
			result.setLocale(locale);
		}
		result.setConceptNameType(conceptNameType);
		result.setLocalePreferred(isLocalePreferred);
		return result;
	}
	
	/**
	 * @see Concept#getName()
	 */
	@Test
	public void getName_shouldReturnNameInBroaderLocaleIncaseNoneIsFoundInSpecificOne() {
		Locale locale = new Locale("en");
		Locale localeToSearch = new Locale("en", "UK");
		Concept concept = new Concept();
		concept.addName(new ConceptName("Test Concept", locale));
		Assert.assertEquals((concept.getName(locale, false).toString()),
		    (concept.getName(localeToSearch, false).toString()));
	}
	
	/**
	 * @see Concept#getName()
	 */
	@Test
	public void getName_shouldReturnNameAnyNameIfNoLocaleMatchGivenExactEqualsFalse() {
		Locale locale = new Locale("en");
		Locale localeToSearch = new Locale("fr");
		Concept concept = new Concept();
		concept.addName(new ConceptName("Test Concept", locale));
		Assert.assertNotNull((concept.getName(localeToSearch, false)));
	}
	
	/**
	 * @see Concept#getDescriptions()
	 */
	@Test
	public void getDescriptions_shouldNotReturnNullIfDescriptionsListIsNull() {
		Concept c = new Concept();
		c.setDescriptions(null);
		Assert.assertTrue(c.getDescriptions().isEmpty());
		Assert.assertNotNull(c.getDescriptions());
	}
	
	/**
	 * @see Concept#hasName(String, Locale)
	 */
	@Test
	public void hasName_shouldReturnFalseIfNameIsNull() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("Test Concept", new Locale("en")));
		Locale localeToSearch = new Locale("en", "UK");
		Assert.assertFalse(concept.hasName(null, localeToSearch));
	}
	
	/**
	 * @see Concept#hasName(String, Locale)
	 */
	@Test
	public void hasName_shouldReturnTrueIfLocaleIsNullButNameExists() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("Test Concept", new Locale("en")));
		Assert.assertTrue(concept.hasName("Test Concept", null));
	}
	
	/**
	 * @see Concept#hasName(String, Locale)
	 */
	@Test
	public void hasName_shouldReturnFalseIfLocaleIsNullButNameDoesNotExist() {
		Concept concept = new Concept();
		concept.addName(new ConceptName("Test Concept", new Locale("en")));
		Assert.assertFalse(concept.hasName("Unknown concept", null));
	}
	
	/**
	 * @see Concept#removeDescription(ConceptDescription)
	 */
	@Test
	public void removeDescription_shouldRemoveDescriptionPassedFromListOfDescriptions() {
		Concept c = new Concept();
		ConceptDescription c1 = new ConceptDescription(1);
		c1.setDescription("Description 1");
		ConceptDescription c2 = new ConceptDescription(2);
		c2.setDescription("Description 2");
		c.addDescription(c1);
		c.addDescription(c2);
		Collection<ConceptDescription> descriptions = c.getDescriptions();
		Assert.assertEquals(2, descriptions.size());
		c.removeDescription(c1);
		descriptions = c.getDescriptions();
		Assert.assertTrue(descriptions.contains(c2));
		Assert.assertEquals(1, descriptions.size());
	}
	
	/**
	 * @see Concept#removeConceptMapping(ContentMap)
	 */
	@Test
	public void removeConceptMapping_shouldRemoveConceptMapPassedFromListOfMappings() {
		Concept c = new Concept();
		ConceptMap c1 = new ConceptMap(1);
		c1.setConceptMapType(new ConceptMapType(1));
		ConceptMap c2 = new ConceptMap(2);
		c2.setConceptMapType(new ConceptMapType(2));
		c.addConceptMapping(c1);
		c.addConceptMapping(c2);
		Collection<ConceptMap> mappings = c.getConceptMappings();
		Assert.assertEquals(2, mappings.size());
		c.removeConceptMapping(c1);
		mappings = c.getConceptMappings();
		Assert.assertTrue(mappings.contains(c2));
		Assert.assertEquals(1, mappings.size());
	}
	
	/**
	 * @see Concept#toString()
	 */
	@Test
	public void toString_shouldReturnConceptIdIfPresentOrNull() {
		Concept c = new Concept();
		Assert.assertEquals("Concept #null", c.toString());
		c.setId(2);
		Assert.assertEquals("Concept #2", c.toString());
	}
	
	@Test
	public void findPossibleValues_shouldReturnListOfConceptsFromMatchingResults() throws Exception {
		Concept concept = new Concept(1);
		concept.addName(new ConceptName("findPossibleValueTest", Context.getLocale()));
		concept.addDescription(new ConceptDescription("en desc", Context.getLocale()));
		concept.setDatatype(new ConceptDatatype(1));
		concept.setConceptClass(new ConceptClass(1));
		
		List<Concept> expectedConcepts = new ArrayList<>();
		
		concept = Context.getConceptService().saveConcept(concept);
		expectedConcepts.add(concept);
		Concept newConcept = new Concept(2);
		newConcept.addName(new ConceptName("New Test Concept", Context.getLocale()));
		newConcept.addDescription(new ConceptDescription("new desc", Context.getLocale()));
		newConcept.setDatatype(new ConceptDatatype(1));
		newConcept.setConceptClass(new ConceptClass(1));
		newConcept = Context.getConceptService().saveConcept(newConcept);
		
		Context.updateSearchIndexForType(ConceptName.class);
		
		List<Concept> resultConcepts = newConcept.findPossibleValues("findPossibleValueTest");
		Assert.assertEquals(expectedConcepts, resultConcepts);
	}
	
	/**
	 * @see Concept#addSetMember(Concept)
	 */
	@Test
	public void addSetMember_shouldAppendConceptToExistingConceptSetHavingRetiredConcept() {
		Concept concept = new Concept();
		Concept setMember1 = new Concept(1);
		setMember1.setRetired(true);
		concept.addSetMember(setMember1);
		Concept setMember2 = new Concept(2);
		concept.addSetMember(setMember2);
		Concept setMember3 = new Concept(3);
		concept.addSetMember(setMember3);
		assertThat(concept.getSetMembers(), hasItem(setMember1));
		assertThat(concept.getSetMembers(), hasItem(setMember2));
		assertThat(concept.getSetMembers(), hasItem(setMember3));
		assertThat(concept.getSetMembers().size(), is(3));
	}
}

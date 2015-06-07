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

import org.databene.benerator.Generator;
import org.databene.benerator.factory.GeneratorFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;

/**
 * Behavior-driven tests of the Concept class.
 */
public class ConceptTest {
	
	final static String NAME_PATTERN = "[a-z]*";
	
	private Generator<String> nameGenerator;
	
	@Before
	public void setup() {
		nameGenerator = GeneratorFactory.getUniqueRegexStringGenerator(NAME_PATTERN, 2, 12, Locale.ENGLISH);
	}
	
	/**
	 * When asked for a collection of compatible names, the returned collection should not include
	 * any incompatible names.
	 * 
	 * @see {@link Concept#getCompatibleNames(Locale)}
	 */
	@Test
	@Verifies(value = "should exclude incompatible country locales", method = "getCompatibleNames(Locale)")
	public void getCompatibleNames_shouldExcludeIncompatibleCountryLocales() throws Exception {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		
		// concept should only have US and generic english names.
		// add an incompatible name -- en_UK
		int initialNameCollectionSize = testConcept.getNames().size();
		ConceptName name_en_UK = createMockConceptName(initialNameCollectionSize + 1, Locale.UK,
		    ConceptNameType.FULLY_SPECIFIED, false);
		testConcept.addName(name_en_UK);
		
		Collection<ConceptName> compatibleNames = testConcept.getCompatibleNames(primaryLocale);
		
		assertFalse(compatibleNames.contains(name_en_UK));
	}
	
	/**
	 * When asked for a collection of compatible names, the returned collection should not include
	 * any incompatible names.
	 * 
	 * @see {@link Concept#getCompatibleNames(Locale)}
	 */
	@Test
	@Verifies(value = "should exclude incompatible language locales", method = "getCompatibleNames(Locale)")
	public void getCompatibleNames_shouldExcludeIncompatibleLanguageLocales() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", new Locale("fr")));
		
		Assert.assertEquals(0, concept.getCompatibleNames(new Locale("en")).size());
	}
	
	/**
	 * The Concept should unmark the old conceptName as the locale preferred one to enforce the rule
	 * that a each locale should have only one preferred name per concept
	 * 
	 * @see {@link Concept#setPreferredName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should only allow one preferred name", method = "setPreferredName(ConceptName)")
	public void setPreferredName_shouldOnlyAllowOnePreferredName() throws Exception {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		
		ConceptName initialPreferred = createMockConceptName(3, primaryLocale, null, true);
		testConcept.addName(initialPreferred);
		Assert.assertEquals(true, initialPreferred.isLocalePreferred());
		ConceptName newPreferredName = createMockConceptName(4, primaryLocale, null, false);
		testConcept.setPreferredName(newPreferredName);
		
		assertEquals(false, initialPreferred.isLocalePreferred());
		assertEquals(true, newPreferredName.isLocalePreferred());
	}
	
	/**
	 * @see {@link Concept#getDescription(Locale,null)}
	 */
	@Test
	@Verifies(value = "should not return language only match for exact matches", method = "getDescription(Locale,boolean)")
	public void getDescription_shouldNotReturnLanguageOnlyMatchForExactMatches() throws Exception {
		Concept mockConcept = new Concept();
		mockConcept.addDescription(new ConceptDescription("en desc", new Locale("en")));
		
		Assert.assertNull(mockConcept.getDescription(new Locale("en", "US"), true));
	}
	
	/**
	 * @see {@link Concept#getDescription(Locale,null)}
	 */
	@Test
	@Verifies(value = "should not return match on language only if exact match exists", method = "getDescription(Locale,boolean)")
	public void getDescription_shouldNotReturnMatchOnLanguageOnlyIfExactMatchExists() throws Exception {
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
	 * @see {@link Concept#getDescription(Locale,null)}
	 */
	@Test
	@Verifies(value = "should return match on language only", method = "getDescription(Locale,boolean)")
	public void getDescription_shouldReturnMatchOnLanguageOnly() throws Exception {
		Concept mockConcept = new Concept();
		mockConcept.addDescription(new ConceptDescription("en desc", new Locale("en")));
		
		Assert.assertEquals("en desc", mockConcept.getDescription(new Locale("en", "US"), false).getDescription());
	}
	
	/**
	 * @see {@link Concept#getDescription(Locale,null)}
	 */
	@Test
	@Verifies(value = "should return match on locale exactly", method = "getDescription(Locale,boolean)")
	public void getDescription_shouldReturnMatchOnLocaleExactly() throws Exception {
		Concept mockConcept = new Concept();
		mockConcept.addDescription(new ConceptDescription("en_US desc", new Locale("en", "US")));
		
		Assert.assertEquals("en_US desc", mockConcept.getDescription(new Locale("en", "US"), false).getDescription());
	}
	
	/**
	 * @see {@link Concept#getName(Locale,null)}
	 */
	@Test
	@Verifies(value = "should not fail if no names are defined", method = "getName(Locale,null)")
	public void getName_shouldNotFailIfNoNamesAreDefined() throws Exception {
		Concept concept = new Concept();
		Assert.assertNull(concept.getName(new Locale("en"), false));
		Assert.assertNull(concept.getName(new Locale("en"), true));
	}
	
	/**
	 * @see {@link Concept#getName(Locale,null)}
	 */
	@Test
	@Verifies(value = "should return exact name locale match given exact equals true", method = "getName(Locale,null)")
	public void getName_shouldReturnExactNameLocaleMatchGivenExactEqualsTrue() throws Exception {
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
	 * @see {@link Concept#getName(Locale,null)}
	 */
	@Test
	@Verifies(value = "return null if no names are found in locale given exact equals true", method = "getName(Locale,null)")
	public void getName_shouldReturnNullIfNoNamesAreFoundInLocaleGivenExactEqualsTrue() throws Exception {
		Locale nonMatchingNameLocale = new Locale("en", "US");
		Locale localeToSearch = new Locale("en");
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", nonMatchingNameLocale));
		Assert.assertNull(concept.getName(localeToSearch, true));
	}
	
	/**
	 * @see {@link Concept#getName(Locale,false)}
	 */
	@Test
	@Verifies(value = "return any name within the same language when exact equals false", method = "getName(Locale,false)")
	public void getName_shouldReturnNameWithinSameLanguageIfExactEqualsFalse() throws Exception {
		Locale localeToSearch = new Locale("en");
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("Test Concept", localeToSearch));
		Assert.assertEquals("Test Concept", (concept.getName(localeToSearch, false).toString()));
	}
	
	/**
	 * @see {@link Concept#getNames(Boolean)}
	 */
	@Test
	@Verifies(value = "should not fail if getName(boolean) is only finding voided conceptNames when true", method = "getName(Boolean)")
	public void getNamesBoolean_shouldNotReturnVoidedConceptName() throws Exception {
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
	 * @see {@link Concept#getNames()}
	 */
	@Test
	@Verifies(value = "should not fail if getNames() is correctly calling getNames(false)", method = "getNames()")
	public void getNames_shouldNotReturnVoidedConceptName() throws Exception {
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
	 * @see {@link Concept#getNames(Locale)}
	 */
	@Test
	@Verifies(value = "getName(Locale) should not return voided conceptName, should return non-voided concept in other locale even if not short", method = "getName(Locale)")
	public void getNamesLocale_shouldReturnNonVoidedConceptName() throws Exception {
		Locale localeToSearch = new Locale("en");
		Concept concept = new Concept();
		
		ConceptName conceptName = new ConceptName("some name", localeToSearch);
		conceptName.setVoided(true);
		concept.addName(conceptName);
		
		Collection<ConceptName> cns = concept.getNames(localeToSearch);
		Assert.assertEquals(cns.size(), 0);
	}
	
	/**
	 * @see {@link Concept#getNames(Locale)}
	 */
	@Test
	@Verifies(value = "getNames(Locale) should return an empty Collection if no concept names", method = "getBestName(Locale)")
	public void getNamesLocale_shouldReturnEmptyCollection() throws Exception {
		Locale localeToSearch = new Locale("en");
		Concept concept = new Concept();
		
		Collection<ConceptName> cns = concept.getNames(localeToSearch);
		Assert.assertEquals(cns.size(), 0);
	}
	
	/**
	 * @see {@link Concept#getBestName(Locale)}
	 */
	@Test
	@Verifies(value = "getBestName should return null if no concept names", method = "getBestName(Locale)")
	public void getBestNameLocale_shouldReturnNull() throws Exception {
		Locale localeToSearch = new Locale("en");
		Concept concept = new Concept();
		ConceptName conceptName = concept.getName(localeToSearch);
		Assert.assertNull(conceptName);
	}
	
	/**
	 * @see {@link Concept#getAnswers()}
	 */
	@Test
	@Verifies(value = "not return null if no answers defined", method = "getAnswers()")
	public void getAnswers_shouldNotReturnNullIfAnswersListIsNull() throws Exception {
		Concept c = new Concept();
		c.setAnswers(null);
		Assert.assertNotNull(c.getAnswers());
		c.setAnswers(null);
		Assert.assertNotNull(c.getAnswers(true));
	}
	
	/**
	 * @see {@link Concept#getAnswers()}
	 */
	@Test
	@Verifies(value = "not return null if answers is null or empty", method = "getAnswers()")
	public void getAnswers_shouldInitAnswersObject() throws Exception {
		Concept c = new Concept();
		c.setAnswers(null); //make sure the list is null
		Assert.assertEquals(c.getAnswers(), c.getAnswers());
	}
	
	/**
	 * @see {@link Concept#addAnswer(ConceptAnswer)}
	 */
	@Test
	@Verifies(value = "should not fail if answers list is null", method = "addAnswer(ConceptAnswer)")
	public void addAnswer_shouldNotFailIfAnswersListIsNull() throws Exception {
		ConceptAnswer ca = new ConceptAnswer(123);
		Concept c = new Concept();
		c.setAnswers(null); // make sure the list is null
		c.addAnswer(ca);
	}
	
	/**
	 * @see {@link Concept#getAnswers()}
	 */
	@Test
	@Verifies(value = "should return retired and non-retired answers", method = "addAnswer(ConceptAnswer)")
	public void getAnswers_shouldReturnRetiredByDefault() throws Exception {
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
	 * @see {@link Concept#getAnswers()}
	 */
	@Test
	@Verifies(value = "should not return retired answers if includeRetired is false", method = "getAnswers(Boolean)")
	public void getAnswers_shouldNotReturnRetiredIfFalse() throws Exception {
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
	 * @see {@link Concept#getAnswers()}
	 */
	@Test
	@Verifies(value = "should return retired answers if includeRetired is true", method = "getAnswers(Boolean)")
	public void getAnswers_shouldReturnRetiredIfTrue() throws Exception {
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
	 * @see {@link Concept#addAnswer(ConceptAnswer)}
	 */
	@Test
	@Verifies(value = "set the sort weight to the max plus one if not provided", method = "addAnswer(ConceptAnswer)")
	public void addAnswer_shouldSetTheSortWeightToTheMaxPlusOneIfNotProvided() throws Exception {
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
	 * @see {@link Concept#setPreferredName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should add the name to the list of names if it not among them before", method = "setPreferredName(ConceptName)")
	public void setPreferredName_shouldAddTheNameToTheListOfNamesIfItNotAmongThemBefore() throws Exception {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		ConceptName newPreferredName = createMockConceptName(3, primaryLocale, null, false);
		assertEquals(false, testConcept.getNames(primaryLocale).contains(newPreferredName));
		testConcept.setPreferredName(newPreferredName);
		assertEquals(true, testConcept.getNames(primaryLocale).contains(newPreferredName));
	}
	
	/**
	 * @see {@link Concept#getFullySpecifiedName(Locale)}
	 */
	@Test
	@Verifies(value = "should return the name marked as fully specified for the given locale", method = "getFullySpecifiedName(Locale)")
	public void getFullySpecifiedName_shouldReturnTheNameMarkedAsFullySpecifiedForTheGivenLocale() throws Exception {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		ConceptName fullySpecifiedName_FR = createMockConceptName(3, new Locale("fr"), ConceptNameType.FULLY_SPECIFIED, true);
		testConcept.addName(fullySpecifiedName_FR);
		Assert.assertEquals(primaryLocale, testConcept.getFullySpecifiedName(primaryLocale).getLocale());
		Assert.assertEquals(ConceptNameType.FULLY_SPECIFIED, testConcept.getFullySpecifiedName(primaryLocale)
		        .getConceptNameType());
	}
	
	/**
	 * @see {@link Concept#addSetMember(Concept,int)}
	 */
	@Test
	@Verifies(value = "should add the concept to the current list of conceptSet", method = "addSetMember(Concept,int)")
	public void addSetMember_shouldAddTheConceptToTheCurrentListOfConceptSet() throws Exception {
		Concept concept = new Concept();
		Concept setMember = new Concept(1);
		
		Assert.assertEquals(0, concept.getConceptSets().size());
		
		concept.addSetMember(setMember);
		
		Assert.assertEquals(1, concept.getConceptSets().size());
		
	}
	
	/**
	 * @see {@link Concept#addSetMember(Concept)}
	 */
	@Test
	@Verifies(value = "should add concept as a conceptSet", method = "addSetMember(Concept)")
	public void addSetMember_shouldAddConceptAsAConceptSet() throws Exception {
		Concept concept = new Concept();
		Concept setMember = new Concept(1);
		concept.addSetMember(setMember);
		
		ConceptSet conceptSet = (ConceptSet) concept.getConceptSets().toArray()[0];
		
		Assert.assertEquals(setMember, conceptSet.getConcept());
	}
	
	/**
	 * @see {@link Concept#addSetMember(Concept,int)}
	 */
	@Test
	@Verifies(value = "should assign the calling component as parent to the ConceptSet", method = "addSetMember(Concept,int)")
	public void addSetMember_shouldAssignTheCallingComponentAsParentToTheConceptSet() throws Exception {
		Concept concept = new Concept();
		Concept setMember = new Concept(11);
		concept.addSetMember(setMember);
		
		ConceptSet conceptSet = (ConceptSet) concept.getConceptSets().toArray()[0];
		
		Assert.assertEquals(concept, conceptSet.getConceptSet());
	}
	
	/**
	 * @see {@link Concept#addSetMember(Concept)}
	 */
	@Test
	@Verifies(value = "should append concept to the existing list of conceptSet", method = "addSetMember(Concept)")
	public void addSetMember_shouldAppendConceptToExistingConceptSet() throws Exception {
		Concept concept = new Concept();
		Concept setMember1 = new Concept(1);
		concept.addSetMember(setMember1);
		Concept setMember2 = new Concept(2);
		concept.addSetMember(setMember2);
		
		Assert.assertEquals(setMember2, concept.getSetMembers().get(1));
	}
	
	/**
	 * @see {@link Concept#addSetMember(Concept)}
	 */
	@Test
	@Verifies(value = "should place the new concept last in the list", method = "addSetMember(Concept)")
	public void addSetMember_shouldPlaceTheNewConceptLastInTheList() throws Exception {
		Concept concept = new Concept();
		Concept setMember1 = new Concept(1);
		concept.addSetMember(setMember1, 3);
		Concept setMember2 = new Concept(2);
		concept.addSetMember(setMember2);
		
		Assert.assertEquals(setMember2, concept.getSetMembers().get(1));
	}
	
	/**
	 * @see {@link Concept#getSetMembers()}
	 */
	@Test
	@Verifies(value = "should return concept set members sorted according to the sort weight", method = "getSetMembers()")
	public void getSetMembers_shouldReturnConceptSetMembersSortedAccordingToTheSortWeight() throws Exception {
		Concept c = new Concept();
		ConceptSet set0 = new ConceptSet(new Concept(0), 3.0);
		ConceptSet set1 = new ConceptSet(new Concept(1), 2.0);
		ConceptSet set2 = new ConceptSet(new Concept(2), 1.0);
		ConceptSet set3 = new ConceptSet(new Concept(3), 0.0);
		
		List<ConceptSet> sets = new ArrayList<ConceptSet>();
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
	 * @see {@link Concept#getSetMembers()}
	 */
	@Test
	@Verifies(value = "should return concept set members sorted with retired last", method = "getSetMembers()")
	public void getSetMembers_shouldReturnConceptSetMembersSortedWithRetiredLast() throws Exception {
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
		
		List<ConceptSet> sets = new ArrayList<ConceptSet>();
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
	 * @see {@link Concept#getSetMembers()}
	 */
	@Test
	@Verifies(value = "should return all the conceptMembers of current Concept", method = "getSetMembers()")
	public void getSetMembers_shouldReturnAllTheConceptMembersOfCurrentConcept() throws Exception {
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
	 * @see {@link Concept#getSetMembers()}
	 */
	@Test(expected = UnsupportedOperationException.class)
	@Verifies(value = "should return unmodifiable list of conceptMember list", method = "getSetMembers()")
	public void getSetMembers_shouldReturnUnmodifiableListOfConceptMemberList() throws Exception {
		Concept c = new Concept();
		c.addSetMember(new Concept(12345));
		List<Concept> setMembers = c.getSetMembers();
		
		Assert.assertEquals(1, setMembers.size());
		setMembers.add(new Concept());
	}
	
	/**
	 * @see {@link Concept#addSetMember(Concept)}
	 */
	@Test
	@Verifies(value = "should append concept to the existing list of conceptSet", method = "addSetMember(Concept)")
	public void addSetMember_shouldAppendConceptToTheExistingListOfConceptSet() throws Exception {
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
	 * @see {@link Concept#addSetMember(Concept,int)}
	 */
	@Test
	@Verifies(value = "should assign the given concept as a ConceptSet", method = "addSetMember(Concept,int)")
	public void addSetMember_shouldAssignTheGivenConceptAsAConceptSet() throws Exception {
		Concept concept = new Concept();
		Concept setMember = new Concept(2);
		concept.addSetMember(setMember, 0);
		
		ConceptSet conceptSet = (ConceptSet) concept.getConceptSets().toArray()[0];
		Assert.assertEquals(setMember, conceptSet.getConcept());
	}
	
	/**
	 * @see {@link Concept#addSetMember(Concept,int)}
	 */
	@Test
	@Verifies(value = "should insert the concept before the first with zero index", method = "addSetMember(Concept,int)")
	public void addSetMember_shouldInsertTheConceptBeforeTheFirstWithZeroIndex() throws Exception {
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
	 * @see {@link Concept#addSetMember(Concept,int)}
	 */
	@Test
	@Verifies(value = "should insert the concept at the end with negative one index", method = "addSetMember(Concept,int)")
	public void addSetMember_shouldInsertTheConceptAtTheEndWithNegativeOneIndex() throws Exception {
		Concept concept = new Concept();
		Concept firstSetMember = new Concept(2);
		concept.addSetMember(firstSetMember);
		
		Concept setMember = new Concept(3);
		concept.addSetMember(setMember, -1);
		
		ConceptSet secondConceptSet = (ConceptSet) concept.getConceptSets().toArray()[1];
		Assert.assertEquals(setMember, secondConceptSet.getConcept());
	}
	
	/**
	 * @see {@link Concept#addSetMember(Concept,int)}
	 */
	@Test
	@Verifies(value = "should insert the concept in the third slot", method = "addSetMember(Concept,int)")
	public void addSetMember_shouldInsertTheConceptInTheThirdSlot() throws Exception {
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
	 * @see {@link Concept#getAllConceptNameLocales()}
	 */
	@Test
	@Verifies(value = "should return all locales for conceptNames for this concept without duplicates", method = "getAllConceptNameLocales()")
	public void getAllConceptNameLocales_shouldReturnAllLocalesForConceptNamesForThisConceptWithoutDuplicates()
	        throws Exception {
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
	 * @see {@link Concept#getPreferredName(Locale)}
	 */
	@Test
	@Verifies(value = "should return the fully specified name if no name is explicitly marked as locale preferred", method = "getPreferredName(Locale)")
	public void getPreferredName_shouldReturnTheFullySpecifiedNameIfNoNameIsExplicitlyMarkedAsLocalePreferred()
	        throws Exception {
		Concept testConcept = createMockConcept(1, Locale.US);
		//preferred name in en_US
		ConceptName preferredNameEN_US = createMockConceptName(3, Locale.US, null, false);
		testConcept.addName(preferredNameEN_US);
		String fullySpecName = testConcept.getFullySpecifiedName(Locale.US).getName();
		//preferred name in en
		ConceptName preferredNameEN = createMockConceptName(4, new Locale("en"), null, false);
		testConcept.addName(preferredNameEN);
		Assert.assertEquals(fullySpecName, testConcept.getPreferredName(Locale.US).getName());
	}
	
	/**
	 * @see {@link Concept#getPreferredName(Locale)}
	 */
	@Test
	@Verifies(value = "should return the concept name explicitly marked as locale preferred", method = "getPreferredName(Locale)")
	public void getPreferredName_shouldReturnTheConceptNameExplicitlyMarkedAsLocalePreferred() throws Exception {
		Concept testConcept = createMockConcept(1, Locale.US);
		//preferred name in en_US
		ConceptName preferredNameEN_US = createMockConceptName(3, Locale.US, null, true);
		testConcept.addName(preferredNameEN_US);
		//preferred name in en
		ConceptName preferredNameEN = createMockConceptName(4, new Locale("en"), null, true);
		testConcept.addName(preferredNameEN);
		Assert.assertEquals(preferredNameEN_US, testConcept.getPreferredName(Locale.US));
		Assert.assertEquals(preferredNameEN, testConcept.getPreferredName(new Locale("en")));
	}
	
	/**
	 * @see {@link Concept#getShortestName(Locale,Boolean)}
	 */
	@Test
	@Verifies(value = "should return the shortest name for the concept from any locale if exact is false", method = "getShortestName(Locale,Boolean)")
	public void getShortestName_shouldReturnTheShortestNameForTheConceptFromAnyLocaleIfExactIsFalse() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("shortName123", Context.getLocale()));
		concept.addName(new ConceptName("shortName12", Context.getLocale()));
		concept.addName(new ConceptName("shortName1", Locale.US));
		concept.addName(new ConceptName("shortName", Locale.FRANCE));
		Assert.assertEquals("shortName", concept.getShortestName(Context.getLocale(), false).getName());
	}
	
	/**
	 * @see {@link Concept#getShortestName(Locale,Boolean)}
	 */
	@Test
	@Verifies(value = "should return the shortest name in a given locale for a concept if exact is true", method = "getShortestName(Locale,Boolean)")
	public void getShortestName_shouldReturnTheShortestNameInAGivenLocaleForAConceptIfExactIsTrue() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("shortName123", Context.getLocale()));
		concept.addName(new ConceptName("shortName12", Context.getLocale()));
		concept.addName(new ConceptName("shortName1", Locale.US));
		concept.addName(new ConceptName("shortName", Locale.FRANCE));
		Assert.assertEquals("shortName12", concept.getShortestName(Context.getLocale(), true).getName());
	}
	
	/**
	 * @see {@link Concept#setFullySpecifiedName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should add the name to the list of names if it not among them before", method = "setFullySpecifiedName(ConceptName)")
	public void setFullySpecifiedName_shouldAddTheNameToTheListOfNamesIfItNotAmongThemBefore() throws Exception {
		Concept concept = createMockConcept(1, Context.getLocale());
		int expectedNumberOfNames = concept.getNames().size() + 1;
		concept.setFullySpecifiedName(new ConceptName("some name", Context.getLocale()));
		Assert.assertEquals(expectedNumberOfNames, concept.getNames().size());
	}
	
	/**
	 * @see {@link Concept#setFullySpecifiedName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should convert the previous fully specified name if any to a synonym", method = "setFullySpecifiedName(ConceptName)")
	public void setFullySpecifiedName_shouldConvertThePreviousFullySpecifiedNameIfAnyToASynonym() throws Exception {
		Concept concept = createMockConcept(1, Context.getLocale());
		ConceptName oldFullySpecifiedName = concept.getFullySpecifiedName(Context.getLocale());
		//sanity check
		Assert.assertEquals(ConceptNameType.FULLY_SPECIFIED, oldFullySpecifiedName.getConceptNameType());
		
		concept.setFullySpecifiedName(new ConceptName("some name", Context.getLocale()));
		Assert.assertEquals(null, oldFullySpecifiedName.getConceptNameType());
	}
	
	/**
	 * @see {@link Concept#setFullySpecifiedName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should set the concept name type of the specified name to fully specified", method = "setFullySpecifiedName(ConceptName)")
	public void setFullySpecifiedName_shouldSetTheConceptNameTypeOfTheSpecifiedNameToFullySpecified() throws Exception {
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("some name", Context.getLocale());
		concept.setFullySpecifiedName(cn);
		Assert.assertEquals(ConceptNameType.FULLY_SPECIFIED, cn.getConceptNameType());
	}
	
	/**
	 * @see {@link Concept#setShortName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should add the name to the list of names if it not among them before", method = "setShortName(ConceptName)")
	public void setShortName_shouldAddTheNameToTheListOfNamesIfItNotAmongThemBefore() throws Exception {
		Concept concept = createMockConcept(1, Context.getLocale());
		int expectedNumberOfNames = concept.getNames().size() + 1;
		concept.setShortName(new ConceptName("some name", Context.getLocale()));
		Assert.assertEquals(expectedNumberOfNames, concept.getNames().size());
	}
	
	/**
	 * @see {@link Concept#setShortName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should convert the previous shortName if any to a synonym", method = "setShortName(ConceptName)")
	public void setShortName_shouldConvertThePreviousShortNameIfAnyToASynonym() throws Exception {
		Concept concept = createMockConcept(1, Context.getLocale());
		ConceptName oldShortName = concept.getShortNameInLocale(Context.getLocale());
		//sanity check
		Assert.assertEquals(ConceptNameType.SHORT, oldShortName.getConceptNameType());
		
		concept.setShortName(new ConceptName("some name", Context.getLocale()));
		Assert.assertEquals(null, oldShortName.getConceptNameType());
	}
	
	/**
	 * @see {@link Concept#setShortName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should set the concept name type of the specified name to short", method = "setShortName(ConceptName)")
	public void setShortName_shouldSetTheConceptNameTypeOfTheSpecifiedNameToShort() throws Exception {
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
	 * @see {@link Concept#getShortestName(Locale,Boolean)}
	 */
	@Test
	@Verifies(value = "should return the name marked as the shortName for the locale if it is present", method = "getShortestName(Locale,Boolean)")
	public void getShortestName_shouldReturnTheNameMarkedAsTheShortNameForTheLocaleIfItIsPresent() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("shortName123", Context.getLocale()));
		concept.setShortName(new ConceptName("shortName12", Context.getLocale()));
		concept.setShortName(new ConceptName("shortName1", Locale.US));
		Assert.assertEquals("shortName1", concept.getShortestName(Locale.US, null).getName());
	}
	
	/**
	 * @see {@link Concept#getShortestName(Locale,Boolean)}
	 */
	@Test
	@Verifies(value = "should return null if their are no names in the specified locale and exact is true", method = "getShortestName(Locale,Boolean)")
	public void getShortestName_shouldReturnNullIfTheirAreNoNamesInTheSpecifiedLocaleAndExactIsTrue() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("shortName123", Context.getLocale()));
		concept.addName(new ConceptName("shortName12", Context.getLocale()));
		concept.addName(new ConceptName("shortName1", Locale.US));
		Assert.assertNull(concept.getShortestName(new Locale("fr"), true));
	}
	
	/**
	 * @see {@link Concept#setPreferredName(ConceptName)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if the preferred name to set to is an index term", method = "setPreferredName(ConceptName)")
	public void setPreferredName_shouldFailIfThePreferredNameToSetToIsAnIndexTerm() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", Context.getLocale()));
		ConceptName preferredName = new ConceptName("some pref name", Context.getLocale());
		preferredName.setLocalePreferred(true);
		preferredName.setConceptNameType(ConceptNameType.INDEX_TERM);
		concept.setPreferredName(preferredName);
	}
	
	/**
	 * @see {@link Concept#addName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should mark the first name added as fully specified", method = "addName(ConceptName)")
	public void addName_shouldMarkTheFirstNameAddedAsFullySpecified() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", Context.getLocale()));
		Assert.assertEquals("some name", concept.getFullySpecifiedName(Context.getLocale()).getName());
	}
	
	/**
	 * @see {@link Concept#addName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should replace the old fully specified name with a current one", method = "addName(ConceptName)")
	public void addName_shouldReplaceTheOldFullySpecifiedNameWithACurrentOne() throws Exception {
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
	 * @see {@link Concept#addName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should replace the old preferred name with a current one", method = "addName(ConceptName)")
	public void addName_shouldReplaceTheOldPreferredNameWithACurrentOne() throws Exception {
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
	 * @see {@link Concept#addName(ConceptName)}
	 */
	@Test
	@Verifies(value = "should replace the old short name with a current one", method = "addName(ConceptName)")
	public void addName_shouldReplaceTheOldShortNameWithACurrentOne() throws Exception {
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
	public void getSynonyms_shouldSortPreferredFirst() throws Exception {
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
	public void getShortNameInLocale_shouldReturnTheBestShortNameForAConcept() throws Exception {
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
	@Verifies(value = "should return the language prefered name if no name is explicitly marked as locale preferred", method = "getPreferredName(Locale)")
	public void getPreferredName_shouldReturnTheBesLocalePreferred() throws Exception {
		Concept testConcept = createMockConcept(1, Locale.US);
		// preferred name in en
		ConceptName preferredNameEN = createMockConceptName(4, new Locale("en"), null, true);
		testConcept.addName(preferredNameEN);
		Assert.assertEquals(preferredNameEN.getName(), testConcept.getPreferredName(Locale.US).getName());
	}
	
	/**
	 * Convenient factory method to create a populated Concept with a one fully specified name and
	 * one short name
	 * 
	 * @param conceptId the id for the concept to create
	 * @param locale the locale of the of the conceptNames for the concept to create
	 * @return the created concept
	 */
	private Concept createMockConcept(int conceptId, Locale locale) {
		Concept mockConcept = new Concept();
		mockConcept.setConceptId(conceptId);
		Locale desiredLocale;
		if (locale == null)
			desiredLocale = Context.getLocale();
		else
			desiredLocale = locale;
		ConceptName shortName = createMockConceptName(1, desiredLocale, ConceptNameType.SHORT, false);
		ConceptName fullySpecifiedName = createMockConceptName(2, desiredLocale, ConceptNameType.FULLY_SPECIFIED, false);
		mockConcept.addName(fullySpecifiedName);
		mockConcept.addName(shortName);
		
		return mockConcept;
	}
	
	/**
	 * Convenient factory method to create a populated Concept name.
	 * 
	 * @param conceptNameId id for the conceptName
	 * @param locale for the conceptName
	 * @param conceptNameType the conceptNameType of the concept
	 * @param isLocalePreferred if this name should be marked as preferred in its locale
	 */
	private ConceptName createMockConceptName(int conceptNameId, Locale locale, ConceptNameType conceptNameType,
	        Boolean isLocalePreferred) {
		ConceptName mockConceptName = new ConceptName();
		
		mockConceptName.setConceptNameId(conceptNameId);
		if (locale == null)
			mockConceptName.setLocale(Context.getLocale());
		else
			mockConceptName.setLocale(locale);
		mockConceptName.setConceptNameType(conceptNameType);
		mockConceptName.setLocalePreferred(isLocalePreferred);
		mockConceptName.setName(nameGenerator.generate());
		
		return mockConceptName;
	}
	
	/**
	 * @see Concept#getName()
	 * @verifies return name in broader locale incase none is found in specific one
	 */
	@Test
	public void getName_shouldReturnNameInBroaderLocaleIncaseNoneIsFoundInSpecificOne() throws Exception {
		Locale locale = new Locale("en");
		Locale localeToSearch = new Locale("en", "UK");
		Concept concept = new Concept();
		concept.addName(new ConceptName("Test Concept", locale));
		Assert.assertEquals((concept.getName(locale, false).toString()), (concept.getName(localeToSearch, false).toString()));
	}
	
}

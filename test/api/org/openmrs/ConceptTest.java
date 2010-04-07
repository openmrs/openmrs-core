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
package org.openmrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Behavior-driven tests of the Concept class.
 */
public class ConceptTest {
	
	/**
	 * The concept should provide a short name even if no names are tagged as short. <br/>
	 * <br/>
	 * precondition: no short names in mock concept
	 * 
	 * @see {@link Concept#getBestShortName(Locale)}
	 */
	@Test
	@Verifies(value = "should always return a short name even if no names are tagged as short", method = "getBestShortName(Locale)")
	public void getBestShortName_shouldAlwaysReturnAShortNameEvenIfNoNamesAreTaggedAsShort() throws Exception {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		
		ConceptName shortName = testConcept.getBestShortName(primaryLocale);
		assertFalse(shortName.isShort()); // precondition: no shorts available
		assertNotNull(shortName);
	}
	
	/**
	 * The concept should provide the preferred concept name for a locale when more than one name is
	 * available.
	 * 
	 * @see {@link Concept#getName(Locale)}
	 */
	@Test
	@Verifies(value = "should get preferred fully specified country", method = "getName(Locale)")
	public void getName_shouldGetPreferredFullySpecifiedCountry() throws Exception {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		
		ConceptName preferredName = testConcept.getName(primaryLocale);
		assertTrue(preferredName.isPreferredInCountry(primaryLocale.getCountry()));
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
		ConceptName name_en_UK = ConceptNameTest.createMockConceptName(initialNameCollectionSize + 1, Locale.UK);
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
	 * The Concept should change the tagging of concept-names to enforce the rule that only one may
	 * be marked as preferred for a locale.
	 * 
	 * @see {@link Concept#setPreferredName(Locale,ConceptName)}
	 */
	@Test
	@Verifies(value = "should only allow one preferred name", method = "setPreferredName(Locale,ConceptName)")
	public void setPreferredName_shouldOnlyAllowOnePreferredName() throws Exception {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		ConceptNameTag preferredTag = ConceptNameTag.preferredCountryTagFor(primaryLocale);
		
		ConceptName initialPreferred = testConcept.getPreferredName(primaryLocale);
		ConceptName expectedPreferred = ConceptNameTest.createMockConceptName(initialPreferred.getConceptNameId() + 10,
		    primaryLocale);
		testConcept.setPreferredName(primaryLocale, expectedPreferred);
		
		ConceptName actualPreferred = testConcept.getPreferredName(primaryLocale);
		
		assertNotSame(initialPreferred, actualPreferred);
		assertSame(expectedPreferred, actualPreferred);
		
		assertFalse(initialPreferred.hasTag(preferredTag));
		assertTrue(expectedPreferred.hasTag(preferredTag));
	}
	
	/**
	 * When there is a preferred name for a locale, it should also be the "best" name for that
	 * locale.
	 * 
	 * @see {@link Concept#getPreferredName(Locale)}
	 */
	@Test
	@Verifies(value = "should match to best name", method = "getPreferredName(Locale)")
	public void getPreferredName_shouldMatchToBestName() throws Exception {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		
		ConceptName preferredName = testConcept.getPreferredName(primaryLocale);
		ConceptName bestName = testConcept.getBestName(primaryLocale);
		
		assertSame(preferredName, bestName);
	}
	
	/**
	 * The concept should always provide a "best" name even if there are no names available for the
	 * requested locale.
	 * 
	 * @see {@link Concept#getBestName(Locale)}
	 */
	@Test
	@Verifies(value = "should always have a best name even if none match locale", method = "getBestName(Locale)")
	public void getBestName_shouldAlwaysHaveABestNameEvenIfNoneMatchLocale() throws Exception {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		
		ConceptName bestNameForNonExistentLocale = testConcept.getBestName(Locale.JAPAN);
		assertNotNull(bestNameForNonExistentLocale);
		assertFalse(Locale.JAPAN.equals(bestNameForNonExistentLocale.getLocale()));
	}
	
	/**
	 * The deprecated getName(Locale) should support getting a name that is only tagged as
	 * "preferred" -- with no language or country indicated -- even when searching for a specific
	 * language_country locale.
	 * 
	 * @see {@link Concept#getName(Locale,null)}
	 */
	@Test
	@Verifies(value = "should support plain preferred", method = "getName(Locale,null)")
	public void getName_shouldSupportPlainPreferred() throws Exception {
		Locale testLocale = Locale.ENGLISH;
		Concept testConcept = new Concept();
		testConcept.setConceptId(1);
		
		ConceptName preferredName = ConceptNameTest.createMockConceptName(1, testLocale);
		preferredName.addTag(ConceptNameTag.PREFERRED);
		testConcept.addName(preferredName);
		
		ConceptName shortName = ConceptNameTest.createMockConceptName(2, testLocale);
		shortName.addTag(ConceptNameTag.SHORT);
		testConcept.addName(shortName);
		
		ConceptName actualName = testConcept.getName(Locale.US);
		assertEquals(preferredName, actualName);
		
	}
	
	/**
	 * getPreferredName(Locale) should support getting a name that is only tagged as "preferred" --
	 * with no language or country indicated -- even when searching for a specific language_country
	 * locale.
	 * 
	 * @see {@link Concept#getPreferredName(Locale)}
	 */
	@Test
	@Verifies(value = "should support plain preferred", method = "getPreferredName(Locale)")
	public void getPreferredName_shouldSupportPlainPreferred() throws Exception {
		Locale testLocale = Locale.ENGLISH;
		Concept testConcept = new Concept();
		testConcept.setConceptId(1);
		
		ConceptName preferredName = ConceptNameTest.createMockConceptName(1, testLocale);
		preferredName.addTag(ConceptNameTag.PREFERRED);
		testConcept.addName(preferredName);
		
		ConceptName shortName = ConceptNameTest.createMockConceptName(2, testLocale);
		shortName.addTag(ConceptNameTag.SHORT);
		testConcept.addName(shortName);
		
		ConceptName actualName = testConcept.getPreferredName(Locale.US);
		assertEquals(preferredName, actualName);
		
	}
	
	/**
	 * getBestName(Locale) should support getting a name that is only tagged as "preferred" -- with
	 * no language or country indicated -- even when searching for a specific language_country
	 * locale.
	 * 
	 * @see {@link Concept#getBestName(Locale)}
	 */
	@Test
	@Verifies(value = "should support plain preferred", method = "getBestName(Locale)")
	public void getBestName_shouldSupportPlainPreferred() throws Exception {
		Locale testLocale = Locale.ENGLISH;
		Concept testConcept = new Concept();
		testConcept.setConceptId(1);
		
		ConceptName preferredName = ConceptNameTest.createMockConceptName(1, testLocale);
		preferredName.addTag(ConceptNameTag.PREFERRED);
		testConcept.addName(preferredName);
		
		ConceptName shortName = ConceptNameTest.createMockConceptName(2, testLocale);
		shortName.addTag(ConceptNameTag.SHORT);
		testConcept.addName(shortName);
		
		ConceptName actualName = testConcept.getBestName(Locale.US);
		assertEquals(preferredName, actualName);
	}
	
	/**
	 * Convenient factory method to create a populated Concept.
	 * 
	 * @return
	 */
	public static Concept createMockConcept(int conceptId, Locale primaryLocale) {
		Concept mockConcept = new Concept();
		mockConcept.setConceptId(conceptId);
		
		ConceptName primaryName = ConceptNameTest.createMockConceptName(1, primaryLocale);
		mockConcept.setPreferredName(primaryLocale, primaryName);
		
		Locale generalLocale = new Locale(primaryLocale.getLanguage());
		ConceptName generalName = ConceptNameTest.createMockConceptName(2, generalLocale);
		mockConcept.addName(generalName);
		
		return mockConcept;
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
	@Verifies(value = "should return any name if no locale match given exact equals false", method = "getName(Locale,null)")
	public void getName_shouldReturnAnyNameIfNoLocaleMatchGivenExactEqualsFalse() throws Exception {
		Locale definedNameLocale = new Locale("en", "US");
		Locale localeToSearch = new Locale("fr");
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", definedNameLocale));
		Assert.assertEquals("some name", concept.getName(localeToSearch, false).getName());
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
		concept.addName(new ConceptName("some name", definedNameLocale));
		Assert.assertNull(concept.getName(localeToSearch, true));
	}
	
	/**
	 * @see {@link Concept#getName(Locale,null)}
	 */
	@Test
	@Verifies(value = "should return loose match given exact equals false", method = "getName(Locale,null)")
	public void getName_shouldReturnLooseMatchGivenExactEqualsFalse() throws Exception {
		Locale localeToSearch = new Locale("en", "US");
		Locale definedNameLocale = new Locale("en");
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", definedNameLocale));
		Assert.assertEquals("some name", concept.getName(localeToSearch, false).getName());
		
		definedNameLocale = new Locale("en", "US");
		localeToSearch = new Locale("en");
		
		concept = new Concept();
		concept.addName(new ConceptName("some name", definedNameLocale));
		Assert.assertEquals("some name", concept.getName(localeToSearch, false).getName());
	}
	
	/**
	 * @see {@link Concept#getName(Locale,null)}
	 */
	@Test
	@Verifies(value = "should return null if no locale match and exact equals true", method = "getName(Locale,null)")
	public void getName_shouldReturnNullIfNoLocaleMatchAndExactEqualsTrue() throws Exception {
		Locale nonMatchingNameLocale = new Locale("en", "US");
		Locale localeToSearch = new Locale("en");
		
		Concept concept = new Concept();
		concept.addName(new ConceptName("some name", nonMatchingNameLocale));
		Assert.assertNull(concept.getName(localeToSearch, true));
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
	 * @see {@link Concept#getBestName(Locale)}
	 */
	@Test
	@Verifies(value = "getBestName should not return voided conceptName, should return non-voided concept in other locale ", method = "getBestName(Locale)")
	public void getBestName_shouldReturnNonVoidedConceptName() throws Exception {
		Locale localeToSearch = new Locale("en");
		Locale nonMatchingNameLocale = new Locale("fr");
		Concept concept = new Concept();
		
		ConceptName conceptName = new ConceptName("some name", localeToSearch);
		conceptName.setVoided(true);
		concept.addName(conceptName);
		
		ConceptName conceptNameOther = new ConceptName("some other name", nonMatchingNameLocale);
		concept.addName(conceptNameOther);
		
		ConceptName cn = concept.getBestName(localeToSearch);
		Assert.assertEquals(cn.getLocale(), nonMatchingNameLocale);
		Assert.assertEquals(cn.getName(), "some other name");
	}
	
	/**
	 * @see {@link Concept#getBestShortName(Locale)}
	 */
	@Test
	@Verifies(value = "getBestShortName should not return voided conceptName, should return non-voided concept in other locale even if not short", method = "getBestShortName(Locale)")
	public void getBestShortName_shouldReturnNonVoidedConceptName() throws Exception {
		Locale localeToSearch = new Locale("en");
		Locale nonMatchingNameLocale = new Locale("fr");
		Concept concept = new Concept();
		
		ConceptName conceptName = new ConceptName("some name", localeToSearch);
		conceptName.setVoided(true);
		concept.setShortName(localeToSearch, conceptName);
		
		ConceptName conceptNameOther = new ConceptName("some other name", nonMatchingNameLocale);
		concept.addName(conceptNameOther);
		
		ConceptName cn = concept.getBestShortName(localeToSearch);
		Assert.assertEquals(cn.getLocale(), nonMatchingNameLocale);
		Assert.assertEquals(cn.getName(), "some other name");
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
		ConceptName conceptName = concept.getBestName(localeToSearch);
		Assert.assertNull(conceptName);
	}
	
	/**
	 * @see {@link Concept#equals(Object)}
	 */
	@Test
	@Verifies(value = "should confirm two new concept objects are equal", method = "equals(Object)")
	public void equals_shouldConfirmTwoNewConceptObjectsAreEqual() throws Exception {
		Concept concept = new Concept(); // an object with a null concept id
		Assert.assertTrue(concept.equals(concept));
	}
	
	/**
	 * @see {@link Concept#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not fail if concept id is null", method = "equals(Object)")
	public void equals_shouldNotFailIfConceptIdIsNull() throws Exception {
		Concept left = new Concept(); // a null concept id
		Concept right = new Concept(1); // a non-null concept id
		Assert.assertFalse(left.equals(right));
	}
	
	/**
	 * @see {@link Concept#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not fail if given obj has null conceptid", method = "equals(Object)")
	public void equals_shouldNotFailIfGivenObjHasNullConceptid() throws Exception {
		Concept left = new Concept(1); // a non-null concept id
		Concept right = new Concept(); // a null concept id
		Assert.assertFalse(left.equals(right));
	}
	
	/**
	 * @see {@link Concept#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not fail if given obj is null", method = "equals(Object)")
	public void equals_shouldNotFailIfGivenObjIsNull() throws Exception {
		Concept left = new Concept(1); // a non-null concept id
		Assert.assertFalse(left.equals(null));
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
	 * @see {@link Concept#equals(Object)}
	 */
	@Test
	@Verifies(value = "should confirm two new different concepts are not equal when their ConceptId are null", method = "equals(Object)")
	public void equals_shouldConfirmTwoNewDifferentConceptsAreNotEqualWhenTheirConceptIdAreNull() throws Exception {
		Concept one = new Concept();
		Concept two = new Concept();
		Assert.assertFalse(one.equals(two));
	}
	
	/**
	 * @see {@link Concept#addAnswer(ConceptAnswer)}
	 */
	@Test
	@Verifies(value = "set the sort weight to the max plus one if not provided", method="addAnswer(ConceptAnswer)")
	public void addAnswer_shouldSetTheSortWeightToTheMaxPlusOneIfNotProvided() throws Exception {
		ConceptAnswer ca = new ConceptAnswer(123);
		Concept c = new Concept();
		c.setAnswers(null);//make sure null list
		c.addAnswer(ca);
		Assert.assertEquals(1d, ca.getSortWeight().doubleValue(), 0);
		
		ConceptAnswer ca2 = new ConceptAnswer(456);
		c.addAnswer(ca2);
		Assert.assertEquals(2d, ca2.getSortWeight().doubleValue(), 0);
	}
}

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
	 * The concept should provide a short name even if no names are tagged as short.
	 * 
	 * @precondition no short names in mock concept
	 */
	@Test
	public void shouldAlwaysReturnAShortNameEvenIfNoNamesAreTaggedAsShort() {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		
		ConceptName shortName = testConcept.getBestShortName(primaryLocale);
		assertFalse(shortName.isShort()); // precondition: no shorts available
		assertNotNull(shortName);
	}
	
	/**
	 * The concept should provide the preferred concept name for a locale when more than one name is
	 * available.
	 */
	@Test
	public void shouldGetPreferredFullySpecifiedCountry() {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		
		ConceptName preferredName = testConcept.getName(primaryLocale);
		assertTrue(preferredName.isPreferredInCountry(primaryLocale.getCountry()));
	}
	
	/**
	 * When asked for a collection of compatible names, the returned collection should not include
	 * any incompatible names.
	 */
	@Test
	public void shouldExcludeIncompatibleCountryLocales() {
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
	 * The Concept should change the tagging of concept-names to enforce the rule that only one may
	 * be marked as preferred for a locale.
	 */
	@Test
	public void shouldOnlyAllowOnePreferredName() {
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
	 */
	@Test
	public void shouldMatchPreferredAndBestName() {
		Locale primaryLocale = Locale.US;
		Concept testConcept = createMockConcept(1, primaryLocale);
		
		ConceptName preferredName = testConcept.getPreferredName(primaryLocale);
		ConceptName bestName = testConcept.getBestName(primaryLocale);
		
		assertSame(preferredName, bestName);
	}
	
	/**
	 * The concept should always provide a "best" name even if there are no names available for the
	 * requested locale.
	 */
	@Test
	public void shouldAlwaysHaveABestNameEvenIfNoneMatchLocale() {
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
	 */
	@Test
	public void shouldSupportPlainPreferredWhenAskingForName() {
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
	 */
	@Test
	public void shouldSupportPlainPreferredWhenAskingForPreferredName() {
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
	 */
	@Test
	public void shouldSupportPlainPreferredWhenAskingForBestName() {
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
}

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
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.ConceptWord;
import org.openmrs.api.ConceptService;
import org.openmrs.test.Verifies;

/**
 * Unit tests for methods that are specific to the {@link ConceptServiceImpl}. General tests that
 * would span implementations should go on the {@link ConceptService}.
 */
public class ConceptServiceImplTest {
	
	/**
	 * @see ConceptServiceImpl#weightWords(String, List, List)
	 */
	@Test
	@Verifies(value = "should not fail with null phrase", method = "weightWords(String,List<QLocale;>,List<QConceptWord;>)")
	public void weightWords_shouldNotFailWithNullPhrase() throws Exception {
		new ConceptServiceImpl().weightWords(null, Collections.singletonList(Locale.ENGLISH), new ArrayList<ConceptWord>());
	}
	
	/**
	 * @see ConceptServiceImpl#weightWords(String, List, List)
	 */
	@Test
	@Verifies(value = "should weight preferred names in country higher than other preferred names", method = "weightWords(String,List<QLocale;>,List<QConceptWord;>)")
	public void weightWords_shouldWeightPreferredNamesInCountryHigherThanOtherPreferredNames() throws Exception {
		Concept c = new Concept(1);
		
		ConceptName namePreferredCountry = new ConceptName("name preferred country", new Locale("en", "US"));
		namePreferredCountry.addTag(ConceptNameTag.preferredCountryTagFor(new Locale("en", "US")));
		c.addName(namePreferredCountry);
		
		ConceptName namePreferredLanguage = new ConceptName("name preferred language", new Locale("en"));
		namePreferredLanguage.addTag(ConceptNameTag.preferredLanguageTagFor(new Locale("en")));
		c.addName(namePreferredLanguage);
		
		ConceptName namePreferred = new ConceptName("name preferred", new Locale("en"));
		namePreferred.addTag(ConceptNameTag.PREFERRED);
		c.addName(namePreferred);
		
		ConceptWord prefferedCountry = new ConceptWord("name", c, namePreferredCountry, new Locale("en", "US"));
		ConceptWord preferredLanguage = new ConceptWord("name", c, namePreferredLanguage, new Locale("en"));
		ConceptWord preferred = new ConceptWord("name", c, namePreferred, new Locale("en"));
		
		List<ConceptWord> words = new ArrayList<ConceptWord>();
		words.add(preferred);
		words.add(preferredLanguage);
		words.add(prefferedCountry);
		
		List<ConceptWord> weightedWords = new ConceptServiceImpl().weightWords("name", Collections.singletonList(new Locale(
		        "en", "US")), words);
		Assert.assertEquals("name preferred country", weightedWords.get(0).getConceptName().getName());
	}
	
	/**
	 * @see ConceptServiceImpl#weightWords(String, List, List)
	 */
	@Test
	@Verifies(value = "should weight preferred names in language higher than just preferred names", method = "weightWords(String,List<QLocale;>,List<QConceptWord;>)")
	public void weightWords_shouldWeightPreferredNamesInLanguageHigherThanJustPreferredNames() throws Exception {
		Concept c = new Concept(1);
		
		ConceptName namePreferredLanguage = new ConceptName("name preferred language", new Locale("en"));
		namePreferredLanguage.addTag(ConceptNameTag.preferredLanguageTagFor(new Locale("en")));
		c.addName(namePreferredLanguage);
		
		ConceptName namePreferred = new ConceptName("just name preferred", new Locale("en"));
		namePreferred.addTag(ConceptNameTag.PREFERRED);
		c.addName(namePreferred);
		
		ConceptWord preferredLanguage = new ConceptWord("name", c, namePreferredLanguage, new Locale("en"));
		ConceptWord preferred = new ConceptWord("name", c, namePreferred, new Locale("en"));
		
		List<ConceptWord> words = new ArrayList<ConceptWord>();
		words.add(preferred);
		words.add(preferredLanguage);
		
		List<ConceptWord> weightedWords = new ConceptServiceImpl().weightWords("name", Collections.singletonList(new Locale(
		        "en")), words);
		Assert.assertEquals("name preferred language", weightedWords.get(0).getConceptName().getName());
	}
	
	/**
	 * @see ConceptServiceImpl#weightWords(String, List, List)
	 */
	@Test
	@Verifies(value = "should weight preferred names higher than other names", method = "weightWords(String,List<QLocale;>,List<QConceptWord;>)")
	public void weightWords_shouldWeightPreferredNamesHigherThanOtherNames() throws Exception {
		Concept c = new Concept(1);
		
		ConceptName namePreferred = new ConceptName("name preferred", new Locale("en"));
		namePreferred.addTag(ConceptNameTag.PREFERRED);
		c.addName(namePreferred);
		
		ConceptName name = new ConceptName("other name", new Locale("en"));
		c.addName(name);
		
		ConceptWord preferred = new ConceptWord("name", c, namePreferred, new Locale("en"));
		ConceptWord nameword = new ConceptWord("name", c, name, new Locale("en"));
		
		List<ConceptWord> words = new ArrayList<ConceptWord>();
		words.add(nameword);
		words.add(preferred);
		
		List<ConceptWord> weightedWords = new ConceptServiceImpl().weightWords("name", Collections.singletonList(new Locale(
		        "en")), words);
		Assert.assertEquals("name preferred", weightedWords.get(0).getConceptName().getName());
	}
	
	/**
	 * @see ConceptServiceImpl#weightWords(String, List, List)
	 */
	@Test
	@Verifies(value = "should weight names that contain all words in search phrase higher than names that dont", method = "weightWords(String,List<QLocale;>,List<QConceptWord;>)")
	public void weightWords_shouldWeightNamesThatContainAllWordsInSearchPhraseHigherThanNamesThatDont() throws Exception {
		Concept c = new Concept(1);
		
		ConceptName nameWithAllSearchTerms = new ConceptName("found matching name", new Locale("en"));
		c.addName(nameWithAllSearchTerms);
		
		ConceptName nameWithoutAllSearchTerms = new ConceptName("nonmatching name", new Locale("en"));
		c.addName(nameWithoutAllSearchTerms);
		
		ConceptWord wordWithAllSearchTerms = new ConceptWord("name", c, nameWithAllSearchTerms, new Locale("en"));
		ConceptWord wordWithoutAllSearchTerms = new ConceptWord("name", c, nameWithoutAllSearchTerms, new Locale("en"));
		
		List<ConceptWord> words = new ArrayList<ConceptWord>();
		words.add(wordWithoutAllSearchTerms);
		words.add(wordWithAllSearchTerms);
		
		List<ConceptWord> weightedWords = new ConceptServiceImpl().weightWords("found name", Collections
		        .singletonList(new Locale("en")), words);
		Assert.assertEquals("found matching name", weightedWords.get(0).getConceptName().getName());
	}
	
	/**
	 * This test makes sure that names with a higher percentage of their total words matching the
	 * query are weighted better
	 * 
	 * @see ConceptServiceImpl#weightWords(String, List, List)
	 */
	@Test
	@Verifies(value = "should weight better matches higher than lower matches", method = "weightWords(String,List<QLocale;>,List<QConceptWord;>)")
	public void weightWords_shouldWeightBetterMatchesHigherThanLowerMatches() throws Exception {
		Concept c = new Concept(1);
		
		ConceptName betterMatch = new ConceptName("the matching name", new Locale("en"));
		c.addName(betterMatch);
		
		// this is a worse match because it has a longer name
		ConceptName worseMatch = new ConceptName("this concept has a very long name", new Locale("en"));
		c.addName(worseMatch);
		
		ConceptWord wordWithAllSearchTerms = new ConceptWord("name", c, betterMatch, new Locale("en"));
		ConceptWord wordWithoutAllSearchTerms = new ConceptWord("name", c, worseMatch, new Locale("en"));
		
		List<ConceptWord> words = new ArrayList<ConceptWord>();
		words.add(wordWithoutAllSearchTerms);
		words.add(wordWithAllSearchTerms);
		
		List<ConceptWord> weightedWords = new ConceptServiceImpl().weightWords("name", Collections.singletonList(new Locale(
		        "en")), words);
		Assert.assertEquals("the matching name", weightedWords.get(0).getConceptName().getName());
	}
}

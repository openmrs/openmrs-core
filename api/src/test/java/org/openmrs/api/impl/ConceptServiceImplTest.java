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
import org.openmrs.ConceptWord;
import org.openmrs.api.ConceptNameType;
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
	@Verifies(value = "should not fail with null phrase", method = "weightWords(String, List, List)")
	public void weightWords_shouldNotFailWithNullPhrase() throws Exception {
		new ConceptServiceImpl().weightWords(null, Collections.singletonList(Locale.ENGLISH), new ArrayList<ConceptWord>());
	}
	
	/**
	 * @see ConceptServiceImpl#weightWords(String, List, List)
	 */
	@Test
	@Verifies(value = "should weight names that contain all words in search phrase higher than names that dont", method = "weightWords(String, List, List)")
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
	@Verifies(value = "should weight better matches higher than lower matches", method = "weightWords(String, List, List)")
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
	
	/**
	 * @see {@link ConceptServiceImpl#weightWords(String, List, List)}
	 */
	@Test
	@Verifies(value = "should weigh preferred names higher than other names in the locale", method = "weightWords(String, List, List)")
	public void weightWords_shouldWeighPreferredNamesHigherThanOtherNamesInTheLocale() throws Exception {
		Concept c = new Concept(1);
		
		ConceptName fullySpecifiedName = new ConceptName("fully specified", new Locale("en", "US"));
		c.addName(fullySpecifiedName);
		
		ConceptName localePreferred = new ConceptName("locale preferred", new Locale("en", "US"));
		c.setPreferredName(localePreferred);
		
		ConceptName indexTerm = new ConceptName("index Term", new Locale("en", "US"));
		indexTerm.setConceptNameType(ConceptNameType.INDEX_TERM);
		c.addName(indexTerm);
		
		ConceptWord preferredWord = new ConceptWord("name", c, localePreferred, new Locale("en", "US"));
		ConceptWord fullySpecifiedWord = new ConceptWord("name", c, fullySpecifiedName, new Locale("en", "US"));
		ConceptWord indexTermWord = new ConceptWord("name", c, indexTerm, new Locale("en", "US"));
		
		List<ConceptWord> words = new ArrayList<ConceptWord>();
		words.add(preferredWord);
		words.add(fullySpecifiedWord);
		words.add(indexTermWord);
		
		List<ConceptWord> weightedWords = new ConceptServiceImpl().weightWords("name", Collections.singletonList(new Locale(
		        "en", "US")), words);
		Assert.assertEquals("locale preferred", weightedWords.get(0).getConceptName().getName());
	}
	
	/**
	 * @see {@link ConceptServiceImpl#weightWords(String,List<QLocale;>,List<QConceptWord;>)}
	 */
	@Test
	@Verifies(value = "should weigh a fully specified name higher than a synonym in the locale", method = "weightWords(String,List<QLocale;>,List<QConceptWord;>)")
	public void weightWords_shouldWeighAFullySpecifiedNameHigherThanASynonymInTheLocale() throws Exception {
		Concept c = new Concept(1);
		
		ConceptName fullySpecifiedName = new ConceptName("fully specified", new Locale("en", "US"));
		c.addName(fullySpecifiedName);
		
		ConceptName synonym = new ConceptName("synonym", new Locale("en", "US"));
		c.addName(synonym);
		
		ConceptWord fullySpecifiedWord = new ConceptWord("name", c, fullySpecifiedName, new Locale("en", "US"));
		ConceptWord synonymWord = new ConceptWord("name", c, synonym, new Locale("en", "US"));
		
		List<ConceptWord> words = new ArrayList<ConceptWord>();
		
		words.add(fullySpecifiedWord);
		words.add(synonymWord);
		
		List<ConceptWord> weightedWords = new ConceptServiceImpl().weightWords("name", Collections.singletonList(new Locale(
		        "en", "US")), words);
		Assert.assertEquals("fully specified", weightedWords.get(0).getConceptName().getName());
	}
	
	/**
	 * @see {@link ConceptServiceImpl#weightWords(String,List<QLocale;>,List<QConceptWord;>)}
	 */
	@Test
	@Verifies(value = "should weigh a fully specified name higher than an indexTerm in the locale", method = "weightWords(String,List<QLocale;>,List<QConceptWord;>)")
	public void weightWords_shouldWeighAFullySpecifiedNameHigherThanAnIndexTermInTheLocale() throws Exception {
		Concept c = new Concept(1);
		
		ConceptName fullySpecifiedName = new ConceptName("fully specified", new Locale("en", "US"));
		c.addName(fullySpecifiedName);
		
		ConceptName indexTerm = new ConceptName("index Term", new Locale("en", "US"));
		indexTerm.setConceptNameType(ConceptNameType.INDEX_TERM);
		c.addName(indexTerm);
		
		ConceptWord fullySpecifiedWord = new ConceptWord("name", c, fullySpecifiedName, new Locale("en", "US"));
		ConceptWord indexTermWord = new ConceptWord("name", c, indexTerm, new Locale("en", "US"));
		
		List<ConceptWord> words = new ArrayList<ConceptWord>();
		
		words.add(fullySpecifiedWord);
		words.add(indexTermWord);
		
		List<ConceptWord> weightedWords = new ConceptServiceImpl().weightWords("name", Collections.singletonList(new Locale(
		        "en", "US")), words);
		Assert.assertEquals("fully specified", weightedWords.get(0).getConceptName().getName());
	}
	
}

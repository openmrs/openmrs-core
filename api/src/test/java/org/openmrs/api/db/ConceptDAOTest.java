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
package org.openmrs.api.db;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptWord;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests the concept word weighing logic, tests in this class assume that other factors remain
 * constant e.g the lengths of the words and the concept name they are associated to.
 */
public class ConceptDAOTest extends BaseContextSensitiveTest {
	
	private ConceptDAO dao = null;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		if (dao == null)
			// fetch the dao from the spring application context
			dao = (ConceptDAO) applicationContext.getBean("conceptDAO");
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should assign a higher weight to a shorter word if both words are equal to concept name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldAssignAHigherWeightToAShorterWordIfBothWordsAreEqualToConceptName() throws Exception {
		Concept c = new Concept();
		
		ConceptName synonymName = new ConceptName("to", Locale.ENGLISH);
		ConceptWord shorterWord = new ConceptWord("TO", c, synonymName, Locale.ENGLISH);
		
		ConceptName fullySpecName = new ConceptName("toy", Locale.ENGLISH);
		ConceptWord longerWord = new ConceptWord("TOY", c, fullySpecName, Locale.ENGLISH);
		//The shorter word should still outweigh this word even if this is a fully specified name and preferred
		fullySpecName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		fullySpecName.setLocalePreferred(true);
		
		Assert.assertTrue("A shorter word should weigh more than a longer one if both words match their concept names", dao
		        .weighConceptWord(shorterWord) > dao.weighConceptWord(longerWord));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should assign a higher weight to a shorter word if both words are at the start of the concept name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldAssignAHigherWeightToAShorterWordIfBothWordsAreAtTheStartOfTheConceptName()
	        throws Exception {
		Concept c = new Concept();
		
		ConceptName synonymName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord shorterWord = new ConceptWord("MY", c, synonymName, Locale.ENGLISH);
		
		ConceptName fullySpecName = new ConceptName("hom depot", Locale.ENGLISH);
		ConceptWord longerWord = new ConceptWord("HOM", c, fullySpecName, Locale.ENGLISH);
		//The shorter word should still outweigh this word even if this is a fully specified name and preferred
		fullySpecName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		fullySpecName.setLocalePreferred(true);
		Assert.assertTrue(
		    "A shorter word should weigh more than a longer one if both words are at the start of their concept names", dao
		            .weighConceptWord(shorterWord) > dao.weighConceptWord(longerWord));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should assign zero weight if the word is not among the concept name words", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldAssignZeroWeightIfTheWordIsNotAmongTheConceptNameWords() throws Exception {
		Concept c = new Concept();
		ConceptName cn = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord invalidWord = new ConceptWord("MYT", c, cn, Locale.ENGLISH);
		Assert.assertEquals(dao.weighConceptWord(invalidWord), new Double(0));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should weigh a word for a fully specified name higher than that of a synonym", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighAWordForAFullySpecifiedNameHigherThanThatOfASynonym() throws Exception {
		Concept c = new Concept();
		
		ConceptName synonymName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord synonymWord = new ConceptWord("MY", c, synonymName, Locale.ENGLISH);
		
		ConceptName fullySpecName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord fullySpecWord = new ConceptWord("MY", c, fullySpecName, Locale.ENGLISH);
		fullySpecName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		
		Assert.assertTrue("A word for a fully specified name should weigh higher than that of a syonym", dao
		        .weighConceptWord(fullySpecWord) > dao.weighConceptWord(synonymWord));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should weigh a word for a preferred fullySpecified higher than that of a plain fullySpecified name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighAWordForAPreferredFullySpecifiedHigherThanThatOfAPlainFullySpecifiedName()
	        throws Exception {
		Concept c = new Concept();
		
		ConceptName prefFullSpecName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord prefFullSpecWord = new ConceptWord("MY", c, prefFullSpecName, Locale.ENGLISH);
		prefFullSpecName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		prefFullSpecName.setLocalePreferred(true);
		
		ConceptName fullySpecName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord fullySpecWord = new ConceptWord("MY", c, fullySpecName, Locale.ENGLISH);
		fullySpecName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		
		Assert.assertTrue(
		    "A word for a preferred fully specified name should weigh higher than that of a plain fully specified name", dao
		            .weighConceptWord(prefFullSpecWord) > dao.weighConceptWord(fullySpecWord));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should weigh a word for a preferred fullySpecified higher than that of a plain preferred name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighAWordForAPreferredFullySpecifiedHigherThanThatOfAPlainPreferredName()
	        throws Exception {
		Concept c = new Concept();
		
		ConceptName prefFullSpecName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord prefFullSpecWord = new ConceptWord("MY", c, prefFullSpecName, Locale.ENGLISH);
		prefFullSpecName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		prefFullSpecName.setLocalePreferred(true);
		
		ConceptName prefName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord prefWord = new ConceptWord("MY", c, prefName, Locale.ENGLISH);
		prefName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		
		Assert.assertTrue(
		    "A word for a preferred fully specified name should weigh higher than that of a plain preferred name", dao
		            .weighConceptWord(prefFullSpecWord) > dao.weighConceptWord(prefWord));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should weigh a word for a preferred name higher than that of a fully specified name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighAWordForAPreferredNameHigherThanThatOfAFullySpecifiedName() throws Exception {
		Concept c = new Concept();
		
		ConceptName prefName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord prefWord = new ConceptWord("MY", c, prefName, Locale.ENGLISH);
		prefName.setLocalePreferred(true);
		
		ConceptName fullySpecName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord fullySpecWord = new ConceptWord("MY", c, fullySpecName, Locale.ENGLISH);
		fullySpecName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		
		Assert.assertTrue("A word for a preferred name should weigh higher than that of a fully specified name", dao
		        .weighConceptWord(prefWord) > dao.weighConceptWord(fullySpecWord));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should weigh a word for a shorter concept name higher than that of a longer concept name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighAWordForAShorterConceptNameHigherThanThatOfALongerConceptName() throws Exception {
		Concept c = new Concept();
		
		ConceptName shorterName = new ConceptName("toy", Locale.ENGLISH);
		ConceptWord shorterNameWord = new ConceptWord("TO", c, shorterName, Locale.ENGLISH);
		
		ConceptName longerName = new ConceptName("toye", Locale.ENGLISH);
		ConceptWord longerNameWord = new ConceptWord("TO", c, longerName, Locale.ENGLISH);
		//The word for the shorter concept name should still outweigh this word even 
		//if this is a fully specified name and preferred
		longerName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		longerName.setLocalePreferred(true);
		
		Assert.assertTrue("A word for shorter concept name should weigh more than that of a longer concept name", dao
		        .weighConceptWord(shorterNameWord) > dao.weighConceptWord(longerNameWord));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should weigh a word for a synonym higher than that of a short name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighAWordForASynonymHigherThanThatOfAShortName() throws Exception {
		Concept c = new Concept();
		
		ConceptName synonymName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord synonymWord = new ConceptWord("MY", c, synonymName, Locale.ENGLISH);
		
		ConceptName shortName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord shortWord = new ConceptWord("MY", c, shortName, Locale.ENGLISH);
		shortName.setConceptNameType(ConceptNameType.SHORT);
		
		Assert.assertTrue("A word for a synonym should weigh higher than that of a short name", dao
		        .weighConceptWord(synonymWord) > dao.weighConceptWord(shortWord));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should weigh a word for an index term higher than that of a fully specified name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighAWordForAnIndexTermHigherThanThatOfAFullySpecifiedName() throws Exception {
		Concept c = new Concept();
		
		ConceptName indexTermName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord indexTermWord = new ConceptWord("MY", c, indexTermName, Locale.ENGLISH);
		indexTermName.setConceptNameType(ConceptNameType.INDEX_TERM);
		
		ConceptName fullySpecName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord fullySpecWord = new ConceptWord("MY", c, fullySpecName, Locale.ENGLISH);
		fullySpecName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		
		Assert.assertTrue("A word for an index term should weigh higher than that of a fully specified name", dao
		        .weighConceptWord(indexTermWord) > dao.weighConceptWord(fullySpecWord));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should weigh a word for an index term higher than that of a preferred name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighAWordForAnIndexTermHigherThanThatOfAPreferredName() throws Exception {
		Concept c = new Concept();
		
		ConceptName indexTermName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord indexTermWord = new ConceptWord("MY", c, indexTermName, Locale.ENGLISH);
		indexTermName.setConceptNameType(ConceptNameType.INDEX_TERM);
		
		ConceptName prefName = new ConceptName("my depot", Locale.ENGLISH);
		ConceptWord prefWord = new ConceptWord("MY", c, prefName, Locale.ENGLISH);
		prefName.setLocalePreferred(true);
		
		Assert.assertTrue("A word for an index term should weigh higher than that of a preferred name", dao
		        .weighConceptWord(indexTermWord) > dao.weighConceptWord(prefWord));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should weigh words closer to the start higher than those closer to the end of the concept name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighWordsCloserToTheStartHigherThanThoseCloserToTheEndOfTheConceptName()
	        throws Exception {
		Concept c = new Concept();
		
		ConceptName cn1 = new ConceptName("on my way", Locale.ENGLISH);
		ConceptWord word1 = new ConceptWord("ON", c, cn1, Locale.ENGLISH);
		//note that we keep the lengths of the concept names the same so that the difference
		//will only be because of the indexes of the words in their concept names
		ConceptName cn2 = new ConceptName("in my way", Locale.ENGLISH);
		//Note that the index of the word here is greater than that  of the other word	
		ConceptWord word2 = new ConceptWord("MY", c, cn2, Locale.ENGLISH);
		cn2.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		cn2.setLocalePreferred(true);
		
		Assert.assertTrue("A word closer to the start of its concept name should weigh more than one closer to the end", dao
		        .weighConceptWord(word1) > dao.weighConceptWord(word2));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "should weigh a word equal to a concept name higher than one that matches the start of the concept name", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighAWordEqualToAConceptNameHigherThanOneThatMatchesTheStartOfTheConceptName()
	        throws Exception {
		Concept c = new Concept();
		
		ConceptName cn1 = new ConceptName("matching", Locale.ENGLISH);
		ConceptWord word1 = new ConceptWord("MATCHING", c, cn1, Locale.ENGLISH);
		//note that we keep the lengths of the concept names the same so that the difference
		//will only be because one word matches the concept name and the other doesn't
		ConceptName cn2 = new ConceptName("in or up", Locale.ENGLISH);
		ConceptWord word2 = new ConceptWord("IN", c, cn2, Locale.ENGLISH);
		cn2.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		cn2.setLocalePreferred(true);
		
		Assert.assertTrue("A word matching the concept name should weigh more than other words that don't", dao
		        .weighConceptWord(word1) > dao.weighConceptWord(word2));
	}
	
	/**
	 * @see {@link ConceptDAO#weighConceptWord(ConceptWord)}
	 */
	@Test
	@Verifies(value = "weigh words when jvm is run in a locale with a different decimal separator character", method = "weighConceptWord(ConceptWord)")
	public void weighConceptWord_shouldWeighWordsWhenJvmIsRunInALocaleWithADifferentDecimalSeparatorCharacter()
	        throws Exception {
		//simulate an environment where the default locale uses a different decimal character
		Locale locale = Locale.FRENCH;
		Locale.setDefault(locale);
		ConceptName cn = new ConceptName("bonjour monsieur", locale);
		ConceptWord word = new ConceptWord("BONJOUR", new Concept(), cn, locale);
		//Sanity check for the test to be concrete, i.e. the word should be part of the concept name
		Assert.assertTrue(cn.getName().toUpperCase().contains(word.getWord()));
		dao.weighConceptWord(word);
	}
	
	/**
	 * @see {@link ConceptDAO#allConceptSources(boolean)}
	 */
	@Test
	@Verifies(value = "should return all concept sources", method = "getAllConceptSources(boolean)")
	public void AllConceptSources_shouldReturnAllConceptSources() throws Exception {
		Assert.assertEquals(dao.getAllConceptSources(true).size(), 5);
	}
	
	/**
	 * @see {@link ConceptDAO#allConceptSources(boolean)}
	 */
	@Test
	@Verifies(value = "should return all unretired concept sources", method = "getAllConceptSources(boolean)")
	public void AllConceptSources_shouldReturnAllUnretiredConceptSources() throws Exception {
		Assert.assertEquals(dao.getAllConceptSources(false).size(), 3);
	}
	
	/**
	 * @see {@link ConceptDAO#isConceptMapTypeInUse(ConceptMapType)}
	 */
	@Test
	@Verifies(value = "should return true if a mapType has a conceptMap or more using it", method = "isConceptMapTypeInUse(ConceptMapType)")
	public void isConceptMapTypeInUse_shouldReturnTrueIfAMapTypeHasAConceptMapOrMoreUsingIt() throws Exception {
		Assert.assertTrue(dao.isConceptMapTypeInUse(Context.getConceptService().getConceptMapType(6)));
	}
	
	/**
	 * @see {@link ConceptDAO#isConceptMapTypeInUse(ConceptMapType)}
	 */
	@Test
	@Verifies(value = "should return true if a mapType has a conceptReferenceTermMap or more using it", method = "isConceptMapTypeInUse(ConceptMapType)")
	public void isConceptMapTypeInUse_shouldReturnTrueIfAMapTypeHasAConceptReferenceTermMapOrMoreUsingIt() throws Exception {
		Assert.assertTrue(dao.isConceptMapTypeInUse(Context.getConceptService().getConceptMapType(4)));
	}
	
	/**
	 * @see {@link ConceptDAO#isConceptReferenceTermInUse(ConceptReferenceTerm)}
	 */
	@Test
	@Verifies(value = "should return true if a term has a conceptMap or more using it", method = "isConceptReferenceTermInUse(ConceptReferenceTerm)")
	public void isConceptReferenceTermInUse_shouldReturnTrueIfATermHasAConceptMapOrMoreUsingIt() throws Exception {
		Assert.assertTrue(dao.isConceptReferenceTermInUse(Context.getConceptService().getConceptReferenceTerm(10)));
	}
	
	/**
	 * @see {@link ConceptDAO#isConceptReferenceTermInUse(ConceptReferenceTerm)}
	 */
	@Test
	@Verifies(value = "should return true if a term has a conceptReferenceTermMap or more using it", method = "isConceptReferenceTermInUse(ConceptReferenceTerm)")
	public void isConceptReferenceTermInUse_shouldReturnTrueIfATermHasAConceptReferenceTermMapOrMoreUsingIt()
	        throws Exception {
		Assert.assertTrue(dao.isConceptReferenceTermInUse(Context.getConceptService().getConceptReferenceTerm(2)));
	}
	
	/**
	 * @see {@link ConceptDAO#isConceptMapTypeInUse(ConceptMapType)}
	 */
	@Test
	@Verifies(value = "should return false if a mapType has no maps using it", method = "isConceptMapTypeInUse(ConceptMapType)")
	public void isConceptMapTypeInUse_shouldReturnFalseIfAMapTypeHasNoMapsUsingIt() throws Exception {
		Assert.assertFalse(dao.isConceptMapTypeInUse(Context.getConceptService().getConceptMapType(3)));
	}
	
	/**
	 * @see {@link ConceptDAO#isConceptReferenceTermInUse(ConceptReferenceTerm)}
	 */
	@Test
	@Verifies(value = "should return false if a term has no maps using it", method = "isConceptReferenceTermInUse(ConceptReferenceTerm)")
	public void isConceptReferenceTermInUse_shouldReturnFalseIfATermHasNoMapsUsingIt() throws Exception {
		Assert.assertFalse(dao.isConceptReferenceTermInUse(Context.getConceptService().getConceptReferenceTerm(11)));
	}
	
	@Test
	@Verifies(value = "should delete concept from datastore", method = "purgeConcept")
	public void purgeConcept_shouldDeleteConceptWithWords() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-words.xml");
		Concept concept = dao.getConcept(5497);
		dao.purgeConcept(concept);
		
		assertNull(dao.getConcept(5497));
	}
	
	@Test
	@Verifies(value = "should update concept in datastore", method = "updateConcept")
	public void updateConceptWord_shouldUpdateConceptWithWords() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-words.xml");
		Concept concept = dao.getConcept(5497);
		dao.updateConceptWord(concept);
		
		assertNotNull(dao.getConcept(5497));
	}
	
	/**
	 * @see {@link
	 *      ConceptDAO#getConcepts(String,Locale,null,List<QConceptClass;>,List<QConceptDatatype;>)}
	 */
	@Test
	@Verifies(value = "should not return concepts with matching names that are voided", method = "getConcepts(String,Locale,null,List<QConceptClass;>,List<QConceptDatatype;>)")
	public void getConcepts_shouldNotReturnConceptsWithMatchingNamesThatAreVoided() throws Exception {
		Concept concept = dao.getConcept(7);
		Context.getConceptService().updateConceptIndex(concept);
		Assert.assertEquals(0, dao.getConcepts("VOIDED", null, false, new ArrayList<ConceptClass>(),
		    new ArrayList<ConceptDatatype>()).size());
	}
	
	/**
	 * @see ConceptDAO#getConceptsByAnswer(Concept)
	 * @verifies return concepts for the given answer concept
	 */
	@Test
	public void getConceptsByAnswer_shouldReturnConceptsForTheGivenAnswerConcept() throws Exception {
		Concept concept = dao.getConcept(22);
		List<Concept> conceptsByAnswer = dao.getConceptsByAnswer(concept);
		Assert.assertNotNull(conceptsByAnswer);
		Assert.assertEquals(1, conceptsByAnswer.size());
		Concept conceptByAnswer = conceptsByAnswer.get(0);
		Assert.assertEquals(21, conceptByAnswer.getConceptId().intValue());
	}
	
	/**
	 * @see {@link
	 *      ConceptDAO#getConcepts(String,List<Locale>,null,List<ConceptClass>,List<ConceptClass
	 *      >,List<ConceptDatatype>,List<ConceptDatatype>,Concept,Integer,Integer)}
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	@Verifies(value = "should return correct results for concept with names that contains words with more weight", method = "getConcepts(String,List<Locale>,null,List<ConceptClass>,List<ConceptClass>,List<ConceptDatatype>,List<ConceptDatatype>,Concept,Integer,Integer)")
	public void getConcepts_shouldReturnCorrectResultsForConceptWithNamesThatContainsWordsWithMoreWeight() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-words.xml");
		Concept conceptWithMultipleMatchingNames = dao.getConcept(3000);
		//recalculate the weights just in case the logic for calculating the weights is changed
		ConceptService cs = Context.getConceptService();
		cs.updateConceptIndex(conceptWithMultipleMatchingNames);
		cs.updateConceptIndex(dao.getConcept(4000));
		List<ConceptSearchResult> searchResults = dao
		        .getConcepts("trust", Collections.singletonList(Locale.ENGLISH), false, Collections.EMPTY_LIST,
		            Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, null, null, null);
		
		Assert.assertEquals(2, searchResults.size());
		//the first concept is the one with a word with the highest weight
		Assert.assertEquals(conceptWithMultipleMatchingNames, searchResults.get(0).getConcept());
		//For conceptId=3000, its search result should ALWAYS match on 'TRUST ME' because it is shorter THAN 'TRUST ALWAYS'
		Assert.assertEquals(9998, searchResults.get(0).getConceptName().getConceptNameId().intValue());
	}
	
	/**
	 * @see {@link
	 *      ConceptDAO#getConcepts(String,List<Locale>,null,List<ConceptClass>,List<ConceptClass>,
	 *      List<ConceptDatatype>,List<ConceptDatatype>,Concept,Integer,Integer)}
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	@Verifies(value = "should return correct results if a concept name contains same word more than once", method = "getConcepts(String,List<QLocale;>,null,List<QConceptClass;>,List<QConceptClass;>,List<QConceptDatatype;>,List<QConceptDatatype;>,Concept,Integer,Integer)")
	public void getConcepts_shouldReturnCorrectResultsIfAConceptNameContainsSameWordMoreThanOnce() throws Exception {
		ConceptService cs = Context.getConceptService();
		ConceptClass cc = cs.getConceptClass(1);
		Locale locale = Locale.ENGLISH;
		ConceptDatatype dt = cs.getConceptDatatype(4);
		Concept c1 = new Concept();
		ConceptName cn1a = new ConceptName("ONE TERM", locale);
		c1.addName(cn1a);
		c1.setConceptClass(cc);
		c1.setDatatype(dt);
		cs.saveConcept(c1);
		
		ConceptName cn1b = new ConceptName("ONE TO ONE", locale);
		cn1b.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		cn1b.setLocalePreferred(true);
		c1.addName(cn1b);
		cs.saveConcept(c1);
		Assert.assertTrue(cn1a.isSynonym());
		Assert.assertTrue(cn1b.getConceptNameId() > cn1a.getConceptNameId());
		
		Concept c2 = new Concept();
		ConceptName cn2a = new ConceptName("ONE TO MANY", locale);
		c2.addName(cn2a);
		c2.setConceptClass(cc);
		c2.setDatatype(dt);
		cs.saveConcept(c2);
		
		List<ConceptSearchResult> searchResults1 = dao
		        .getConcepts("one", Collections.singletonList(locale), false, Collections.EMPTY_LIST,
		            Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, null, null, null);
		
		Assert.assertEquals(2, searchResults1.size());
		Assert.assertEquals(c1, searchResults1.get(0).getConcept());
		Assert.assertEquals(cn1b, searchResults1.get(0).getConceptName());
	}
	
}

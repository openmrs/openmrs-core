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

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptWord;
import org.openmrs.api.ConceptNameType;
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
	
}

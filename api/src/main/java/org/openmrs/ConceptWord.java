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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

/**
 * ConceptWord Concept words are the individual terms of which a concept name is composed. They are
 * case-preserving but compare case insensitively.
 */
public class ConceptWord implements java.io.Serializable, Comparable<ConceptWord> {
	
	public static final long serialVersionUID = 888677L;
	
	// Fields
	/**
	 * @since 1.5
	 */
	private Integer conceptWordId;
	
	private Concept concept;
	
	private ConceptName conceptName;
	
	private String word;
	
	@Deprecated
	private String synonym;
	
	private Locale locale;
	
	private Double weight = 0.0;
	
	/**
	 * Get the unique internal database identifier for this concept word
	 * 
	 * @since 1.5
	 */
	public Integer getConceptWordId() {
		return conceptWordId;
	}
	
	/**
	 * Set the unique identifier for this concept word
	 * 
	 * @since 1.5
	 */
	public void setConceptWordId(Integer conceptWordId) {
		this.conceptWordId = conceptWordId;
	}
	
	// Constructors
	
	/** default constructor */
	public ConceptWord() {
	}
	
	/**
	 * Convenience constructor
	 * 
	 * @param word the single word that will be matched to search terms
	 * @param concept the concept that is being matched to
	 * @param conceptName the specific name that will be matched
	 * @param locale the locale that is being pulled from
	 */
	public ConceptWord(String word, Concept concept, ConceptName conceptName, Locale locale) {
		this.concept = concept;
		this.conceptName = conceptName;
		this.word = word;
		this.locale = locale;
	}
	
	/**
	 * @deprecated see {@link #ConceptWord(String, Concept, ConceptName, Locale)}
	 */
	@Deprecated
	public ConceptWord(String word, Concept concept, ConceptName conceptName, Locale locale, String synonym) {
		this.concept = concept;
		this.conceptName = conceptName;
		this.word = word;
		this.locale = locale;
		this.synonym = synonym;
	}
	
	public ConceptWord(Concept c, ConceptName conceptName) {
		this.concept = c;
		this.conceptName = conceptName;
		this.word = null;
		this.locale = null;
	}
	
	/**
	 * @return Returns the locale.
	 */
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * @param locale The locale to set.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	/**
	 * @return Returns the synonym.
	 * @deprecated this value is not stored anymore
	 */
	@Deprecated
	public String getSynonym() {
		return synonym;
	}
	
	/**
	 * @param synonym The synonym to set.
	 * @deprecated this value is not stored anymore
	 */
	@Deprecated
	public void setSynonym(String synonym) {
		this.synonym = synonym;
	}
	
	/**
	 * @return Returns the word.
	 */
	public String getWord() {
		return word;
	}
	
	/**
	 * @param word The word to set.
	 */
	public void setWord(String word) {
		this.word = word;
	}
	
	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * Sets the concept name associated with this word.
	 * 
	 * @param conceptName
	 */
	public void setConceptName(ConceptName conceptName) {
		this.conceptName = conceptName;
	}
	
	/**
	 * @return Returns the concept name from which this word was derived.
	 */
	public ConceptName getConceptName() {
		return conceptName;
	}
	
	/**
	 * @return Returns the weight.
	 */
	public Double getWeight() {
		return weight;
	}
	
	/**
	 * @param weight The weight to set.
	 */
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
	/**
	 * Increment the weight by i
	 * 
	 * @param i
	 */
	public void increaseWeight(Double i) {
		this.weight += i;
	}
	
	/**
	 * @param concept The concept from which to make the list of concept words.
	 * @return Returns a list of unique concept words based on the specified concept.
	 * @should return separate ConceptWord objects for the same word in different ConceptNames
	 * @should not include voided names
	 */
	
	public static Set<ConceptWord> makeConceptWords(Concept concept) {
		Set<ConceptWord> words = new HashSet<ConceptWord>();
		
		for (ConceptName name : concept.getNames()) {
			if (!name.isVoided()) {
				List<String> uniqueParts = getUniqueWords(name.getName(), name.getLocale());
				for (String part : uniqueParts) {
					words.add(new ConceptWord(part, concept, name, name.getLocale()));
				}
			}
		}
		return words;
	}
	
	/**
	 * Split the given phrase on words and remove unique and stop words
	 * 
	 * @param phrase
	 * @return Returns a list of the unique parts of the phrase, in all upper case.
	 */
	public static List<String> getUniqueWords(String phrase) {
		return getUniqueWords(phrase, Context.getLocale());
	}
	
	/**
	 * Split the given phrase on words and remove unique and stop words for the given locale
	 * 
	 * @param phrase
	 * @param locale
	 * @return Returns a list of the unique parts of the phrase, in all upper case.
	 * @since 1.8
	 */
	public static List<String> getUniqueWords(String phrase, Locale locale) {
		String[] parts = splitPhrase(phrase);
		List<String> uniqueParts = new Vector<String>();
		
		if (parts != null) {
			List<String> conceptStopWords = Context.getConceptService().getConceptStopWords(locale);
			for (String part : parts) {
				if (!StringUtils.isBlank(part)) {
					String upper = part.trim().toUpperCase();
					if (!conceptStopWords.contains(upper) && !uniqueParts.contains(upper))
						uniqueParts.add(upper);
				}
			}
		}
		
		return uniqueParts;
	}
	
	/**
	 * Split words according to OpenmrsConstants.REGEXes, newlines, and spaces
	 * 
	 * @param phrase
	 * @return String[] array of words
	 */
	public static String[] splitPhrase(String phrase) {
		if (StringUtils.isBlank(phrase)) {
			return null;
		}
		if (phrase.length() > 2) {
			phrase = phrase.replaceAll(OpenmrsConstants.REGEX_LARGE, " ");
		} else {
			phrase = phrase.replaceAll(OpenmrsConstants.REGEX_SMALL, " ");
		}
		
		return phrase.trim().replace('\n', ' ').split(" ");
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = "";
		if (concept != null)
			s += concept.getConceptId() + "|";
		if (word != null)
			s += word + "|";
		if (locale != null)
			s += locale;
		return s;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ConceptWord word) {
		return Double.compare(word.getWeight(), weight);
	}
	
}

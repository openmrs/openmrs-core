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

/**
 * An Object of this class represents a search result returned when searching for concepts, it holds
 * extra metadata about the matched concept(s).
 *
 * @since 1.8
 */
public class ConceptSearchResult {
	
	private static final long serialVersionUID = 6394792520635644989L;
	
	// the matching concept that was found
	private Concept concept;
	
	// the actual conceptName that was matching a given word in the search
	private ConceptName conceptName;
	
	// the word in the search string that was matched against the conceptNameHit
	private String word;
	
	private Double transientWeight = 0.0;
	
	/** default constructor */
	public ConceptSearchResult() {
	}
	
	/**
	 * Convenience constructor
	 *
	 * @param word the single word that will be matched to search terms
	 * @param concept the concept that is being matched to
	 * @param conceptName the specific name that will be matched
	 */
	public ConceptSearchResult(String word, Concept concept, ConceptName conceptName) {
		this.concept = concept;
		this.conceptName = conceptName;
		this.word = word;
	}
	
	/**
	 * Convenience constructor that takes in a weight too
	 *
	 * @param word the single word that will be matched to search terms
	 * @param concept the concept that is being matched to
	 * @param conceptName the specific name that will be matched
	 * @param transientWeight the weight for this conceptSearchResult
	 */
	public ConceptSearchResult(String word, Concept concept, ConceptName conceptName, Double transientWeight) {
		this.concept = concept;
		this.conceptName = conceptName;
		this.word = word;
		if (transientWeight != null) {
			this.transientWeight = transientWeight;
		}
	}
	
	/**
	 * @return the concept
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * @param concept the concept to set
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * @return the conceptName
	 */
	public ConceptName getConceptName() {
		return conceptName;
	}
	
	/**
	 * @param conceptName the conceptName to set
	 */
	public void setConceptName(ConceptName conceptName) {
		this.conceptName = conceptName;
	}
	
	/**
	 * @return the word
	 */
	public String getWord() {
		return word;
	}
	
	/**
	 * @param word the word to set
	 */
	public void setWord(String word) {
		this.word = word;
	}
	
	/**
	 * Getter for transientWeight
	 *
	 * @return transient weight
	 */
	public Double getTransientWeight() {
		return transientWeight;
	}
	
	/**
	 * Setter transientWeight
	 *
	 * @param transientWeight
	 */
	public void setTransientWeight(Double transientWeight) {
		this.transientWeight = transientWeight;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ConceptSearchResult)) {
			return false;
		}
		ConceptSearchResult other = (ConceptSearchResult) obj;
		if (getConcept() == null) {
			return false;
		}
		return getConcept().equals(other.getConcept());
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (getConcept() == null) {
			return super.hashCode();
		}
		return getConcept().hashCode();
	}
}

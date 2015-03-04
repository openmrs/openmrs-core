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
 * ConceptSetDerived
 */
public class ConceptSetDerived extends BaseOpenmrsObject implements java.io.Serializable {
	
	public static final long serialVersionUID = 3788L;
	
	// Fields
	
	private Concept concept;
	
	private Concept conceptSet;
	
	private Double sortWeight;
	
	// Constructors
	
	/** default constructor */
	public ConceptSetDerived() {
	}
	
	public ConceptSetDerived(Concept set, Concept concept, Double weight) {
		setConceptSet(set);
		setConcept(concept);
		setSortWeight(weight);
	}
	
	/**
	 * 
	 */
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * 
	 */
	public Concept getConceptSet() {
		return conceptSet;
	}
	
	public void setConceptSet(Concept set) {
		this.conceptSet = set;
	}
	
	/**
	 * @return Returns the sortWeight.
	 */
	public Double getSortWeight() {
		return sortWeight;
	}
	
	/**
	 * @param sortWeight The sortWeight to set.
	 */
	public void setSortWeight(Double sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
	}
	
}

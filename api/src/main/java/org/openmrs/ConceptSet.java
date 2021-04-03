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

import java.util.Date;

import org.openmrs.util.OpenmrsUtil;

/**
 * This represents a single concept within a concept set.
 */
public class ConceptSet extends BaseOpenmrsObject implements Auditable, java.io.Serializable, Comparable<ConceptSet> {
	
	public static final long serialVersionUID = 3787L;
	
	// Fields
	private Integer conceptSetId;
	
	// concept in the set
	private Concept concept; 
	
	// parent concept that uses this set
	private Concept conceptSet; 
	
	private Double sortWeight;
	
	private User creator;
	
	private Date dateCreated;
	
	// Constructors
	
	/** default constructor */
	public ConceptSet() {
	}
	
	public ConceptSet(Concept concept, Double weight) {
		setConcept(concept);
		setSortWeight(weight);
	}
	
	// Property accessors
	
	/**
	 * Gets the concept set identifier.
	 * 
	 * @return the concept set identifier
	 */
	public Integer getConceptSetId() {
		return conceptSetId;
	}
	
	/**
	 * Sets the concept set identifier.
	 * 
	 * @param conceptSetId The concept set identifier.
	 */
	public void setConceptSetId(Integer conceptSetId) {
		this.conceptSetId = conceptSetId;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
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
	 * @return Returns the creator.
	 */
	@Override
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @param creator The creator to set.
	 */
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return Returns the dateCreated.
	 */
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptSetId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		this.setConceptSetId(id);
	}
	
	/**
	 * Not currently used. Always returns null.
	 * 
	 * @see org.openmrs.Auditable#getChangedBy()
	 */
	@Override
	public User getChangedBy() {
		return null;
	}
	
	/**
	 * Not currently used. Always returns null.
	 * 
	 * @see org.openmrs.Auditable#getDateChanged()
	 */
	@Override
	public Date getDateChanged() {
		return null;
	}
	
	/**
	 * Not currently used.
	 * 
	 * @see org.openmrs.Auditable#setChangedBy(org.openmrs.User)
	 */
	@Override
	public void setChangedBy(User changedBy) {
	}
	
	/**
	 * Not currently used.
	 * 
	 * @see org.openmrs.Auditable#setDateChanged(java.util.Date)
	 */
	@Override
	public void setDateChanged(Date dateChanged) {
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Note: this comparator imposes orderings that are inconsistent with equals.
	 */
	@Override
	@SuppressWarnings("squid:S1210")
	public int compareTo(ConceptSet cs) {
		int value = OpenmrsUtil.compareWithNullAsLowest(concept.getRetired(), cs.concept.getRetired());
		if (value == 0) {
			value = OpenmrsUtil.compareWithNullAsLowest(this.getSortWeight(), cs.getSortWeight());
		}
		return value;
	}
	
}

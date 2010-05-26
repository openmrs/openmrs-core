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

import java.util.Date;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * This represents a single concept within a concept set.
 */
@Root
public class ConceptSet extends BaseOpenmrsObject implements Auditable, java.io.Serializable, Comparable<ConceptSet>  {
	
	public static final long serialVersionUID = 3787L;
	
	// Fields
	private Integer conceptSetId;
	
	private Concept concept; // concept in the set
	
	private Concept conceptSet; // parent concept that uses this set
	
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
	
	@Override
    public boolean equals(Object obj) {
		if (obj instanceof ConceptSet) {
			if (concept == null || conceptSet == null)
				return false;
			
			ConceptSet c = (ConceptSet) obj;
			return (this.concept.equals(c.getConcept()) && this.conceptSet.equals(c.getConceptSet()));
		}
		return false;
	}
	
	@Override
    public int hashCode() {
		if (this.getConcept() == null || this.getConceptSet() == null)
			return super.hashCode();
		return this.getConcept().hashCode() + this.getConceptSet().hashCode();
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
	
	/**
	 * 
	 */
	@Element
	public Concept getConcept() {
		return concept;
	}
	
	@Element
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * 
	 */
	@Element
	public Concept getConceptSet() {
		return conceptSet;
	}
	
	@Element
	public void setConceptSet(Concept set) {
		this.conceptSet = set;
	}
	
	/**
	 * @return Returns the sortWeight.
	 */
	@Attribute
	public Double getSortWeight() {
		return sortWeight;
	}
	
	/**
	 * @param sortWeight The sortWeight to set.
	 */
	@Attribute
	public void setSortWeight(Double sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	/**
	 * @return Returns the creator.
	 */
	@Element
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @param creator The creator to set.
	 */
	@Element
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return Returns the dateCreated.
	 */
	@Element
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Element
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getConceptSetId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		this.setConceptSetId(id);
	}
	
	/**
	 * Not currently used. Always returns null.
	 * 
	 * @see org.openmrs.Auditable#getChangedBy()
	 */
	public User getChangedBy() {
		return null;
	}
	
	/**
	 * Not currently used. Always returns null.
	 * 
	 * @see org.openmrs.Auditable#getDateChanged()
	 */
	public Date getDateChanged() {
		return null;
	}
	
	/**
	 * Not currently used.
	 * 
	 * @see org.openmrs.Auditable#setChangedBy(org.openmrs.User)
	 */
	public void setChangedBy(User changedBy) {
	}
	
	/**
	 * Not currently used.
	 * 
	 * @see org.openmrs.Auditable#setDateChanged(java.util.Date)
	 */
	public void setDateChanged(Date dateChanged) {
	}

	/**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ConceptSet cs) {
    	if((getSortWeight() == null) && (cs.getSortWeight() != null)) return -1;
    	if((getSortWeight() != null) && (cs.getSortWeight() == null)) return 1;
    	if((getSortWeight() == null) && (cs.getSortWeight() == null)) return 0;
		return (getSortWeight() < cs.getSortWeight()) ? -1 : (getSortWeight() > cs.getSortWeight()) ? 1 : 0;
    }
}

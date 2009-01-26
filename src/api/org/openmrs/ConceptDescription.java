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
import java.util.Locale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * ConceptDescription is the localized description of a concept.
 */
@Root
public class ConceptDescription implements java.io.Serializable {
	
	private static final long serialVersionUID = -7223075113369136584L;
	
	// Fields
	private Integer conceptDescriptionId;
	
	private Concept concept;
	
	private String description;
	
	private Locale locale;
	
	private User creator;
	
	private Date dateCreated;
	
	private User changedBy;
	
	private Date dateChanged;
	
	// Constructors
	
	/** default constructor */
	public ConceptDescription() {
	}
	
	/**
	 * Constructor that takes in the primary key for this object
	 * 
	 * @param conceptDescriptionId the id for this description
	 */
	public ConceptDescription(Integer conceptDescriptionId) {
		this.conceptDescriptionId = conceptDescriptionId;
	}
	
	/**
	 * Constructor specifying the description and locale.
	 * 
	 * @param description
	 * @param locale
	 */
	public ConceptDescription(String description, Locale locale) {
		setLocale(locale);
		setDescription(description);
	}
	
	/**
	 * @see java.lang.Object#equals(Object)
	 * 
	 * @should compare on id if its non null
	 * @should not return true with different objects and null ids
	 * @should default to object equality
	 */
	public boolean equals(Object object) {
		if (!(object instanceof ConceptDescription)) {
			return false;
		}
		ConceptDescription rhs = (ConceptDescription) object;
		if (conceptDescriptionId != null && rhs.conceptDescriptionId != null)
			return this.conceptDescriptionId == rhs.conceptDescriptionId;
		else
			return this == object;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getConceptDescriptionId() == null)
			return super.hashCode();
		int hash = 8;
		hash = 33 * this.getConceptDescriptionId() + hash;
		return hash;
	}
	
	/**
	 * @return Returns the conceptDescriptionId.
	 */
	@Attribute
	public Integer getConceptDescriptionId() {
		return conceptDescriptionId;
	}
	
	/**
	 * @param conceptDescriptionId The conceptId to set.
	 */
	@Attribute
	public void setConceptDescriptionId(Integer conceptDescriptionId) {
		this.conceptDescriptionId = conceptDescriptionId;
	}
	
	// Property accessors
	
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
	@Element(data = true)
	public String getDescription() {
		return description;
	}
	
	@Element(data = true)
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * 
	 */
	@Attribute
	public Locale getLocale() {
		return locale;
	}
	
	@Attribute
	public void setLocale(Locale locale) {
		this.locale = locale;
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
	 * @return Returns the User who last changed the description.
	 */
	@Element(required = false)
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param creator The creator to set.
	 */
	@Element(required = false)
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return Returns the date the description was last changed.
	 */
	@Element(required = false)
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * Sets the date when the description was changed.
	 * 
	 * @param dateChanged the data the description was changed.
	 */
	@Element(required = false)
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.description;
	}
	
}

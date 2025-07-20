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

import org.hibernate.envers.Audited;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.Locale;

/**
 * ConceptDescription is the localized description of a concept.
 */
@Audited
public class ConceptDescription extends BaseOpenmrsObject implements Auditable, java.io.Serializable {
	
	private static final long serialVersionUID = -7223075113369136584L;
	
	// Fields
	private Integer conceptDescriptionId;
	
	@ManyToOne(optional = false) 
	@JoinColumn(name = "concept_id", nullable = false)
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
	 * @return Returns the conceptDescriptionId.
	 */
	public Integer getConceptDescriptionId() {
		return conceptDescriptionId;
	}
	
	/**
	 * @param conceptDescriptionId The conceptId to set.
	 */
	public void setConceptDescriptionId(Integer conceptDescriptionId) {
		this.conceptDescriptionId = conceptDescriptionId;
	}
	
	// Property accessors
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
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
	 * @return Returns the User who last changed the description.
	 */
	@Override
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy The user who changed this description
	 */
	@Override
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return Returns the date the description was last changed.
	 */
	@Override
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * Sets the date when the description was changed.
	 * 
	 * @param dateChanged the data the description was changed.
	 */
	@Override
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.description;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptDescriptionId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptDescriptionId(id);
	}
	
}

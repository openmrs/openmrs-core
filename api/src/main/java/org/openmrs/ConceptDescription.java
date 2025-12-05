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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;

import java.util.Date;
import java.util.Locale;

/**
 * ConceptDescription is the localized description of a concept.
 */
@Audited
@Entity
@Table(name = "concept_description")
public class ConceptDescription extends BaseOpenmrsObject implements Auditable, java.io.Serializable {
	
	private static final long serialVersionUID = -7223075113369136584L;
	
	// Fields
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concept_description_id", nullable = false)
	private Integer conceptDescriptionId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;

	@Column(name = "description", nullable = false, length = 65535, columnDefinition = "text")
	private String description;

	@Column(name = "locale", nullable = false, length = 50)
	private Locale locale;

	@ManyToOne(optional = false)
	@JoinColumn(name = "creator", nullable = false)
	private User creator;

	@Column(name = "date_created", nullable = false)
	private Date dateCreated;

	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;

	@Column(name = "date_changed")
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

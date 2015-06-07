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

import org.hibernate.search.annotations.Field;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * In OpenMRS, we distinguish between data and metadata within our data model. Metadata represent
 * system and descriptive data such as data types &mdash; a relationship type or encounter type.
 * Metadata are generally referenced by clinical data but don't represent patient-specific data
 * themselves. This provides a default abstract implementation of the OpenmrsMetadata interface
 * 
 * @since 1.5
 * @see OpenmrsMetadata
 */
@MappedSuperclass
public abstract class BaseOpenmrsMetadata extends BaseOpenmrsObject implements OpenmrsMetadata {
	
	//***** Properties *****
	@Column(name = "name", nullable = false, length = 255)
	@Field
	private String name;
	
	@Column(name = "description", length = 255)
	private String description;
	
	@ManyToOne(optional = false)
	@Column(name = "creator")
	private User creator;
	
	@Column(name = "date_created", nullable = false)
	private Date dateCreated;
	
	@ManyToOne
	@Column(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed")
	private Date dateChanged;
	
	@Column(name = "retired", nullable = false)
	@Field
	private Boolean retired = Boolean.FALSE;
	
	@Column(name = "date_retired")
	private Date dateRetired;
	
	@ManyToOne
	@Column(name = "retired_by")
	private User retiredBy;
	
	@Column(name = "retire_reason", length = 255)
	private String retireReason;
	
	//***** Constructors *****
	
	/**
	 * Default Constructor
	 */
	public BaseOpenmrsMetadata() {
	}
	
	//***** Property Access *****
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @see org.openmrs.Auditable#getCreator()
	 */
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @see org.openmrs.Auditable#setCreator(org.openmrs.User)
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @see org.openmrs.Auditable#getDateCreated()
	 */
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @see org.openmrs.Auditable#setDateCreated(java.util.Date)
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @see org.openmrs.Auditable#getChangedBy()
	 */
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @see org.openmrs.Auditable#setChangedBy(org.openmrs.User)
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @see org.openmrs.Auditable#getDateChanged()
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @see org.openmrs.Auditable#setDateChanged(java.util.Date)
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @see org.openmrs.Retireable#isRetired()
	 */
	public Boolean isRetired() {
		return retired;
	}
	
	/**
	 * This method delegates to {@link #isRetired()}. This is only needed for jstl syntax like
	 * ${fieldType.retired} because the return type is a Boolean object instead of a boolean
	 * primitive type.
	 * 
	 * @see org.openmrs.Retireable#isRetired()
	 */
	public Boolean getRetired() {
		return isRetired();
	}
	
	/**
	 * @see org.openmrs.Retireable#setRetired(java.lang.Boolean)
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	/**
	 * @see org.openmrs.Retireable#getDateRetired()
	 */
	public Date getDateRetired() {
		return dateRetired;
	}
	
	/**
	 * @see org.openmrs.Retireable#setDateRetired(java.util.Date)
	 */
	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
	}
	
	/**
	 * @see org.openmrs.Retireable#getRetiredBy()
	 */
	public User getRetiredBy() {
		return retiredBy;
	}
	
	/**
	 * @see org.openmrs.Retireable#setRetiredBy(org.openmrs.User)
	 */
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}
	
	/**
	 * @see org.openmrs.Retireable#getRetireReason()
	 */
	public String getRetireReason() {
		return retireReason;
	}
	
	/**
	 * @see org.openmrs.Retireable#setRetireReason(java.lang.String)
	 */
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}
	
}

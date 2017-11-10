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

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Field;

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
	@JoinColumn(name = "creator")
	private User creator;
	
	@Column(name = "date_created", nullable = false)
	private Date dateCreated;
	
	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed")
	private Date dateChanged;
	
	@Column(name = "retired", nullable = false)
	@Field
	private Boolean retired = Boolean.FALSE;
	
	@Column(name = "date_retired")
	private Date dateRetired;
	
	@ManyToOne
	@JoinColumn(name = "retired_by")
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
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @see org.openmrs.OpenmrsMetadata#getCreator()
	 */
	@Override
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @see org.openmrs.OpenmrsMetadata#setCreator(org.openmrs.User)
	 */
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @see org.openmrs.OpenmrsMetadata#getDateCreated()
	 */
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @see org.openmrs.OpenmrsMetadata#setDateCreated(java.util.Date)
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @see org.openmrs.OpenmrsMetadata#getChangedBy()
	 * @deprecated as of version 2.2
	 */
	@Override
	@Deprecated
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @see org.openmrs.OpenmrsMetadata#setChangedBy(User)
	 * @deprecated as of version 2.2
	 */
	@Override
	@Deprecated
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @see org.openmrs.OpenmrsMetadata#getDateChanged()
	 * @deprecated as of version 2.2
	 */
	@Override
	@Deprecated
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @see org.openmrs.OpenmrsMetadata#setDateChanged(Date)
	 * @deprecated as of version 2.2
	 */
	@Override
	@Deprecated
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @deprecated as of 2.0, use {@link #getRetired()}
	 * @see org.openmrs.Retireable#isRetired()
	 */
	@Override
	@Deprecated
	@JsonIgnore
	public Boolean isRetired() {
		return getRetired();
	}
	
	/**
	 * This method delegates to {@link #isRetired()}. This is only needed for jstl syntax like
	 * ${fieldType.retired} because the return type is a Boolean object instead of a boolean
	 * primitive type.
	 * 
	 * @see org.openmrs.Retireable#isRetired()
	 */
	@Override
	public Boolean getRetired() {
		return retired;
	}
	
	/**
	 * @see org.openmrs.Retireable#setRetired(java.lang.Boolean)
	 */
	@Override
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	/**
	 * @see org.openmrs.Retireable#getDateRetired()
	 */
	@Override
	public Date getDateRetired() {
		return dateRetired;
	}
	
	/**
	 * @see org.openmrs.Retireable#setDateRetired(java.util.Date)
	 */
	@Override
	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
	}
	
	/**
	 * @see org.openmrs.Retireable#getRetiredBy()
	 */
	@Override
	public User getRetiredBy() {
		return retiredBy;
	}
	
	/**
	 * @see org.openmrs.Retireable#setRetiredBy(org.openmrs.User)
	 */
	@Override
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}
	
	/**
	 * @see org.openmrs.Retireable#getRetireReason()
	 */
	@Override
	public String getRetireReason() {
		return retireReason;
	}
	
	/**
	 * @see org.openmrs.Retireable#setRetireReason(java.lang.String)
	 */
	@Override
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}
	
}

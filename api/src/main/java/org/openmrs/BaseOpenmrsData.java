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
 * In OpenMRS, we distinguish between data and metadata within our data model. Data (as opposed to
 * metadata) generally represent person- or patient-specific data. This provides a default abstract
 * implementation of the OpenmrsData interface
 * 
 * @since 1.5
 * @see OpenmrsData
 */
@MappedSuperclass
public abstract class BaseOpenmrsData extends BaseOpenmrsObject implements OpenmrsData {
	
	//***** Properties *****
	@ManyToOne(optional = false)
	@JoinColumn(name = "creator", updatable = false)
	protected User creator;
	
	@Column(name = "date_created", nullable = false, updatable = false)
	private Date dateCreated;
	
	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed")
	private Date dateChanged;
	
	@Column(name = "voided", nullable = false)
	@Field
	private Boolean voided = Boolean.FALSE;
	
	@Column(name = "date_voided")
	private Date dateVoided;
	
	@ManyToOne
	@JoinColumn(name = "voided_by")
	private User voidedBy;
	
	@Column(name = "void_reason", length = 255)
	private String voidReason;
	
	//***** Constructors *****
	
	/**
	 * Default Constructor
	 */
	public BaseOpenmrsData() {
	}
	
	//***** Property Access *****
	
	/**
	 * @see org.openmrs.OpenmrsData#getCreator()
	 */
	@Override
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @see org.openmrs.OpenmrsData#setCreator(org.openmrs.User)
	 */
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @see org.openmrs.OpenmrsData#getDateCreated()
	 */
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @see org.openmrs.OpenmrsData#setDateCreated(java.util.Date)
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @deprecated as of version 2.2
	 * @see org.openmrs.OpenmrsData#getChangedBy()
	 */
	@Override
	@Deprecated
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @deprecated as of version 2.2
	 * @see org.openmrs.OpenmrsData#setChangedBy(User)
	 */
	@Override
	@Deprecated
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @deprecated as of version 2.2
	 * @see org.openmrs.OpenmrsData#getDateChanged()
	 */
	@Override
	@Deprecated
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @deprecated as of version 2.2
	 * @see org.openmrs.OpenmrsData#setDateChanged(Date)
	 */
	@Override
	@Deprecated
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @deprecated as of 2.0, use {@link #getVoided()}
	 * @see org.openmrs.Voidable#isVoided()
	 */
	@Override
	@Deprecated
	@JsonIgnore
	public Boolean isVoided() {
		return getVoided();
	}
	
	/**
	 * @see org.openmrs.Voidable#getVoided()
	 */
	@Override
	public Boolean getVoided() {
		return voided;
	}
	
	/**
	 * @see org.openmrs.Voidable#setVoided(java.lang.Boolean)
	 */
	@Override
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	/**
	 * @see org.openmrs.Voidable#getDateVoided()
	 */
	@Override
	public Date getDateVoided() {
		return dateVoided;
	}
	
	/**
	 * @see org.openmrs.Voidable#setDateVoided(java.util.Date)
	 */
	@Override
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	/**
	 * @see org.openmrs.Voidable#getVoidedBy()
	 */
	@Override
	public User getVoidedBy() {
		return voidedBy;
	}
	
	/**
	 * @see org.openmrs.Voidable#setVoidedBy(org.openmrs.User)
	 */
	@Override
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	
	/**
	 * @see org.openmrs.Voidable#getVoidReason()
	 */
	@Override
	public String getVoidReason() {
		return voidReason;
	}
	
	/**
	 * @see org.openmrs.Voidable#setVoidReason(java.lang.String)
	 */
	@Override
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
}

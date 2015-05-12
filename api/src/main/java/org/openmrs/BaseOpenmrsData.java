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

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.util.Date;

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
	@Column(name = "creator")
	protected User creator;
	
	@Column(name = "date_created", nullable = false)
	private Date dateCreated;
	
	@ManyToOne
	@Column(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_changed")
	private Date dateChanged;
	
	@Column(name = "voided", nullable = false)
	private Boolean voided = Boolean.FALSE;
	
	@Column(name = "date_voided")
	private Date dateVoided;
	
	@ManyToOne
	@Column(name = "voided_by")
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
	 * @see org.openmrs.Voidable#isVoided()
	 */
	public Boolean isVoided() {
		return voided;
	}
	
	/**
	 * This method delegates to {@link #isVoided()}. This is only needed for jstl syntax like
	 * ${person.voided} because the return type is a Boolean object instead of a boolean primitive
	 * type.
	 * 
	 * @see org.openmrs.Voidable#isVoided()
	 */
	public Boolean getVoided() {
		return isVoided();
	}
	
	/**
	 * @see org.openmrs.Voidable#setVoided(java.lang.Boolean)
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	/**
	 * @see org.openmrs.Voidable#getDateVoided()
	 */
	public Date getDateVoided() {
		return dateVoided;
	}
	
	/**
	 * @see org.openmrs.Voidable#setDateVoided(java.util.Date)
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	/**
	 * @see org.openmrs.Voidable#getVoidedBy()
	 */
	public User getVoidedBy() {
		return voidedBy;
	}
	
	/**
	 * @see org.openmrs.Voidable#setVoidedBy(org.openmrs.User)
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	
	/**
	 * @see org.openmrs.Voidable#getVoidReason()
	 */
	public String getVoidReason() {
		return voidReason;
	}
	
	/**
	 * @see org.openmrs.Voidable#setVoidReason(java.lang.String)
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
}

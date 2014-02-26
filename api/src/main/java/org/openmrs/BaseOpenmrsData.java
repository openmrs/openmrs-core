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

/**
 * In OpenMRS, we distinguish between data and metadata within our data model. Data (as opposed to
 * metadata) generally represent person- or patient-specific data. This provides a default abstract
 * implementation of the OpenmrsData interface
 * 
 * @since 1.5
 * @see OpenmrsData
 */
public abstract class BaseOpenmrsData extends BaseOpenmrsObject implements OpenmrsData {
	
	//***** Properties *****
	
	protected User creator;
	
	private Date dateCreated;
	
	private User changedBy;
	
	private Date dateChanged;
	
	private Boolean voided = Boolean.FALSE;
	
	private Date dateVoided;
	
	private User voidedBy;
	
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

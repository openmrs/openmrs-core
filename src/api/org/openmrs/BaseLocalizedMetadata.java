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
 * All {@link OpenmrsMetadata} classes(e.g., {@link EncounterType}, {@link Location}) that have
 * localized name or description will extend from this class.
 * 
 * @since 1.9
 */
public abstract class BaseLocalizedMetadata extends BaseOpenmrsObject implements LocalizedMetadata {

	//***** Properties *****
	private LocalizedString localizedName;
	
	private String description;
	
	private User creator;
	
	private Date dateCreated;
	
	private User changedBy;
	
	private Date dateChanged;
	
	private Boolean retired = Boolean.FALSE;
	
	private Date dateRetired;
	
	private User retiredBy;
	
	private String retireReason;
	
	//***** Constructors *****
	
	/**
	 * Default Constructor
	 */
	public BaseLocalizedMetadata() {
	}
	
	//***** Property Access *****

	/**
	 * @return the localizedName
	 */
	public LocalizedString getLocalizedName() {
		if (localizedName == null)
			localizedName = new LocalizedString();
		return localizedName;
	}
	
    /**
	 * @param localizedName the localizedName to set
	 */
	public void setLocalizedName(LocalizedString localizedName) {
		this.localizedName = localizedName;
	}
    
	/**
	 * @return the name
	 */
	public String getName() {
		return getLocalizedName().getValue();
	}
	

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		getLocalizedName().setUnlocalizedValue(name);
	}
	
	/**
	 * @see org.openmrs.LocalizedMetadata#getLocalizedDescription()
	 */
	@Override
	public LocalizedString getLocalizedDescription() {
		// TODO support localization for description property
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see org.openmrs.LocalizedMetadata#setLocalizedDescription(org.openmrs.LocalizedString)
	 */
	@Override
	public void setLocalizedDescription(LocalizedString localizedDescription) {
		// TODO support localization for description property
		throw new UnsupportedOperationException();
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

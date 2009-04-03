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
 * In OpenMRS, we distinguish between data and metadata within our data model.  
 * Metadata represent system and descriptive data such as data types &mdash; a relationship type or encounter type.
 * Metadata are generally referenced by clinical data but don't represent patient-specific data themselves.  
 * This provides a default abstract implementation of the OpenmrsMetadata interface
 * 
 * @see OpenmrsMetadata
*/
public abstract class BaseOpenmrsMetadata implements OpenmrsMetadata {
	
	//***** Properties *****

	private String name;
	private String description;
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private Boolean retired;
	private Date dateRetired;
	private User retiredBy;
	private String retireReason;
	
	//***** Constructors *****
	
	/**
	 * Default Constructor
	 */
	public BaseOpenmrsMetadata() {}
	
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
     * @return the retired
     */
    public Boolean getRetired() {
    	return retired;
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

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
 * EncounterType 
 * @version 1.0
 */
public class EncounterType implements java.io.Serializable {

	public static final long serialVersionUID = 789L;

	// Fields

	private Integer encounterTypeId;
	private String name;
	private String description;
	private Date dateCreated;
	private User creator;
	private User retiredBy;
	private Boolean retired = Boolean.FALSE;
	private Date dateRetired;
	private String retireReason;

	// Constructors

	/** default constructor */
	public EncounterType() {
	}

	/** constructor with id */
	public EncounterType(Integer encounterTypeId) {
		this.encounterTypeId = encounterTypeId;
	}

	/** 
	 * Compares two EncounterType objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (encounterTypeId == null || obj == null || !(obj instanceof EncounterType))
			return false;
		
		EncounterType encounterType = (EncounterType) obj;
		return (this.encounterTypeId.equals(encounterType.getEncounterTypeId()));
	}
	
	public int hashCode() {
		if (this.getEncounterTypeId() == null) return super.hashCode();
		return this.getEncounterTypeId().hashCode();
	}

	// Property accessors
	
	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the encounterTypeId.
	 */
	public Integer getEncounterTypeId() {
		return encounterTypeId;
	}

	/**
	 * @param encounterTypeId The encounterTypeId to set.
	 */
	public void setEncounterTypeId(Integer encounterTypeId) {
		this.encounterTypeId = encounterTypeId;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	/**
     * @return the retiredBy
     */
    public User getRetiredBy() {
    	return retiredBy;
    }

	/**
     * @param retiredBy the retiredBy to set
     */
    public void setRetiredBy(User retiredBy) {
    	this.retiredBy = retiredBy;
    }

	/**
     * @return the retired
     */
    public Boolean getRetired() {
    	return retired;
    }

	/**
     * @param retired the retired to set
     */
    public void setRetired(Boolean retired) {
    	this.retired = retired;
    }

	/**
     * @return the dateRetired
     */
    public Date getDateRetired() {
    	return dateRetired;
    }

	/**
     * @param dateRetired the dateRetired to set
     */
    public void setDateRetired(Date dateRetired) {
    	this.dateRetired = dateRetired;
    }

	/**
     * @return the retireReason
     */
    public String getRetireReason() {
    	return retireReason;
    }

	/**
     * @param retireReason the retireReason to set
     */
    public void setRetireReason(String retireReason) {
    	this.retireReason = retireReason;
    }
	
}
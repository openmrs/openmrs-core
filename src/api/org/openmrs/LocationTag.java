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
 * An LocationTag defines how a certain kind of {@link Encounter}.
 * 
 * @see Encounter
 * @since 1.5
 */
public class LocationTag implements java.io.Serializable {
	
	public static final long serialVersionUID = 7654L;
	
	private Integer locationTagId;
	
	private String tag;
	
	private String description;
	
	private User creator;
	
	private Date dateCreated;
	
	private Boolean retired = false;
	
	private User retiredBy;
	
	private Date dateRetired;
	
	private String retireReason;
	
	// Constructors
	
	/** default constructor */
	public LocationTag() {
	}
	
	/** constructor with id */
	public LocationTag(Integer locationTagId) {
		this.locationTagId = locationTagId;
	}
	
	/**
	 * Required values constructor. This is the minimum number of values that must be non-null in
	 * order to have a successful save to the database
	 * 
	 * @param name the name of this encounter type
	 * @param description a short description of why this encounter type exists
	 */
	public LocationTag(String tag, String description) {
		this.tag = tag;
		this.description = description;
	}
	
	/**
	 * Compares two LocationTag objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof LocationTag))
			return false;
		
		LocationTag locationTag = (LocationTag) obj;
		if (this.locationTagId != null && locationTag.getLocationTagId() != null)
			return (this.locationTagId.equals(locationTag.getLocationTagId()));
		else
			return this == locationTag;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getLocationTagId() == null)
			return super.hashCode();
		return this.getLocationTagId().hashCode();
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
	 * @return Returns the locationTagId.
	 */
	public Integer getLocationTagId() {
		return locationTagId;
	}
	
	/**
	 * @param locationTagId The locationTagId to set.
	 */
	public void setLocationTagId(Integer locationTagId) {
		this.locationTagId = locationTagId;
	}
	
	/**
	 * @return Returns the tag.
	 */
	public String getTag() {
		return tag;
	}
	
	/**
	 * @param name The tag to set.
	 */
	public void setTag(String tag) {
		this.tag = tag;
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
	 * @return the retired status
	 */
	public Boolean getRetired() {
		return retired;
	}
	
	/**
	 * Convenience method, returns false when <code>retired</code> is null
	 * 
	 * @return retired
	 */
	public Boolean isRetired() {
		return getRetired();
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
	
	public String toString() {
		return tag;
	}
}

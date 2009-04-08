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

/**
 * An LocationTag defines how a certain kind of {@link Encounter}.
 * 
 * @see Encounter
 * @since 1.5
 */
public class LocationTag extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 7654L;
	
	private Integer locationTagId;
	
	private String tag;
	
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
	 * @param tag the name of this encounter type
	 * @param description a short description of why this encounter type exists
	 */
	public LocationTag(String tag, String description) {
		this.tag = tag;
		setDescription(description);
	}
	
	/**
	 * Compares two LocationTag objects for similarity
	 * 
	 * @param obj The LocationTag object to be compared.
	 * @return Returns true if the objects share the same locationTagId, false otherwise.
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
	 * @param tag The tag to set.
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String toString() {
		return tag;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getLocationTagId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setLocationTagId(id);
		
	}
}

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

/**
 * An LocationTag allows categorization of {@link Location}s
 * 
 * @see Location
 * @since 1.5
 */
public class LocationTag extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 7654L;
	
	private Integer locationTagId;
	
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
	public LocationTag(String name, String description) {
		setName(name);
		setDescription(description);
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
	 * @deprecated use {@link #getName()} instead
	 */
	@Deprecated
	public String getTag() {
		return getName();
	}
	
	/**
	 * @param tag The tag to set.
	 * @deprecated use {@link #setName(String)} instead
	 */
	@Deprecated
	public void setTag(String tag) {
		setName(tag);
	}
	
	public String toString() {
		return getName();
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

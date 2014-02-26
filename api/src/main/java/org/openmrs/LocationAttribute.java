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

import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;

/**
 * A value for a user-defined {@link LocationAttributeType} that is stored on a {@link Location}.
 * @see Attribute
 * @since 1.9
 */
public class LocationAttribute extends BaseAttribute<LocationAttributeType, Location> implements Attribute<LocationAttributeType, Location> {
	
	private Integer locationAttributeId;
	
	// BaseAttribute<Location> has an "owner" property of type Location, which we re-expose as "location"
	
	/**
	 * @return the locationAttributeId
	 */
	public Integer getLocationAttributeId() {
		return locationAttributeId;
	}
	
	/**
	 * @param locationAttributeId the locationAttributeId to set
	 */
	public void setLocationAttributeId(Integer locationAttributeId) {
		this.locationAttributeId = locationAttributeId;
	}
	
	/**
	 * @return the location
	 */
	public Location getLocation() {
		return getOwner();
	}
	
	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		setOwner(location);
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getLocationAttributeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setLocationAttributeId(id);
	}
	
}

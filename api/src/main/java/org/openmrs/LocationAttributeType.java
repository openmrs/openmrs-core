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

import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

/**
 * A user-defined extension to the {@link Location} class.
 * @see AttributeType
 * @since 1.9
 */
public class LocationAttributeType extends BaseAttributeType<Location> implements AttributeType<Location> {
	
	private Integer locationAttributeTypeId;
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getLocationAttributeTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setLocationAttributeTypeId(id);
	}
	
	/**
	 * @return the locationAttributeTypeId
	 */
	public Integer getLocationAttributeTypeId() {
		return locationAttributeTypeId;
	}
	
	/**
	 * @param locationAttributeTypeId the locationAttributeTypeId to set
	 */
	public void setLocationAttributeTypeId(Integer locationAttributeTypeId) {
		this.locationAttributeTypeId = locationAttributeTypeId;
	}
	
}

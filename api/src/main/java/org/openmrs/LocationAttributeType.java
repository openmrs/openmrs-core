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

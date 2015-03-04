/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * Property editor for {@link LocationAttributeType}s
 * 
 * @since 1.9
 */
public class LocationAttributeTypeEditor extends PropertyEditorSupport {
	
	/**
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	public String getAsText() {
		LocationAttributeType lat = (LocationAttributeType) getValue();
		return lat == null ? null : lat.getId().toString();
	}
	
	/**
	 * @should set using id
	 * @should set using uuid
	 * 
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		LocationService ls = Context.getLocationService();
		if (Context.isAuthenticated() && StringUtils.hasText(text)) {
			try {
				setValue(ls.getLocationAttributeType(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				LocationAttributeType lat = ls.getLocationAttributeTypeByUuid(text);
				setValue(lat);
				if (lat == null) {
					throw new IllegalArgumentException("LocationAttributeType not found for " + text, ex);
				}
			}
		} else {
			setValue(null);
		}
	}
	
}

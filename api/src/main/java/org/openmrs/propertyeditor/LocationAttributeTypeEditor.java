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
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

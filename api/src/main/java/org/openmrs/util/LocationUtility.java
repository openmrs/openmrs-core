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
package org.openmrs.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;

/**
 * A utility class for working with locations
 * 
 * @since 1.9
 */
public class LocationUtility implements GlobalPropertyListener {
	
	private static Log log = LogFactory.getLog(LocationUtility.class);
	
	/**
	 * Cached version of the system default location. This is cached so that we don't have to look
	 * it up in the global property table every time it is requested for
	 */
	private static Location defaultLocation = null;
	
	/**
	 * Gets the system default location specified as a global property.
	 * 
	 * @return default location object.
	 * @should return the updated defaultLocation when the value of the global property is changed
	 */
	public static Location getDefaultLocation() {
		if (defaultLocation == null && Context.isSessionOpen())
			defaultLocation = Context.getLocationService().getDefaultLocation();
		
		return defaultLocation;
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		// reset the value
		defaultLocation = null;
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(java.lang.String)
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {
		// reset the value
		defaultLocation = null;
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return propertyName.equals(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION_NAME);
	}
	
}

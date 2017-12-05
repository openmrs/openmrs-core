/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

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
		if (defaultLocation == null && Context.isSessionOpen()) {
			defaultLocation = Context.getLocationService().getDefaultLocation();
		}
		
		return defaultLocation;
	}
	
	/**
	 * Convenience method that returns the default location of the authenticated user. It should
	 * return the user's specified location from the user properties if any is set.
	 *
	 * @should return the user specified location if any is set
	 */
	public static Location getUserDefaultLocation() {
		return Context.getUserContext().getLocation();
	}
	
	public static void setDefaultLocation(Location defaultLocation) {
		LocationUtility.defaultLocation = defaultLocation;
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		// reset the value
		setDefaultLocation(null);
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(java.lang.String)
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {
		// reset the value
		setDefaultLocation(null);
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return propertyName.equals(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION_NAME);
	}
	
}

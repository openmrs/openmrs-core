/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.user;

import java.util.Map;

import org.openmrs.util.OpenmrsConstants;

/**
 * An abstraction over user properties
 *
 * @see org.openmrs.User#getUserProperties()
 */
public class UserProperties {
	
	private Map<String, String> properties;
	
	/**
	 * @param properties map of the user properties
	 */
	public UserProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	/**
	 * Sets the user property which determines if a new user should change his password upon logging
	 * in the first time.
	 *
	 * @param change decides if the user should be forced to change the password
	 * @should add forcePassword property in user properties map when value is set to true
	 * @should do not add forcePassword property in user properties when set to false
	 * @should remove forcePassword property from user properties when set to false
	 * @should do not add forcePassword property in user properties when set to null
	 * @should remove forcePassword property from user properties when set to null
	 */
	public void setSupposedToChangePassword(Boolean change) {
		if ((change == null || !change)) {
			removeProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD);
		} else {
			addProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, String.valueOf(change));
		}
	}
	
	/**
	 * @return the properties
	 */
	private Map<String, String> getProperties() {
		return properties;
	}
	
	/**
	 * Utility method. Removes the given property from the user's properties
	 *
	 * @param property to be removed.
	 */
	private void removeProperty(String property) {
		if (getProperties().containsKey(property)) {
			getProperties().remove(property);
		}
	}
	
	/**
	 * Utility method. Adds the given property to the user's properties
	 *
	 * @param key of the property
	 * @param property value
	 */
	private void addProperty(String key, String property) {
		getProperties().put(key, property);
	}
	
	/**
	 * Method to read the value of forcePassword property
	 *
	 * @return true or false based on the value of forcePassword property
	 * @should "return true or false depending on the presence or absence of forcePassword key in the user properties"
	 */
	public Boolean isSupposedToChangePassword() {
		return Boolean.valueOf(getProperties().get(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD));
	}
}

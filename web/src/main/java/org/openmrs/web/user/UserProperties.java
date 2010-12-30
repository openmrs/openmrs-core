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
		if (getProperties().containsKey(property))
			getProperties().remove(property);
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

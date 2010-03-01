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
package org.openmrs.web.controller;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.openmrs.User;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.user.UserProperties;

/**
 * Test cases for behavior in the User class
 */
public class UserPropertiesTest {
	
	/**
	 * Add the user property 'forcePassword' when setSupposedToChangePassword is set to true.
	 * 
	 * @see {@link UserProperties#setSupposedToChangePassword(Boolean)}
	 */
	@Test
	@Verifies(value = "add forcePassword property in user properties map when value is set to true", method = "setSupposedToChangePassword(Boolean)")
	public void setSupposedToChangePassword_shouldAddForcePasswordPropertyWhenValueIsSetToTrue() throws Exception {
		User user = new User();
		UserProperties userProperties = new UserProperties(user.getUserProperties());
		userProperties.setSupposedToChangePassword(true);
		
		Map<String, String> properties = user.getUserProperties();
		
		assertTrue(properties.containsKey(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD));
		assertTrue(Boolean.valueOf(properties.get(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD)));
	}
	
	/**
	 * Do nothing if the setSupposedToChangePassword is set to false.
	 * 
	 * @see {@link UserProperties#setSupposedToChangePassword(Boolean)}
	 */
	@Test
	@Verifies(value = "do not add forcePassword property in user properties when set to false", method = "setSupposedToChangePassword(Boolean)")
	public void setSupposedToChangePassword_shouldNotAddForcePasswordToUserPropertyWhenValueIsSetAsFalse() throws Exception {
		User user = new User();
		UserProperties userProperties = new UserProperties(user.getUserProperties());
		userProperties.setSupposedToChangePassword(false);
		
		assertFalse(user.getUserProperties().containsKey(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD));
	}
	
	/**
	 * Remove the existing 'forcePassword' property if setSupposedToChangePassword is set to false.
	 * 
	 * @see {@link UserProperties#setSupposedToChangePassword(Boolean)}
	 */
	@Test
	@Verifies(value = "remove forcePassword property from user properties when set to false", method = "setSupposedToChangePassword(Boolean)")
	public void setSupposedToChangePassword_shouldRemoveForcePasswordFromUserPropertyIfValueIsSetAsFalse() throws Exception {
		User user = new User();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, String.valueOf(true));
		
		UserProperties userProperties = new UserProperties(user.getUserProperties());
		userProperties.setSupposedToChangePassword(false);
		assertFalse(user.getUserProperties().containsKey(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD));
		
	}
	
	/**
	 * Do nothing if the setSupposedToChangePassword is set to null.
	 * 
	 * @see {@link UserProperties#setSupposedToChangePassword(Boolean)}
	 */
	@Test
	@Verifies(value = "do not add forcePassword property in user properties when set to null", method = "setSupposedToChangePassword(Boolean)")
	public void setSupposedToChangePassword_shouldNotAddForcePasswordToUserPropertyWhenValueIsSetAsNull() throws Exception {
		User user = new User();
		
		UserProperties userProperties = new UserProperties(user.getUserProperties());
		userProperties.setSupposedToChangePassword(null);
		assertFalse(user.getUserProperties().containsKey(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD));
	}
	
	/**
	 * Remove the existing 'forcePassword' property if setSupposedToChangePassword is set to null.
	 * 
	 * @see {@link UserProperties#setSupposedToChangePassword(Boolean)}
	 */
	@Test
	@Verifies(value = "remove forcePassword property from user properties when set to null", method = "setSupposedToChangePassword(Boolean)")
	public void setSupposedToChangePassword_shouldRemoveForcePasswordFromUserPropertyIfValueIsSetAsNull() throws Exception {
		User user = new User();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, String.valueOf(true));
		
		UserProperties userProperties = new UserProperties(user.getUserProperties());
		userProperties.setSupposedToChangePassword(null);
		assertFalse(user.getUserProperties().containsKey(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD));
		
	}
	
	/**
	 * @see {@link UserProperties#isSupposedToChangePassword()}
	 */
	@Test
	@Verifies(value = "return true or false depending on the presence or absence of forcePassword key in "
	        + "the user properties", method = "isSupposedToChangePassword()")
	public void isSupposedToChangePassword_shouldReturnTrueOrFalseBasedOnTheValueInUserProperties() throws Exception {
		User user = new User();
		UserProperties userProperties = new UserProperties(user.getUserProperties());
		assertFalse(userProperties.isSupposedToChangePassword());
		
		userProperties.setSupposedToChangePassword(true);
		assertTrue(userProperties.isSupposedToChangePassword());
		
		userProperties.setSupposedToChangePassword(false);
		assertFalse(userProperties.isSupposedToChangePassword());
		
		userProperties.setSupposedToChangePassword(null);
		assertFalse(userProperties.isSupposedToChangePassword());
	}
}

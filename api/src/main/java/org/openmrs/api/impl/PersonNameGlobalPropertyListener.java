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
package org.openmrs.api.impl;

import org.openmrs.GlobalProperty;
import org.openmrs.PersonName;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.util.OpenmrsConstants;

/**
 * This is a property Listener class for Person Name
 * This class is responsible for handling Person Name format
 */
public class PersonNameGlobalPropertyListener implements GlobalPropertyListener {
	
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GLOBAL_PROPERTY_LAYOUT_NAME_FORMAT.equals(propertyName);
	}
	
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		PersonName.setFormat(newValue.getPropertyValue());
	}
	
	@Override
	public void globalPropertyDeleted(String propertyName) {
		
	}
	
}

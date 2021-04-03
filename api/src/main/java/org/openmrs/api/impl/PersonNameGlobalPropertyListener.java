/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

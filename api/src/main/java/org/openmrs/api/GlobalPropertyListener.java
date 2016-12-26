/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.openmrs.GlobalProperty;

/**
 * This interface allows code to be run when global properties are created, edited, or deleted. <br>
 * 
 * @see AdministrationService#addGlobalPropertyListener(GlobalPropertyListener)
 */
public interface GlobalPropertyListener {
	
	/**
	 * Asks this listener whether it wants to be notified about the given property name
	 * 
	 * @param propertyName
	 * @return whether this listener wants its action methods to be notified of properties with the
	 *         given name
	 */
	public boolean supportsPropertyName(String propertyName);
	
	/**
	 * Called after a global property is created or updated
	 * 
	 * @param newValue the new value of the property that was just saved
	 */
	public void globalPropertyChanged(GlobalProperty newValue);
	
	/**
	 * Called after a global property is deleted
	 * 
	 * @param propertyName the name of the property that was just deleted
	 */
	public void globalPropertyDeleted(String propertyName);
	
}

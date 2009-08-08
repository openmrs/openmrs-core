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
package org.openmrs.api;

import org.openmrs.GlobalProperty;

/**
 * This interface allows code to be run when global properties are created, edited, or deleted.
 * 
 * @see AdministrationService#addGlobalPropertyListener(String, GlobalPropertyListener) TODO: Make
 *      sure listeners are notified if a global property's name is changed.
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

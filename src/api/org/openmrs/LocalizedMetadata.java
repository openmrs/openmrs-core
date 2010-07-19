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
package org.openmrs;

/**
 * All {@link OpenmrsMetadata} classes(e.g., {@link EncounterType}, {@link Location}) that have
 * localized name or description will implement this interface.
 * 
 * @since 1.9
 */
public interface LocalizedMetadata extends OpenmrsMetadata {
	
	/**
	 * @return the localized name
	 */
	public LocalizedString getLocalizedName();
	
	/**
	 * @param localizedName the localized name to set
	 */
	public void setLocalizedName(LocalizedString localizedName);
	
	/**
	 * @return the localized description
	 */
	public LocalizedString getLocalizedDescription();
	
	/**
	 * @param localizedDescription the localized description to set
	 */
	public void setLocalizedDescription(LocalizedString localizedDescription);
}

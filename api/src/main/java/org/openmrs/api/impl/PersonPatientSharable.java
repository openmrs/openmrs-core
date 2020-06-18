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

/**
 * 
 * Contains shared person and patient methods.
 *
 * @param <T> T is a generic type.
 */
public interface PersonPatientSharable<T> {
	
	/**
	 * Gets a boolean indicating whether the detail is voided.
	 * @return A boolean indicating whether the detail is voided.
	 */
	public Boolean getVoided();
	
	/**
	 * Gets a boolean indicating whether the detail is the preferred.
	 * @return A boolean indicating whether the detail is the preferred.
	 */
	public Boolean getPreferred();
	
	/**
	 * Sets a boolean indicating whether the detail is the preferred.
	 * 
	 * @param preferred A boolean to set against if the detail is preferred.
	 */
	public void setPreferred(Boolean preferred);
}

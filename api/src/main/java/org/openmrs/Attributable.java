/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

/**
 * Classes marked with this interface are able to be special values for a PersonAttribute.
 */
public interface Attributable<E> {

	/**
	 * Deserialize the given string into a full object
	 *
	 * @param s String to deserialize
	 * @return hydrated object
	 */
	public E hydrate(String s);

	/**
	 * Turn the current object into an identifying string that can be retrieved later
	 *
	 * @return String representing this object (Usually an identifier or primary key)
	 */
	public String serialize();

	/**
	 * Gets a descriptive String used for display purposes This is meant as an alternative to using the
	 * toString() to display this object to a user
	 *
	 * @return String acceptable to display on a page
	 */
	public String getDisplayString();

}

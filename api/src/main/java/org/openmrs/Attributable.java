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

import java.util.List;

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
	 * Find all possible values of this object. For example, if this object is a Location, the
	 * database is delved into and all Location objects should be returned
	 * 
	 * @return List of objects that can be assigned
	 */
	public List<E> getPossibleValues();
	
	/**
	 * Search for possible values of this object using the given search string
	 * 
	 * @param searchText String to search on
	 * @return List of possible objects that can be assigned
	 */
	public List<E> findPossibleValues(String searchText);
	
	/**
	 * Gets a descriptive String used for display purposes This is meant as an alternative to using
	 * the toString() to display this object to a user
	 * 
	 * @return String acceptable to display on a page
	 */
	public String getDisplayString();
	
}

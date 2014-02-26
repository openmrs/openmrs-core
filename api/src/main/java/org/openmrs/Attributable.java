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

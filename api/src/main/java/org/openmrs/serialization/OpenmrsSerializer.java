/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

/**
 * Implementations of this interface provide serialization implementations for OpenMRS.
 */
public interface OpenmrsSerializer {
	
	/**
	 * Turn the current object into an identifying string that can be retrieved later
	 * 
	 * @param o - the object to serialize
	 * @return String representing this object
	 */
	public String serialize(Object o) throws SerializationException;
	
	/**
	 * Deserialize the given string into a full object
	 * 
	 * @param serializedObject - String to deserialize
	 * @param clazz - The class to deserialize the Object into
	 * @return hydrated object of the appropriate type
	 */
	public <T> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException;
}

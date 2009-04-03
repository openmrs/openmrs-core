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
package org.openmrs.serialization;

import org.openmrs.api.APIException;

/**
 * Implmentations of this interface provide serialization implementations for OpenMRS.
 */
public interface OpenmrsSerializer {
	
	/**
	 * Turn the current object into an identifying string that can be retrieved later
	 * @param o - the object to serialize
	 * @return String representing this object
	 */
	public String serialize(Object o) throws APIException;
	
	/**
	 * Deserialize the given string into a full object
	 * @param serializedObject - String to deserialize
	 * @param clazz - The class to deserialize the Object into
	 * @return hydrated object of the appropriate type
	 */
	public <T extends Object> T deserialize(String serializedObject, Class<? extends T> clazz) throws APIException;
}

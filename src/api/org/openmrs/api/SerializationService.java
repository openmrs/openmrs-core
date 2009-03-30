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

import org.openmrs.serialization.OpenmrsSerializer;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods for retrieving registered Serializer instances,
 * and for persisting/retrieving/deleting objects using serialization
 */
@Transactional
public interface SerializationService extends OpenmrsService {
	
	/**
	 * Returns the default serializer configured for the system.
	 * This enables a user to serialize objects without needing
	 * to know the underlying serialization implementation class.
	 * @return {@link OpenmrsSerializer} the default configured serializer
	 * @should return a serializer
	 */
	public OpenmrsSerializer getDefaultSerializer();
	
	/**
	 * Returns the serializer that matches the passed class, or null if no such serializer exists.
	 * @param class - the serialization class to retrieve
	 * @return {@link OpenmrsSerializer} that matches the passed class
	 * @should return a serializer of the given class
	 */
	public OpenmrsSerializer getSerializer(Class<? extends OpenmrsSerializer> serializationClass);
	
	/**
	 * Serialize the passed object into an identifying string that can be retrieved later
	 * using the passed {@link OpenmrsSerializer} class
	 * @param Object - the object to serialize
	 * @param clazz - the {@link OpenmrsSerializer} class to use for serialization
	 * @return String representing this object
	 */
    public String serialize(Object o, Class<? extends OpenmrsSerializer> clazz) throws APIException;
    
	/**
	 * Deserialize the given string into a full object using the given {@link OpenmrsSerializer} class
	 * @param serializedObject - String to deserialize into an Object
	 * @param objectClass - The class to deserialize the Object into
	 * @param serializerClass - The {@link OpenmrsSerializer} class to use to perform the deserialization
	 * @return hydrated object of the appropriate type
	 */
    public <T extends Object> T deserialize(String serializedObject, Class<? extends T> objectClass, 
                                            Class<? extends OpenmrsSerializer> serializerClass) throws APIException;
}

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

import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.Logging;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.SerializationException;

/**
 * Contains methods for retrieving registered Serializer instances, and for
 * persisting/retrieving/deleting objects using serialization
 * 
 * @since 1.5
 */
public interface SerializationService extends OpenmrsService {
	
	/**
	 * Returns the default serializer configured for the system. This enables a user to serialize
	 * objects without needing to know the underlying serialization implementation class.
	 * 
	 * @return {@link OpenmrsSerializer} the default configured serializer
	 * @should return a serializer
	 */
	public OpenmrsSerializer getDefaultSerializer();
	
	/**
	 * Returns the serializer that matches the passed class, or null if no such serializer exists.
	 * 
	 * @param serializationClass - the serialization class to retrieve
	 * @return {@link OpenmrsSerializer} that matches the passed class
	 * @should return a serializer of the given class
	 */
	public OpenmrsSerializer getSerializer(Class<? extends OpenmrsSerializer> serializationClass);
	
	/**
	 * Serialize the passed object into an identifying string that can be retrieved later using the
	 * passed {@link OpenmrsSerializer} class
	 * 
	 * @param o - the object to serialize
	 * @param clazz - the {@link OpenmrsSerializer} class to use for serialization
	 * @return String representing this object
	 * @should Serialize And Deserialize Correctly
	 * @should Serialize And Deserialize Hibernate Objects Correctly
	 */
	public String serialize(Object o, Class<? extends OpenmrsSerializer> clazz) throws SerializationException;
	
	/**
	 * Deserialize the given string into a full object using the given {@link OpenmrsSerializer}
	 * class
	 * 
	 * @param serializedObject - String to deserialize into an Object
	 * @param objectClass - The class to deserialize the Object into
	 * @param serializerClass - The {@link OpenmrsSerializer} class to use to perform the
	 *            deserialization
	 * @return hydrated object of the appropriate type
	 */
	@Logging(ignoredArgumentIndexes = { 0 })
    @Authorized
	public <T> T deserialize(String serializedObject, Class<? extends T> objectClass,
	        Class<? extends OpenmrsSerializer> serializerClass) throws SerializationException;
	
	/**
	 * Gets the list of OpenmrsSerializers that have been registered with this service. <br>
	 * <br>
	 * Modules are able to add more serializers by adding this in their moduleApplicationContext.
	 * e.g.:
	 * 
	 * <pre>
	 * 	&lt;bean parent="serializationServiceTarget"&gt;
	 * 		&lt;property name="serializers"&gt;
	 * 		&lt;list&gt;
	 * 			&lt;ref bean="xstreamSerializer"/&gt;
	 * 		&lt;/list&gt;
	 * 		&lt;/property&gt;
	 *  &lt;/bean&gt;
	 *  &lt;bean id="xstreamSerializer" class="org.openmrs.module.serialization.xstream.XStreamSerializer"/&gt;
	 * </pre>
	 * 
	 * @return list of serializers currently loaded in openmrs
	 */
	public List<? extends OpenmrsSerializer> getSerializers();
}

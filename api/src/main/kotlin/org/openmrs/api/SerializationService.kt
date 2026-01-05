/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.annotation.Authorized
import org.openmrs.annotation.Logging
import org.openmrs.serialization.OpenmrsSerializer
import org.openmrs.serialization.SerializationException

/**
 * Contains methods for retrieving registered Serializer instances, and for
 * persisting/retrieving/deleting objects using serialization.
 *
 * @since 1.5
 */
interface SerializationService : OpenmrsService {

    /**
     * Returns the default serializer configured for the system. This enables a user to serialize
     * objects without needing to know the underlying serialization implementation class.
     *
     * @return the default configured serializer
     */
    fun getDefaultSerializer(): OpenmrsSerializer?

    /**
     * Returns the serializer that matches the passed class, or null if no such serializer exists.
     *
     * @param serializationClass the serialization class to retrieve
     * @return serializer that matches the passed class
     */
    fun getSerializer(serializationClass: Class<out OpenmrsSerializer>): OpenmrsSerializer?

    /**
     * Serialize the passed object into an identifying string that can be retrieved later using the
     * passed [OpenmrsSerializer] class.
     *
     * @param o the object to serialize
     * @param clazz the [OpenmrsSerializer] class to use for serialization
     * @return String representing this object
     * @throws SerializationException if serialization fails
     */
    @Throws(SerializationException::class)
    fun serialize(o: Any, clazz: Class<out OpenmrsSerializer>): String

    /**
     * Deserialize the given string into a full object using the given [OpenmrsSerializer]
     * class.
     *
     * @param serializedObject String to deserialize into an Object
     * @param objectClass The class to deserialize the Object into
     * @param serializerClass The [OpenmrsSerializer] class to use to perform the deserialization
     * @return hydrated object of the appropriate type
     * @throws SerializationException if deserialization fails
     */
    @Logging(ignoredArgumentIndexes = [0])
    @Authorized
    @Throws(SerializationException::class)
    fun <T> deserialize(
        serializedObject: String,
        objectClass: Class<out T>,
        serializerClass: Class<out OpenmrsSerializer>
    ): T

    /**
     * Gets the list of OpenmrsSerializers that have been registered with this service.
     *
     * Modules are able to add more serializers by adding this in their moduleApplicationContext.
     *
     * @return list of serializers currently loaded in openmrs
     */
    fun getSerializers(): List<@JvmWildcard OpenmrsSerializer>
}

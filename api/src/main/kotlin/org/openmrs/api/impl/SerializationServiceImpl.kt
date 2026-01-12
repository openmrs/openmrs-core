/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl

import org.apache.commons.lang3.StringUtils
import org.openmrs.api.APIException
import org.openmrs.api.SerializationService
import org.openmrs.api.context.Context
import org.openmrs.serialization.OpenmrsSerializer
import org.openmrs.serialization.SerializationException
import org.openmrs.serialization.SimpleXStreamSerializer
import org.openmrs.util.OpenmrsConstants
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Contains methods for retrieving registered OpenmrsSerializer instances, and for
 * persisting/retrieving/deleting objects using serialization
 */
@Service("serializationService")
@Transactional
open class SerializationServiceImpl : BaseOpenmrsService(), SerializationService {
    
    companion object {
        private val log = LoggerFactory.getLogger(SerializationServiceImpl::class.java)
    }
    
    // Properties (set by spring)
    private var serializerMap: MutableMap<Class<out OpenmrsSerializer>, OpenmrsSerializer>? = null

    @Autowired
    fun initializeSerializerMap(@Qualifier("serializerList") serializerList: List<OpenmrsSerializer>) {
        setSerializers(serializerList)
    }
    
    // Service method implementations
    
    /**
     * @see org.openmrs.api.SerializationService.getSerializer
     */
    override fun getSerializer(serializationClass: Class<out OpenmrsSerializer>): OpenmrsSerializer? {
        return serializerMap?.get(serializationClass)
    }
    
    /**
     * @see org.openmrs.api.SerializationService.getDefaultSerializer
     */
    @Transactional(readOnly = true)
    override fun getDefaultSerializer(): OpenmrsSerializer {
        val prop = Context.getAdministrationService().getGlobalProperty(
            OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_SERIALIZER
        )
        
        if (StringUtils.isNotEmpty(prop)) {
            try {
                val clazz = Context.loadClass(prop)
                if (clazz != null && OpenmrsSerializer::class.java.isAssignableFrom(clazz)) {
                    return clazz.getDeclaredConstructor().newInstance() as OpenmrsSerializer
                }
            } catch (e: Exception) {
                log.info("Cannot create an instance of $prop - using builtin SimpleXStreamSerializer.")
            }
        } else {
            log.info("No default serializer specified - using builtin SimpleXStreamSerializer.")
        }
        return serializerMap?.get(SimpleXStreamSerializer::class.java)
            ?: throw IllegalStateException("SimpleXStreamSerializer not found in serializer map")
    }
    
    /**
     * @see org.openmrs.api.SerializationService.serialize
     */
    override fun serialize(o: Any, clazz: Class<out OpenmrsSerializer>): String {
        // Get appropriate OpenmrsSerializer implementation
        val serializer = getSerializer(clazz)
            ?: throw SerializationException("OpenmrsSerializer of class <$clazz> not found.")
        
        // Attempt to Serialize the object
        return try {
            serializer.serialize(o)
        } catch (e: Exception) {
            throw SerializationException("An error occurred during serialization of object <$o>", e)
        }
    }
    
    /**
     * @see org.openmrs.api.SerializationService.deserialize
     */
    override fun <T> deserialize(
        serializedObject: String,
        objectClass: Class<out T>,
        serializerClass: Class<out OpenmrsSerializer>
    ): T {
        // Get appropriate OpenmrsSerializer implementation
        val serializer = getSerializer(serializerClass)
            ?: throw APIException("serializer.not.found", arrayOf(serializerClass))
        
        // Attempt to Deserialize the object
        return try {
            serializer.deserialize(serializedObject, objectClass)
        } catch (e: Exception) {
            val msg = "An error occurred during deserialization of data <$serializedObject>"
            throw SerializationException(msg, e)
        }
    }
    
    // Property access
    
    /**
     * @return the serializers
     */
    override fun getSerializers(): List<OpenmrsSerializer> {
        if (serializerMap == null) {
            serializerMap = LinkedHashMap()
        }
        return ArrayList(serializerMap!!.values)
    }
    
    /**
     * @param serializers the serializers to set
     * Should not reset serializers list when called multiple times
     */
    fun setSerializers(serializers: List<OpenmrsSerializer>?) {
        if (serializers == null || serializerMap == null) {
            this.serializerMap = LinkedHashMap()
        }
        serializers?.forEach { s ->
            serializerMap!![s.javaClass] = s
        }
    }
}

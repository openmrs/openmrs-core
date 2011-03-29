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
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.SerializationService;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.SerializationException;
import org.openmrs.serialization.SimpleXStreamSerializer;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods for retrieving registered OpenmrsSerializer instances, and for
 * persisting/retrieving/deleting objects using serialization
 */
@Transactional
public class SerializationServiceImpl extends BaseOpenmrsService implements SerializationService {
	
	public Log log = LogFactory.getLog(this.getClass());
	
	//***** Properties (set by spring)
	private static Map<Class<? extends OpenmrsSerializer>, OpenmrsSerializer> serializerMap;
	
	//***** Service method implementations *****
	
	/**
	 * @see org.openmrs.api.SerializationService#getSerializer(java.lang.Class)
	 */
	public OpenmrsSerializer getSerializer(Class<? extends OpenmrsSerializer> serializationClass) {
		if (serializerMap != null) {
			return serializerMap.get(serializationClass);
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.api.SerializationService#getDefaultSerializer()
	 */
	public OpenmrsSerializer getDefaultSerializer() {
		String prop = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_SERIALIZER);
		try {
			Class<?> clazz = Context.loadClass(prop);
			if (clazz != null && OpenmrsSerializer.class.isAssignableFrom(clazz)) {
				return (OpenmrsSerializer) clazz.newInstance();
			}
		}
		catch (Exception e) {
			log.warn("No default serializer specified - trying first serializer found.");
		}
		
		return serializerMap.get(SimpleXStreamSerializer.class);
	}
	
	/**
	 * @see org.openmrs.api.SerializationService#serialize(java.lang.Object, java.lang.Class)
	 */
	public String serialize(Object o, Class<? extends OpenmrsSerializer> clazz) throws SerializationException {
		
		// Get appropriate OpenmrsSerializer implementation
		OpenmrsSerializer serializer = getSerializer(clazz);
		if (serializer == null) {
			throw new SerializationException("OpenmrsSerializer of class <" + clazz + "> not found.");
		}
		
		// Attempt to Serialize the object
		try {
			return serializer.serialize(o);
		}
		catch (Throwable t) {
			throw new SerializationException("An error occurred during serialization of object <" + o + ">", t);
		}
	}
	
	/**
	 * @see org.openmrs.api.SerializationService#deserialize(java.lang.String, java.lang.Class,
	 *      java.lang.Class)
	 */
	public <T extends Object> T deserialize(String serializedObject, Class<? extends T> objectClass,
	        Class<? extends OpenmrsSerializer> serializerClass) throws SerializationException {
		
		// Get appropriate OpenmrsSerializer implementation
		OpenmrsSerializer serializer = getSerializer(serializerClass);
		if (serializer == null) {
			throw new APIException("OpenmrsSerializer of class <" + serializerClass + "> not found.");
		}
		
		// Attempt to Deserialize the object
		try {
			return (T) serializer.deserialize(serializedObject, objectClass);
		}
		catch (Throwable t) {
			String msg = "An error occurred during deserialization of data <" + serializedObject + ">";
			throw new SerializationException(msg, t);
		}
	}
	
	//***** Property access *****
	
	/**
	 * @return the serializers
	 */
	public List<? extends OpenmrsSerializer> getSerializers() {
		if (serializerMap == null) {
			serializerMap = new LinkedHashMap<Class<? extends OpenmrsSerializer>, OpenmrsSerializer>();
		}
		return new ArrayList<OpenmrsSerializer>(serializerMap.values());
	}
	
	/**
	 * @param serializers the serializers to set
	 * @should not reset serializers list when called multiple times
	 */
	public void setSerializers(List<? extends OpenmrsSerializer> serializers) {
		if (serializers == null || serializerMap == null) {
			serializerMap = new LinkedHashMap<Class<? extends OpenmrsSerializer>, OpenmrsSerializer>();
		}
		if (serializers != null) {
			for (OpenmrsSerializer s : serializers) {
				serializerMap.put(s.getClass(), s);
			}
		}
	}
}

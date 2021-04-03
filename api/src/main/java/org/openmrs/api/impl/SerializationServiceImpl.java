/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.SerializationService;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.SerializationException;
import org.openmrs.serialization.SimpleXStreamSerializer;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods for retrieving registered OpenmrsSerializer instances, and for
 * persisting/retrieving/deleting objects using serialization
 */
@Transactional
public class SerializationServiceImpl extends BaseOpenmrsService implements SerializationService {
	
	private static final Logger log = LoggerFactory.getLogger(SerializationServiceImpl.class);
	
	//***** Properties (set by spring)
	private static Map<Class<? extends OpenmrsSerializer>, OpenmrsSerializer> serializerMap;
	
	//***** Service method implementations *****
	
	/**
	 * @see org.openmrs.api.SerializationService#getSerializer(java.lang.Class)
	 */
	@Override
	public OpenmrsSerializer getSerializer(Class<? extends OpenmrsSerializer> serializationClass) {
		if (serializerMap != null) {
			return serializerMap.get(serializationClass);
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.api.SerializationService#getDefaultSerializer()
	 */
	@Override
	@Transactional(readOnly = true)
	public OpenmrsSerializer getDefaultSerializer() {
		String prop = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_SERIALIZER);
		if (StringUtils.isNotEmpty(prop)) {
			try {
				Class<?> clazz = Context.loadClass(prop);
				if (clazz != null && OpenmrsSerializer.class.isAssignableFrom(clazz)) {
					return (OpenmrsSerializer) clazz.newInstance();
				}
			}
			catch (Exception e) {
				log.info("Cannot create an instance of " + prop + " - using builtin SimpleXStreamSerializer.");
			}
		} else {
			log.info("No default serializer specified - using builtin SimpleXStreamSerializer.");
		}
		return serializerMap.get(SimpleXStreamSerializer.class);
	}
	
	/**
	 * @see org.openmrs.api.SerializationService#serialize(java.lang.Object, java.lang.Class)
	 */
	@Override
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
		catch (Exception e) {
			throw new SerializationException("An error occurred during serialization of object <" + o + ">", e);
		}
	}
	
	/**
	 * @see org.openmrs.api.SerializationService#deserialize(java.lang.String, java.lang.Class,
	 *      java.lang.Class)
	 */
	@Override
	public <T> T deserialize(String serializedObject, Class<? extends T> objectClass,
	        Class<? extends OpenmrsSerializer> serializerClass) throws SerializationException {
		
		// Get appropriate OpenmrsSerializer implementation
		OpenmrsSerializer serializer = getSerializer(serializerClass);
		if (serializer == null) {
			throw new APIException("serializer.not.found", new Object[] { serializerClass });
		}
		
		// Attempt to Deserialize the object
		try {
			return serializer.deserialize(serializedObject, objectClass);
		}
		catch (Exception e) {
			String msg = "An error occurred during deserialization of data <" + serializedObject + ">";
			throw new SerializationException(msg, e);
		}
	}
	
	//***** Property access *****
	
	/**
	 * @return the serializers
	 */
	@Override
	public List<? extends OpenmrsSerializer> getSerializers() {
		if (serializerMap == null) {
			serializerMap = new LinkedHashMap<>();
		}
		return new ArrayList<>(serializerMap.values());
	}
	
	public static void setSerializerMap(Map<Class<? extends OpenmrsSerializer>, OpenmrsSerializer> serializerMap) {
		SerializationServiceImpl.serializerMap = serializerMap;
	}
	
	/**
	 * @param serializers the serializers to set
	 * <strong>Should</strong> not reset serializers list when called multiple times
	 */
	public void setSerializers(List<? extends OpenmrsSerializer> serializers) {
		if (serializers == null || serializerMap == null) {
			setSerializerMap(new LinkedHashMap<>());
		}
		if (serializers != null) {
			for (OpenmrsSerializer s : serializers) {
				serializerMap.put(s.getClass(), s);
			}
		}
	}
}

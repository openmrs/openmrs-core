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
import org.openmrs.serialization.OpenmrsSerializer;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods for retrieving registered Serializer instances, and for
 * persisting/retrieving/deleting objects using serialization
 */
@Transactional
public class SerializationServiceImpl extends BaseOpenmrsService implements SerializationService {
	
	public Log log = LogFactory.getLog(this.getClass());
	
	//***** Properties (set by spring)
	private Map<Class<? extends OpenmrsSerializer>, OpenmrsSerializer> serializerMap;
	
	private OpenmrsSerializer defaultSerializer;
	
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
		if (defaultSerializer != null) {
			return defaultSerializer;
		}
		if (serializerMap != null && !serializerMap.isEmpty()) {
			return serializerMap.values().iterator().next();
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.api.SerializationService#serialize(java.lang.Object, java.lang.Class)
	 */
	public String serialize(Object o, Class<? extends OpenmrsSerializer> clazz) throws APIException {
		OpenmrsSerializer serializer = getSerializer(clazz);
		if (serializer == null) {
			throw new APIException("OpenmrsSerializer of class <" + clazz + "> not found.");
		}
		return serializer.serialize(o);
	}
	
	/**
	 * @see org.openmrs.api.SerializationService#deserialize(java.lang.String, java.lang.Class,
	 *      java.lang.Class)
	 */
	public <T extends Object> T deserialize(String serializedObject, Class<? extends T> objectClass,
	                                        Class<? extends OpenmrsSerializer> serializerClass) throws APIException {
		OpenmrsSerializer serializer = getSerializer(serializerClass);
		if (serializer == null) {
			throw new APIException("OpenmrsSerializer of class <" + serializerClass + "> not found.");
		}
		return serializer.deserialize(serializedObject, objectClass);
	}
	
	//***** Property access *****
	
	/**
	 * @return the serializers
	 */
	public List<? extends OpenmrsSerializer> getSerializers() {
		return new ArrayList<OpenmrsSerializer>(serializerMap.values());
	}
	
	/**
	 * @param serializers the serializers to set
	 */
	public void setSerializers(List<? extends OpenmrsSerializer> serializers) {
		serializerMap = new LinkedHashMap<Class<? extends OpenmrsSerializer>, OpenmrsSerializer>();
		if (serializers != null) {
			for (OpenmrsSerializer s : serializers) {
				serializerMap.put(s.getClass(), s);
			}
		}
	}
	
	/**
	 * @param defaultSerializer the defaultSerializer to set
	 */
	public void setDefaultSerializer(OpenmrsSerializer defaultSerializer) {
		this.defaultSerializer = defaultSerializer;
	}
}

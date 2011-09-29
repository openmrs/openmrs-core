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
package org.openmrs.attribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.AttributeService;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.handler.AttributeHandler;
import org.openmrs.serialization.SerializationException;

/**
 * Convenient utility methods related to {@link Attribute}s and {@link AttributeHandler}s
 * @since 1.9
 */
public class AttributeUtil {
	
	/**
	 * Converts a simple String-based configuration to a serialized form.
	 * Utility method for {@link AttributeHandler}s that have property-style configuration.
	 * 
	 * @param simpleConfig
	 * @return
	 */
	public static String serializeSimpleConfiguration(Map<String, String> simpleConfig) {
		if (simpleConfig == null || simpleConfig.size() == 0)
			return "";
		try {
			return Context.getSerializationService().getDefaultSerializer().serialize(simpleConfig);
		}
		catch (SerializationException ex) {
			throw new APIException(ex);
		}
	}
	
	/**
	 * Deserializes a simple String-based configuration from the serialized form used by
	 * {@link #serializeSimpleConfiguration(Map)} 
	 * Utility method for {@link AttributeHandler}s that have property-style configuration.
	 * 
	 * @param serializedConfig
	 * @return
	 * @should deserialize a configuration serialized by the corresponding serialize method
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> deserializeSimpleConfiguration(String serializedConfig) {
		if (StringUtils.isBlank(serializedConfig))
			return Collections.emptyMap();
		try {
			return Context.getSerializationService().getDefaultSerializer().deserialize(serializedConfig, Map.class);
		}
		catch (SerializationException ex) {
			throw new APIException(ex);
		}
	}
	
	/**
	 * Checks whether a serialized String value is legal for the given handler
	 * 
	 * @param handler
	 * @param serialized
	 * @return
	 */
	public static <T> boolean isValidValue(AttributeHandler<T> handler, String serialized) {
		try {
			handler.validate(handler.deserialize(serialized));
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * Uses the appropriate handlers to serialize all attribute values in the input map.
	 * This is a convenience method for calling XyzService.getXyz(..., attributeValues, ...). 
	 * 
	 * @param attributeValues
	 * @return a map similar to the input parameter, but with typed values converted to their serialized versions with the appropriate handlers
	 */
	public static <T extends AttributeType<?>> Map<T, String> getSerializedAttributeValues(Map<T, Object> attributeValues) {
		Map<T, String> serializedAttributeValues = null;
		if (attributeValues != null) {
			serializedAttributeValues = new HashMap<T, String>();
			AttributeService attrService = Context.getAttributeService();
			for (Map.Entry<T, Object> e : attributeValues.entrySet()) {
				T vat = e.getKey();
				serializedAttributeValues.put(vat, attrService.getHandler(vat).serialize(e.getValue()));
			}
		}
		return serializedAttributeValues;
	}
}

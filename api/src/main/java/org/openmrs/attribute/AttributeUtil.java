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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.handler.AttributeHandler;
import org.openmrs.serialization.SerializationException;

/**
 * Convenient utility methods related to {@link Attribute}s and {@link AttributeHandler}s
 * @since 1.9
 */
public class AttributeUtil {
	
	/**
	 * Converts a simple String-based configuration to a serialized form
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
	 * 
	 * @param serializedConfig
	 * @return
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
	
}

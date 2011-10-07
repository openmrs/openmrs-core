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
package org.openmrs.customdatatype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.AttributeType;
import org.openmrs.serialization.SerializationException;

/**
 * Helper methods for dealing with custom datatypes and their handlers
 * @since 1.9
 */
public class CustomDatatypeUtil {
	
	private static Log log = LogFactory.getLog(CustomDatatypeUtil.class);
	
	public static CustomDatatype<?> getDatatype(CustomValueDescriptor descriptor) {
		try {
			Class dtClass = Context.loadClass(descriptor.getDatatypeClassname());
			CustomDatatype<?> ret = (CustomDatatype<?>) Context.getDatatypeService().getDatatype(dtClass,
			    descriptor.getDatatypeConfig());
			if (ret == null)
				throw new CustomDatatypeException("Can't find datatype: " + descriptor.getDatatypeClassname());
			return ret;
		}
		catch (Exception ex) {
			throw new CustomDatatypeException("Error loading " + descriptor.getDatatypeClassname()
			        + " and configuring it with " + descriptor.getDatatypeConfig(), ex);
		}
	}
	
	public static CustomDatatypeHandler getHandler(CustomValueDescriptor descriptor) {
		if (descriptor.getPreferredHandlerClassname() != null) {
			try {
				Class<? extends CustomDatatypeHandler> clazz = (Class<? extends CustomDatatypeHandler>) Context
				        .loadClass(descriptor.getPreferredHandlerClassname());
				CustomDatatypeHandler handler = clazz.newInstance();
				if (descriptor.getHandlerConfig() != null)
					handler.setHandlerConfiguration(descriptor.getHandlerConfig());
				return handler;
			}
			catch (Exception ex) {
				log.warn("Failed to instantiate and configure preferred handler for " + descriptor, ex);
			}
		}
		
		// if we couldn't get the preferred handler (or none was specified) we get the default one by datatype
		
		CustomDatatype<?> datatype = getDatatype(descriptor);
		return Context.getDatatypeService().getHandler(datatype, descriptor.getHandlerConfig());
	}
	
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
	 * {@link serializeSimpleConfiguration} 
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
	 * Uses the appropriate datatypes to convert all values in the input map to their valueReference equivalents.
	 * This is a convenience method for calling XyzService.getXyz(..., attributeValues, ...).
	 * 
	 * @param datatypeValues
	 * @return a map similar to the input parameter, but with typed values converted to their reference equivalents
	 */
	public static <T extends AttributeType<?>, U> Map<T, String> getValueReferences(Map<T, U> datatypeValues) {
		Map<T, String> serializedAttributeValues = null;
		if (datatypeValues != null) {
			serializedAttributeValues = new HashMap<T, String>();
			for (Map.Entry<T, U> e : datatypeValues.entrySet()) {
				T vat = e.getKey();
				serializedAttributeValues.put(vat, ((CustomDatatype<U>) getDatatype(vat)).toReferenceString(e.getValue()));
			}
		}
		return serializedAttributeValues;
	}
	
	/**
	 * @return fully-qualified classnames of all registered datatypes
	 */
	public static List<String> getDatatypeClassnames() {
		List<String> ret = new ArrayList<String>();
		for (Class<?> c : Context.getDatatypeService().getAllDatatypeClasses())
			ret.add(c.getName());
		return ret;
	}
	
	/**
	 * @return full-qualified classnames of all registered handlers
	 */
	public static List<String> getHandlerClassnames() {
		List<String> ret = new ArrayList<String>();
		for (Class<?> c : Context.getDatatypeService().getAllHandlerClasses())
			ret.add(c.getName());
		return ret;
	}
	
	/**
	 * @param handler
	 * @param datatype
	 * @return whether or not handler is compatible with datatype
	 */
	public static boolean isCompatibleHandler(CustomDatatypeHandler handler, CustomDatatype<?> datatype) {
		List<Class<? extends CustomDatatypeHandler>> handlerClasses = Context.getDatatypeService().getHandlerClasses(
		    (Class<? extends CustomDatatype<?>>) datatype.getClass());
		return handlerClasses.contains(handler.getClass());
	}
	
}

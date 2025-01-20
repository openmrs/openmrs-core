/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.AttributeType;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for dealing with custom datatypes and their handlers
 * @since 1.9
 */
public class CustomDatatypeUtil {

	private CustomDatatypeUtil() {
	}
	
	private static final Logger log = LoggerFactory.getLogger(CustomDatatypeUtil.class);
	
	/**
	 * @param descriptor
	 * @return a configured datatype appropriate for descriptor
	 */
	public static CustomDatatype<?> getDatatype(CustomValueDescriptor descriptor) {
		return getDatatype(descriptor.getDatatypeClassname(), descriptor.getDatatypeConfig());
	}
	
	/**
	 * @param datatypeClassname
	 * @param datatypeConfig
	 * @return a configured datatype with the given classname and configuration
	 */
	public static CustomDatatype<?> getDatatype(String datatypeClassname, String datatypeConfig) {
		try {
			Class dtClass = Context.loadClass(datatypeClassname);
			CustomDatatype<?> ret = (CustomDatatype<?>) Context.getDatatypeService().getDatatype(dtClass, datatypeConfig);
			if (ret == null) {
				throw new CustomDatatypeException("Can't find datatype: " + datatypeClassname);
			}
			return ret;
		}
		catch (Exception ex) {
			throw new CustomDatatypeException("Error loading " + datatypeClassname + " and configuring it with "
			        + datatypeConfig, ex);
		}
	}
	
	/**
	 * @param descriptor
	 * @return a configured datatype appropriate for descriptor
	 */
	public static CustomDatatype<?> getDatatypeOrDefault(CustomValueDescriptor descriptor) {
		try {
			return getDatatype(descriptor);
		}
		catch (CustomDatatypeException ex) {
			return getDatatype(OpenmrsConstants.DEFAULT_CUSTOM_DATATYPE, null);
		}
	}
	
	/**
	 * @param descriptor
	 * @return a configured datatype handler appropriate for descriptor
	 */
	public static CustomDatatypeHandler getHandler(CustomValueDescriptor descriptor) {
		return getHandler(getDatatypeOrDefault(descriptor), descriptor.getPreferredHandlerClassname(), descriptor
		        .getHandlerConfig());
	}
	
	/**
	 * @param dt the datatype that this handler should be for
	 * @param preferredHandlerClassname
	 * @param handlerConfig
	 * @return a configured datatype handler with the given classname and configuration
	 */
	public static CustomDatatypeHandler getHandler(CustomDatatype<?> dt, String preferredHandlerClassname,
	        String handlerConfig) {
		if (preferredHandlerClassname != null) {
			try {
				Class<? extends CustomDatatypeHandler> clazz = (Class<? extends CustomDatatypeHandler>) Context
				        .loadClass(preferredHandlerClassname);
				CustomDatatypeHandler handler = clazz.newInstance();
				if (handlerConfig != null) {
					handler.setHandlerConfiguration(handlerConfig);
				}
				return handler;
			}
			catch (Exception ex) {
				log.warn("Failed to instantiate and configure preferred handler with class " + preferredHandlerClassname
				        + " and config " + handlerConfig, ex);
			}
		}
		
		// if we couldn't get the preferred handler (or none was specified) we get the default one by datatype
		return Context.getDatatypeService().getHandler(dt, handlerConfig);
	}
	
	/**
	 * Converts a simple String-based configuration to a serialized form.
	 * Utility method for property-style configuration implementations.
	 *
	 * @param simpleConfig
	 * @return serialized form
	 */
	public static String serializeSimpleConfiguration(Map<String, String> simpleConfig) {
		if (simpleConfig == null || simpleConfig.size() == 0) {
			return "";
		}
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
	 * Utility method for property-style configuration implementations.
	 *
	 * @param serializedConfig
	 * @return deserialized configuration
	 * <strong>Should</strong> deserialize a configuration serialized by the corresponding serialize method
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> deserializeSimpleConfiguration(String serializedConfig) {
		if (StringUtils.isBlank(serializedConfig)) {
			return Collections.emptyMap();
		}
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
			serializedAttributeValues = new HashMap<>();
			for (Map.Entry<T, U> e : datatypeValues.entrySet()) {
				T vat = e.getKey();
				CustomDatatype<U> customDatatype = (CustomDatatype<U>) getDatatype(vat);
				String valueReference;
				try {
					valueReference = customDatatype.getReferenceStringForValue(e.getValue());
				}
				catch (UnsupportedOperationException ex) {
					throw new APIException("CustomDatatype.error.cannot.search", new Object[] { customDatatype.getClass() });
				}
				serializedAttributeValues.put(vat, valueReference);
			}
		}
		return serializedAttributeValues;
	}
	
	/**
	 * @return fully-qualified classnames of all registered datatypes
	 */
	public static List<String> getDatatypeClassnames() {
		List<String> ret = new ArrayList<>();
		for (Class<?> c : Context.getDatatypeService().getAllDatatypeClasses()) {
			ret.add(c.getName());
		}
		return ret;
	}
	
	/**
	 * @return full-qualified classnames of all registered handlers
	 */
	public static List<String> getHandlerClassnames() {
		List<String> ret = new ArrayList<>();
		for (Class<?> c : Context.getDatatypeService().getAllHandlerClasses()) {
			ret.add(c.getName());
		}
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
	
	/**
	 * To be called by service save methods for customizable implementations.
	 * Iterates over all attributes and calls save on the {@link ConceptDatatype} for any dirty ones.
	 *
	 * @param customizable
	 */
	public static void saveAttributesIfNecessary(Customizable<?> customizable) {
		// TODO decide whether we can move this into a SingleCustomValueSaveHandler instead of leaving it here to be called by each Customizable service's save method
		for (Attribute attr : customizable.getAttributes()) {
			saveIfDirty(attr);
		}
	}
	
	/**
	 * Calls the save method on value's {@link ConceptDatatype} if necessary
	 *
	 * @param value
	 */
	public static void saveIfDirty(SingleCustomValue<?> value) {
		if (value.isDirty()) {
			CustomDatatype datatype = CustomDatatypeUtil.getDatatype(value.getDescriptor());
			if (value.getValue() == null) {
				throw new InvalidCustomValueException(value.getClass() + " with type=" + value.getDescriptor()
				        + " cannot be null");
			}
			String existingValueReference = null;
			try {
				existingValueReference = value.getValueReference();
			}
			catch (NotYetPersistedException ex) {
				// this is expected
			}
			String newValueReference = datatype.save(value.getValue(), existingValueReference);
			value.setValueReferenceInternal(newValueReference);
		}
		
	}
	
	/**
	 * Validates a {@link SingleCustomValue}
	 *
	 * @param value
	 * @return true is value is valid, according to its configured datatype
	 */
	@SuppressWarnings("unchecked")
	public static <T, D extends CustomValueDescriptor> boolean validate(SingleCustomValue<D> value) {
		try {
			CustomDatatype<T> datatype = (CustomDatatype<T>) getDatatype(value.getDescriptor());
			datatype.validate((T) value.getValue());
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
}

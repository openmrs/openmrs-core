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
package org.openmrs.attribute.handler;

import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.InvalidAttributeValueException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * An {@link AttributeType} (e.g. Visit Attribute Type) uses an {@link AttributeHandler} to deal with
 * converting typed attributes (which potentially have validation logic) to and from the String values that
 * are actually stored in the database.
 * Each implementation is for a particular "logical type" (see {@link #getLogicalTypeHandled()}) and its
 * validation and serialization methods work on a concrete data type T.
 * You register an {@link AttributeHandler} by instantiating an implementation as a Spring bean. There may
 * be multiple handlers for any given logical type, so your implementation should also have an {@link Order}
 * annotation defining its priority. As a general guideline, handlers provided by the core OpenMRS API have
 * priority {@link Ordered#LOWEST_PRECEDENCE}, and handlers provided by the core OpenMRS web layer have
 * priority 0. To override existing handlers, just declare a lower order (corresponding to a higher
 * precedence.)
 * @param <T> the concrete java type that this handler validates and serializes
 * @since 1.9
 */
public interface AttributeHandler<T> {
	
	/**
	 * An {@link AttributeHandler} handles a particular "logical type". (E.g. "date", "date-and-time",
	 * "regex-validated-string".
	 * @return String representing the type that this AttributeHandler handles
	 */
	String getLogicalTypeHandled();
	
	/**
	 * An {@link AttributeType} will typically be configured with a handler and a configuration. The framework
	 * takes care of constructing a handler, and setting its configuration (if any)  
	 * @param handlerConfig
	 */
	void setConfiguration(String handlerConfig);
	
	/**
	 * Validates the given value to see if it is a legal value for the given handler. (For example a
	 * String handler might check against a regular expression, or a date handler might validate that the
	 * date is in the past.)   
	 * @param typedValue
	 */
	void validate(T typedValue) throws InvalidAttributeValueException;
	
	/**
	 * @param typedValue run-time type should be T
	 * @return the {@link String} representation of the typed value
	 */
	String serialize(Object typedValue);
	
	/**
	 * @param stringValue
	 * @return converts a previously-serialized value back to its original type
	 */
	T deserialize(String stringValue) throws InvalidAttributeValueException;
	
}

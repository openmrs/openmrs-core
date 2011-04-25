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
package org.openmrs.api;

import java.util.Set;

import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.AttributeHolder;
import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.handler.AttributeHandler;

/**
 * Contains methods pertaining to user-defined attributes that extend core data types.
 * @see {@link Attribute}
 * @see {@link AttributeType}
 * @see {@link AttributeHandler}
 * @see AttributeHolder
 * @since 1.9
 */
public interface AttributeService extends OpenmrsService {
	
	/**
	 * @return a list of all logical types (e.g. "date", "regex-validated-string") that have handler registered
	 */
	Set<String> getLogicalTypes();
	
	/**
	 * Instantiates and configures a handler.
	 *  
	 * @param logicalType
	 * @param handlerConfig
	 * @return an instantiated and configured handler for the given logicalType
	 * @should get a handler for the date logical type
	 * @should get a handler for the string with regex logical type
	 * @should get the default handler for an unknown logical type
	 */
	AttributeHandler<?> getHandler(String logicalType, String handlerConfig);
	
	/**
	 * Convenience method for getting the handler for an attribute type
	 * 
	 * @param attributeType
	 * @return an instantiated and configured handler, defined by attributeType
	 */
	AttributeHandler<?> getHandler(AttributeType<?> attributeType);
	
}

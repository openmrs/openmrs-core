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

import org.openmrs.attribute.InvalidAttributeValueException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Simple attribute handler that takes any String, with no validation or transformation. Also used as a
 * default handler when no matching handler is found for a logical type. 
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class StringAttributeHandler implements AttributeHandler<String> {
	
	public StringAttributeHandler() {
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#getLogicalTypeHandled()
	 */
	@Override
	public String getLogicalTypeHandled() {
		return "string";
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String handlerConfig) {
		// do nothing
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#validate(java.lang.Object)
	 */
	@Override
	public void validate(String typedValue) throws InvalidAttributeValueException {
		// do nothing
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#serialize(java.lang.Object)
	 */
	@Override
	public String serialize(Object typedValue) {
		return (String) typedValue;
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#deserialize(java.lang.String)
	 */
	@Override
	public String deserialize(String stringValue) throws InvalidAttributeValueException {
		return stringValue;
	}
	
}

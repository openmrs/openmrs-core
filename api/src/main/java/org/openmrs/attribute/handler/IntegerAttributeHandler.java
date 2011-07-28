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
 * Simple attribute handler that takes any Integer, with no validation or transformation. 
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class IntegerAttributeHandler implements AttributeHandler<Integer> {
	
	public IntegerAttributeHandler() {
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#getDatatypeHandled()
	 */
	@Override
	public String getDatatypeHandled() {
		return "integer";
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
	public void validate(Integer typedValue) throws InvalidAttributeValueException {
		// do nothing
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#serialize(java.lang.Object)
	 */
	@Override
	public String serialize(Object typedValue) {
		return ""+typedValue;
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#deserialize(java.lang.String)
	 */
	@Override
	public Integer deserialize(String stringValue) throws InvalidAttributeValueException {
		return Integer.parseInt(stringValue);
	}
	
}

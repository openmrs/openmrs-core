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

import java.util.regex.Pattern;

import org.openmrs.attribute.InvalidAttributeValueException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Handler for the logical type "regex-validated-string". Should be configured with a String representation
 * of the regular expression.
 * @since 1.9
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RegexValidatedStringAttributeHandler implements AttributeHandler<String> {
	
	private Pattern regex;
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#getLogicalTypeHandled()
	 */
	@Override
	public String getLogicalTypeHandled() {
		return "regex-validated-string";
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String handlerConfig) {
		regex = Pattern.compile(handlerConfig);
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#validate(java.lang.Object)
	 */
	@Override
	public void validate(String typedValue) throws InvalidAttributeValueException {
		if (!regex.matcher(typedValue).matches())
			throw new InvalidAttributeValueException("Illegal");
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#serialize(java.lang.Object)
	 */
	@Override
	public String serialize(String typedValue) {
		return typedValue;
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#deserialize(java.lang.String)
	 */
	@Override
	public String deserialize(String stringValue) throws InvalidAttributeValueException {
		return stringValue;
	}
	
}

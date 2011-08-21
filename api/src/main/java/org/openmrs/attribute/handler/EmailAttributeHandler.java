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

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class EmailAttributeHandler implements AttributeHandler<String> {
	
	private Pattern regex;
	
	public EmailAttributeHandler() {
		regex = Pattern
		        .compile("^[\\w-]+(\\.[\\w-]+)*@([a-z0-9-]+(\\.[a-z0-9-]+)*?\\.[a-z]{2,6}|(\\d{1,3}\\.){3}\\d{1,3})(:\\d{4})?$");
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#getDatatypeHandled()
	 */
	@Override
	public String getDatatypeHandled() {
		return "email address";
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
	 * @should accept username@domain.com and  u-s_e.r1@s.domain-name.com:8080  or a string that matches the regex
	 */
	@Override
	public void validate(String typedValue) throws InvalidAttributeValueException {
		if (!regex.matcher(typedValue).matches())
			throw new InvalidAttributeValueException("Illegal");
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#serialize(java.lang.Object)
	 * @should fail if the string does not match the regex
	 */
	@Override
	public String serialize(Object typedValue) {
		String asString = (String) typedValue;
		validate(asString);
		return asString;
	}
	
	/**
	 * @see org.openmrs.attribute.handler.AttributeHandler#deserialize(java.lang.String)
	 */
	@Override
	public String deserialize(String stringValue) throws InvalidAttributeValueException {
		return stringValue;
	}
	
}

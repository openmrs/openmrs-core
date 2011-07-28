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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.attribute.InvalidAttributeValueException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class EnumeratedStringHandler implements AttributeHandler<String> {
	
	protected List<String> values;
	
	@Override
	public String getDatatypeHandled() {
		return "enumerated-string";
	}
	
	@Override
	public void setConfiguration(String handlerConfig) {
		values = new ArrayList<String>();
		for (String value : StringUtils.split(handlerConfig, ","))
			values.add(StringUtils.trim(value));
	}
	
	@Override
	public void validate(String typedValue) throws InvalidAttributeValueException {
		if (CollectionUtils.isNotEmpty(values) && !values.contains(typedValue))
			throw new InvalidAttributeValueException("Atribute is not in the available value list.");
	}
	
	@Override
	public String serialize(Object typedValue) {
		String value = String.valueOf(typedValue);
		validate(value);
		return value;
	}
	
	@Override
	public String deserialize(String stringValue) throws InvalidAttributeValueException {
		return stringValue;
	}
}
